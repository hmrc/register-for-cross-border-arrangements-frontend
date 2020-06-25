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

import connectors.AddressLookupConnector
import controllers.actions._
import forms.SelectAddressFormProvider
import javax.inject.Inject
import models.{AddressLookup, Mode, SelectAddress}
import navigation.Navigator
import pages.{PostCodePage, SelectAddressPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import uk.gov.hmrc.viewmodels._

import scala.concurrent.{ExecutionContext, Future}

class SelectAddressController @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: SelectAddressFormProvider,
    addressLookupConnector: AddressLookupConnector,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val postCode = request.userAnswers.get(PostCodePage) match {
        case Some(postCode) => postCode.replaceAll(" ", "").toUpperCase
        case None => "" //TODO
      }

    addressLookupConnector.addressLookupByPostcode(postCode) flatMap {
      case Nil => Future.successful(Redirect(routes.IndexController.onPageLoad()))
      case addresses =>
        val preparedForm: Form[SelectAddress] = request.userAnswers.get(SelectAddressPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        val addressItems = addresses.map( address =>
          Radios.Radio(label = msg"${formatAddress(address)}", value = "VALUE NAME")
        )

        val radios = Radios(field = preparedForm("value"), items = addressItems)

        val json = Json.obj(
          "form" -> preparedForm,
          "mode" -> mode,
          "radios" -> radios
        )

        renderer.render("selectAddress.njk", json).map(Ok(_))
      case _ => Future.successful(Redirect(routes.IndexController.onPageLoad()))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
//TODO Need to fix onSubmit
      form.bindFromRequest().fold(
        formWithErrors => {

          val json = Json.obj(
            "form"   -> formWithErrors,
            "mode"   -> mode,
            "radios" -> SelectAddress.radios(formWithErrors)
          )

          renderer.render("selectAddress.njk", json).map(BadRequest(_))
        },
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(SelectAddressPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(SelectAddressPage, mode, updatedAnswers))
      )
  }

  private def formatAddress(address: AddressLookup) ={
    val lines = address.lines.mkString(", ")
    val county = address.county.fold("")(county => s"$county, ")

    s"$lines, ${address.town}, $county ${address.postcode}"
  }
}
