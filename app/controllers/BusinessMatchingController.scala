/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.SubscriptionConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, NotEnrolledForDAC6Action}
import models.{BusinessDetails, Mode, NormalMode, UserAnswers}
import navigation.Navigator
import org.slf4j.LoggerFactory
import pages._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import renderer.Renderer
import repositories.SessionRepository
import services.{BusinessMatchingService, EmailService}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessMatchingController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            sessionRepository: SessionRepository,
                                            navigator: Navigator,
                                            identify: IdentifierAction,
                                            notEnrolled: NotEnrolledForDAC6Action,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            businessMatchingService: BusinessMatchingService,
                                            val controllerComponents: MessagesControllerComponents,
                                            renderer: Renderer,
                                            subscriptionConnector: SubscriptionConnector,
                                            emailService: EmailService,
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  private val logger = LoggerFactory.getLogger(getClass)

  def matchIndividual(mode: Mode): Action[AnyContent] = (identify andThen notEnrolled andThen getData andThen requireData).async {
    implicit request =>
      businessMatchingService.sendIndividualMatchingInformation(request.userAnswers).flatMap {
        case Right((Some(_), Some(id), existingSubscriptionDetails)) =>

          updateIndividualAnswers(request.userAnswers, id).flatMap(updatedUserAnswers =>
              if(existingSubscriptionDetails.isDefined) {
                createEnrolment(updatedUserAnswers, existingSubscriptionDetails.get.displaySubscriptionForDACResponse.responseDetail.subscriptionID)
              } else {
                Future(Redirect(routes.IdentityConfirmedController.onPageLoad()))
              }
          )
        case Right(_) => Future.successful(Redirect(routes.IndividualNotConfirmedController.onPageLoad()))
        //we are missing a name or a date of birth take them back to fill it in
        case Left(_) => Future.successful(Redirect(routes.NameController.onPageLoad(NormalMode)))
      }
  }

  private def updateIndividualAnswers(userAnswers: UserAnswers, safeId: String): Future[UserAnswers] = {
    for {
      updatedAnswersWithSafeID <- Future.fromTry(userAnswers.set(SafeIDPage, safeId))
      _                        <- sessionRepository.set(updatedAnswersWithSafeID)
    } yield {
      updatedAnswersWithSafeID
    }
  }

  def matchBusiness(mode: Mode): Action[AnyContent] =
    (identify andThen notEnrolled andThen getData andThen requireData).async {
    implicit request =>

      /*Note: Needs business type, name and utr to business match
      * Checking UTR page only because /registered-business-name uses the business type before calling this method
      */
      val utrExist: Boolean = (request.userAnswers.get(SelfAssessmentUTRPage), request.userAnswers.get(CorporationTaxUTRPage)) match {
        case (Some(_), _) | (_, Some(_)) => true
        case _ => false
      }

      if (utrExist) {
        businessMatchingService.sendBusinessMatchingInformation(request.userAnswers) flatMap {
          case (Some(details), Some(id), existingSubscriptionInfo) =>
            updateUserAnswers(request.userAnswers, details, id).flatMap { updatedUserAnswers =>
              if (existingSubscriptionInfo.isDefined) {
                createEnrolment(updatedUserAnswers, existingSubscriptionInfo.get.displaySubscriptionForDACResponse.responseDetail.subscriptionID)
              } else {
                Future.successful(Redirect(routes.ConfirmBusinessController.onPageLoad(NormalMode)))
              }
            }
          case _ => Future.successful(Redirect(routes.BusinessNotConfirmedController.onPageLoad()))
        } recover {
          case _ => Redirect(routes.ProblemWithServiceController.onPageLoad())
        }

      } else {
        Future.successful(Redirect(routes.DoYouHaveUTRController.onPageLoad(NormalMode)))
      }
  }

  def updateUserAnswers(userAnswers: UserAnswers, details: BusinessDetails, id :String): Future[UserAnswers] = {
   for {
     updatedAnswersWithBusinessAddress <- Future.fromTry(userAnswers.set(BusinessAddressPage, details.address.toAddress))
     updatedAnswersWithBusinessName <- Future.fromTry(updatedAnswersWithBusinessAddress.set(RetrievedNamePage, details.name))
     updatedAnswersWithSafeID <- Future.fromTry(updatedAnswersWithBusinessName.set(SafeIDPage, id))
     _ <- sessionRepository.set(updatedAnswersWithSafeID)
   } yield updatedAnswersWithSafeID
  }

  def createEnrolment(userAnswers: UserAnswers, subscriptionID: String)(implicit hc: HeaderCarrier): Future[Result] = {
    addEnrolmentIdToUserAnswers(userAnswers, subscriptionID).flatMap { updatedUserAnswers =>
      subscriptionConnector.createEnrolment(updatedUserAnswers).flatMap {
        enrolmentResponse: HttpResponse =>
          enrolmentResponse.status match {
            case NO_CONTENT =>
              emailService.sendEmail(updatedUserAnswers).map {
                emailResponse =>
                  logEmailResponse(emailResponse)
                  Redirect(routes.RegistrationSuccessfulController.onPageLoad())
              }.recover {
                case e: Exception =>
                  logger.warn("email service failed - redirect to registration successful without email")
                  Redirect(routes.RegistrationSuccessfulController.onPageLoad())
              }
            case _ if enrolmentResponse.body.contains("MULTIPLE_ENROLMENTS_INVALID") =>
              Future(Redirect(routes.ThisOrganisationHasAlreadyBeenRegisteredController.onPageLoad()))

            case _ => Future(Redirect(routes.ProblemWithServiceController.onPageLoad()))
          }
      }
    }
  }

  private def addEnrolmentIdToUserAnswers(userAnswers: UserAnswers, subscriptionID: String): Future[UserAnswers] = {

    for {
      updatedAnswersWithSafeID <- Future.fromTry(userAnswers.set(SubscriptionIDPage, subscriptionID))
      _ <- sessionRepository.set(updatedAnswersWithSafeID)
    } yield updatedAnswersWithSafeID
  }

  private def logEmailResponse(emailResponse: Option[HttpResponse]): Unit = {
    emailResponse match {
      case Some(HttpResponse(NOT_FOUND, _, _)) => logger.warn("The template cannot be found within the email service")
      case Some(HttpResponse(BAD_REQUEST, _, _)) => logger.warn("Missing email or name parameter")
      case _ => Unit
    }
  }
}
