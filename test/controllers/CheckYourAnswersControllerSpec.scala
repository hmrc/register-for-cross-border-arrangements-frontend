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

package controllers

import base.SpecBase
import models.{Address, BusinessType, Country, Name, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.{BusinessAddressPage, BusinessTypePage, ConfirmBusinessPage, ContactEmailAddressPage, ContactNamePage, NinoPage, RetrievedNamePage, TelephoneNumberQuestionPage}
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase {

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET - Business with ID" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val address = Address("", "", None, None, None, Country("", "", ""))

      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(BusinessTypePage, BusinessType.Partnership).success.value
        .set(BusinessAddressPage, address)
        .success.value
        .set(RetrievedNamePage, "My Business")
        .success.value
        .set(ConfirmBusinessPage, true)
        .success.value
        .set(ContactNamePage, Name("Contact", "Name"))
        .success.value
        .set(ContactEmailAddressPage, "email@email.com")
        .success.value
        .set(TelephoneNumberQuestionPage, false)
        .success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual "check-your-answers.njk"

      application.stop()
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
