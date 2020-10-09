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

import java.time.LocalDate

import base.SpecBase
import connectors.SubscriptionConnector
import helpers.JsonFixtures.registerWithoutIDResponse
import models.RegistrationType.{Business, Individual}
import models.{Address, BusinessType, Country, CreateSubscriptionForDACResponse, Name, RegistrationType, ResponseCommon, ResponseDetailForDACSubscription, SecondaryContactPreference, SubscriptionForDACResponse, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages._
import play.api.inject.bind
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.{EmailService, RegistrationService}
import uk.gov.hmrc.domain.{Generator, Nino}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with BeforeAndAfterEach {

  val address: Address = Address("value 1","value 2",Some("value 3"),Some("value 4"),Some("XX9 9XX"),
    Country("valid","GB","United Kingdom"))
  val nino: Nino = new Generator().nextNino
  val name: Name = Name("FirstName", "LastName")
  val email: String = "email@email.com"
  val dacSubscriptionResponse: CreateSubscriptionForDACResponse = CreateSubscriptionForDACResponse(
    SubscriptionForDACResponse(
      responseCommon = ResponseCommon("OK", None, "2020-09-23T16:12:11Z", None),
      responseDetail = ResponseDetailForDACSubscription("XADAC0000123456")))

  val mockEmailService: EmailService = mock[EmailService]
  val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]
  val mockRegistrationService: RegistrationService = mock[RegistrationService]


  override def beforeEach: Unit =
    reset(
      mockRenderer,
      mockEmailService,
      mockSubscriptionConnector,
      mockRegistrationService
    )


  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET - Business with ID (inc. Sole proprietor)" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(BusinessTypePage, BusinessType.UnIncorporatedBody)
        .success.value
        .set(BusinessAddressPage, address)
        .success.value
        .set(RetrievedNamePage, "My Business")
        .success.value
        .set(ConfirmBusinessPage, true)
        .success.value
        .set(ContactNamePage, name)
        .success.value
        .set(ContactEmailAddressPage, email)
        .success.value
        .set(TelephoneNumberQuestionPage, false)
        .success.value
        .set(HaveSecondContactPage, true)
        .success.value
        .set(SecondaryContactNamePage, "Secondary Contact")
        .success.value
        .set(SecondaryContactPreferencePage, SecondaryContactPreference.values.toSet)
        .success.value
        .set(SecondaryContactEmailAddressPage, email)
        .success.value
        .set(SecondaryContactTelephoneNumberPage, "07888888888")
        .success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val json = jsonCaptor.getValue
      val header = (json \ "header").toString
      val businessDetails = (json \ "businessDetailsList").toString
      val contactDetails = (json \ "contactDetailsList").toString

      templateCaptor.getValue mustEqual "check-your-answers.njk"
      header.contains("checkYourAnswers.businessDetails.h2") mustBe true
      businessDetails.contains("Your business") mustBe true
      contactDetails.contains("Contact name") mustBe true
      contactDetails.contains("Email address") mustBe true
      contactDetails.contains("Do they have a telephone number?") mustBe true
      contactDetails.contains("Do you have an additional contact?") mustBe true
      contactDetails.contains("Additional contact name") mustBe true
      contactDetails.contains("Contact preferences") mustBe true
      contactDetails.contains("Additional contact email address") mustBe true
      contactDetails.contains("Additional contact telephone number") mustBe true

      application.stop()
    }

    "must return OK and the correct view for a GET - Individual with ID" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(DoYouHaveUTRPage, false)
        .success.value
        .set(RegistrationTypePage, Individual)
        .success.value
        .set(DoYouHaveANationalInsuranceNumberPage, true)
        .success.value
        .set(NinoPage, nino)
        .success.value
        .set(NamePage, name)
        .success.value
        .set(DateOfBirthPage, LocalDate.now())
        .success.value
        .set(ContactEmailAddressPage, email)
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

      val json = jsonCaptor.getValue
      val header = (json \ "header").toString
      val businessDetails = (json \ "businessDetailsList").toString
      val contactDetails = (json \ "contactDetailsList").toString

      templateCaptor.getValue mustEqual "check-your-answers.njk"
      header.contains("checkYourAnswers.individualDetails.h2") mustBe true
      businessDetails.contains("Do you have UK Unique Taxpayer Reference?") mustBe true
      businessDetails.contains("Registering as") mustBe true
      businessDetails.contains("Do you have a National Insurance number?") mustBe true
      businessDetails.contains("Your National Insurance number") mustBe true
      businessDetails.contains("Your name") mustBe true
      businessDetails.contains("Your date of birth") mustBe true
      contactDetails.contains("Email address") mustBe true
      contactDetails.contains("Do they have a telephone number?") mustBe true

      application.stop()
    }

    "must return OK and the correct view for a GET - Business without ID" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(DoYouHaveUTRPage, false)
        .success.value
        .set(RegistrationTypePage, RegistrationType.values.head)
        .success.value
        .set(BusinessWithoutIDNamePage, "Business name")
        .success.value
        .set(BusinessAddressPage, address)
        .success.value
        .set(ContactNamePage, name)
        .success.value
        .set(ContactEmailAddressPage, email)
        .success.value
        .set(TelephoneNumberQuestionPage, true)
        .success.value
        .set(ContactTelephoneNumberPage, "07111111111")
        .success.value
        .set(HaveSecondContactPage, false)
        .success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val json = jsonCaptor.getValue
      val header = (json \ "header").toString
      val businessDetails = (json \ "businessDetailsList").toString
      val contactDetails = (json \ "contactDetailsList").toString

      templateCaptor.getValue mustEqual "check-your-answers.njk"
      header.contains("checkYourAnswers.businessDetails.h2") mustBe true
      businessDetails.contains("Do you have UK Unique Taxpayer Reference?") mustBe true
      businessDetails.contains("Registering as") mustBe true
      businessDetails.contains("Legal name of business") mustBe true
      businessDetails.contains("Main business address") mustBe true
      contactDetails.contains("Contact name") mustBe true
      contactDetails.contains("Email address") mustBe true
      contactDetails.contains("Do they have a telephone number?") mustBe true
      contactDetails.contains("Telephone number") mustBe true
      contactDetails.contains("Do you have an additional contact?") mustBe true

      application.stop()
    }

    "must return OK and the correct view for a GET - Individual without ID" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(DoYouHaveUTRPage, false)
        .success.value
        .set(DoYouHaveANationalInsuranceNumberPage, false)
        .success.value
        .set(NonUkNamePage, name)
        .success.value
        .set(DateOfBirthPage, LocalDate.now())
        .success.value
        .set(DoYouLiveInTheUKPage, true)
        .success.value
        .set(WhatIsYourAddressUkPage, address)
        .success.value
        .set(ContactEmailAddressPage, email)
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

      val json = jsonCaptor.getValue
      val header = (json \ "header").toString
      val businessDetails = (json \ "businessDetailsList").toString
      val contactDetails = (json \ "contactDetailsList").toString

      templateCaptor.getValue mustEqual "check-your-answers.njk"
      header.contains("checkYourAnswers.individualDetails.h2") mustBe true
      businessDetails.contains("Do you have UK Unique Taxpayer Reference?") mustBe true
      businessDetails.contains("Do you have a National Insurance number?") mustBe true
      businessDetails.contains("Your name") mustBe true
      businessDetails.contains("Your date of birth") mustBe true
      businessDetails.contains("Do you live in the United Kingdom?") mustBe true
      businessDetails.contains("Your home address") mustBe true
      contactDetails.contains("Email address") mustBe true
      contactDetails.contains("Do they have a telephone number?") mustBe true

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

    "OnSubmit" - {

      "must send email and redirect to the confirmation page when OK response received for individual" in {

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUTRPage, false)
          .success.value
          .set(RegistrationTypePage, Individual)
          .success.value
          .set(DoYouHaveANationalInsuranceNumberPage, true)
          .success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[EmailService]
            .toInstance(mockEmailService),
            bind[SubscriptionConnector]
              .toInstance(mockSubscriptionConnector))
          .build()

        when(mockSubscriptionConnector.createSubscription(any())(any(), any()))
          .thenReturn(Future.successful(Some(dacSubscriptionResponse)))

        when(mockEmailService.sendEmail(any())(any()))
          .thenReturn(Future.successful(Some(HttpResponse(OK, ""))))

        when(mockSubscriptionConnector.createEnrolment(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/confirm-registration")
        verify(mockEmailService, times(1)).sendEmail(any())(any())
      }

      "must send email and redirect to the confirmation page when OK response received for individual (no nino) and " +
        "EIS subscription returned a subscription ID" in {

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUTRPage, false)
          .success.value
          .set(RegistrationTypePage, Individual)
          .success.value
          .set(DoYouHaveANationalInsuranceNumberPage, false)
          .success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[RegistrationService]
            .toInstance(mockRegistrationService))
          .overrides(bind[EmailService]
            .toInstance(mockEmailService),
            bind[SubscriptionConnector]
            .toInstance(mockSubscriptionConnector))
          .build()

        when(mockRegistrationService.sendRegistration(any())(any(), any()))
          .thenReturn(Future.successful(Some(HttpResponse(OK, registerWithoutIDResponse))))

        when(mockSubscriptionConnector.createSubscription(any())(any(), any()))
          .thenReturn(Future.successful(Some(dacSubscriptionResponse)))

        when(mockEmailService.sendEmail(any())(any()))
          .thenReturn(Future.successful(Some(HttpResponse(OK, ""))))

        when(mockSubscriptionConnector.createEnrolment(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/confirm-registration")
        verify(mockEmailService, times(1)).sendEmail(any())(any())
      }

      "must redirect to the problem with service page if registration response doesn't have a responseDetail" in {
        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUTRPage, false)
          .success.value
          .set(RegistrationTypePage, Business)
          .success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[RegistrationService]
            .toInstance(mockRegistrationService))
          .build()

        val registrationResponse: String =
          """
            |{"registerWithoutIDResponse": {
            |    "responseCommon": {
            |      "status": "OK",
            |      "statusText": "Success",
            |      "processingDate": "2020-09-01T01:00:00Z"
            |    }
            |  }
            |}
            |""".stripMargin

        when(mockRegistrationService.sendRegistration(any())(any(), any()))
          .thenReturn(Future.successful(Some(HttpResponse(OK, registrationResponse))))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/problem-with-service")
      }

      "must redirect to the problem with service page if registration response throws a JsError" in {
        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUTRPage, false)
          .success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[RegistrationService]
            .toInstance(mockRegistrationService))
          .build()

        val invalidRegistrationResponse: String =
          """
            |{"registerWithoutIDResponse": {
            |    "responseCommon": {
            |      "status": "OK",
            |      "statusText": "Success",
            |      "processingDate": "2020-09-01T01:00:00Z"
            |    },
            |    "responseDetail": {
            |      "SAFEID": 123456789012345
            |    }
            |  }
            |}
            |""".stripMargin

        when(mockRegistrationService.sendRegistration(any())(any(), any()))
          .thenReturn(Future.successful(Some(HttpResponse(OK, invalidRegistrationResponse))))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/problem-with-service")
      }

      "must redirect to the problem with service if EIS subscription returns None" in {
        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUTRPage, false)
          .success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[RegistrationService]
            .toInstance(mockRegistrationService),
            bind[SubscriptionConnector]
            .toInstance(mockSubscriptionConnector))
          .build()

        when(mockRegistrationService.sendRegistration(any())(any(), any()))
          .thenReturn(Future.successful(Some(HttpResponse(OK, registerWithoutIDResponse))))

        when(mockSubscriptionConnector.createSubscription(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/problem-with-service")
      }

      "must redirect to the problem with service if EIS subscription throws an error" in {
        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUTRPage, false)
          .success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[RegistrationService]
            .toInstance(mockRegistrationService),
            bind[SubscriptionConnector]
              .toInstance(mockSubscriptionConnector))
          .build()

        when(mockRegistrationService.sendRegistration(any())(any(), any()))
          .thenReturn(Future.successful(Some(HttpResponse(OK, registerWithoutIDResponse))))

        when(mockSubscriptionConnector.createSubscription(any())(any(), any()))
          .thenReturn(Future.failed(new Exception("")))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/problem-with-service")
      }

      "must redirect to the problem with service and not send email when error response received from subscription connector" in {

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUTRPage, true)
          .success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)
        )
          .overrides(bind[EmailService]
            .toInstance(mockEmailService),
            bind[SubscriptionConnector]
            .toInstance(mockSubscriptionConnector))
          .build()

        when(mockEmailService.sendEmail(any())(any()))
          .thenReturn(Future.successful(Some(HttpResponse(OK, ""))))

        when(mockSubscriptionConnector.createSubscription(any())(any(), any()))
          .thenReturn(Future.failed(new Exception("")))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/problem-with-service")

        verify(mockEmailService, times(0)).sendEmail(any())(any())

      }

      "must send email and redirect to the confirmation page when OK response received for organisation" in {

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(DoYouHaveUTRPage, true)
          .success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[EmailService]
            .toInstance(mockEmailService),
            bind[SubscriptionConnector]
              .toInstance(mockSubscriptionConnector))
          .build()

        when(mockSubscriptionConnector.createSubscription(any())(any(), any()))
          .thenReturn(Future.successful(Some(dacSubscriptionResponse)))

        when(mockEmailService.sendEmail(any())(any()))
          .thenReturn(Future.successful(Some(HttpResponse(OK, ""))))

        when(mockSubscriptionConnector.createEnrolment(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/confirm-registration")
      }

    "must send email and redirect to the confirmation page when OK response received for organisation (no utr) and " +
      "EIS subscription returned a subscription ID" in {

      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(DoYouHaveUTRPage, false)
        .success.value
        .set(RegistrationTypePage, Business)
        .success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[RegistrationService]
          .toInstance(mockRegistrationService))
        .overrides(bind[EmailService]
          .toInstance(mockEmailService),
          bind[SubscriptionConnector]
            .toInstance(mockSubscriptionConnector))
        .build()

      when(mockSubscriptionConnector.createSubscription(any())(any(), any()))
        .thenReturn(Future.successful(Some(dacSubscriptionResponse)))

      when(mockRegistrationService.sendRegistration(any())(any(), any()))
        .thenReturn(Future.successful(Some(HttpResponse(OK, registerWithoutIDResponse))))

      when(mockEmailService.sendEmail(any())(any()))
        .thenReturn(Future.successful(Some(HttpResponse(OK, ""))))

      when(mockSubscriptionConnector.createEnrolment(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/confirm-registration")
    }

      "must redirect to problem with service when NOT_FOUND response received from registration for organisation" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[RegistrationService]
            .toInstance(mockRegistrationService))
          .build()

        when(mockRegistrationService.sendRegistration(any())(any(), any()))
          .thenReturn(Future.successful(Some(HttpResponse(NOT_FOUND, ""))))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/problem-with-service")
      }

      "must redirect to problem with service when BAD_REQUEST response received from registration for organisation" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))

          .overrides(bind[SubscriptionConnector]
              .toInstance(mockSubscriptionConnector),
            bind[RegistrationService]
            .toInstance(mockRegistrationService))
          .build()

        when(mockRegistrationService.sendRegistration(any())(any(), any()))
          .thenReturn(Future.successful(Some(HttpResponse(BAD_REQUEST, ""))))

        when(mockSubscriptionConnector.createEnrolment(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/problem-with-service")
      }

      "must redirect to problem with service when None response received from registration for organisation" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[EmailService]
            .toInstance(mockEmailService),
            bind[SubscriptionConnector]
              .toInstance(mockSubscriptionConnector))
          .overrides(bind[RegistrationService]
            .toInstance(mockRegistrationService))
          .build()

        when(mockRegistrationService.sendRegistration(any())(any(), any()))
          .thenReturn(Future.successful(None))

        when(mockSubscriptionConnector.createEnrolment(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/problem-with-service")
      }


      "must redirect the user to the index page when send email call fails" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[EmailService]
            .toInstance(mockEmailService),
            bind[SubscriptionConnector]
              .toInstance(mockSubscriptionConnector))
          .build()

        when(mockEmailService.sendEmail(any())(any()))
          .thenReturn(Future.failed(new RuntimeException))

        when(mockSubscriptionConnector.createEnrolment(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/problem-with-service")
      }

      "must redirect to problem with service when NOT_FOUND response received from registration for individual" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[RegistrationService]
            .toInstance(mockRegistrationService))
          .build()

        when(mockRegistrationService.sendRegistration(any())(any(), any()))
          .thenReturn(Future.successful(Some(HttpResponse(NOT_FOUND, ""))))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/problem-with-service")
      }

      "must redirect to problem with service when BAD_REQUEST response received from registration for individual" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[RegistrationService]
            .toInstance(mockRegistrationService))
          .build()

        when(mockRegistrationService.sendRegistration(any())(any(), any()))
          .thenReturn(Future.successful(Some(HttpResponse(BAD_REQUEST, ""))))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/problem-with-service")
      }

      "must redirect to problem with service when None response received from registration for individual" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[RegistrationService]
            .toInstance(mockRegistrationService))
          .build()

        when(mockRegistrationService.sendRegistration(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/problem-with-service")
      }
    }
  }
}

