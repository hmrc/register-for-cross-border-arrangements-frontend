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

import controllers.actions._
import forms.ConfirmBusinessFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.{BusinessAddressPage, ConfirmBusinessPage, RetrievedNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class ConfirmBusinessController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  notEnrolled: NotEnrolledForDAC6Action,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ConfirmBusinessFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen notEnrolled andThen getData andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(ConfirmBusinessPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      val businessName = request.userAnswers.get(RetrievedNamePage).getOrElse(throw new Exception("Cannot retrieve business name"))
      val addressModel = request.userAnswers.get(BusinessAddressPage).getOrElse(throw new Exception("Cannot retrieve business address"))

      val json = Json.obj(
        "businessName" -> businessName,
        "address"      -> addressModel,
        "form"         -> preparedForm,
        "mode"         -> mode,
        "radios"       -> Radios.yesNo(preparedForm("confirm"))
      )

      renderer.render("confirmBusiness.njk", json).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen notEnrolled andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {

            val businessName = request.userAnswers.get(RetrievedNamePage).getOrElse(throw new Exception("Cannot retrieve business name"))
            val addressModel = request.userAnswers.get(BusinessAddressPage).getOrElse(throw new Exception("Cannot retrieve business address"))

            val json = Json.obj(
              "businessName" -> businessName,
              "address"      -> addressModel,
              "form"         -> formWithErrors,
              "mode"         -> mode,
              "radios"       -> Radios.yesNo(formWithErrors("confirm"))
            )

            renderer.render("confirmBusiness.njk", json).map(BadRequest(_))
          },
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ConfirmBusinessPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ConfirmBusinessPage, mode, updatedAnswers))
        )
  }
}
