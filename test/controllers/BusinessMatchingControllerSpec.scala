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

import java.time.LocalDate

import base.SpecBase
import generators.Generators
import matchers.JsonMatchers
import models.{BusinessAddress, BusinessType, Name, UniqueTaxpayerReference, UserAnswers}
import models.{BusinessAddress, BusinessType, Name, UniqueTaxpayerReference, UserAnswers}
import models.{BusinessType, Name, UniqueTaxpayerReference, UserAnswers}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import pages._
import pages._
import pages.{BusinessNamePage, BusinessTypePage, DateOfBirthPage, NamePage, NinoPage, UniqueTaxpayerReferencePage}
import play.api.inject._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.BusinessMatchingService
import uk.gov.hmrc.domain.Generator
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class BusinessMatchingControllerSpec extends SpecBase
  with MockitoSugar
  with NunjucksSupport
  with JsonMatchers
  with Generators {

  lazy val individualMatchingRoute: String = routes.BusinessMatchingController.matchIndividual().url
  lazy val businessMatchingRoute: String = routes.BusinessMatchingController.matchBusiness().url

  def getRequest(route: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, route)

  val mockBusinessMatchingService: BusinessMatchingService = mock[BusinessMatchingService]

  val businessUserAnswers: UserAnswers = UserAnswers(userAnswersId)
    .set(BusinessTypePage, BusinessType.CorporateBody)
    .success
    .value
    .set(UniqueTaxpayerReferencePage, UniqueTaxpayerReference("0123456789"))
    .success
    .value
    .set(BusinessNamePage, "Business Name")
    .success
    .value


  "BusinessMatching Controller" - {
    "when a correct submission can be created and returns an individual match" - {

      "must redirect the user to the check your answers page" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(DateOfBirthPage, LocalDate.now())
          .success
          .value
          .set(NamePage, Name("", ""))
          .success
          .value
          .set(NinoPage, (new Generator()).nextNino)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[BusinessMatchingService].toInstance(mockBusinessMatchingService)
          ).build()

        when(mockBusinessMatchingService.sendIndividualMatchingInformation(any())(any(), any()))
          .thenReturn(Future.successful(Some(HttpResponse(OK, None))))

        val result = route(application, getRequest(individualMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements-frontend/check-your-answers")
      }
    }

    "when a correct submission can be created and returns no individual match" - {

      "must redirect the user to the cant find identity page" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(DateOfBirthPage, LocalDate.now())
          .success
          .value
          .set(NamePage, Name("", ""))
          .success
          .value
          .set(NinoPage, (new Generator()).nextNino)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[BusinessMatchingService].toInstance(mockBusinessMatchingService)
          ).build()

        when(mockBusinessMatchingService.sendIndividualMatchingInformation(any())(any(), any()))
          .thenReturn(Future.successful(Some(HttpResponse(NOT_FOUND, None))))

        val result = route(application, getRequest(individualMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements-frontend/register/individual-identity-not-confirmed")
      }
    }

    "when a correct submission can be created and returns a business match" - {

      "must redirect the user to /is-this-your-business page" in {

        val application = applicationBuilder(userAnswers = Some(businessUserAnswers))
          .overrides(
            bind[BusinessMatchingService].toInstance(mockBusinessMatchingService)
          ).build()

        val businessAddress = BusinessAddress("1 Address Street", None, None, None, "NE11 1BB", "GB")

        when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
          .thenReturn(Future.successful(Some(businessAddress)))

        val result = route(application, getRequest(businessMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        //TODO Uncomment below once /is-this-your-business is ready
//        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements-frontend/is-this-your-business")
      }
    }

    "when a correct submission can be created and returns no business match" - {

      "must redirect the user to the can't find business page" in {

        val application = applicationBuilder(userAnswers = Some(businessUserAnswers))
          .overrides(
            bind[BusinessMatchingService].toInstance(mockBusinessMatchingService)
          ).build()

        when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val result = route(application, getRequest(businessMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements-frontend/register/could-not-confirm-identity")
      }
    }

    "when a correct submission can be created and returns a business match" - {

      "must redirect to the error page if validation fails" in {

        val application = applicationBuilder(userAnswers = Some(businessUserAnswers))
          .overrides(
            bind[BusinessMatchingService].toInstance(mockBusinessMatchingService)
          ).build()

        when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
          .thenReturn(Future.failed(new Exception))

        val result = route(application, getRequest(businessMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        //TODO Redirect to error page when it's ready
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements-frontend/register/could-not-confirm-identity")
      }
    }

    "when a correct submission can't be created due to missing data required to business match" - {

      "must redirect the user to the business type page if it's missing" in {

        val businessUserAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(UniqueTaxpayerReferencePage, UniqueTaxpayerReference("0123456789"))
          .success
          .value
          .set(BusinessNamePage, "Business Name")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(businessUserAnswers))
          .overrides(
            bind[BusinessMatchingService].toInstance(mockBusinessMatchingService)
          ).build()

        when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val result = route(application, getRequest(businessMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements-frontend/register/business/with-id/type")
      }

      "must redirect the user to the utr page if it's missing" in {

        val businessUserAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(BusinessTypePage, BusinessType.CorporateBody)
          .success
          .value
          .set(BusinessNamePage, "Business Name")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(businessUserAnswers))
          .overrides(
            bind[BusinessMatchingService].toInstance(mockBusinessMatchingService)
          ).build()

        when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val result = route(application, getRequest(businessMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements-frontend/register/business/with-id/utr")
      }

      "must redirect the user to the business name page if it's missing" in {

        val businessUserAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(BusinessTypePage, BusinessType.CorporateBody)
          .success
          .value
          .set(UniqueTaxpayerReferencePage, UniqueTaxpayerReference("0123456789"))
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(businessUserAnswers))
          .overrides(
            bind[BusinessMatchingService].toInstance(mockBusinessMatchingService)
          ).build()

        when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val result = route(application, getRequest(businessMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements-frontend/register/could-not-confirm-identity")
      }
    }
  }

}
