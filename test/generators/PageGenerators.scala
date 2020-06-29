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

package generators

import java.time.LocalDate

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {
  self: Generators =>

  implicit lazy val arbitrarySecondaryContactEmailAddressPage: Arbitrary[SecondaryContactEmailAddressPage.type] =
    Arbitrary(SecondaryContactEmailAddressPage)

  implicit lazy val arbitrarySecondaryContactTelephoneNumberPage: Arbitrary[SecondaryContactTelephoneNumberPage.type] =
    Arbitrary(SecondaryContactTelephoneNumberPage)

  implicit lazy val arbitraryWhatIsYourAddressUkPage: Arbitrary[WhatIsYourAddressUkPage.type] =
    Arbitrary(WhatIsYourAddressUkPage)

  implicit lazy val arbitraryContactTelephoneNumberPage: Arbitrary[ContactTelephoneNumberPage.type] =
    Arbitrary(ContactTelephoneNumberPage)

  implicit lazy val arbitraryContactEmailAddressPage: Arbitrary[ContactEmailAddressPage.type] =
    Arbitrary(ContactEmailAddressPage)

  implicit lazy val arbitraryBusinessAddressPage: Arbitrary[BusinessAddressPage.type] =
    Arbitrary(BusinessAddressPage)

  implicit lazy val arbitraryBusinessNamePage: Arbitrary[BusinessNamePage.type] =
    Arbitrary(BusinessNamePage)

  implicit lazy val arbitraryConfirmBusinessPage: Arbitrary[ConfirmBusinessPage.type] =
    Arbitrary(ConfirmBusinessPage)

  implicit lazy val arbitraryWhatIsYourAddressPage: Arbitrary[WhatIsYourAddressPage.type] =
      Arbitrary(WhatIsYourAddressPage)

  implicit lazy val arbitraryIndividualUKPostcodePage: Arbitrary[IndividualUKPostcodePage.type] =
    Arbitrary(IndividualUKPostcodePage)

  implicit lazy val arbitraryDoYouLiveInTheUKPage: Arbitrary[DoYouLiveInTheUKPage.type] =
      Arbitrary(DoYouLiveInTheUKPage)

  implicit lazy val arbitraryNonUkNamePage: Arbitrary[NonUkNamePage.type] =
      Arbitrary(NonUkNamePage)

  implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
    datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
  }

  implicit lazy val arbitraryDateOfBirthPage: Arbitrary[DateOfBirthPage.type] =
    Arbitrary(DateOfBirthPage)
  
  implicit lazy val arbitraryDoYouHaveUTRPage: Arbitrary[DoYouHaveUTRPage.type] =
    Arbitrary(DoYouHaveUTRPage)

  implicit lazy val arbitraryRegistrationTypePage: Arbitrary[RegistrationTypePage.type] =
    Arbitrary(RegistrationTypePage)

  implicit lazy val arbitraryNamePagePage: Arbitrary[NamePage.type] =
    Arbitrary(NamePage)

  implicit lazy val arbitraryNinoPage: Arbitrary[NinoPage.type] =
    Arbitrary(NinoPage)

  implicit lazy val arbitraryDoYouHaveANationalInsuranceNumberPage: Arbitrary[DoYouHaveANationalInsuranceNumberPage.type] =
    Arbitrary(DoYouHaveANationalInsuranceNumberPage)

  implicit lazy val arbitraryPostCodePage: Arbitrary[PostCodePage.type] =
    Arbitrary(PostCodePage)

  implicit lazy val arbitraryCorporationTaxUTRPage: Arbitrary[CorporationTaxUTRPage.type] =
    Arbitrary(CorporationTaxUTRPage)

  implicit lazy val arbitrarySelfAssessmentUTRPage: Arbitrary[SelfAssessmentUTRPage.type] =
    Arbitrary(SelfAssessmentUTRPage)

  implicit lazy val arbitraryBusinessTypePage: Arbitrary[BusinessTypePage.type] =
    Arbitrary(BusinessTypePage)

  implicit lazy val arbitraryBusinessWithoutIDNamePage: Arbitrary[BusinessWithoutIDNamePage.type] =
    Arbitrary(BusinessWithoutIDNamePage)

  implicit lazy val arbitraryContactNamePage: Arbitrary[ContactNamePage.type] =
    Arbitrary(ContactNamePage)

  implicit lazy val arbitrarySoleTraderNamePage: Arbitrary[SoleTraderNamePage.type] =
    Arbitrary(SoleTraderNamePage)

  implicit lazy val arbitraryTelephoneNumberQuestionPage: Arbitrary[TelephoneNumberQuestionPage.type] =
    Arbitrary(TelephoneNumberQuestionPage)

  implicit lazy val arbitraryHaveSecondContactPage: Arbitrary[HaveSecondContactPage.type] =
    Arbitrary(HaveSecondContactPage)

  implicit lazy val arbitrarySecondaryContactNamePage: Arbitrary[SecondaryContactNamePage.type] =
    Arbitrary(SecondaryContactNamePage)

  implicit lazy val arbitrarySecondaryContactPreferencePage: Arbitrary[SecondaryContactPreferencePage.type] =
    Arbitrary(SecondaryContactPreferencePage)

  implicit lazy val arbitrarySelectAddressPage: Arbitrary[SelectAddressPage.type] =
    Arbitrary(SelectAddressPage)

}
