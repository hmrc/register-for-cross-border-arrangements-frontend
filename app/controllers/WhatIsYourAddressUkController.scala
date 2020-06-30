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
import forms.WhatIsYourAddressUkFormProvider
import javax.inject.Inject
import models.{Mode, Country}
import navigation.Navigator
import pages.WhatIsYourAddressUkPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.CountryListFactory
import play.api.libs.json.{JsObject, Json}
import scala.concurrent.{ExecutionContext, Future}

class WhatIsYourAddressUkController @Inject()(
    override val messagesApi: MessagesApi,
    countryListFactory: CountryListFactory,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: WhatIsYourAddressUkFormProvider,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val countries = Seq(countryListFactory.uk)
      val form = formProvider(countries)

      val preparedForm = request.userAnswers.get(WhatIsYourAddressUkPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val json = Json.obj(
        "form" -> preparedForm,
        "mode" -> mode,
        "countries" -> countryJsonList
      )

      renderer.render("whatIsYourAddressUk.njk", json).map(Ok(_))
  }


  private def countryJsonList: Seq[JsObject] = Seq(Json.obj("text" -> countryListFactory.uk.description,
    "value" -> countryListFactory.uk.code,
    "selected" -> true))

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>


      val countries = Seq(countryListFactory.uk)
      val form = formProvider(countries)



      form.bindFromRequest().fold(
        formWithErrors => {

          val json = Json.obj(
            "form" -> formWithErrors,
            "mode" -> mode,
            "countries" -> countryJsonList
          )

          renderer.render("whatIsYourAddressUk.njk", json).map(BadRequest(_))
        },
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatIsYourAddressUkPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(WhatIsYourAddressUkPage, mode, updatedAnswers))
      )
  }
}
