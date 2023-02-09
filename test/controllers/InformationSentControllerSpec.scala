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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import forms.InformationSentFormProvider
import matchers.JsonMatchers.containJson
import models.NormalMode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository

import scala.concurrent.Future

class InformationSentControllerSpec extends SpecBase {

  def onwardRoute: Call = Call("GET", "/foo")

  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val mockFrontendAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  val formProvider       = new InformationSentFormProvider()
  val form: Form[String] = formProvider()

  lazy val informationSentPageRoute: String = routes.InformationSentController.onPageLoad().url

  "InformationSent Controller" - {

    "return OK and the correct view for a GET without requiring data" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application    = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request        = FakeRequest(GET, informationSentPageRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "informationSent.njk"

      application.stop()
    }
  }
}
