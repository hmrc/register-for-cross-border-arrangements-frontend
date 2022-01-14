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
import forms.SecondaryContactEmailAddressFormProvider
import models.{CheckMode, Mode}
import navigation.Navigator
import pages.{SecondaryContactEmailAddressPage, SecondaryContactNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SecondaryContactEmailAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  notEnrolled: NotEnrolledForDAC6Action,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SecondaryContactEmailAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen notEnrolled andThen getData andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(SecondaryContactEmailAddressPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      val contactName = request.userAnswers.get(SecondaryContactNamePage) match {
        case None              => "your second contact"
        case Some(contactName) => s"$contactName"
      }

      val json = Json.obj(
        "form"        -> preparedForm,
        "mode"        -> mode,
        "contactName" -> contactName
      )

      renderer.render("secondaryContactEmailAddress.njk", json).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen notEnrolled andThen getData andThen requireData).async {
    implicit request =>
      val contactName = request.userAnswers.get(SecondaryContactNamePage) match {
        case None              => "your second contact"
        case Some(contactName) => s"$contactName"
      }

      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val json = Json.obj(
              "form"        -> formWithErrors,
              "mode"        -> mode,
              "contactName" -> contactName
            )

            renderer.render("secondaryContactEmailAddress.njk", json).map(BadRequest(_))
          },
          value => {
            val redirectToSummary = request.userAnswers.get(SecondaryContactEmailAddressPage) match {
              case Some(ans) if (ans == value) && (mode == CheckMode) => true
              case _                                                  => false
            }
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(SecondaryContactEmailAddressPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield
              if (redirectToSummary) {
                Redirect(routes.CheckYourAnswersController.onPageLoad())
              } else {
                Redirect(navigator.nextPage(SecondaryContactEmailAddressPage, mode, updatedAnswers))
              }
          }
        )
  }
}
