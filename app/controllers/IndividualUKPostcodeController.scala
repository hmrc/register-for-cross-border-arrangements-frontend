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

import connectors.AddressLookupConnector
import controllers.actions._
import forms.IndividualUKPostcodeFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.{AddressLookupPage, IndividualUKPostcodePage}
import play.api.data.FormError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class IndividualUKPostcodeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  addressLookupConnector: AddressLookupConnector,
  identify: IdentifierAction,
  notEnrolled: NotEnrolledForDAC6Action,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: IndividualUKPostcodeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen notEnrolled andThen getData andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(IndividualUKPostcodePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      val json = Json.obj(
        "form"             -> preparedForm,
        "mode"             -> mode,
        "manualAddressURL" -> routes.WhatIsYourAddressUkController.onPageLoad(mode).url
      )

      renderer.render("individualUKPostcode.njk", json).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen notEnrolled andThen getData andThen requireData).async {
    implicit request =>
      val manualAddressURL: String = routes.WhatIsYourAddressUkController.onPageLoad(mode).url
      val formReturned             = form.bindFromRequest()

      formReturned.fold(
        formWithErrors => {

          val json = Json.obj(
            "form"             -> formWithErrors,
            "mode"             -> mode,
            "manualAddressURL" -> manualAddressURL
          )

          {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.remove(IndividualUKPostcodePage))
              _              <- sessionRepository.set(updatedAnswers)
            } yield renderer.render("individualUKPostcode.njk", json).map(BadRequest(_))
          }.flatten
        },
        postCode =>
          addressLookupConnector.addressLookupByPostcode(postCode).flatMap {
            case Nil =>
              val formError = formReturned.withError(FormError("postCode", List("individualUKPostcode.error.notFound")))

              val json = Json.obj(
                "form"             -> formError,
                "mode"             -> mode,
                "manualAddressURL" -> manualAddressURL
              )

              {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(IndividualUKPostcodePage, postCode))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield renderer.render("individualUKPostcode.njk", json).map(BadRequest(_))
              }.flatten
            case addresses =>
              for {
                updatedAnswers              <- Future.fromTry(request.userAnswers.set(IndividualUKPostcodePage, postCode))
                updatedAnswersWithAddresses <- Future.fromTry(updatedAnswers.set(AddressLookupPage, addresses))
                _                           <- sessionRepository.set(updatedAnswersWithAddresses)
              } yield Redirect(navigator.nextPage(IndividualUKPostcodePage, mode, updatedAnswersWithAddresses))
          }
      )
  }
}
