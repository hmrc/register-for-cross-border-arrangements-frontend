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
import models.BusinessType.{CorporateBody, UnIncorporatedBody}
import models.NormalMode
import pages.BusinessTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class BusinessNotIdentifiedController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  notEnrolled: NotEnrolledForDAC6Action,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  appConfig: FrontendAppConfig,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen notEnrolled andThen getData andThen requireData).async {
    implicit request =>
      val contactLink: String = request.userAnswers.get(BusinessTypePage) match {
        case Some(CorporateBody) | Some(UnIncorporatedBody) => appConfig.corporationTaxEnquiriesUrl
        case _                                              => appConfig.selfAssessmentEnquiriesUrl
      }

      val json = Json.obj(
        "contactLink"  -> contactLink,
        "emailAddress" -> appConfig.emailEnquiries,
        "lostUtrLink"  -> appConfig.lostUTRUrl,
        "tryAgainLink" -> routes.DoYouHaveUTRController.onSubmit(NormalMode).url
      )

      renderer.render("businessNotIdentified.njk", json).map(Ok(_))
  }

}
