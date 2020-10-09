/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import com.google.inject.Inject
import connectors.SubscriptionConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, NotEnrolledForDAC6Action}
import models.RegistrationType.Individual
import models.{PayloadRegistrationWithoutIDResponse, RegistrationType, UserAnswers}
import org.slf4j.LoggerFactory
import pages._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import renderer.Renderer
import repositories.SessionRepository
import services.{EmailService, RegistrationService}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, SummaryList}
import utils.CheckYourAnswersHelper

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            sessionRepository: SessionRepository,
                                            identify: IdentifierAction,
                                            notEnrolled: NotEnrolledForDAC6Action,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            emailService: EmailService,
                                            registrationService: RegistrationService,
                                            subscriptionConnector: SubscriptionConnector,
                                            renderer: Renderer
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  private val logger = LoggerFactory.getLogger(getClass)

  def onPageLoad(): Action[AnyContent] = (identify andThen notEnrolled andThen getData andThen requireData).async {
    implicit request =>

      val helper = new CheckYourAnswersHelper(request.userAnswers)
      val businessDetails: Seq[SummaryList.Row] = buildDetails(helper)
      val contactDetails: Seq[SummaryList.Row] = buildContactDetails(helper)

      val header: String =
        (request.userAnswers.get(BusinessTypePage), request.userAnswers.get(RegistrationTypePage)) match {
          case (Some(_), _) => "checkYourAnswers.businessDetails.h2"
          case (_, Some(RegistrationType.Business)) => "checkYourAnswers.businessDetails.h2"
          case _ => "checkYourAnswers.individualDetails.h2"
        }

      renderer.render(
        "check-your-answers.njk",
        Json.obj(
          "header" -> header,
          "businessDetailsList" -> businessDetails,
          "contactDetailsList" -> contactDetails,
        )
      ).map(Ok(_))
  }

  private def buildDetails(helper: CheckYourAnswersHelper): Seq[SummaryList.Row] = {

    val pagesToCheck = Tuple4(
      helper.businessType,
      helper.nino,
      helper.businessWithoutIDName,
      helper.nonUkName
    )

    pagesToCheck match {
      case (Some(_), None, None, None) =>
        //Business with ID (inc. Sole proprietor)
        Seq(
          helper.confirmBusiness
        ).flatten

      case (None, Some(_), None, None) =>
        //Individual with ID
        Seq(
          helper.doYouHaveUTR,
          helper.registrationType,
          helper.doYouHaveANationalInsuranceNumber,
          helper.nino,
          helper.namePage,
          helper.dateOfBirth
        ).flatten
      case (None, None, Some(_), None) =>
        //Business without ID
        Seq(
          helper.doYouHaveUTR,
          helper.registrationType,
          helper.businessWithoutIDName,
          helper.businessAddress
        ).flatten
      case (None, None, None, Some(_)) =>
        //Individual without ID
        Seq(
          helper.doYouHaveUTR,
          helper.registrationType,
          helper.doYouHaveANationalInsuranceNumber,
          helper.nonUkName,
          helper.dateOfBirth,
          helper.doYouLiveInTheUK,
          helper.whatIsYourAddress,
          helper.selectAddress,
          helper.whatIsYourAddressUk
        ).flatten
      case _ =>
        //All pages
        Seq(
          helper.doYouHaveUTR,
          helper.confirmBusiness,
          helper.nino,
          helper.namePage,
          helper.dateOfBirth,
          helper.registrationType,
          helper.businessWithoutIDName,
          helper.businessAddress,
          helper.doYouHaveANationalInsuranceNumber,
          helper.nonUkName,
          helper.doYouLiveInTheUK,
          helper.whatIsYourAddress,
          helper.whatIsYourAddressUk
        ).flatten
    }
  }

  private def buildContactDetails(helper: CheckYourAnswersHelper): Seq[SummaryList.Row] = {
    Seq(
      helper.contactName,
      helper.contactEmailAddress,
      helper.telephoneNumberQuestion,
      helper.contactTelephoneNumber,
      helper.haveSecondContact,
      helper.secondaryContactName,
      helper.secondaryContactPreference,
      helper.secondaryContactEmailAddress,
      helper.secondaryContactTelephoneNumber
    ).flatten
  }

  def onSubmit(): Action[AnyContent] = (identify andThen notEnrolled andThen getData andThen requireData).async {
    implicit request =>

      (request.userAnswers.get(DoYouHaveUTRPage), request.userAnswers.get(RegistrationTypePage),
        request.userAnswers.get(DoYouHaveANationalInsuranceNumberPage)) match {

        case (Some(true), None, None) => createSubscriptionThenEnrolment(request.userAnswers)
        case (Some(false), Some(Individual), Some(true)) => createSubscriptionThenEnrolment(request.userAnswers)

        case (Some(false), _, Some(false) | None) => registrationService.sendRegistration(request.userAnswers) flatMap {
          case Some(response) => response.status match {
            case OK => createSubscriptionThenEnrolment(request.userAnswers, Some(response))
            case _ => Future.successful(Redirect(routes.ProblemWithServiceController.onPageLoad()))
          }
          case _ => Future.successful(Redirect(routes.ProblemWithServiceController.onPageLoad()))
        }
        case _ => Future.successful(Redirect(routes.ProblemWithServiceController.onPageLoad()))
      }
  }

  private def createSubscriptionThenEnrolment(userAnswers: UserAnswers,
                                              response: Option[HttpResponse] = None)
                                              (implicit hc: HeaderCarrier): Future[Result] = {

    if (response.isDefined) {
      //Without id journey
      response.get.json.validate[PayloadRegistrationWithoutIDResponse] match {
        case JsSuccess(registerWithoutIDResponse, _) if registerWithoutIDResponse.registerWithoutIDResponse.responseDetail.isDefined =>
          updateUserAnswersWithSafeID(userAnswers, registerWithoutIDResponse).flatMap {
            userAnswersWithSafeID =>
              createEISSubscription(userAnswersWithSafeID).flatMap {
                userAnswersWithSubscriptionID =>
                  createEnrolment(userAnswersWithSubscriptionID)
              }
          }.recover {
            case e: Exception =>
              logger.warn("Unable to update UserAnswers with SafeID", e)
              Redirect(routes.ProblemWithServiceController.onPageLoad())
          }
        case JsSuccess(_, _) =>
          logger.warn("Response detail is missing from PayloadRegistrationWithoutIDResponse")
          Future.successful(Redirect(routes.ProblemWithServiceController.onPageLoad()))
        case JsError(errors) =>
          logger.warn("Unable to deserialise into PayloadRegistrationWithoutIDResponse", errors)
          Future.successful(Redirect(routes.ProblemWithServiceController.onPageLoad()))
      }
    } else {
      //With id journey
      createEISSubscription(userAnswers).flatMap {
        userAnswersWithSubscriptionID =>
          createEnrolment(userAnswersWithSubscriptionID)
      }.recover {
        case e: Exception =>
          logger.warn("Unable to create an EIS subscription", e)
          Redirect(routes.ProblemWithServiceController.onPageLoad())
      }
    }
  }

  private def updateUserAnswersWithSafeID(userAnswers: UserAnswers,
                                          registerWithoutIDResponse: PayloadRegistrationWithoutIDResponse)
                                          (implicit hc: HeaderCarrier): Future[UserAnswers] = {
      val safeID = registerWithoutIDResponse.registerWithoutIDResponse.responseDetail.get.SAFEID

      for {
        updatedUserAnswers <- Future.fromTry(userAnswers.set(SafeIDPage, safeID))
        _ <- sessionRepository.set(updatedUserAnswers)
      } yield updatedUserAnswers
  }

  private def createEISSubscription(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers] = {
    subscriptionConnector.createSubscription(userAnswers).flatMap {
      response =>
        val subscriptionID = response.get.createSubscriptionForDACResponse.responseDetail.subscriptionID
        for {
          updatedUserAnswers <- Future.fromTry(userAnswers.set(SubscriptionIDPage, subscriptionID))
          _ <- sessionRepository.set(updatedUserAnswers)
        } yield updatedUserAnswers
    }.recover {
      case e: Exception =>
        logger.warn("Unable to create an EIS subscription", e)
        throw e
    }
  }

  private def logEmailResponse(emailResponse: Option[HttpResponse]): Unit = {
    emailResponse match {
      case Some(HttpResponse(NOT_FOUND, _, _)) => logger.warn("The template cannot be found within the email service")
      case Some(HttpResponse(BAD_REQUEST, _, _)) => logger.warn("Missing email or name parameter")
      case _ => Unit
    }
  }

  def createEnrolment(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Result] = {
    subscriptionConnector.createEnrolment(userAnswers).flatMap {
      subscriptionResponse =>
        if (subscriptionResponse.status.equals(NO_CONTENT)) {
          emailService.sendEmail(userAnswers).map {
            emailResponse =>
              logEmailResponse(emailResponse)
              Redirect(routes.RegistrationSuccessfulController.onPageLoad())
          }.recover {
            case e: Exception => Redirect(routes.RegistrationSuccessfulController.onPageLoad())
          }
        } else {
          Future(Redirect(routes.ProblemWithServiceController.onPageLoad()))
        }
    }

  }
}
