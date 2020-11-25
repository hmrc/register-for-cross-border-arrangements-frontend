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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, put, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import helpers.WireMockServerHandler
import models.{CreateSubscriptionForDACResponse, Name, RegistrationType, ResponseCommon, ResponseDetailForDACSubscription, SubscriptionForDACResponse, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, SERVICE_UNAVAILABLE}
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionConnectorSpec extends SpecBase
  with WireMockServerHandler
  with Generators
  with ScalaCheckPropertyChecks {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.business-matching.port" -> server.port()
    )
    .build()

  lazy val connector: SubscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

  "SubscriptionConnector" - {
    "must return status as OK for submission of valid enrolment request" in {


      forAll(arbitrary[UserAnswers], validSafeID, validSubsrciptionID) {
        (userAnswers, safeId, subscriptionId )=>
          val userAnswerWithSafeId = userAnswers.set(SafeIDPage, safeId).success.value
          val userAnswerWithSubscriptionId = userAnswerWithSafeId.set(SubscriptionIDPage, subscriptionId).success.value
          stubResponse(s"/register-for-cross-border-arrangements/enrolment/create-enrolment", OK)

          val result = connector.createEnrolment(userAnswerWithSubscriptionId)
          result.futureValue.status mustBe OK
      }
    }

    "must return status as BAD_REQUEST for invalid request" in {


      forAll(arbitrary[UserAnswers], validSafeID, validSubsrciptionID) {
        (userAnswers, safeId, subscriptionId )=>
          val userAnswerWithSafeId = userAnswers.set(SafeIDPage, safeId).success.value
          val userAnswerWithSubscriptionId = userAnswerWithSafeId.set(SubscriptionIDPage, subscriptionId).success.value
          stubResponse(s"/register-for-cross-border-arrangements/enrolment/create-enrolment", BAD_REQUEST)

          val result = connector.createEnrolment(userAnswerWithSubscriptionId)
          result.futureValue.status mustBe BAD_REQUEST
      }
    }

    "must return status as INTERNAL_SERVER_ERROR for technical error incurred" in {


      forAll(arbitrary[UserAnswers], validSafeID, validSubsrciptionID) {
        (userAnswers, safeId, subscriptionId )=>
          val userAnswerWithSafeId = userAnswers.set(SafeIDPage, safeId).success.value
          val userAnswerWithSubscriptionId = userAnswerWithSafeId.set(SubscriptionIDPage, subscriptionId).success.value
          stubResponse(s"/register-for-cross-border-arrangements/enrolment/create-enrolment", INTERNAL_SERVER_ERROR)

          val result = connector.createEnrolment(userAnswerWithSubscriptionId)
          result.futureValue.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "when calling createEISSubscription" - {

      "must return status OK for submission of valid registration details" in {

        forAll(arbitrary[UserAnswers], validPersonalName, validEmailAddress, validSafeID) {
          (userAnswers, name, email, safeID) =>
            val updatedUserAnswers = userAnswers.set(ContactNamePage, Name(name, name)).success.value
              .set(ContactEmailAddressPage, email).success.value
              .set(HaveSecondContactPage, false).success.value
              .set(SafeIDPage, safeID).success.value
              .remove(RegistrationTypePage).success.value

            val response = CreateSubscriptionForDACResponse(
              SubscriptionForDACResponse(
                responseCommon = ResponseCommon("OK", None, "2020-09-23T16:12:11Z", None),
                responseDetail = ResponseDetailForDACSubscription("XADAC0000123456"))
            )

            val expectedBody =
              """
                |{
                | "createSubscriptionForDACResponse": {
                |   "responseCommon": {
                |     "status": "OK",
                |     "processingDate": "2020-09-23T16:12:11Z"
                |   },
                |   "responseDetail": {
                |      "subscriptionID": "XADAC0000123456"
                |   }
                | }
                |}""".stripMargin

            stubPostResponse("/register-for-cross-border-arrangements/subscription/create-dac-subscription", OK, expectedBody)

            val result = connector.createSubscription(updatedUserAnswers)
            result.futureValue mustBe response
        }
      }

      "must throw an exception if unable to create a CreateSubscriptionForDACRequest object e.g. missing secondary name" in {

        forAll(arbitrary[UserAnswers], validPersonalName, validEmailAddress, validSafeID) {
          (userAnswers, name, email, safeID) =>
            val updatedUserAnswers = userAnswers.set(ContactNamePage, Name(name, name)).success.value
              .set(ContactEmailAddressPage, email).success.value
              .set(HaveSecondContactPage, true).success.value
              .set(SafeIDPage, safeID).success.value
              .remove(RegistrationTypePage).success.value

            stubPostResponse("/register-for-cross-border-arrangements/subscription/create-dac-subscription", OK, "")

            val result = connector.createSubscription(updatedUserAnswers)
            an[Exception] mustBe thrownBy(result.futureValue)
        }
      }

      "must throw an exception if status is not OK and subscription fails" in {

        forAll(arbitrary[UserAnswers], validPersonalName, validEmailAddress, validSafeID) {
          (userAnswers, name, email, safeID) =>
            val updatedUserAnswers = userAnswers.set(RegistrationTypePage, RegistrationType.Individual).success.value
              .set(NamePage, Name(name, name)).success.value
              .set(ContactEmailAddressPage, email).success.value
              .set(SafeIDPage, safeID).success.value
              .remove(HaveSecondContactPage).success.value

            stubPostResponse("/register-for-cross-border-arrangements/subscription/create-dac-subscription", SERVICE_UNAVAILABLE, "")

            val result = connector.createSubscription(updatedUserAnswers)
            an[Exception] mustBe thrownBy(result.futureValue)
        }
      }
    }
  }

  private def stubResponse(expectedUrl: String, expectedStatus: Int): StubMapping =
    server.stubFor(
      put(urlEqualTo(expectedUrl))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
        )
    )

  private def stubPostResponse(expectedUrl: String, expectedStatus: Int, expectedBody: String): StubMapping =
    server.stubFor(
      post(urlEqualTo(expectedUrl))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )
}
