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

package navigation

import base.SpecBase
import config.FrontendAppConfig
import controllers.routes
import generators.Generators
import models.BusinessType._
import models.RegistrationType.Individual
import models._
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class NavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val mockFrontendConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val navigator: Navigator                  = new Navigator(mockFrontendConfig)

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnknownPage, NormalMode, answers)
              .mustBe(routes.IndexController.onPageLoad())
        }
      }

      "must go from  Do you have a UK Unique Taxpayer Reference (UTR)? to What type of business do you have? when Yes is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveUTRPage, true)
                .success
                .value

            navigator
              .nextPage(DoYouHaveUTRPage, NormalMode, updatedAnswers)
              .mustBe(routes.BusinessTypeController.onPageLoad(NormalMode))
        }
      }

      "must go from  Do you have a UK Unique Taxpayer Reference (UTR)? to What are you registering as? when No is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveUTRPage, false)
                .success
                .value

            navigator
              .nextPage(DoYouHaveUTRPage, NormalMode, updatedAnswers)
              .mustBe(routes.RegistrationTypeController.onPageLoad(NormalMode))
        }
      }

      "must go from What are you registering as? to Do you have a National Insurance number? when Individual is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(RegistrationTypePage, Individual)
                .success
                .value

            navigator
              .nextPage(RegistrationTypePage, NormalMode, updatedAnswers)
              .mustBe(routes.DoYouHaveANationalInsuranceNumberController.onPageLoad(NormalMode))
        }
      }

      "must go from Do you have a national insurance number to Nino page when I respond yes" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveANationalInsuranceNumberPage, true)
                .success
                .value

            navigator
              .nextPage(DoYouHaveANationalInsuranceNumberPage, NormalMode, updatedAnswers)
              .mustBe(routes.NinoController.onPageLoad(NormalMode))
        }
      }

      "must go from Nino page to Name page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(NinoPage, NormalMode, answers)
              .mustBe(routes.NameController.onPageLoad(NormalMode))
        }
      }

      "must go from Name page to DOB page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(NamePage, NormalMode, answers)
              .mustBe(routes.DateOfBirthController.onPageLoad(NormalMode))
        }
      }

      "must go from Business Type Page to Corporation Tax UTR when I selected CorporateBody" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessTypePage, BusinessType.CorporateBody)
                .success
                .value

            navigator
              .nextPage(BusinessTypePage, NormalMode, updatedAnswers)
              .mustBe(routes.CorporationTaxUTRController.onPageLoad(NormalMode))
        }
      }

      "must go from Business Type Page to Corporation Tax UTR when I selected UnIncorporatedBody" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessTypePage, BusinessType.UnIncorporatedBody)
                .success
                .value

            navigator
              .nextPage(BusinessTypePage, NormalMode, updatedAnswers)
              .mustBe(routes.CorporationTaxUTRController.onPageLoad(NormalMode))
        }
      }

      "must go from Business Type Page to Self Assessment when I selected Partnership" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessTypePage, BusinessType.Partnership)
                .success
                .value

            navigator
              .nextPage(BusinessTypePage, NormalMode, updatedAnswers)
              .mustBe(routes.SelfAssessmentUTRController.onPageLoad(NormalMode))
        }
      }

      "must go from Business Type Page to Self Assessment when I selected NotSpecified" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessTypePage, BusinessType.NotSpecified)
                .success
                .value

            navigator
              .nextPage(BusinessTypePage, NormalMode, updatedAnswers)
              .mustBe(routes.SelfAssessmentUTRController.onPageLoad(NormalMode))
        }
      }

      "must go from Business Type Page to Self Assessment when I selected LimitedLiability" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessTypePage, BusinessType.LimitedLiability)
                .success
                .value

            navigator
              .nextPage(BusinessTypePage, NormalMode, updatedAnswers)
              .mustBe(routes.SelfAssessmentUTRController.onPageLoad(NormalMode))
        }
      }

      "must go from businessAddress page to Check your answers page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(BusinessAddressPage, NormalMode, answers)
              .mustBe(routes.ContactNameController.onPageLoad(NormalMode))
        }
      }

      "must go from DOB page for a sole trader to the business matching check" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(SoleTraderNamePage, Name("Bobby", "Bobson"))
                .success
                .value

            navigator
              .nextPage(DateOfBirthPage, NormalMode, updatedAnswers)
              .mustBe(routes.BusinessMatchingController.matchBusiness())
        }
      }

      "must go from DOB page for an Individual with ID to the business matching check" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveANationalInsuranceNumberPage, true)
                .success
                .value
                .remove(SoleTraderNamePage)
                .success
                .value

            navigator
              .nextPage(DateOfBirthPage, NormalMode, updatedAnswers)
              .mustBe(routes.BusinessMatchingController.matchIndividual(NormalMode))
        }
      }

      "must go from DOB page for an Individual without ID to the do you live the UK" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveANationalInsuranceNumberPage, false)
                .success
                .value
                .remove(SoleTraderNamePage)
                .success
                .value

            navigator
              .nextPage(DateOfBirthPage, NormalMode, updatedAnswers)
              .mustBe(routes.DoYouLiveInTheUKController.onPageLoad(NormalMode))
        }
      }

      "must go from unique tax reference to business name page when not unspecified" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessTypePage, BusinessType.NotSpecified)
                .success
                .value

            navigator
              .nextPage(SelfAssessmentUTRPage, NormalMode, updatedAnswers)
              .mustBe(routes.SoleTraderNameController.onPageLoad(NormalMode))
        }
      }

      "must go from unique tax reference to organisation business name page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessTypePage, UnIncorporatedBody)
                .success
                .value

            navigator
              .nextPage(SelfAssessmentUTRPage, NormalMode, updatedAnswers)
              .mustBe(routes.BusinessNameOrganisationController.onPageLoad(NormalMode))
        }
      }

      "as a limited liability business must go from unique tax reference to registered business name page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessTypePage, LimitedLiability)
                .success
                .value

            navigator
              .nextPage(SelfAssessmentUTRPage, NormalMode, updatedAnswers)
              .mustBe(routes.BusinessNameRegisteredBusinessController.onPageLoad(NormalMode))
        }
      }

      "as a corporate body business must go from unique tax reference to registered business name page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessTypePage, BusinessType.CorporateBody)
                .success
                .value

            navigator
              .nextPage(SelfAssessmentUTRPage, NormalMode, updatedAnswers)
              .mustBe(routes.BusinessNameRegisteredBusinessController.onPageLoad(NormalMode))
        }
      }

      "as a partnership must go from unique tax reference to partnership name page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessTypePage, BusinessType.Partnership)
                .success
                .value

            navigator
              .nextPage(SelfAssessmentUTRPage, NormalMode, updatedAnswers)
              .mustBe(routes.BusinessNamePartnershipController.onPageLoad(NormalMode))
        }
      }

      "must go from business name page to making the business match check" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(BusinessNamePage, NormalMode, answers)
              .mustBe(routes.BusinessMatchingController.matchBusiness())
        }
      }

      "must go from sole proprietor name page to individual match check" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(SoleTraderNamePage, NormalMode, answers)
              .mustBe(routes.DateOfBirthController.onPageLoad(NormalMode))
        }
      }

      "must go from Do you have a national insurance number to Non Uk Name page when I respond No" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouHaveANationalInsuranceNumberPage, false)
                .success
                .value

            navigator
              .nextPage(DoYouHaveANationalInsuranceNumberPage, NormalMode, updatedAnswers)
              .mustBe(routes.NonUkNameController.onPageLoad(NormalMode))
        }
      }

      "must go from Non Uk Name page to Date of Birth Page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(NonUkNamePage, NormalMode, answers)
              .mustBe(routes.DateOfBirthController.onPageLoad(NormalMode))
        }
      }

      "must go from the Do You Live in the UK page for people who answer yes to What is your postcode? - address lookup toggle is true" in {

        when(mockFrontendConfig.addressLookupToggle).thenReturn(true)

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouLiveInTheUKPage, true)
                .success
                .value

            navigator
              .nextPage(DoYouLiveInTheUKPage, NormalMode, updatedAnswers)
              .mustBe(routes.IndividualUKPostcodeController.onPageLoad(NormalMode))
        }
      }

      "must go from the Do You Live in the UK page for people who answer yes to What is your address? - address lookup toggle is false" in {

        when(mockFrontendConfig.addressLookupToggle).thenReturn(false)

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouLiveInTheUKPage, true)
                .success
                .value

            navigator
              .nextPage(DoYouLiveInTheUKPage, NormalMode, updatedAnswers)
              .mustBe(routes.WhatIsYourAddressUkController.onPageLoad(NormalMode))
        }
      }

      "must go from the Do You Live in the UK page for people who do to the What is your address page (non-UK)" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(DoYouLiveInTheUKPage, false)
                .success
                .value

            navigator
              .nextPage(DoYouLiveInTheUKPage, NormalMode, updatedAnswers)
              .mustBe(routes.WhatIsYourAddressController.onPageLoad(NormalMode))
        }
      }

      "must go from the What is your postcode? page to What is your address? page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(IndividualUKPostcodePage, "AA1 1AA")
                .success
                .value

            navigator
              .nextPage(IndividualUKPostcodePage, NormalMode, updatedAnswers)
              .mustBe(routes.SelectAddressController.onPageLoad(NormalMode))
        }
      }

      "must go from the What is your address? page to What is your email address? page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(SelectAddressPage, "Some UK address")
                .success
                .value

            navigator
              .nextPage(SelectAddressPage, NormalMode, updatedAnswers)
              .mustBe(routes.ContactEmailAddressController.onPageLoad(NormalMode))
        }
      }

      "must go from the What is your business name page (without-id) to Does your business trade under a different name? page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(BusinessWithoutIDNamePage, "Business name")
                .success
                .value

            navigator
              .nextPage(BusinessWithoutIDNamePage, NormalMode, updatedAnswers)
              .mustBe(routes.DoYouHaveBusinessTradingNameController.onPageLoad(NormalMode))
        }
      }

      "must go from 'Does your business trade under a different name?' page to 'What is the trading name of your business?' page" +
        "when YES is selected" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(DoYouHaveBusinessTradingNamePage, true)
                  .success
                  .value

              navigator
                .nextPage(DoYouHaveBusinessTradingNamePage, NormalMode, updatedAnswers)
                .mustBe(routes.BusinessTradingNameController.onPageLoad(NormalMode))
          }
        }

      "must go from 'Does your business trade under a different name?' page to 'What is your business address?' page" +
        "when NO is selected" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(DoYouHaveBusinessTradingNamePage, false)
                  .success
                  .value

              navigator
                .nextPage(DoYouHaveBusinessTradingNamePage, NormalMode, updatedAnswers)
                .mustBe(routes.BusinessAddressController.onPageLoad(NormalMode))
          }
        }

      "must go from what is your address uk page to enter your email page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(WhatIsYourAddressUkPage, NormalMode, answers)
              .mustBe(routes.ContactEmailAddressController.onPageLoad(NormalMode))
        }
      }

      "must go from What is your home address (non-UK) page to What is your email address page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(WhatIsYourAddressPage, NormalMode, answers)
              .mustBe(routes.ContactEmailAddressController.onPageLoad(NormalMode))
        }
      }

      "must go from the Who should we contact if we have any questions about your disclosures page" +
        " to the What is your email address page" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(ContactNamePage, "name")
                  .success
                  .value

              navigator
                .nextPage(ContactNamePage, NormalMode, updatedAnswers)
                .mustBe(routes.ContactEmailAddressController.onPageLoad(NormalMode))
          }
        }

      "must go from the What is your email address? page to Do you have a telephone number? page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(ContactEmailAddressPage, "example@test.com")
                .success
                .value

            navigator
              .nextPage(ContactEmailAddressPage, NormalMode, updatedAnswers)
              .mustBe(routes.TelephoneNumberQuestionController.onPageLoad(NormalMode))
        }
      }

      "must go from the What is your telephone number page? to the Is there someone else we can contact? page" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(RegistrationTypePage, RegistrationType.Business)
          .success
          .value
          .set(ContactTelephoneNumberPage, "07540000000")
          .success
          .value

        navigator
          .nextPage(ContactTelephoneNumberPage, NormalMode, userAnswers)
          .mustBe(routes.HaveSecondContactController.onPageLoad(NormalMode))
      }

      "must go from the What is your telephone number page? to Check Your Answers page is registration type is Individual" in {

        val userAnswers = UserAnswers(userAnswersId)
          .set(RegistrationTypePage, RegistrationType.Individual)
          .success
          .value
          .set(ContactTelephoneNumberPage, "07540000000")
          .success
          .value

        navigator
          .nextPage(ContactTelephoneNumberPage, NormalMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from the What is your telephone number page? to Check Your Answers page if business type is Sole trader" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(BusinessTypePage, BusinessType.NotSpecified)
          .success
          .value
          .set(ContactTelephoneNumberPage, "07540000000")
          .success
          .value

        navigator
          .nextPage(ContactTelephoneNumberPage, NormalMode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from the Do you have telephone page to the What is the telephone number page when the answer is 'Yes'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers =
              answers
                .set(TelephoneNumberQuestionPage, true)
                .success
                .value

            navigator
              .nextPage(TelephoneNumberQuestionPage, NormalMode, updatedAnswers)
              .mustBe(routes.ContactTelephoneNumberController.onPageLoad(NormalMode))
        }
      }

      "must go from the Do you have telephone page to Have second contact page when the answer is 'No' and " +
        "the user is an Organisation" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(BusinessTypePage, BusinessType.CorporateBody)
                  .success
                  .value
                  .set(TelephoneNumberQuestionPage, false)
                  .success
                  .value

              navigator
                .nextPage(TelephoneNumberQuestionPage, NormalMode, updatedAnswers)
                .mustBe(routes.HaveSecondContactController.onPageLoad(NormalMode))
          }
        }

      "must go from the Do you have telephone page to the Check answers page when the answer is 'No' and " +
        "the business type is Sole proprietor" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(BusinessTypePage, BusinessType.NotSpecified)
                  .success
                  .value
                  .set(TelephoneNumberQuestionPage, false)
                  .success
                  .value

              navigator
                .nextPage(TelephoneNumberQuestionPage, NormalMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad())
          }
        }

      "must go from the Do you have telephone page to the Check answers page when the answer is 'No' and " +
        "the user is an Individual" in {

          val userAnswers = UserAnswers(userAnswersId).set(TelephoneNumberQuestionPage, false).success.value

          navigator
            .nextPage(TelephoneNumberQuestionPage, NormalMode, userAnswers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }

      "must go from Is there someone else we can contact if *name* is not available??" +
        "to What is the name of the individual or team we should contact? when Yes is selected" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(HaveSecondContactPage, true)
                  .success
                  .value

              navigator
                .nextPage(HaveSecondContactPage, NormalMode, updatedAnswers)
                .mustBe(routes.SecondaryContactNameController.onPageLoad(NormalMode))
          }
        }

      "must go from Is there someone else we can contact if *name* is not available??" +
        "to Check your answers? when No is selected" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(HaveSecondContactPage, false)
                  .success
                  .value

              navigator
                .nextPage(HaveSecondContactPage, NormalMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad())
          }
        }

      "must go from the What is the name of the individual or team we should contact? page " +
        "to what is the email address for *name*? page" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(SecondaryContactNamePage, "DAC6 Team")
                  .success
                  .value

              navigator
                .nextPage(SecondaryContactNamePage, NormalMode, updatedAnswers)
                .mustBe(routes.SecondaryContactEmailAddressController.onPageLoad(NormalMode))
          }
        }

      "must go from the What is the email address for *name*? page " +
        "to does *name*? have a telephone number page" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(SecondaryContactNamePage, "DAC6 Team")
                  .success
                  .value

              navigator
                .nextPage(SecondaryContactEmailAddressPage, NormalMode, updatedAnswers)
                .mustBe(routes.SecondaryContactTelephoneQuestionController.onPageLoad(NormalMode))
          }
        }

      "must go from the does *name*? have a telephone number page " +
        "to what is the telephone number for *name*? page when option 'yes' is selected" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(SecondaryContactNamePage, "DAC6 Team")
                  .success
                  .value
                  .set(SecondaryContactTelephoneQuestionPage, true)
                  .success
                  .value

              navigator
                .nextPage(SecondaryContactTelephoneQuestionPage, NormalMode, updatedAnswers)
                .mustBe(routes.SecondaryContactTelephoneNumberController.onPageLoad(NormalMode))
          }
        }

      "must go from the does *name*? have a telephone number page " +
        "to Check your answers page when option 'no' is selected" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(SecondaryContactNamePage, "DAC6 Team")
                  .success
                  .value
                  .set(SecondaryContactTelephoneQuestionPage, false)
                  .success
                  .value

              navigator
                .nextPage(SecondaryContactTelephoneQuestionPage, NormalMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad())
          }
        }

      "must go from the What is the telephone number for *name* contact page " +
        "to Check your answers page" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(SecondaryContactTelephoneNumberPage, "07000000000")
                  .success
                  .value

              navigator
                .nextPage(SecondaryContactTelephoneNumberPage, NormalMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad())
          }
        }

      "must got from Is this your business? page to" - {
        "Contact email address page when answer is 'Yes' is business type not specified" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(ConfirmBusinessPage, true)
                  .success
                  .value
                  .set(BusinessTypePage, BusinessType.NotSpecified)
                  .success
                  .value

              navigator
                .nextPage(ConfirmBusinessPage, NormalMode, updatedAnswers)
                .mustBe(routes.ContactEmailAddressController.onPageLoad(NormalMode))
          }
        }

        "Contact name page when answer is 'Yes' and business type is specified" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(ConfirmBusinessPage, true)
                  .success
                  .value
                  .set(BusinessTypePage, BusinessType.LimitedLiability)
                  .success
                  .value

              navigator
                .nextPage(ConfirmBusinessPage, NormalMode, updatedAnswers)
                .mustBe(routes.ContactNameController.onPageLoad(NormalMode))
          }
        }

        "Identity not confirmed when answer is 'No'" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(ConfirmBusinessPage, false)
                  .success
                  .value

              navigator
                .nextPage(ConfirmBusinessPage, NormalMode, updatedAnswers)
                .mustBe(routes.BusinessNotConfirmedController.onPageLoad())
          }
        }
      }
    }
  }
}
