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
import handlers.ErrorHandler

import javax.inject.Inject
import pages.SubscriptionIDPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.Html

import scala.concurrent.ExecutionContext

class RegistrationSuccessfulController @Inject() (
  override val messagesApi: MessagesApi,
  appConfig: FrontendAppConfig,
  identify: IdentifierAction,
  ignoreSubscription: IgnoreSubscriptionAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  errorHandler: ErrorHandler,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen ignoreSubscription andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(SubscriptionIDPage) match {
        case Some(id) =>
          val json = Json.obj(
            "subscriptionID"     -> confirmationPanelText(id),
            "submissionUrl"      -> appConfig.dacSubmissionsUrl,
            "betaFeedbackSurvey" -> appConfig.betaFeedbackUrl
          )
          sessionRepository.reset(request.internalId) flatMap {
            _ =>
              renderer.render("registrationSuccessful.njk", json).map(Ok(_))
          }

        case None =>
          errorHandler.onServerError(request, throw new RuntimeException("Subscription ID missing"))
      }
  }

  private def confirmationPanelText(id: String)(implicit messages: Messages): Html =
    Html(s"${messages("registrationSuccessful.panel.html")}<div id='userid'><strong>$id</strong></div>")
}
