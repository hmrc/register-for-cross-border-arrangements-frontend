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

import base.SpecBase
import config.FrontendAppConfig
import connectors.AddressLookupConnector
import forms.SelectAddressFormProvider
import matchers.JsonMatchers
import models.{AddressLookup, NormalMode, SelectAddress, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{IndividualUKPostcodePage, SelectAddressPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.JsObject
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class SelectAddressControllerSpec extends SpecBase
  with MockitoSugar
  with NunjucksSupport
  with JsonMatchers {

  val mockAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]
  val mockFrontendConfig: FrontendAppConfig = mock[FrontendAppConfig]

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val selectAddressRoute: String = routes.SelectAddressController.onPageLoad(NormalMode).url

  val formProvider = new SelectAddressFormProvider()
  val form: Form[String] = formProvider()
  val addresses: Seq[AddressLookup] = Seq(
    AddressLookup(Some("1 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ"),
    AddressLookup(Some("2 Address line 1"), None, None, None, "Town", None, "ZZ1 1ZZ")
  )
  val dacFrontendUrl = "http://localhost:9755/register-for-cross-border-arrangements/"

  "SelectAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockAddressLookupConnector.addressLookupByPostcode(any())(any(), any()))
        .thenReturn(Future.successful(addresses))
      when(mockFrontendConfig.dacFrontendUrl).thenReturn(dacFrontendUrl)

      val answers = UserAnswers(userAnswersId)
        .set(IndividualUKPostcodePage, "ZZ1 1ZZ")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[AddressLookupConnector].toInstance(mockAddressLookupConnector)
        ).build()

      val request = FakeRequest(GET, selectAddressRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
//TODO Needs fixing
//      val expectedJson = Json.obj(
//        "form"   -> form,
//        "mode"   -> NormalMode,
//        "radios" -> SelectAddress.radios(form)
//      )

      templateCaptor.getValue mustEqual "selectAddress.njk"
//      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockAddressLookupConnector.addressLookupByPostcode(any())(any(), any()))
        .thenReturn(Future.successful(addresses))
      when(mockFrontendConfig.dacFrontendUrl).thenReturn(dacFrontendUrl)

      val userAnswers = UserAnswers(userAnswersId)
        .set(SelectAddressPage, "Address")
        .success
        .value
        .set(IndividualUKPostcodePage, "ZZ1 1ZZ")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AddressLookupConnector].toInstance(mockAddressLookupConnector)
        ).build()

      val request = FakeRequest(GET, selectAddressRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> SelectAddress.values.head.toString))

//      val expectedJson = Json.obj(
//        "form"   -> filledForm,
//        "mode"   -> NormalMode,
//        "radios" -> SelectAddress.radios(filledForm)
//      )

      templateCaptor.getValue mustEqual "selectAddress.njk"
//      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockAddressLookupConnector.addressLookupByPostcode(any())(any(), any()))
        .thenReturn(Future.successful(addresses))
      when(mockFrontendConfig.dacFrontendUrl).thenReturn(dacFrontendUrl)

      val answers = UserAnswers(userAnswersId)
        .set(IndividualUKPostcodePage, "ZZ1 1ZZ")
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupConnector].toInstance(mockAddressLookupConnector)
          ).build()

      val request =
        FakeRequest(POST, selectAddressRoute)
          .withFormUrlEncodedBody(("value", "Address"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockAddressLookupConnector.addressLookupByPostcode(any())(any(), any()))
        .thenReturn(Future.successful(addresses))
      when(mockFrontendConfig.dacFrontendUrl).thenReturn(dacFrontendUrl)

      val answers = UserAnswers(userAnswersId)
        .set(IndividualUKPostcodePage, "ZZ1 1ZZ")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[AddressLookupConnector].toInstance(mockAddressLookupConnector)
        ).build()

      val request = FakeRequest(POST, selectAddressRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

//      val expectedJson = Json.obj(
//        "form"   -> boundForm,
//        "mode"   -> NormalMode,
//        "radios" -> SelectAddress.radios(boundForm)
//      )

      templateCaptor.getValue mustEqual "selectAddress.njk"
//      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, selectAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, selectAddressRoute)
          .withFormUrlEncodedBody(("value", SelectAddress.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}