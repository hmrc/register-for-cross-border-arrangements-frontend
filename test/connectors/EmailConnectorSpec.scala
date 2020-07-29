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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.Generators
import helpers.WireMockServerHandler
import models.EmailRequest
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.OK
import play.api.http.Status.BAD_REQUEST
import play.api.http.Status.NOT_FOUND
import play.api.inject.guice.GuiceApplicationBuilder

class EmailConnectorSpec extends SpecBase
  with WireMockServerHandler
  with Generators
  with ScalaCheckPropertyChecks {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.email.port" -> server.port()
    )
    .build()

    lazy val connector: EmailConnector = app.injector.instanceOf[EmailConnector]

  "EmailConnector" - {
    "must return status as OK for valid email submission" in {

      forAll(arbitrary[EmailRequest]) {
        emailRequest =>
          stubResponse(s"/hmrc/email", OK)

          val result = connector.sendEmail(emailRequest)
          result.futureValue.status mustBe OK
      }
    }

    "must return status as BAD_REQUEST for invalid email submission" in {

      forAll(arbitrary[EmailRequest]) {
        emailRequest =>
          stubResponse(s"/hmrc/email", BAD_REQUEST)

          val result = connector.sendEmail(emailRequest)
          result.futureValue.status mustBe BAD_REQUEST
      }
    }

    "must return status as NOT_FOUND for invalid email submission" in {

      forAll(arbitrary[EmailRequest]) {
        emailRequest =>
          stubResponse(s"/hmrc/email", NOT_FOUND)

          val result = connector.sendEmail(emailRequest)
          result.futureValue.status mustBe NOT_FOUND
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