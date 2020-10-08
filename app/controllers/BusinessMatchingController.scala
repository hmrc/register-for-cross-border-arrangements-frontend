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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, NotEnrolledForDAC6Action}
import javax.inject.Inject
import models.{Mode, NormalMode}
import navigation.Navigator
import pages._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import services.BusinessMatchingService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

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
                                            renderer: Renderer
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  def matchIndividual(mode: Mode): Action[AnyContent] = (identify andThen notEnrolled andThen getData andThen requireData).async {
    implicit request =>
      businessMatchingService.sendIndividualMatchingInformation(request.userAnswers) map {
        case Right(Some(_)) => Redirect(routes.IdentityConfirmedController.onPageLoad()) //TODO: may need more data collected for Cardiff team
        case Right(None) => Redirect(routes.IndividualNotConfirmedController.onPageLoad())
        //we are missing a name or a date of birth take them back to fill it in
        case Left(_) => Redirect(routes.NameController.onPageLoad(NormalMode))
      }
  }

  def matchBusiness(mode: Mode): Action[AnyContent] =
    (identify andThen notEnrolled andThen getData andThen requireData).async {
    implicit request =>

      /*Note: Needs business type, name and utr to business match
      * Checking UTR page only because /registered-business-name uses the business type before calling this method
      */
      val utrExist = (request.userAnswers.get(SelfAssessmentUTRPage), request.userAnswers.get(CorporationTaxUTRPage)) match {
        case (Some(_), _) | (_, Some(_)) => true
        case _ => false
      }

      if (utrExist) {
        businessMatchingService.sendBusinessMatchingInformation(request.userAnswers) flatMap {

          case (Some(details), Some(id)) =>
            for {
              updatedAnswersWithBusinessDetails <- Future.fromTry(request.userAnswers.set(BusinessAddressPage, details.address.toAddress))
              updatedAnswersWithBusinessDetails <- Future.fromTry(updatedAnswersWithBusinessDetails.set(RetrievedNamePage, details.name))
              updatedAnswersWithSafeID <- Future.fromTry(updatedAnswersWithBusinessDetails.set(SafeIDPage, id))
              _                  <- sessionRepository.set(updatedAnswersWithSafeID)
            } yield {
              Redirect(routes.ConfirmBusinessController.onPageLoad(NormalMode))
            }
          case (None, _) => Future.successful(Redirect(routes.BusinessNotConfirmedController.onPageLoad()))
        } recover {
          case _ => Redirect(routes.BusinessNotConfirmedController.onPageLoad()) //TODO Redirect to error page when it's ready
        }
      } else {
        Future.successful(Redirect(routes.DoYouHaveUTRController.onPageLoad(NormalMode)))
      }
  }
}
