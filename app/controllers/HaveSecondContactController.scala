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
import forms.HaveSecondContactFormProvider
import javax.inject.Inject
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.{ContactNamePage, HaveSecondContactPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class HaveSecondContactController @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: HaveSecondContactFormProvider,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      if (request.userAnswers.get(ContactNamePage).isEmpty){
        Future(Redirect(routes.ContactNameController.onPageLoad(NormalMode))) }

        else{

        val contactName = request.userAnswers.get(ContactNamePage).get
        val updatedContactName = s"${contactName.firstName} ${contactName.secondName}"

          val preparedForm = request.userAnswers.get(HaveSecondContactPage) match {
            case None => form
            case Some(confirm) => form.fill(confirm)
          }



          val json = Json.obj(
            "form"   -> preparedForm,
            "mode"   -> mode,
            "radios" -> Radios.yesNo(preparedForm("confirm")),
            "contactName" -> updatedContactName
          )

          renderer.render("haveSecondContact.njk", json).map(Ok(_))
        }
      }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val contactName = request.userAnswers.get(ContactNamePage).get
      val updatedContactName = s"${contactName.firstName} ${contactName.secondName}"

      form.bindFromRequest().fold(
        formWithErrors => {


          val json = Json.obj(
            "form"   -> formWithErrors,
            "mode"   -> mode,
            "radios" -> Radios.yesNo(formWithErrors("confirm")),
            "contactName" -> updatedContactName
          )

          renderer.render("haveSecondContact.njk", json).map(BadRequest(_))
        },
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(HaveSecondContactPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(HaveSecondContactPage, mode, updatedAnswers))
      )
  }
}
