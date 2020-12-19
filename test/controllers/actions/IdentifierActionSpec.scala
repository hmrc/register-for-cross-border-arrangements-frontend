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

package controllers.actions

import com.google.inject.Inject
import controllers.routes
import models.requests.UserRequest
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, FreeSpec, MustMatchers}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.SEE_OTHER
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Action, AnyContent, InjectedController}
import play.api.test.FakeRequest
import play.api.test.Helpers.{redirectLocation, status, _}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.~
import utils.RetrievalOps._

import scala.concurrent.Future

class Harness @Inject()(authAction: IdentifierAction) extends InjectedController {
  def onPageLoad(): Action[AnyContent] = authAction { request: UserRequest[AnyContent] =>
    Ok
  }
}

class IdentifierActionSpec extends FreeSpec with MustMatchers with MockitoSugar with GuiceOneAppPerSuite with BeforeAndAfterEach {
  val mockAuthConnector = mock[AuthConnector]

  override def beforeEach: Unit =
    reset(
      mockAuthConnector
    )

  override implicit lazy val app: Application = GuiceApplicationBuilder()
  .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
  .configure(Map("metrics.enabled" -> false))
  .build()

  type AuthRetrievals = Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole]
  val emptyEnrolments = Enrolments(Set.empty)


  "An Agent" - {
    "must be taken to the unauthorised controller" in {
      val retrieval : AuthRetrievals = None ~ emptyEnrolments ~ Some(Agent) ~ None
      when(mockAuthConnector.authorise[AuthRetrievals](any(), any())(any(), any())) thenReturn Future.successful(retrieval)

      val harness = app.injector.instanceOf[Harness]
      val result = harness.onPageLoad()(FakeRequest("GET", "/"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
    }
  }

  "An Assistant" - {
    "must be taken to the unauthorised controller" in {
      val retrieval : AuthRetrievals = None ~ emptyEnrolments ~ None ~ Some(Assistant)
      when(mockAuthConnector.authorise[AuthRetrievals](any(), any())(any(), any())) thenReturn Future.successful(retrieval)

      val harness = app.injector.instanceOf[Harness]
      val result = harness.onPageLoad()(FakeRequest("GET", "/"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
    }
  }

  "A user role for an organisation" - {
    "must be allowed through the refiner" in {
      val retrieval : AuthRetrievals = Some("internalID") ~ emptyEnrolments ~ Some(Organisation) ~ Some(User)
      when(mockAuthConnector.authorise[AuthRetrievals](any(), any())(any(), any())) thenReturn Future.successful(retrieval)

      val harness = app.injector.instanceOf[Harness]
      val result = harness.onPageLoad()(FakeRequest("GET", "/"))
      status(result) mustBe OK
    }
  }

}
