/*
 * Copyright 2021 HM Revenue & Customs
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
import connectors.SubscriptionConnector
import generators.Generators
import matchers.JsonMatchers
import models.readSubscription._
import models.{BusinessAddress, BusinessDetails, BusinessType, Name, NormalMode, PayloadRegistrationWithIDResponse, RegisterWithIDResponse, ResponseCommon, UniqueTaxpayerReference, UserAnswers}
import pages._
import play.api.inject._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{BusinessMatchingService, EmailService}
import uk.gov.hmrc.domain.Generator
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.viewmodels.NunjucksSupport
import org.mockito.ArgumentMatchers.any

import java.time.LocalDate
import scala.concurrent.Future

class BusinessMatchingControllerSpec extends SpecBase with NunjucksSupport  with JsonMatchers  with Generators {

  lazy val individualMatchingRoute: String = routes.BusinessMatchingController.matchIndividual(NormalMode).url
  lazy val businessMatchingRoute: String = routes.BusinessMatchingController.matchBusiness().url
  lazy val businessMatchNotFoundRoute: String = routes.BusinessNotConfirmedController.onPageLoad().url
  lazy val problemWithServiceRoute: String = routes.ProblemWithServiceController.onPageLoad().url
  lazy val alreadyRegisteredRoute: String = routes.ThisOrganisationHasAlreadyBeenRegisteredController.onPageLoad().url

  def getRequest(route: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, route)

  val mockBusinessMatchingService: BusinessMatchingService = mock[BusinessMatchingService]

  val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]

  val mockEmailService: EmailService = mock[EmailService]

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  def createBusinessUserAnswers(utr: String): UserAnswers = UserAnswers(userAnswersId)
    .set(BusinessTypePage, BusinessType.UnIncorporatedBody)
    .success
    .value
    .set(CorporationTaxUTRPage, UniqueTaxpayerReference(utr))
    .success
    .value
    .set(BusinessNamePage, "Business Name")
    .success
    .value


  val primaryContact: PrimaryContact = PrimaryContact(Seq(
    ContactInformationForIndividual(
      individual = IndividualDetails(firstName = "FirstName", lastName = "LastName", middleName = None),
      email = "email@email.com", phone = Some("07111222333"), mobile = Some("07111222333"))
  ))
  val secondaryContact: SecondaryContact = SecondaryContact(Seq(
    ContactInformationForOrganisation(
      organisation = OrganisationDetails(organisationName = "Organisation Name"),
      email = "email@email.com", phone = None, mobile = None)
  ))


  def createResponseDetail(id: String): ResponseDetailForReadSubscription = ResponseDetailForReadSubscription(
    subscriptionID = id,
    tradingName = Some("Trading Name"),
    isGBUser = true,
    primaryContact = primaryContact,
    secondaryContact = Some(secondaryContact))

  val responseCommon: ResponseCommon = ResponseCommon(
    status = "OK",
    statusText = None,
    processingDate = "2020-08-09T11:23:45Z",
    returnParameters = None)

  when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

  "BusinessMatching Controller" - {
    "when a correct submission can be created and returns an individual match" - {

      "must redirect the user to the check your answers page" in {

        forAll(validSafeID) {
          safeId =>
            val userAnswers = UserAnswers(userAnswersId)
              .set(DateOfBirthPage, LocalDate.now())
              .success
              .value
              .set(NamePage, Name("", ""))
              .success
              .value
              .set(NinoPage, (new Generator()).nextNino)
              .success
              .value

            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[BusinessMatchingService].toInstance(mockBusinessMatchingService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

            when(mockBusinessMatchingService.sendIndividualMatchingInformation(any())(any(), any()))
              .thenReturn(
                Future.successful(Right((Some(
                  PayloadRegistrationWithIDResponse(
                    RegisterWithIDResponse(
                      ResponseCommon("OK", None, "", None),
                      None
                    )
                  )
                ), Some(safeId), None)
                ))
              )

            val result = route(application, getRequest(individualMatchingRoute)).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some(routes.IdentityConfirmedController.onPageLoad().url)
        }
      }

      "must redirect the user to registration confirmation page if user is already subscribed" in {

        forAll(validSubscriptionID, validSafeID) {
          (existingSubscriptionID, safeId) =>

            val userAnswers = UserAnswers(userAnswersId)
              .set(DateOfBirthPage, LocalDate.now())
              .success
              .value
              .set(NamePage, Name("", ""))
              .success
              .value
              .set(NinoPage, (new Generator()).nextNino)
              .success
              .value

            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[BusinessMatchingService].toInstance(mockBusinessMatchingService),
                bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                bind[EmailService].toInstance(mockEmailService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

            val responseDetailRead: ResponseDetailForReadSubscription = createResponseDetail(existingSubscriptionID)

            val displaySubscriptionForDACResponse: DisplaySubscriptionForDACResponse =
              DisplaySubscriptionForDACResponse(
                ReadSubscriptionForDACResponse(responseCommon = responseCommon, responseDetail = responseDetailRead)
              )

            when(mockBusinessMatchingService.sendIndividualMatchingInformation(any())(any(), any()))
              .thenReturn(
                Future.successful(Right((Some(
                  PayloadRegistrationWithIDResponse(
                    RegisterWithIDResponse(
                      ResponseCommon("OK", None, "", None),
                      None
                    )
                  )
                ), Some(safeId), Some(displaySubscriptionForDACResponse))
                ))
              )

            when(mockSubscriptionConnector.createEnrolment(any())(any(), any()))
              .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

            when(mockEmailService.sendEmail(any())(any()))
              .thenReturn(Future.successful(Some(HttpResponse(OK, ""))))

            val result = route(application, getRequest(individualMatchingRoute)).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some(routes.RegistrationSuccessfulController.onPageLoad().url)
        }
      }


      "must redirect the user to registration confirmation page if user is already subscribed even if email call fails" in {

        forAll(validSubscriptionID, validSafeID) {
          (existingSubscriptionID, safeId) =>

            val userAnswers = UserAnswers(userAnswersId)
              .set(DateOfBirthPage, LocalDate.now())
              .success
              .value
              .set(NamePage, Name("", ""))
              .success
              .value
              .set(NinoPage, (new Generator()).nextNino)
              .success
              .value

            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[BusinessMatchingService].toInstance(mockBusinessMatchingService),
                bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                bind[EmailService].toInstance(mockEmailService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

            val responseDetailRead: ResponseDetailForReadSubscription = createResponseDetail(existingSubscriptionID)

            val displaySubscriptionForDACResponse: DisplaySubscriptionForDACResponse =
              DisplaySubscriptionForDACResponse(
                ReadSubscriptionForDACResponse(responseCommon = responseCommon, responseDetail = responseDetailRead)
              )

            when(mockBusinessMatchingService.sendIndividualMatchingInformation(any())(any(), any()))
              .thenReturn(
                Future.successful(Right((Some(
                  PayloadRegistrationWithIDResponse(
                    RegisterWithIDResponse(
                      ResponseCommon("OK", None, "", None),
                      None
                    )
                  )
                ), Some(safeId), Some(displaySubscriptionForDACResponse))
                ))
              )

            when(mockSubscriptionConnector.createEnrolment(any())(any(), any()))
              .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

            when(mockEmailService.sendEmail(any())(any()))
              .thenReturn(Future.successful(Some(HttpResponse(BAD_REQUEST, ""))))

            val result = route(application, getRequest(individualMatchingRoute)).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some(routes.RegistrationSuccessfulController.onPageLoad().url)
        }
      }

      "must redirect the user to tech difficulties page if user is already subscribed and create enrolments call fails" in {

        forAll(validSubscriptionID, validSafeID) {
          (existingSubscriptionID, safeId) =>

            val userAnswers = UserAnswers(userAnswersId)
              .set(DateOfBirthPage, LocalDate.now())
              .success
              .value
              .set(NamePage, Name("", ""))
              .success
              .value
              .set(NinoPage, (new Generator()).nextNino)
              .success
              .value

            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[BusinessMatchingService].toInstance(mockBusinessMatchingService),
                bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                bind[EmailService].toInstance(mockEmailService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

            val responseDetailRead: ResponseDetailForReadSubscription = createResponseDetail(existingSubscriptionID)

            val displaySubscriptionForDACResponse: DisplaySubscriptionForDACResponse =
              DisplaySubscriptionForDACResponse(
                ReadSubscriptionForDACResponse(responseCommon = responseCommon, responseDetail = responseDetailRead)
              )

            when(mockBusinessMatchingService.sendIndividualMatchingInformation(any())(any(), any()))
              .thenReturn(
                Future.successful(Right((Some(
                  PayloadRegistrationWithIDResponse(
                    RegisterWithIDResponse(
                      ResponseCommon("OK", None, "", None),
                      None
                    )
                  )
                ), Some(safeId), Some(displaySubscriptionForDACResponse))
                ))
              )

            when(mockSubscriptionConnector.createEnrolment(any())(any(), any()))
              .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "")))


            val result = route(application, getRequest(individualMatchingRoute)).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/problem-with-service")
        }
      }
    }

    "when a correct submission can be created and returns no individual match" - {

      "must redirect the user to the cant find identity page" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(DateOfBirthPage, LocalDate.now())
          .success
          .value
          .set(NamePage, Name("", ""))
          .success
          .value
          .set(NinoPage, (new Generator()).nextNino)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[BusinessMatchingService].toInstance(mockBusinessMatchingService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

        when(mockBusinessMatchingService.sendIndividualMatchingInformation(any())(any(), any()))
          .thenReturn(Future.successful(Right((None, None, None))))

        val result = route(application, getRequest(individualMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.IndividualNotConfirmedController.onPageLoad().url)
      }
    }

    "when a correct submission can be created and returns a business match" - {

      "must redirect the user to /confirm-business page if business is unincorporated or corporate" in {
        forAll(validSafeID, validUtr) {
          (safeId, utr) =>
            val application = applicationBuilder(userAnswers = Some(createBusinessUserAnswers(utr)))
              .overrides(
                bind[BusinessMatchingService].toInstance(mockBusinessMatchingService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

            val businessDetails = BusinessDetails(
              name = "My Company",
              address = BusinessAddress("1 Address Street", None, None, None, "NE11 1BB", "GB"))

            when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
              .thenReturn(Future.successful((Some(businessDetails), Some(safeId), None)))

            val result = route(application, getRequest(businessMatchingRoute)).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/confirm-business")
        }
      }

      "must create the enrolment redirect the user to registration confirmation when user already subscribed" in {
        forAll(validSubscriptionID, validSafeID, validUtr) {
          (existingSubscriptionID, safeId, utr) =>

            val application = applicationBuilder(userAnswers = Some(createBusinessUserAnswers(utr)))
              .overrides(
                bind[BusinessMatchingService].toInstance(mockBusinessMatchingService),
                bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                bind[EmailService].toInstance(mockEmailService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

            val businessDetails = BusinessDetails(
              name = "My Company",
              address = BusinessAddress("1 Address Street", None, None, None, "NE11 1BB", "GB"))

            val responseDetailRead: ResponseDetailForReadSubscription = createResponseDetail(existingSubscriptionID)

            val displaySubscriptionForDACResponse: DisplaySubscriptionForDACResponse =
              DisplaySubscriptionForDACResponse(
                ReadSubscriptionForDACResponse(responseCommon = responseCommon, responseDetail = responseDetailRead)
              )

            when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
              .thenReturn(Future.successful((Some(businessDetails), Some(safeId), Some(displaySubscriptionForDACResponse))))


            when(mockSubscriptionConnector.createEnrolment(any())(any(), any()))
              .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

            when(mockEmailService.sendEmail(any())(any()))
              .thenReturn(Future.successful(Some(HttpResponse(OK, ""))))

            val result = route(application, getRequest(businessMatchingRoute)).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/confirm-registration")
        }
      }


      "must create the enrolment redirect the user to registration confirmation when user already subscribed even if call to email service fails" in {
        forAll(validSubscriptionID, validSafeID, validUtr) {
          (existingSubscriptionID, safeId, utr) =>

            val application = applicationBuilder(userAnswers = Some(createBusinessUserAnswers(utr)))
              .overrides(
                bind[BusinessMatchingService].toInstance(mockBusinessMatchingService),
                bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                bind[EmailService].toInstance(mockEmailService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

            val businessDetails = BusinessDetails(
              name = "My Company",
              address = BusinessAddress("1 Address Street", None, None, None, "NE11 1BB", "GB"))

            val responseDetailRead: ResponseDetailForReadSubscription = createResponseDetail(existingSubscriptionID)

            val displaySubscriptionForDACResponse: DisplaySubscriptionForDACResponse =
              DisplaySubscriptionForDACResponse(
                ReadSubscriptionForDACResponse(responseCommon = responseCommon, responseDetail = responseDetailRead)
              )

            when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
              .thenReturn(Future.successful((Some(businessDetails), Some(safeId), Some(displaySubscriptionForDACResponse))))


            when(mockSubscriptionConnector.createEnrolment(any())(any(), any()))
              .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

            when(mockEmailService.sendEmail(any())(any()))
              .thenReturn(Future.successful(Some(HttpResponse(BAD_REQUEST, ""))))

            val result = route(application, getRequest(businessMatchingRoute)).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/confirm-registration")
        }
      }

      "must redirect to technical difficulties page if call to create the enrolment fails when user already subscribed" in {
        forAll(validSubscriptionID, validSafeID, validUtr) {
          (existingSubscriptionID, safeId, utr) =>

            val application = applicationBuilder(userAnswers = Some(createBusinessUserAnswers(utr)))
              .overrides(
                bind[BusinessMatchingService].toInstance(mockBusinessMatchingService),
                bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                bind[EmailService].toInstance(mockEmailService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

            val businessDetails = BusinessDetails(
              name = "My Company",
              address = BusinessAddress("1 Address Street", None, None, None, "NE11 1BB", "GB"))

            val responseDetailRead: ResponseDetailForReadSubscription = createResponseDetail(existingSubscriptionID)

            val displaySubscriptionForDACResponse: DisplaySubscriptionForDACResponse =
              DisplaySubscriptionForDACResponse(
                ReadSubscriptionForDACResponse(responseCommon = responseCommon, responseDetail = responseDetailRead)
              )

            when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
              .thenReturn(Future.successful((Some(businessDetails), Some(safeId), Some(displaySubscriptionForDACResponse))))


            when(mockSubscriptionConnector.createEnrolment(any())(any(), any()))
              .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "")))

            val result = route(application, getRequest(businessMatchingRoute)).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/problem-with-service")
        }
      }

      "must redirect to technical difficulties page if call to create the enrolment fails when user already enrolled" in {
        forAll(validSubscriptionID, validSafeID, validUtr) {
          (existingSubscriptionID, safeId, utr) =>

            val application = applicationBuilder(userAnswers = Some(createBusinessUserAnswers(utr)))
              .overrides(
                bind[BusinessMatchingService].toInstance(mockBusinessMatchingService),
                bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                bind[EmailService].toInstance(mockEmailService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

            val businessDetails = BusinessDetails(
              name = "My Company",
              address = BusinessAddress("1 Address Street", None, None, None, "NE11 1BB", "GB"))

            val responseDetailRead: ResponseDetailForReadSubscription = createResponseDetail(existingSubscriptionID)

            val displaySubscriptionForDACResponse: DisplaySubscriptionForDACResponse =
              DisplaySubscriptionForDACResponse(
                ReadSubscriptionForDACResponse(responseCommon = responseCommon, responseDetail = responseDetailRead)
              )

            when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
              .thenReturn(Future.successful((Some(businessDetails), Some(safeId), Some(displaySubscriptionForDACResponse))))


            when(mockSubscriptionConnector.createEnrolment(any())(any(), any())).thenReturn(Future.successful(
              HttpResponse(BAD_REQUEST, """{"code":"MULTIPLE_ENROLMENTS_INVALID","message":"Multiple Enrolments are not valid for this service"}""")))

            val result = route(application, getRequest(businessMatchingRoute)).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/org-already-registered")
        }
      }

      "must redirect the user to /confirm-business page if business is not unincorporated or corporate" in {
        forAll(validSafeID, validUtr) {
          (safeId, utr) =>
            val businessUserAnswers: UserAnswers = UserAnswers(userAnswersId)
              .set(BusinessTypePage, BusinessType.Partnership)
              .success
              .value
              .set(SelfAssessmentUTRPage, UniqueTaxpayerReference(utr))
              .success
              .value
              .set(BusinessNamePage, "Business Name")
              .success
              .value

            val application = applicationBuilder(userAnswers = Some(businessUserAnswers))
              .overrides(
                bind[BusinessMatchingService].toInstance(mockBusinessMatchingService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

            val businessDetails = BusinessDetails(
              name = "My Company",
              address = BusinessAddress("1 Address Street", None, None, None, "NE11 1BB", "GB"))

            when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
              .thenReturn(Future.successful((Some(businessDetails), Some(safeId), None)))

            val result = route(application, getRequest(businessMatchingRoute)).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/confirm-business")
        }
      }
    }

    "when a correct submission can be created and returns no business match" - {

      "must redirect the user to the can't find business page" in {
        forAll(validUtr) {
          utr =>
            val application = applicationBuilder(userAnswers = Some(createBusinessUserAnswers(utr)))
              .overrides(
                bind[BusinessMatchingService].toInstance(mockBusinessMatchingService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

            when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
              .thenReturn(Future.successful((None, None, None)))

            val result = route(application, getRequest(businessMatchingRoute)).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some(businessMatchNotFoundRoute)
        }
      }
    }

    "when a correct submission can be created and returns a business match" - {

      "must redirect to the error page if validation fails" in {
        forAll(validUtr) {
          utr =>
            val application = applicationBuilder(userAnswers = Some(createBusinessUserAnswers(utr)))
              .overrides(
                bind[BusinessMatchingService].toInstance(mockBusinessMatchingService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

            when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
              .thenReturn(Future.failed(new Exception))

            val result = route(application, getRequest(businessMatchingRoute)).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some(problemWithServiceRoute)
        }
      }
    }

    "when a correct submission can't be created due to missing data required to business match" - {

      "must redirect the user to the utr page if it's missing" in {

        val businessUserAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(BusinessTypePage, BusinessType.CorporateBody)
          .success
          .value
          .set(BusinessNamePage, "Business Name")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(businessUserAnswers))
          .overrides(
            bind[BusinessMatchingService].toInstance(mockBusinessMatchingService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

        when(mockBusinessMatchingService.sendBusinessMatchingInformation(any())(any(), any()))
          .thenReturn(Future.successful((None, None, None)))

        val result = route(application, getRequest(businessMatchingRoute)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/register-for-cross-border-arrangements/register/have-utr")
      }
    }
  }

}
