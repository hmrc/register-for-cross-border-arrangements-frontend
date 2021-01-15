/*
 * Copyright 2021 HM Revenue & Customs
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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import helpers.WireMockServerHandler
import models.{BusinessMatchingSubmission, BusinessType, IndividualMatchingSubmission, Name, UniqueTaxpayerReference, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{BusinessTypePage, SelfAssessmentUTRPage, SoleTraderNamePage}
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Nino

import scala.concurrent.ExecutionContext.Implicits.global

class BusinessMatchingConnectorSpec extends SpecBase
  with WireMockServerHandler
  with Generators
  with ScalaCheckPropertyChecks {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.business-matching.port" -> server.port()
    )
    .build()

  lazy val connector: BusinessMatchingConnector = app.injector.instanceOf[BusinessMatchingConnector]

  "BusinessMatchingConnector" - {
    "when calling sendIndividualMatchingInformation" - {
      "must return status as OK for submission of valid Individual Matching Submission" in {


        forAll(arbitrary[Nino], arbitrary[IndividualMatchingSubmission]) {
          (nino, ims) =>
            stubResponse(s"/register-for-cross-border-arrangements/matching/individual/nino/$nino", OK)

            val result = connector.sendIndividualMatchingInformation(nino, ims)
            result.futureValue.status mustBe OK
        }
      }

      "must return status as BAD_REQUEST for submission of invalid arrival notification" in {


        forAll(arbitrary[Nino], arbitrary[IndividualMatchingSubmission]) {
          (nino, ims) =>
            stubResponse(s"/register-for-cross-border-arrangements/matching/individual/nino/$nino", BAD_REQUEST)

            val result = connector.sendIndividualMatchingInformation(nino, ims)
            result.futureValue.status mustBe BAD_REQUEST
        }
      }

      "must return status as FORBIDDEN for authorisation errors" in {

        forAll(arbitrary[Nino], arbitrary[IndividualMatchingSubmission]) {
          (nino, ims) =>
            stubResponse(s"/register-for-cross-border-arrangements/matching/individual/nino/$nino", FORBIDDEN)

            val result = connector.sendIndividualMatchingInformation(nino, ims)
            result.futureValue.status mustBe FORBIDDEN
        }
      }

      "must return status as INTERNAL_SERVER_ERROR for technical error incurred" in {


        forAll(arbitrary[Nino], arbitrary[IndividualMatchingSubmission]) {
          (nino, ims) =>
            stubResponse(s"/register-for-cross-border-arrangements/matching/individual/nino/$nino", INTERNAL_SERVER_ERROR)

            val result = connector.sendIndividualMatchingInformation(nino, ims)
            result.futureValue.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "when calling sendSoleProprietorMatchingInformation" - {

      val utr = UniqueTaxpayerReference("1234567890")

      val answers = UserAnswers(userAnswersId)
        .set(BusinessTypePage, BusinessType.NotSpecified)
        .success
        .value
        .set(SelfAssessmentUTRPage, utr)
        .success
        .value
        .set(SoleTraderNamePage, Name("FirstName", "LastName"))
        .success
        .value

      val bms = BusinessMatchingSubmission(answers).get

      "must return status as OK for submission of valid Sole Proprietor Matching Submission" in {

        stubResponse(s"/register-for-cross-border-arrangements/matching/individual/utr/${utr.uniqueTaxPayerReference}", OK)

        val result = connector.sendSoleProprietorMatchingInformation(utr, bms)
        result.futureValue.status mustBe OK
      }

      "must return status as BAD_REQUEST for submission of invalid utr" in {

        val invalidUTR = UniqueTaxpayerReference("12345678901")

        val answers = UserAnswers(userAnswersId)
          .set(BusinessTypePage, BusinessType.NotSpecified)
          .success
          .value
          .set(SelfAssessmentUTRPage, invalidUTR)
          .success
          .value
          .set(SoleTraderNamePage, Name("FirstName", "LastName"))
          .success
          .value

        val bms = BusinessMatchingSubmission(answers).get

        stubResponse(s"/register-for-cross-border-arrangements/matching/individual/utr/${invalidUTR.uniqueTaxPayerReference}", BAD_REQUEST)

        val result = connector.sendSoleProprietorMatchingInformation(invalidUTR, bms)
        result.futureValue.status mustBe BAD_REQUEST
      }

      "must return status as FORBIDDEN for authorisation errors" in {

        stubResponse(s"/register-for-cross-border-arrangements/matching/individual/utr/${utr.uniqueTaxPayerReference}", FORBIDDEN)

        val result = connector.sendSoleProprietorMatchingInformation(utr, bms)
        result.futureValue.status mustBe FORBIDDEN
      }

      "must return status as INTERNAL_SERVER_ERROR for technical error incurred" in {

        stubResponse(s"/register-for-cross-border-arrangements/matching/individual/utr/${utr.uniqueTaxPayerReference}", INTERNAL_SERVER_ERROR)

        val result = connector.sendSoleProprietorMatchingInformation(utr, bms)
        result.futureValue.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "when calling sendBusinessMatchingInformation" - {
      "must return status as OK for submission of valid Business Matching Submission" in {

        forAll(arbitraryBusinessMatchingSubmission.arbitrary) {
          bms =>
            val utr = UniqueTaxpayerReference("0123456789")
            stubResponse(s"/register-for-cross-border-arrangements/matching/organisation/utr/${utr.uniqueTaxPayerReference}", OK)

            val result = connector.sendBusinessMatchingInformation(utr, bms)
            result.futureValue.status mustBe OK
        }
      }

      "must return status as BAD_REQUEST for submission of invalid UTR" in {
        forAll(arbitraryBusinessMatchingSubmission.arbitrary) {
          bms =>
            val invalidUTR = UniqueTaxpayerReference("01234567891")
            stubResponse(s"/register-for-cross-border-arrangements/matching/organisation/utr/${invalidUTR.uniqueTaxPayerReference}", BAD_REQUEST)

            val result = connector.sendBusinessMatchingInformation(invalidUTR, bms)
            result.futureValue.status mustBe BAD_REQUEST
        }
      }

      "must return status as FORBIDDEN for authorisation errors" in {
        forAll(arbitraryBusinessMatchingSubmission.arbitrary) {
          bms =>
            val utr = UniqueTaxpayerReference("0123456789")
            stubResponse(s"/register-for-cross-border-arrangements/matching/organisation/utr/${utr.uniqueTaxPayerReference}", FORBIDDEN)

            val result = connector.sendBusinessMatchingInformation(utr, bms)
            result.futureValue.status mustBe FORBIDDEN
        }
      }

      "must return status as INTERNAL_SERVER_ERROR for technical error incurred" in {
        forAll(arbitraryBusinessMatchingSubmission.arbitrary) {
          bms =>
            val utr = UniqueTaxpayerReference("0123456789")
            stubResponse(s"/register-for-cross-border-arrangements/matching/organisation/utr/${utr.uniqueTaxPayerReference}", INTERNAL_SERVER_ERROR)

            val result = connector.sendBusinessMatchingInformation(utr, bms)
            result.futureValue.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  private def stubResponse(expectedUrl: String, expectedStatus: Int): StubMapping =
    server.stubFor(
      post(urlEqualTo(expectedUrl))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
        )
    )
}
