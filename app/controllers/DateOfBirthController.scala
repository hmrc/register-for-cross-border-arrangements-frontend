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

import controllers.actions._
import forms.DateOfBirthFormProvider
import javax.inject.Inject
import models.{CheckMode, Mode}
import navigation.Navigator
import pages.{DateOfBirthPage, DoYouHaveANationalInsuranceNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{DateInput, NunjucksSupport}

import scala.concurrent.{ExecutionContext, Future}

class DateOfBirthController @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: DateOfBirthFormProvider,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(DateOfBirthPage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }

      val viewModel = DateInput.localDate(preparedForm("value"))

      val json = Json.obj(
        "form" -> preparedForm,
        "mode" -> mode,
        "date" -> viewModel
      )

      renderer.render("dateOfBirth.njk", json).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>  {

          val viewModel = DateInput.localDate(formWithErrors("value"))

          val json = Json.obj(
            "form" -> formWithErrors,
            "mode" -> mode,
            "date" -> viewModel
          )

          renderer.render("dateOfBirth.njk", json).map(BadRequest(_))
        },
        value => {
          val redirectToSummary =
            (request.userAnswers.get(DoYouHaveANationalInsuranceNumberPage),
              request.userAnswers.get(DateOfBirthPage)) match {
              case (Some(false), Some(_)) if mode == CheckMode => true //Individual without ID
              case (Some(true), Some(ans)) if (ans == value) && (mode == CheckMode) => false //Individual with ID
              case _ => false //Normal mode journey
            }

          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(DateOfBirthPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            if (redirectToSummary) {
              Redirect(routes.CheckYourAnswersController.onPageLoad())
            } else {
              Redirect(navigator.nextPage(DateOfBirthPage, mode, updatedAnswers))
            }
          }
        }
      )
  }
}
