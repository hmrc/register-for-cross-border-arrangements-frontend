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

package controllers.auth

import base.SpecBase
import config.FrontendAppConfig
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.MustMatchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class AuthActionSpec extends SpecBase with MustMatchers with MockitoSugar with GuiceOneAppPerSuite with ScalaFutures {

  val mockAuthConnector = mock[AuthConnector]
  val mockFrontendAppConfig = mock[FrontendAppConfig]
  val mockDac6Enrolment = mock[Enrolment]

  implicit val executionContext: ExecutionContext = ExecutionContext.global

  lazy val appConfig = app.injector.instanceOf[FrontendAppConfig]
  lazy val mockMcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]


  def fakeDac6Enrolments = Enrolments(Set(mockDac6Enrolment))



  override implicit lazy val app: Application = GuiceApplicationBuilder()
    .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
    .build()

  object Harness {

    sealed class Harness(authAction: AuthAction, controllerComponents: MessagesControllerComponents = mockMcc)
      extends FrontendController(controllerComponents) {

      def onPageLoad() = authAction { request: AuthenticatedRequest[AnyContent] =>
        Ok(s"enrolments:${request.enrolments}")
      }
    }
    def failure(ex: Throwable): Harness =
      fromAction(new AuthActionImpl(new FakeFailingAuthConnector(ex), mockFrontendAppConfig, mockMcc))

    def fromAction(action: AuthAction): Harness =
      new Harness(action)

    def successful[A](a: A): Harness = {

      val mocked = mock[AuthConnector]
      when(mocked.authorise[A](any(), any())(any(), any())).thenReturn(Future.successful(a))
      fromAction(new AuthActionImpl(mocked, mockFrontendAppConfig, mockMcc))
    }
  }

  private implicit class HelperOps[A](a: A) {
    def ~[B](b: B) = new ~(a, b)
  }

  "AuthActionImpl must " - {
    "return logged in user" - {

      val baseRetrieval = Enrolments(Set.empty)

      "the user has enrolments and is authenticated" in {
        val controller = Harness.successful(baseRetrieval)
        val result = controller.onPageLoad()(fakeRequest)
        contentAsString(result) mustBe "enrolments:Set()"
      }
    }

    "redirect to the unauthorised page when" - {

      "the user doesn't have sufficient enrolments" in {
        val controller =  Harness.failure(new InsufficientEnrolments)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad().url)
      }

      "the user wasn't authenticated by GG" in {
        val controller =  Harness.failure(new UnsupportedAuthProvider)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad().url)
      }

      "the user has have confidence level less than 50" in {
        val controller =  Harness.failure(new InsufficientConfidenceLevel)
        val result = controller.onPageLoad()(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad().url)
      }
    }
  }

  class FakeFailingAuthConnector(exceptionToReturn: Throwable) extends AuthConnector {
    val serviceUrl: String = ""

    override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
      Future.failed(exceptionToReturn)
  }

}