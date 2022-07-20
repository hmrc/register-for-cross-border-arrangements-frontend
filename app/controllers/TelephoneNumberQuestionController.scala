/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.actions._
import forms.TelephoneNumberQuestionFormProvider
import helpers.JourneyHelpers._
import javax.inject.Inject
import models.{CheckMode, Mode, NormalMode}
import navigation.Navigator
import pages.{ContactNamePage, TelephoneNumberQuestionPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class TelephoneNumberQuestionController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  notEnrolled: NotEnrolledForDAC6Action,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: TelephoneNumberQuestionFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen notEnrolled andThen getData andThen requireData).async {
    implicit request =>
      (isOrganisationJourney(request.userAnswers), request.userAnswers.get(ContactNamePage)) match {
        case (true, None) => Future.successful(Redirect(routes.ContactNameController.onPageLoad(NormalMode)))
        case _ =>
          val preparedForm = request.userAnswers.get(TelephoneNumberQuestionPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          val (pageTitle, heading) = request.userAnswers.get(ContactNamePage) match {
            case Some(name) =>
              (Messages("telephoneNumberQuestion.business.title"), Messages("telephoneNumberQuestion.business.heading", name))
            case None =>
              (Messages("telephoneNumberQuestion.individual.title"), Messages("telephoneNumberQuestion.individual.heading"))
          }

          val json = Json.obj(
            "form"      -> preparedForm,
            "mode"      -> mode,
            "radios"    -> Radios.yesNo(preparedForm("confirm")),
            "pageTitle" -> pageTitle,
            "heading"   -> heading
          )

          renderer.render("telephoneNumberQuestion.njk", json).map(Ok(_))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen notEnrolled andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {

            val (pageTitle: String, heading: String) = request.userAnswers.get(ContactNamePage) match {
              case Some(name) =>
                (Messages("telephoneNumberQuestion.business.title"), Messages("telephoneNumberQuestion.business.heading", name))
              case None =>
                (Messages("telephoneNumberQuestion.individual.title"), Messages("telephoneNumberQuestion.individual.heading"))
            }

            val json = Json.obj(
              "form"      -> formWithErrors,
              "mode"      -> mode,
              "radios"    -> Radios.yesNo(formWithErrors("confirm")),
              "pageTitle" -> pageTitle,
              "heading"   -> heading
            )

            renderer.render("telephoneNumberQuestion.njk", json).map(BadRequest(_))
          },
          value => {
            val redirectToSummary = request.userAnswers.get(TelephoneNumberQuestionPage) match {
              case Some(ans) if (ans == value) && (mode == CheckMode) => true
              case Some(_) if !value && (mode == CheckMode)           => true
              case _                                                  => false
            }

            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(TelephoneNumberQuestionPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield
              if (redirectToSummary) {
                Redirect(routes.CheckYourAnswersController.onPageLoad)
              } else {
                Redirect(navigator.nextPage(TelephoneNumberQuestionPage, mode, updatedAnswers))
              }
          }
        )
  }
}
