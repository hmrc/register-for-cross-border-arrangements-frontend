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
import models.{BusinessAddress, BusinessDetails, BusinessType, Name, NormalMode, PayloadRegistrationWithIDResponse, RegisterWithIDResponse, ResponseCommon, UniqueTaxpayerReference, UserAnswers}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import pages._
import play.api.inject._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.BusinessMatchingService
import uk.gov.hmrc.domain.Generator
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class BusinessMatchingControllerSpec extends SpecBase
  with MockitoSugar
  with NunjucksSupport
  with JsonMatchers
  with Generators {

  lazy val individualMatchingRoute: String = routes.BusinessMatchingController.matchIndividual(NormalMode).url
  lazy val businessMatchingRoute: String = routes.BusinessMatchingController.matchBusiness().url
  lazy val businessMatchNotFoundRoute: String = routes.BusinessNotConfirmedController.onPageLoad().url

  def getRequest(route: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, route)

  val mockBusinessMatchingService: BusinessMatchingService = mock[BusinessMatchingService]

  val businessUserAnswers: UserAnswers = UserAnswers(userAnswersId)
    .set(BusinessTypePage, BusinessType.UnIncorporatedBody)
    .success
    .value
    .set(CorporationTaxUTRPage, UniqueTaxpayerReference("0123456789"))
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
          .thenReturn(
            Future.successful(Right((Some(
              PayloadRegistrationWithIDResponse(
                RegisterWithIDResponse(
                  ResponseCommon("OK", None, "", None),
                  None
                )
              )
              ), Some("XE0000123456789"))
            ))
          )

        val result = route(application, getRequest(individualMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.IdentityConfirmedController.onPageLoad().url)
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
          .thenReturn(Future.successful(Right((None, None))))

        val result = route(application, getRequest(individualMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.IndividualNotConfirmedController.onPageLoad().url)
      }
    }

    "when a correct submission can be created and returns a business match" - {

      "must redirect the user to /confirm-business page if business is unincorporated or corporate" in {

        val application = applicationBuilder(userAnswers = Some(businessUserAnswers))
          .overrides(
            bind[BusinessMatchingService].toInstance(mockBusinessMatchingService)
          ).build()

        val businessDetails = BusinessDetails(
          name = "My Company",
          address = BusinessAddress("1 Address Street", None, None, None, "NE11 1BB", "GB"))

        val safeId = "XE0001234567890"

        when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
          .thenReturn(Future.successful((Some(businessDetails), Some(safeId))))

        val result = route(application, getRequest(businessMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/confirm-business")
      }

      "must redirect the user to /confirm-business page if business is not unincorporated or corporate" in {

        val businessUserAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(BusinessTypePage, BusinessType.Partnership)
          .success
          .value
          .set(SelfAssessmentUTRPage, UniqueTaxpayerReference("0123456789"))
          .success
          .value
          .set(BusinessNamePage, "Business Name")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(businessUserAnswers))
          .overrides(
            bind[BusinessMatchingService].toInstance(mockBusinessMatchingService)
          ).build()

        val businessDetails = BusinessDetails(
          name = "My Company",
          address = BusinessAddress("1 Address Street", None, None, None, "NE11 1BB", "GB"))

        val safeId = "XE0001234567890"

        when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
          .thenReturn(Future.successful((Some(businessDetails), Some(safeId))))

        val result = route(application, getRequest(businessMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/confirm-business")
      }
    }

    "when a correct submission can be created and returns no business match" - {

      "must redirect the user to the can't find business page" in {

        val application = applicationBuilder(userAnswers = Some(businessUserAnswers))
          .overrides(
            bind[BusinessMatchingService].toInstance(mockBusinessMatchingService)
          ).build()

        when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
          .thenReturn(Future.successful((None, None)))

        val result = route(application, getRequest(businessMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(businessMatchNotFoundRoute)
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
        redirectLocation(result) mustBe Some(businessMatchNotFoundRoute)
      }
    }

    "when a correct submission can't be created due to missing data required to business match" - {

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
          .thenReturn(Future.successful((None, None)))

        val result = route(application, getRequest(businessMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/have-utr")
      }
    }
  }

}
