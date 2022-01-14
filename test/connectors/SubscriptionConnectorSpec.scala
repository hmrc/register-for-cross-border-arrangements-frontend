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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, put, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import helpers.WireMockServerHandler
import models.error.RegisterError
import models.error.RegisterError.{SomeInformationIsMissingError, UnableToCreateEMTPSubscriptionError}
import models.readSubscription._
import models.{Name, RegistrationType, ResponseCommon, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.business-matching.port" -> server.port(),
      "microservice.services.cross-border-arrangements.port" -> server.port()
    )
    .build()

  lazy val connector: SubscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

  val primaryContact: PrimaryContact = PrimaryContact(
    Seq(
      ContactInformationForIndividual(
        individual = IndividualDetails(firstName = "FirstName", lastName = "LastName", middleName = None),
        email = "email@email.com",
        phone = Some("07111222333"),
        mobile = Some("07111222333")
      )
    )
  )

  val secondaryContact: SecondaryContact = SecondaryContact(
    Seq(
      ContactInformationForOrganisation(organisation = OrganisationDetails(organisationName = "Organisation Name"),
                                        email = "email@email.com",
                                        phone = None,
                                        mobile = None
      )
    )
  )

  val responseDetail: ResponseDetailForReadSubscription = ResponseDetailForReadSubscription(subscriptionID = "XE0001234567890",
                                                                                            tradingName = Some("Trading Name"),
                                                                                            isGBUser = true,
                                                                                            primaryContact = primaryContact,
                                                                                            secondaryContact = Some(secondaryContact)
  )

  val responseCommon: ResponseCommon = ResponseCommon(status = "OK", statusText = None, processingDate = "2020-08-09T11:23:45Z", returnParameters = None)

  "SubscriptionConnector" - {
    "must return status as OK for submission of valid enrolment request" in {

      forAll(arbitrary[UserAnswers], validSafeID, validSubscriptionID) {
        (userAnswers, safeId, subscriptionId) =>
          val userAnswerWithSafeId         = userAnswers.set(SafeIDPage, safeId).success.value
          val userAnswerWithSubscriptionId = userAnswerWithSafeId.set(SubscriptionIDPage, subscriptionId).success.value
          stubResponse(s"/register-for-cross-border-arrangements/enrolment/create-enrolment", OK)

          val result = connector.createEnrolment(userAnswerWithSubscriptionId)
          result.futureValue.status mustBe OK
      }
    }

    "must return status as BAD_REQUEST for invalid request" in {

      forAll(arbitrary[UserAnswers], validSafeID, validSubscriptionID) {
        (userAnswers, safeId, subscriptionId) =>
          val userAnswerWithSafeId         = userAnswers.set(SafeIDPage, safeId).success.value
          val userAnswerWithSubscriptionId = userAnswerWithSafeId.set(SubscriptionIDPage, subscriptionId).success.value
          stubResponse(s"/register-for-cross-border-arrangements/enrolment/create-enrolment", BAD_REQUEST)

          val result = connector.createEnrolment(userAnswerWithSubscriptionId)
          result.futureValue.status mustBe BAD_REQUEST
      }
    }

    "must return status as INTERNAL_SERVER_ERROR for technical error incurred" in {

      forAll(arbitrary[UserAnswers], validSafeID, validSubscriptionID) {
        (userAnswers, safeId, subscriptionId) =>
          val userAnswerWithSafeId         = userAnswers.set(SafeIDPage, safeId).success.value
          val userAnswerWithSubscriptionId = userAnswerWithSafeId.set(SubscriptionIDPage, subscriptionId).success.value
          stubResponse(s"/register-for-cross-border-arrangements/enrolment/create-enrolment", INTERNAL_SERVER_ERROR)

          val result = connector.createEnrolment(userAnswerWithSubscriptionId)
          result.futureValue.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "when calling createEISSubscription" - {

      "must return status OK for submission of valid registration details" in {

        forAll(validPersonalName, validEmailAddress, validSafeID, validSubscriptionID) {
          (name, email, safeID, subscriptionID) =>
            val updatedUserAnswers = UserAnswers("internalId")
              .remove(RegistrationTypePage)
              .success
              .value
              .set(ContactNamePage, name)
              .success
              .value
              .set(ContactEmailAddressPage, email)
              .success
              .value
              .set(TelephoneNumberQuestionPage, false)
              .success
              .value
              .set(HaveSecondContactPage, false)
              .success
              .value
              .set(SafeIDPage, safeID)
              .success
              .value

            def expectedBody(subscriptionID: String): String =
              s"""
                |{
                | "createSubscriptionForDACResponse": {
                |   "responseCommon": {
                |     "status": "OK",
                |     "processingDate": "2020-09-23T16:12:11Z"
                |   },
                |   "responseDetail": {
                |      "subscriptionID": "$subscriptionID"
                |   }
                | }
                |}""".stripMargin

            stubPostResponse("/register-for-cross-border-arrangements/subscription/create-dac-subscription", OK, expectedBody(subscriptionID))

            val result = connector.createSubscription(updatedUserAnswers)
            result.futureValue mustBe Right(subscriptionID)
        }
      }

      "must return status OK for caching a submission of valid registration details" in {

        forAll(validPersonalName, validEmailAddress, validSafeID, validSubscriptionID) {
          (name, email, safeID, subscriptionID) =>
            val updatedUserAnswers = UserAnswers("internalId")
              .remove(RegistrationTypePage)
              .success
              .value
              .set(ContactNamePage, name)
              .success
              .value
              .set(ContactEmailAddressPage, email)
              .success
              .value
              .set(TelephoneNumberQuestionPage, false)
              .success
              .value
              .set(HaveSecondContactPage, false)
              .success
              .value
              .set(SafeIDPage, safeID)
              .success
              .value

            def expectedBody(subscriptionID: String): String =
              s"""
                |{
                | "createSubscriptionForDACResponse": {
                |   "responseCommon": {
                |     "status": "OK",
                |     "processingDate": "2020-09-23T16:12:11Z"
                |   },
                |   "responseDetail": {
                |      "subscriptionID": "$subscriptionID"
                |   }
                | }
                |}""".stripMargin

            stubPostResponse("/disclose-cross-border-arrangements/subscription/cache-subscription", OK, expectedBody(subscriptionID))

            val result = connector.cacheSubscription(updatedUserAnswers, subscriptionID)
            result.futureValue.status mustBe OK
        }
      }

      "must return an error if unable to create a CreateSubscriptionForDACRequest object e.g. missing secondary name" in {

        forAll(arbitrary[UserAnswers], validPersonalName, validEmailAddress, validSafeID) {
          (userAnswers, name, email, safeID) =>
            val updatedUserAnswers = userAnswers
              .set(ContactNamePage, name)
              .success
              .value
              .remove(RegistrationTypePage)
              .success
              .value
              .set(ContactEmailAddressPage, email)
              .success
              .value
              .set(TelephoneNumberQuestionPage, false)
              .success
              .value
              .set(HaveSecondContactPage, true)
              .success
              .value
              .set(SafeIDPage, safeID)
              .success
              .value

            stubPostResponse("/register-for-cross-border-arrangements/subscription/create-dac-subscription", OK, "")

            val result: Future[Either[RegisterError, String]] = connector.createSubscription(updatedUserAnswers)
            result.futureValue mustBe (Left(SomeInformationIsMissingError))
        }
      }

      "must return an error if status is not OK and subscription fails" in {

        forAll(validPersonalName, validEmailAddress, validSafeID, validSubscriptionID) {
          (name, email, safeID, subscriptionID) =>
            val updatedUserAnswers = UserAnswers("internalId")
              .remove(RegistrationTypePage)
              .success
              .value
              .set(ContactNamePage, name)
              .success
              .value
              .set(ContactEmailAddressPage, email)
              .success
              .value
              .set(TelephoneNumberQuestionPage, false)
              .success
              .value
              .set(HaveSecondContactPage, false)
              .success
              .value
              .set(SafeIDPage, safeID)
              .success
              .value

            stubPostResponse("/register-for-cross-border-arrangements/subscription/create-dac-subscription", SERVICE_UNAVAILABLE, "")

            val result = connector.createSubscription(updatedUserAnswers)
            result.futureValue mustBe (Left(UnableToCreateEMTPSubscriptionError))
        }
      }

      "must return the correct DisplaySubscriptionForDACResponse" in {
        forAll(validSafeID) {
          safeID =>
            val expectedBody = displaySubscriptionPayload(
              JsString(safeID),
              JsString("FirstName"),
              JsString("LastName"),
              JsString("Organisation Name"),
              JsString("email@email.com"),
              JsString("email@email.com"),
              JsString("07111222333")
            )

            val responseDetailRead: ResponseDetailForReadSubscription = responseDetail.copy(subscriptionID = safeID)

            val displaySubscriptionForDACResponse: DisplaySubscriptionForDACResponse =
              DisplaySubscriptionForDACResponse(
                ReadSubscriptionForDACResponse(responseCommon = responseCommon, responseDetail = responseDetailRead)
              )

            stubPostResponse("/disclose-cross-border-arrangements/subscription/display-subscription", OK, expectedBody)

            val result = connector.readSubscriptionDetails(safeID)
            result.futureValue mustBe Some(displaySubscriptionForDACResponse)
        }
      }

      "must None if json returned is invalid" in {
        forAll(validSafeID) {
          safeID =>
            val invalidJson = s"""
                                 |{
                                 |}""".stripMargin
            stubPostResponse("/disclose-cross-border-arrangements/subscription/display-subscription", OK, invalidJson)

            val result = connector.readSubscriptionDetails(safeID)
            result.futureValue mustBe None
        }
      }

      "must None if subscription doesnt exist DisplaySubscriptionForDACResponse" in {
        forAll(validSafeID) {
          safeID =>
            stubPostResponse("/disclose-cross-border-arrangements/subscription/display-subscription", NOT_FOUND, "")

            val result = connector.readSubscriptionDetails(safeID)
            result.futureValue mustBe None
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

  private def displaySubscriptionPayload(subscriptionID: JsString,
                                         firstName: JsString,
                                         lastName: JsString,
                                         organisationName: JsString,
                                         primaryEmail: JsString,
                                         secondaryEmail: JsString,
                                         phone: JsString
  ): String =
    s"""
       |{
       |  "displaySubscriptionForDACResponse": {
       |    "responseCommon": {
       |      "status": "OK",
       |      "processingDate": "2020-08-09T11:23:45Z"
       |    },
       |    "responseDetail": {
       |      "subscriptionID": $subscriptionID,
       |      "tradingName": "Trading Name",
       |      "isGBUser": true,
       |      "primaryContact": [
       |        {
       |          "email": $primaryEmail,
       |          "phone": $phone,
       |          "mobile": $phone,
       |          "individual": {
       |            "lastName": $lastName,
       |            "firstName": $firstName
       |          }
       |        }
       |      ],
       |      "secondaryContact": [
       |        {
       |          "email": $secondaryEmail,
       |          "organisation": {
       |            "organisationName": $organisationName
       |          }
       |        }
       |      ]
       |    }
       |  }
       |}""".stripMargin
}
