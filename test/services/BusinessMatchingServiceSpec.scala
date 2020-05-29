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

package services

import java.time.LocalDate

import base.SpecBase
import connectors.BusinessMatchingConnector
import generators.Generators
import models.{BusinessAddress, BusinessType, Name, UniqueTaxpayerReference, UserAnswers}
import org.mockito.Matchers._
import org.mockito.Mockito.{reset, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.Application
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessMatchingServiceSpec extends SpecBase
  with MockitoSugar
  with Generators
  with ScalaCheckPropertyChecks {

  val mockBusinessMatchingConnector: BusinessMatchingConnector = mock[BusinessMatchingConnector]

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[BusinessMatchingConnector].toInstance(mockBusinessMatchingConnector)
    )
    .build()

  val businessMatchingService: BusinessMatchingService = app.injector.instanceOf[BusinessMatchingService]

  override def beforeEach: Unit =
    reset(
      mockBusinessMatchingConnector
    )

  "Business Matching Service" - {
    "when able to construct an individual matching submission" - {
      "should send a request to the business matching connector" in {
        forAll(arbitrary[UserAnswers], arbitrary[Name], arbitrary[LocalDate], arbitrary[Nino]){
          (userAnswers, name, dob, nino) =>
            val answers = userAnswers
              .set(NamePage, name)
              .success
              .value
              .set(DateOfBirthPage, dob)
              .success
              .value
              .set(NinoPage, nino)
              .success
              .value

            when(mockBusinessMatchingConnector.sendIndividualMatchingInformation(any(), any())(any(), any()))
              .thenReturn(
                Future.successful(HttpResponse(OK, None))
              )
            val result = businessMatchingService.sendIndividualMatchingInformation(answers)

            whenReady(result){
              _.map(_.status) mustBe Some(OK)
            }
        }
      }
    }

    "when unable to construct an individual matching submission" - {
      "should return a future with no value" in {
        forAll(arbitrary[UserAnswers]){
          userAnswers =>
            val answers = userAnswers
                .remove(NamePage)
                .success
                .value
                .remove(DateOfBirthPage)
                .success
                .value
            val result = businessMatchingService.sendIndividualMatchingInformation(answers)

            whenReady(result){
              _ mustBe None
            }
        }
      }
    }

    "when able to construct a business/organisation matching submission" - {
      "must return the validated business name" in {
        forAll(arbitrary[UserAnswers], arbitrary[BusinessType], arbitrary[UniqueTaxpayerReference], arbitrary[String]){
          (userAnswers, businessType, utr, businessName) =>
            val answers = userAnswers
              .set(BusinessTypePage, businessType)
              .success
              .value
              .set(UniqueTaxpayerReferencePage, utr)
              .success
              .value
              .set(BusinessNamePage, businessName)
              .success
              .value

            val responseJson: JsValue = Json.parse("""
              {
                "anotherKey" : "DAC6",
                "address" : {
                  "addressLine1" : "1 TestStreet",
                  "addressLine2" : "Test",
                  "postalCode" : "AA11BB",
                  "countryCode" : "GB"
                }
              }
              """)

            val businessAddress = BusinessAddress("1 TestStreet", Some("Test"), None, None, "AA11BB", "GB")

            when(mockBusinessMatchingConnector.sendBusinessMatchingInformation(any(), any())(any(), any()))
              .thenReturn(
                Future.successful(HttpResponse(OK, Some(responseJson)))
              )
            val result = businessMatchingService.sendBusinessMatchingInformation(answers)

            whenReady(result){ result =>
              result mustBe Some(businessAddress)
            }
        }
      }

      "must throw an error if Json validation fails" in {
        forAll(arbitrary[UserAnswers], arbitrary[BusinessType], arbitrary[UniqueTaxpayerReference], arbitrary[String]){
          (userAnswers, businessType, utr, businessName) =>
            val answers = userAnswers
              .set(BusinessTypePage, businessType)
              .success
              .value
              .set(UniqueTaxpayerReferencePage, utr)
              .success
              .value
              .set(BusinessNamePage, businessName)
              .success
              .value

            val invalidJson: JsValue = Json.parse("""
              {
                "anotherKey" : "DAC6",
                "address" : {
                  "addressLine1" : "1 TestStreet",
                  "addressLine2" : "Test",
                  "postalCode" : "AA11BB"
                }
              }
              """)

            when(mockBusinessMatchingConnector.sendBusinessMatchingInformation(any(), any())(any(), any()))
              .thenReturn(Future.successful(HttpResponse(OK, Some(invalidJson))))

            val result = businessMatchingService.sendBusinessMatchingInformation(answers)

            an[Exception] mustBe thrownBy(await(result))
        }
      }

      "should return a future None if business can't be found" in {
        forAll(arbitrary[UserAnswers], arbitrary[BusinessType], arbitrary[UniqueTaxpayerReference], arbitrary[String]){
          (userAnswers, businessType, utr, businessName) =>
            val answers = userAnswers
              .set(BusinessTypePage, businessType)
              .success
              .value
              .set(UniqueTaxpayerReferencePage, utr)
              .success
              .value
              .set(BusinessNamePage, businessName)
              .success
              .value

            when(mockBusinessMatchingConnector.sendBusinessMatchingInformation(any(), any())(any(), any()))
              .thenReturn(Future.successful(HttpResponse(NOT_FOUND, None)))

            val result = businessMatchingService.sendBusinessMatchingInformation(answers)

            whenReady(result){ result =>
              result mustBe None
            }
        }
      }
    }

  }
}
