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

package services

import base.SpecBase
import connectors.{RegistrationConnector, SubscriptionConnector}
import generators.Generators
import models.BusinessType._
import models.readSubscription._
import models.{
  AddressResponse,
  BusinessAddress,
  BusinessDetails,
  BusinessType,
  ContactDetails,
  IndividualResponse,
  Name,
  OrganisationResponse,
  PayloadRegistrationWithIDResponse,
  RegisterWithIDResponse,
  ResponseCommon,
  ResponseDetail,
  UniqueTaxpayerReference,
  UserAnswers
}
import org.scalacheck.Arbitrary.arbitrary
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.Application
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Nino
import wolfendale.scalacheck.regexp.RegexpGen

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

class BusinessMatchingServiceSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]
  val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[RegistrationConnector].toInstance(mockRegistrationConnector),
      bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
    )
    .build()

  val businessMatchingService: BusinessMatchingService = app.injector.instanceOf[BusinessMatchingService]

  val businessTypesNoSoleTrader = Seq(Partnership, LimitedLiability, CorporateBody, UnIncorporatedBody)

  def utrPage(businessType: BusinessType): QuestionPage[UniqueTaxpayerReference] =
    if (businessType == BusinessType.UnIncorporatedBody | businessType == BusinessType.CorporateBody) {
      CorporationTaxUTRPage
    } else {
      SelfAssessmentUTRPage
    }

  override def beforeEach: Unit =
    reset(
      mockRegistrationConnector,
      mockSubscriptionConnector
    )

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

  "Business Matching Service" - {
    "when able to construct an individual matching submission" - {
      "should send a request to the business matching connector for an individual" in {
        forAll(arbitrary[UserAnswers], arbitrary[Name], arbitrary[LocalDate], arbitrary[Nino], arbitrary[PayloadRegistrationWithIDResponse]) {
          (userAnswers, name, dob, nino, response) =>
            val answers = userAnswers
              .set(NamePage, name)
              .success
              .value
              .set(DateOfBirthPage, dob)
              .success
              .value
              .set(NinoPage, nino)
              .success
              .value

            val registerWithSafeId = response.registerWithIDResponse.copy(
              responseDetail = Some(
                ResponseDetail(
                  "XE0001234567890",
                  None,
                  isEditable = false,
                  isAnAgent = false,
                  None,
                  isAnIndividual = false,
                  IndividualResponse("Bobby", None, "Bob", None),
                  AddressResponse("1 TestStreet", Some("Test"), None, None, Some("AA11BB"), "GB"),
                  ContactDetails(None, None, None, None)
                )
              )
            )
            val responseWithSafeId = response.copy(registerWithSafeId)

            when(mockRegistrationConnector.registerWithID(any())(any(), any()))
              .thenReturn(
                Future.successful(Some(responseWithSafeId))
              )

            when(mockSubscriptionConnector.readSubscriptionDetails(any())(any(), any()))
              .thenReturn(
                Future.successful(None)
              )

            val result = businessMatchingService.sendIndividualMatchingInformation(answers)

            whenReady(result) {
              res =>
                res.map(_._1.get) mustBe Right(responseWithSafeId)
                res.map(_._2.get) mustBe Right("XE0001234567890")
            }
        }
      }

      "should send a request to the business matching connector for an individual and return " +
        "subscription info if this already exists" in {
          forAll(arbitrary[UserAnswers],
                 arbitrary[Name],
                 arbitrary[LocalDate],
                 arbitrary[Nino],
                 arbitrary[PayloadRegistrationWithIDResponse],
                 validSubscriptionID
          ) {
            (userAnswers, name, dob, nino, response, existingSubscriptionID) =>
              val answers = userAnswers
                .set(NamePage, name)
                .success
                .value
                .set(DateOfBirthPage, dob)
                .success
                .value
                .set(NinoPage, nino)
                .success
                .value

              val registerWithSafeId = response.registerWithIDResponse.copy(
                responseDetail = Some(
                  ResponseDetail(
                    "XE0001234567890",
                    None,
                    isEditable = false,
                    isAnAgent = false,
                    None,
                    isAnIndividual = false,
                    IndividualResponse("Bobby", None, "Bob", None),
                    AddressResponse("1 TestStreet", Some("Test"), None, None, Some("AA11BB"), "GB"),
                    ContactDetails(None, None, None, None)
                  )
                )
              )
              val responseWithSafeId = response.copy(registerWithSafeId)

              val responseDetailRead: ResponseDetailForReadSubscription = responseDetail.copy(subscriptionID = existingSubscriptionID)

              val displaySubscriptionForDACResponse: DisplaySubscriptionForDACResponse =
                DisplaySubscriptionForDACResponse(
                  ReadSubscriptionForDACResponse(responseCommon = responseCommon, responseDetail = responseDetailRead)
                )

              when(mockRegistrationConnector.registerWithID(any())(any(), any()))
                .thenReturn(
                  Future.successful(Some(responseWithSafeId))
                )

              when(mockSubscriptionConnector.readSubscriptionDetails(any())(any(), any()))
                .thenReturn(
                  Future.successful(Some(displaySubscriptionForDACResponse))
                )

              val result = businessMatchingService.sendIndividualMatchingInformation(answers)

              whenReady(result) {
                res =>
                  res.map(_._1.get) mustBe Right(responseWithSafeId)
                  res.map(_._2.get) mustBe Right("XE0001234567890")
                  res.map(_._3.get) mustBe Right(displaySubscriptionForDACResponse)

              }
          }
        }
    }

    "when unable to construct an individual matching submission" - {
      "should return a future with no value" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers
              .remove(NamePage)
              .success
              .value
              .remove(DateOfBirthPage)
              .success
              .value
            val result = businessMatchingService.sendIndividualMatchingInformation(answers)

            whenReady(result) {
              _.isLeft mustBe true //ie Exception thrown
            }
        }
      }
    }

    "when able to construct a business/organisation matching submission" - {
      "must return the validated business name" in {
        forAll(
          arbitrary[UserAnswers],
          arbitrary[UniqueTaxpayerReference],
          RegexpGen.from("^[a-zA-Z0-9 '&\\/]{1,105}$"),
          for {
            firstName  <- RegexpGen.from("^[a-zA-Z0-9 '&\\/]{1,35}$")
            secondName <- RegexpGen.from("^[a-zA-Z0-9 '&\\/]{1,35}$")
          } yield Name(firstName, secondName)
        ) {
          (userAnswers, utr, businessName, soleTraderName) =>
            val getRandomBusinessTypeNoSoleTrader = Random.shuffle(businessTypesNoSoleTrader).head

            val answers = userAnswers
              .set(BusinessTypePage, getRandomBusinessTypeNoSoleTrader)
              .success
              .value
              .set(utrPage(getRandomBusinessTypeNoSoleTrader), utr)
              .success
              .value
              .set(BusinessNamePage, businessName)
              .success
              .value
              .set(SoleTraderNamePage, soleTraderName)
              .success
              .value

            val payload = PayloadRegistrationWithIDResponse(
              RegisterWithIDResponse(
                ResponseCommon("", None, "", None),
                Some(
                  ResponseDetail(
                    "XE0001234567890",
                    None,
                    isEditable = false,
                    isAnAgent = false,
                    None,
                    isAnIndividual = false,
                    OrganisationResponse(businessName, isAGroup = false, None, None),
                    AddressResponse("1 TestStreet", Some("Test"), Some("Test"), None, Some("AA11BB"), "GB"),
                    ContactDetails(None, None, None, None)
                  )
                )
              )
            )

            val businessDetailsWithSafeID = (Some(
                                               BusinessDetails(
                                                 businessName,
                                                 BusinessAddress("1 TestStreet", Some("Test"), Some("Test"), None, "AA11BB", "GB")
                                               )
                                             ),
                                             Some("XE0001234567890"),
                                             None
            )

            when(mockRegistrationConnector.registerWithID(any())(any(), any()))
              .thenReturn(
                Future.successful(Some(payload))
              )

            when(mockSubscriptionConnector.readSubscriptionDetails(any())(any(), any()))
              .thenReturn(
                Future.successful(None)
              )
            val result = businessMatchingService.sendBusinessMatchingInformation(answers)

            whenReady(result) {
              result =>
                result mustBe businessDetailsWithSafeID
            }
        }
      }

      "must return the validated business name with existing subscriptionId defined if subscription already exists" in {
        forAll(
          arbitrary[UserAnswers],
          arbitrary[UniqueTaxpayerReference],
          RegexpGen.from("^[a-zA-Z0-9 '&\\/]{1,105}$"),
          for {
            firstName  <- RegexpGen.from("^[a-zA-Z0-9 '&\\/]{1,35}$")
            secondName <- RegexpGen.from("^[a-zA-Z0-9 '&\\/]{1,35}$")
          } yield Name(firstName, secondName),
          validSafeID,
          validSubscriptionID
        ) {
          (userAnswers, utr, businessName, soleTraderName, safeID, existingSubscriptionID) =>
            val getRandomBusinessTypeNoSoleTrader = Random.shuffle(businessTypesNoSoleTrader).head

            val answers = userAnswers
              .set(BusinessTypePage, getRandomBusinessTypeNoSoleTrader)
              .success
              .value
              .set(utrPage(getRandomBusinessTypeNoSoleTrader), utr)
              .success
              .value
              .set(BusinessNamePage, businessName)
              .success
              .value
              .set(SoleTraderNamePage, soleTraderName)
              .success
              .value

            val payload = PayloadRegistrationWithIDResponse(
              RegisterWithIDResponse(
                ResponseCommon("", None, "", None),
                Some(
                  ResponseDetail(
                    "XE0001234567890",
                    None,
                    isEditable = false,
                    isAnAgent = false,
                    None,
                    isAnIndividual = false,
                    OrganisationResponse(businessName, isAGroup = false, None, None),
                    AddressResponse("1 TestStreet", Some("Test"), Some("Test"), None, Some("AA11BB"), "GB"),
                    ContactDetails(None, None, None, None)
                  )
                )
              )
            )

            val responseDetailRead: ResponseDetailForReadSubscription = responseDetail.copy(subscriptionID = existingSubscriptionID)

            val displaySubscriptionForDACResponse: DisplaySubscriptionForDACResponse =
              DisplaySubscriptionForDACResponse(
                ReadSubscriptionForDACResponse(responseCommon = responseCommon, responseDetail = responseDetailRead)
              )

            val businessDetailsWithSafeID = (Some(
                                               BusinessDetails(
                                                 businessName,
                                                 BusinessAddress("1 TestStreet", Some("Test"), Some("Test"), None, "AA11BB", "GB")
                                               )
                                             ),
                                             Some("XE0001234567890"),
                                             Some(displaySubscriptionForDACResponse)
            )

            when(mockRegistrationConnector.registerWithID(any())(any(), any()))
              .thenReturn(
                Future.successful(Some(payload))
              )

            when(mockSubscriptionConnector.readSubscriptionDetails(any())(any(), any()))
              .thenReturn(
                Future.successful(Some(displaySubscriptionForDACResponse))
              )

            val result = businessMatchingService.sendBusinessMatchingInformation(answers)

            whenReady(result) {
              result =>
                result mustBe businessDetailsWithSafeID
            }
        }
      }

      "should send a request to the business matching connector for a sole proprietor" in {
        forAll(arbitrary[UniqueTaxpayerReference]) {
          utr =>
            val answers = UserAnswers(userAnswersId)
              .set(BusinessTypePage, NotSpecified)
              .success
              .value
              .set(utrPage(NotSpecified), utr)
              .success
              .value
              .set(DateOfBirthPage, LocalDate.now())
              .success
              .value
              .set(SoleTraderNamePage, Name("Bobby", "Bob"))
              .success
              .value

            val payload = PayloadRegistrationWithIDResponse(
              RegisterWithIDResponse(
                ResponseCommon("", None, "", None),
                Some(
                  ResponseDetail(
                    "XE0001234567890",
                    None,
                    isEditable = false,
                    isAnAgent = false,
                    None,
                    isAnIndividual = false,
                    IndividualResponse("Bobby", None, "Bob", None),
                    AddressResponse("1 TestStreet", Some("Test"), None, None, Some("AA11BB"), "GB"),
                    ContactDetails(None, None, None, None)
                  )
                )
              )
            )

            val businessDetailsWithSafeID = (Some(
                                               BusinessDetails(
                                                 "Bobby Bob",
                                                 BusinessAddress("1 TestStreet", Some("Test"), None, None, "AA11BB", "GB")
                                               )
                                             ),
                                             Some("XE0001234567890"),
                                             None
            )

            when(mockRegistrationConnector.registerWithID(any())(any(), any()))
              .thenReturn(
                Future.successful(Some(payload))
              )
            when(mockSubscriptionConnector.readSubscriptionDetails(any())(any(), any()))
              .thenReturn(
                Future.successful(None)
              )

            val result = businessMatchingService.sendBusinessMatchingInformation(answers)

            whenReady(result) {
              result =>
                result mustBe businessDetailsWithSafeID
            }
        }
      }

      "should return (None,None) for retrieval of SafeID if business can't be found" in {
        forAll(arbitrary[UserAnswers], arbitrary[UniqueTaxpayerReference], arbitrary[String], arbitrary[Name]) {
          (userAnswers, utr, businessName, soleTraderName) =>
            val getRandomBusinessTypeNoSoleTrader = Random.shuffle(businessTypesNoSoleTrader).head

            val answers = userAnswers
              .set(BusinessTypePage, getRandomBusinessTypeNoSoleTrader)
              .success
              .value
              .set(utrPage(getRandomBusinessTypeNoSoleTrader), utr)
              .success
              .value
              .set(BusinessNamePage, businessName)
              .success
              .value
              .set(SoleTraderNamePage, soleTraderName)
              .success
              .value

            when(mockRegistrationConnector.registerWithID(any())(any(), any()))
              .thenReturn(Future.successful(None))

            val result = businessMatchingService.sendBusinessMatchingInformation(answers)

            result.futureValue mustBe (None, None, None)
        }
      }
    }

    "when retrieveSafeId is called" - {
      "must return SafeID given a valid payload response" in {

        val payload = PayloadRegistrationWithIDResponse(
          RegisterWithIDResponse(
            ResponseCommon("", None, "", None),
            Some(
              ResponseDetail(
                "XE0001234567890",
                None,
                isEditable = false,
                isAnAgent = false,
                None,
                isAnIndividual = false,
                IndividualResponse("Bobby", None, "Bob", None),
                AddressResponse("1 TestStreet", Some("Test"), None, None, Some("AA11BB"), "GB"),
                ContactDetails(None, None, None, None)
              )
            )
          )
        )
        businessMatchingService.retrieveSafeID(Some(payload)) mustBe Some("XE0001234567890")
      }

      "must return None if ID can't be retrieved because of missing Response Detail" in {

        val payload = PayloadRegistrationWithIDResponse(
          RegisterWithIDResponse(
            ResponseCommon("", None, "", None),
            None
          )
        )
        businessMatchingService.retrieveSafeID(Some(payload)) mustBe None
      }

      "must return None if ID can't be retrieved" in {
        businessMatchingService.retrieveSafeID(None) mustBe None
      }
    }
  }
}
