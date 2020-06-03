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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, put, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import helpers.WireMockServerHandler
import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Nino
import scala.concurrent.ExecutionContext.Implicits.global

class TaxEnrolmentsConnectorSpec extends SpecBase
  with WireMockServerHandler
  with Generators
  with ScalaCheckPropertyChecks {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.business-matching.port" -> server.port()
    )
    .build()

  lazy val connector: TaxEnrolmentsConnector = app.injector.instanceOf[TaxEnrolmentsConnector]

  "BusinessMatchingConnector" - {
    "must return status as OK for submission of valid enrolment request" in {


      forAll(arbitrary[UserAnswers]) {
        (userAnswers) =>
          stubResponse(s"/register-for-cross-border-arrangements/enrolment/create-enrolment", OK)

          val result = connector.createEnrolment(userAnswers)
          result.futureValue.status mustBe OK
      }
    }

    "must return status as BAD_REQUEST for invalid request" in {


      forAll(arbitrary[UserAnswers]) {
        (userAnswers) =>
          stubResponse(s"/register-for-cross-border-arrangements/enrolment/create-enrolment", BAD_REQUEST)

          val result = connector.createEnrolment(userAnswers)
          result.futureValue.status mustBe BAD_REQUEST
      }
    }

    "must return status as INTERNAL_SERVER_ERROR for technical error incurred" in {


      forAll(arbitrary[UserAnswers]) {
        (userAnswers) =>
          stubResponse(s"/register-for-cross-border-arrangements/enrolment/create-enrolment", INTERNAL_SERVER_ERROR)

          val result = connector.createEnrolment(userAnswers)
          result.futureValue.status mustBe INTERNAL_SERVER_ERROR
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
}
