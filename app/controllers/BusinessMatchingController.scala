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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import javax.inject.Inject
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.{BusinessNamePage, BusinessTypePage, IsThisYourBusinessPage, UniqueTaxpayerReferencePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import services.BusinessMatchingService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class BusinessMatchingController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            sessionRepository: SessionRepository,
                                            navigator: Navigator,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            businessMatchingService: BusinessMatchingService,
                                            val controllerComponents: MessagesControllerComponents,
                                            renderer: Renderer
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  def matchIndividual(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      businessMatchingService.sendIndividualMatchingInformation(request.userAnswers) map {
        case Some(response) => response.status match {
          case OK => Redirect(routes.CheckYourAnswersController.onPageLoad()) //TODO: may need more data collected for Cardiff team
          case NOT_FOUND => Redirect(routes.IdentityController.couldntConfirmIdentity())
          case _ => Redirect(routes.IndexController.onPageLoad())
        }

        //we are missing a name or a date of birth take them back to fill it in
        case None => Redirect(routes.NameController.onPageLoad(NormalMode))
      }
  }

  def matchBusiness(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val pagesToCheck = Tuple3(
        request.userAnswers.get(BusinessTypePage),
        request.userAnswers.get(UniqueTaxpayerReferencePage),
        request.userAnswers.get(BusinessNamePage)
      )

      pagesToCheck match {
        case (Some(_), Some(_), Some(_)) =>
          businessMatchingService.sendBusinessMatchingInformation(request.userAnswers) flatMap {
            case Some(address) =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(IsThisYourBusinessPage, address))
                _              <- sessionRepository.set(updatedAnswers)
              } yield {
                Redirect(navigator.nextPage(IsThisYourBusinessPage, mode, updatedAnswers))
              }
            case None => Future.successful(Redirect(routes.IdentityController.couldntConfirmIdentity()))
          } recover {
            case _ => Redirect(routes.IdentityController.couldntConfirmIdentity()) //TODO Redirect to error page when it's ready
          }
        case (None, _, _) => Future.successful(Redirect(routes.BusinessTypeController.onPageLoad(NormalMode)))
        case (Some(_), None, _) => Future.successful(Redirect(routes.UniqueTaxpayerReferenceController.onPageLoad(NormalMode)))
        case (Some(_), Some(_), None) => Future.successful(Redirect(routes.IdentityController.couldntConfirmIdentity())) //TODO Redirect to business name controller when it's ready
      }
  }

}
