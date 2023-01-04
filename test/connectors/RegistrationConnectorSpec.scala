/*
 * Copyright 2023 HM Revenue & Customs
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
import helpers.JsonFixtures.{badRequestResponse, requestCouldNotBeProcessedResponse, withIDResponse}
import helpers.WireMockServerHandler
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.business-matching.port" -> server.port()
    )
    .build()

  lazy val connector: RegistrationConnector = app.injector.instanceOf[RegistrationConnector]

  val withIDSubmissionUrl: String = "/register-for-cross-border-arrangements/registration/02.00.00/withId"

  "registrationConnector" - {
    "must return status as OK for registration" in {

      forAll(arbitrary[Register]) {
        reg =>
          stubResponse(s"/register-for-cross-border-arrangements/registration/02.00.00/noId", OK)

          val result = connector.sendWithoutIDInformation(reg)
          result.futureValue.status mustBe OK
      }
    }

    "must return status as BAD_REQUEST for  registration" in {

      forAll(arbitrary[Register]) {
        reg =>
          stubResponse("/register-for-cross-border-arrangements/registration/02.00.00/noId", BAD_REQUEST)

          val result = connector.sendWithoutIDInformation(reg)
          result.futureValue.status mustBe BAD_REQUEST
      }
    }

    "must return status as INTERNAL_SERVER_ERROR for a technical error" in {

      forAll(arbitrary[Register]) {
        reg =>
          stubResponse("/register-for-cross-border-arrangements/registration/02.00.00/noId", INTERNAL_SERVER_ERROR)

          val result = connector.sendWithoutIDInformation(reg)
          result.futureValue.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "registerWithID" - {
      "must return PayloadRegistrationWithIDResponse if status is OK" in {
        forAll(arbitrary[PayloadRegisterWithID]) {
          request =>
            val expected = PayloadRegistrationWithIDResponse(
              RegisterWithIDResponse(
                ResponseCommon("OK", Some("Sample status text"), "2016-08-16T15:55:30Z", Some(Vector(ReturnParameters("SAP_NUMBER", "0123456789")))),
                Some(
                  ResponseDetail(
                    "XE0000123456789",
                    Some("WARN8764123"),
                    isEditable = true,
                    isAnAgent = false,
                    None,
                    isAnIndividual = true,
                    IndividualResponse("Ron", Some("Madisson"), "Burgundy", Some("1980-12-12")),
                    AddressResponse("100 Parliament Street", None, None, Some("London"), Some("SW1A 2BQ"), "GB"),
                    ContactDetails(Some("1111111"), Some("2222222"), Some("1111111"), Some("test@test.org"))
                  )
                )
              )
            )

            stubResponse(withIDSubmissionUrl, OK, withIDResponse)

            val result = connector.registerWithID(request)
            result.futureValue mustBe Some(expected)
        }
      }

      "must return None if status is NOT_FOUND" in {
        forAll(arbitrary[PayloadRegisterWithID]) {
          request =>
            stubResponse(withIDSubmissionUrl, NOT_FOUND)

            val result = connector.registerWithID(request)
            result.futureValue mustBe None
        }
      }

      "must return None if status is not OK and ErrorDetail contains '001 - Request could not be processed' and '503'" in {
        forAll(arbitrary[PayloadRegisterWithID]) {
          request =>
            stubResponse(withIDSubmissionUrl, SERVICE_UNAVAILABLE, requestCouldNotBeProcessedResponse)

            val result = connector.registerWithID(request)
            result.futureValue mustBe None
        }
      }

      "must throw an exception if status is not OK" in {
        forAll(arbitrary[PayloadRegisterWithID]) {
          request =>
            stubResponse(withIDSubmissionUrl, BAD_REQUEST, badRequestResponse)

            val result = connector.registerWithID(request)

            assertThrows[Exception] {
              result.futureValue
            }
        }
      }

      "must throw an exception if status is not OK and parsing failed" in {
        forAll(arbitrary[PayloadRegisterWithID]) {
          request =>
            val invalidBody =
              """
                |{
                |  "errorDetail": {
                |    "timestamp" : "2017-02-14T12:58:44Z",
                |    "correlationId": "c181e730-2386-4359-8ee0-f911d6e5f3bc",
                |    "errorMessage": "Invalid ID",
                |    "source": "Back End",
                |    "sourceFaultDetail":{
                |      "detail":[
                |        "001 - Regime missing or invalid"
                |      ]
                |    }
                | }
                |}""".stripMargin

            stubResponse(withIDSubmissionUrl, BAD_REQUEST, invalidBody)

            val result = connector.registerWithID(request)

            assertThrows[Exception] {
              result.futureValue
            }
        }
      }
    }
  }

  private def stubResponse(expectedUrl: String, expectedStatus: Int, expectedBody: String = ""): StubMapping =
    server.stubFor(
      post(urlEqualTo(expectedUrl))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )
}
