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
import forms.ContactTelephoneNumberFormProvider
import helpers.JourneyHelpers
import javax.inject.Inject
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.{ContactNamePage, ContactTelephoneNumberPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class ContactTelephoneNumberController @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: ContactTelephoneNumberFormProvider,
    journeyHelpers: JourneyHelpers,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      (journeyHelpers.organisationJourney(request.userAnswers), request.userAnswers.get(ContactNamePage)) match {
        case (true, None) => Future.successful(Redirect(routes.ContactNameController.onPageLoad(NormalMode)))
        case _ =>
          val preparedForm = request.userAnswers.get(ContactTelephoneNumberPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          val (pageTitle, heading) = request.userAnswers.get(ContactNamePage) match {
            case Some(name) =>
              (Messages("contactTelephoneNumber.business.title"),
                Messages("contactTelephoneNumber.business.heading", s"${name.firstName} ${name.secondName}"))
            case None =>
              (Messages("contactTelephoneNumber.individual.title"),
                Messages("contactTelephoneNumber.individual.heading"))
          }

          val json = Json.obj(
            "form" -> preparedForm,
            "mode" -> mode,
            "pageTitle" -> pageTitle,
            "heading" -> heading
          )

          renderer.render("contactTelephoneNumber.njk", json).map(Ok(_))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => {

          val (pageTitle, heading) = request.userAnswers.get(ContactNamePage) match {
            case Some(name) =>
              (Messages("contactTelephoneNumber.business.title"),
                Messages("contactTelephoneNumber.business.heading", s"${name.firstName} ${name.secondName}"))
            case None =>
              (Messages("contactTelephoneNumber.title"),
                Messages("contactTelephoneNumber.heading"))
          }

          val json = Json.obj(
            "form" -> formWithErrors,
            "mode" -> mode,
            "pageTitle" -> pageTitle,
            "heading" -> heading
          )

          renderer.render("contactTelephoneNumber.njk", json).map(BadRequest(_))
        },
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ContactTelephoneNumberPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ContactTelephoneNumberPage, mode, updatedAnswers))//TODO change to second contact page once built
      )
  }
}
