/*
 * Copyright 2023 HM Revenue & Customs
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

import config.FrontendAppConfig
import controllers.actions._
import forms.DoYouHaveUTRFormProvider
import helpers.JourneyHelpers.redirectToSummary
import models.{Mode, UserAnswers, UserAnswersHelper}
import navigation.Navigator
import pages.DoYouHaveUTRPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DoYouHaveUTRController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  appConfig: FrontendAppConfig,
  navigator: Navigator,
  identify: IdentifierAction,
  notEnrolled: NotEnrolledForDAC6Action,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DoYouHaveUTRFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen notEnrolled andThen getData).async {
    implicit request =>
      val preparedForm = request.userAnswers.flatMap(_.get(DoYouHaveUTRPage)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      val json = Json.obj(
        "form"       -> preparedForm,
        "mode"       -> mode,
        "radios"     -> Radios.yesNo(preparedForm("confirm")),
        "lostUTRUrl" -> appConfig.lostUTRUrl
      )

      renderer.render("doYouHaveUTR.njk", json).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen notEnrolled andThen getData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {

            val json = Json.obj(
              "form"       -> formWithErrors,
              "mode"       -> mode,
              "radios"     -> Radios.yesNo(formWithErrors("confirm")),
              "lostUTRUrl" -> appConfig.lostUTRUrl
            )

            renderer.render("doYouHaveUTR.njk", json).map(BadRequest(_))
          },
          value => {
            val initialUserAnswers = UserAnswers(request.internalId)
            val userAnswers = request.userAnswers.fold(initialUserAnswers)(
              ua => ua
            )

            val redirectUsers = redirectToSummary(value, DoYouHaveUTRPage, mode, userAnswers)

            for {
              updatedAnswers <- UserAnswersHelper.updateUserAnswersIfValueChanged(userAnswers, DoYouHaveUTRPage, value)
              _              <- sessionRepository.set(updatedAnswers)
            } yield
              if (redirectUsers) {
                Redirect(routes.CheckYourAnswersController.onPageLoad)
              } else {
                Redirect(navigator.nextPage(DoYouHaveUTRPage, mode, updatedAnswers))
              }
          }
        )
  }
}
