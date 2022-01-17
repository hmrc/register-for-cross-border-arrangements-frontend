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

import base.SpecBase
import matchers.JsonMatchers
import models.BusinessType.{CorporateBody, Partnership}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import pages.BusinessTypePage
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, route, status, _}
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class BusinessNotIdentifiedControllerSpec extends SpecBase with NunjucksSupport with JsonMatchers {

  "Business not confirmed Controller" - {

    lazy val corporationTaxEnquiriesLink: String = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/corporation-tax-enquiries"
    lazy val selfAssessmentEnquiriesLink: String = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/self-assessment"

    "must return OK and the correct view for a GET when a Limited Company" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = emptyUserAnswers.set(BusinessTypePage, CorporateBody).success.value

      lazy val cantConfirmIdentityRoute = routes.BusinessNotIdentifiedController.onPageLoad().url

      val application    = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request        = FakeRequest(GET, cantConfirmIdentityRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      val expectedJson = Json.obj(
        "contactLink"  -> corporationTaxEnquiriesLink,
        "lostUtrLink"  -> "https://www.gov.uk/find-lost-utr-number",
        "tryAgainLink" -> "/register-for-cross-border-arrangements/register/have-utr"
      )

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual "businessNotIdentified.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must return OK and the correct view for a GET when a Partnership" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = emptyUserAnswers.set(BusinessTypePage, Partnership).success.value

      lazy val cantConfirmIdentityRoute = routes.BusinessNotIdentifiedController.onPageLoad().url

      val application    = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request        = FakeRequest(GET, cantConfirmIdentityRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      val expectedJson = Json.obj(
        "contactLink"  -> selfAssessmentEnquiriesLink,
        "lostUtrLink"  -> "https://www.gov.uk/find-lost-utr-number",
        "tryAgainLink" -> "/register-for-cross-border-arrangements/register/have-utr"
      )

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual "businessNotIdentified.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }
  }
}
