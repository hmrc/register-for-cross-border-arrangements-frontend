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
import forms.WhatIsYourEmailAddressFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.{ContactNamePage, WhatIsYourEmailAddressPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class WhatIsYourEmailAddressController @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: WhatIsYourEmailAddressFormProvider,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val contactName = request.userAnswers.get(ContactNamePage) match {
        case None => "your"
        case Some(contactName) => s"${contactName.firstName} ${contactName.secondName}’s"
      }

      val preparedForm = request.userAnswers.get(WhatIsYourEmailAddressPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val json = Json.obj(
        "form" -> preparedForm,
        "mode" -> mode,
        "contactName" -> contactName
      )

      renderer.render("whatIsYourEmailAddress.njk", json).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val contactName = request.userAnswers.get(ContactNamePage) match {
        case None => "your"
        case Some(contactName) => s"${contactName.firstName} ${contactName.secondName}’s"
      }

      form.bindFromRequest().fold(
        formWithErrors => {
          val json = Json.obj(
            "form" -> formWithErrors,
            "mode" -> mode,
            "contactName"-> contactName)

          renderer.render("whatIsYourEmailAddress.njk", json).map(BadRequest(_))
        },
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatIsYourEmailAddressPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(WhatIsYourEmailAddressPage, mode, updatedAnswers)) //TODO to be changed once telephone page is created
      )
  }
}
