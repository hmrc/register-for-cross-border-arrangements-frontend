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

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.actions._
import forms.SelectAddressFormProvider
import javax.inject.Inject
import models.{AddressLookup, Mode, NormalMode}
import navigation.Navigator
import pages.{IndividualUKPostcodePage, SelectAddressPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, _}

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
    appConfig: FrontendAppConfig,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
    val manualAddressURL: String = routes.WhatIsYourAddressUkController.onPageLoad(NormalMode).url
    val postCode = request.userAnswers.get(IndividualUKPostcodePage) match {
      case Some(postCode) => postCode.replaceAll(" ", "").toUpperCase
      case None => ""
    }

    addressLookupConnector.addressLookupByPostcode(postCode) flatMap {
      case Nil => Future.successful(Redirect(routes.WhatIsYourAddressUkController.onPageLoad(NormalMode)))
      case addresses =>
        val preparedForm: Form[String] = request.userAnswers.get(SelectAddressPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        val addressItems: Seq[Radios.Radio] = addresses.map(address =>
          Radios.Radio(label = msg"${formatAddress(address)}", value = s"${formatAddress(address)}"))
        val radios = Radios(field = preparedForm("value"), items = addressItems)

        val json = Json.obj(
          "form" -> preparedForm,
          "mode" -> mode,
          "manualAddressURL" -> manualAddressURL,
          "radios" -> radios
        )

        renderer.render("selectAddress.njk", json).map(Ok(_))
    } recover {
      case _: Exception => Redirect(routes.WhatIsYourAddressUkController.onPageLoad(NormalMode))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val manualAddressURL: String = routes.WhatIsYourAddressUkController.onPageLoad(NormalMode).url
      val postCode = request.userAnswers.get(IndividualUKPostcodePage) match {
        case Some(postCode) => postCode.replaceAll(" ", "").toUpperCase
        case None => ""
      }

      addressLookupConnector.addressLookupByPostcode(postCode) flatMap {
        addresses =>
          val addressItems: Seq[Radios.Radio] = addresses.map(address =>
            Radios.Radio(label = msg"${formatAddress(address)}", value = s"${formatAddress(address)}")
          )

          form.bindFromRequest().fold(
            formWithErrors => {

              val radios = Radios(field = formWithErrors("value"), items = addressItems)

              val json = Json.obj(
                "form" -> formWithErrors,
                "mode" -> mode,
                "manualAddressURL" -> manualAddressURL,
                "radios" -> radios
              )

              renderer.render("selectAddress.njk", json).map(BadRequest(_))
            },
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(SelectAddressPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(SelectAddressPage, mode, updatedAnswers))
          )
      } recover {
        case _: Exception => Redirect(routes.WhatIsYourAddressUkController.onPageLoad(NormalMode))
      }
  }

  private def formatAddress(address: AddressLookup) = {
    val lines = Seq(address.addressLine1, address.addressLine2, address.addressLine3, address.addressLine4).flatten.mkString(", ")
    val county = address.county.fold("")(county => s"$county, ")

    s"$lines, ${address.town}, $county${address.postcode}"
  }
}
