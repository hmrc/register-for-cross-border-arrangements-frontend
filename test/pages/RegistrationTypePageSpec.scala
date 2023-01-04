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

package pages

import models.RegistrationType.{Business, Individual}
import models.{Address, AddressLookup, Country, Name, RegistrationType, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import uk.gov.hmrc.domain.Generator

import java.time.LocalDate

class RegistrationTypePageSpec extends PageBehaviours {

  val address: Address         = Address("", None, "", None, None, Country("", "", ""))
  val addlookup: AddressLookup = AddressLookup(None, None, None, None, "town", None, "pcode")

  val name: Name = Name("FirstName", "LastName")

  "RegistrationTypePage" - {

    beRetrievable[RegistrationType](RegistrationTypePage)

    beSettable[RegistrationType](RegistrationTypePage)

    beRemovable[RegistrationType](RegistrationTypePage)

    "must remove all possible individual details when user changes answer to 'A business'" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val result = answers
            .set(DoYouHaveANationalInsuranceNumberPage, true)
            .success
            .value
            .set(NinoPage, new Generator().nextNino)
            .success
            .value
            .set(NamePage, name)
            .success
            .value
            .set(DateOfBirthPage, LocalDate.now())
            .success
            .value
            .set(NonUkNamePage, name)
            .success
            .value
            .set(DoYouLiveInTheUKPage, false)
            .success
            .value
            .set(WhatIsYourAddressUkPage, address)
            .success
            .value
            .set(WhatIsYourAddressPage, address)
            .success
            .value
            .set(SelectAddressPage, "add")
            .success
            .value
            .set(PostCodePage, "ZZ1Z 7AB")
            .success
            .value
            .set(SelectedAddressLookupPage, addlookup)
            .success
            .value
            .set(IndividualUKPostcodePage, "pc")
            .success
            .value
            .set(ContactEmailAddressPage, "test@test.com")
            .success
            .value
            .set(TelephoneNumberQuestionPage, true)
            .success
            .value
            .set(ContactTelephoneNumberPage, "99")
            .success
            .value
            .set(RegistrationTypePage, Business)
            .success
            .value

          result.get(DoYouHaveANationalInsuranceNumberPage) mustBe None
          result.get(NinoPage) mustBe None
          result.get(NamePage) mustBe None
          result.get(DateOfBirthPage) mustBe None
          result.get(NonUkNamePage) mustBe None
          result.get(DoYouLiveInTheUKPage) mustBe None
          result.get(WhatIsYourAddressUkPage) mustBe None
          result.get(WhatIsYourAddressPage) mustBe None
          result.get(SelectAddressPage) mustBe None
          result.get(PostCodePage) mustBe None
          result.get(SelectedAddressLookupPage) mustBe None
          result.get(IndividualUKPostcodePage) mustBe None
          result.get(ContactEmailAddressPage) mustBe None
          result.get(TelephoneNumberQuestionPage) mustBe None
          result.get(ContactTelephoneNumberPage) mustBe None

      }
    }

    "must remove all possible business details when user changes answer to 'An individual' and they have a second contact" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val result = answers
            .set(BusinessWithoutIDNamePage, "Business name")
            .success
            .value
            .set(BusinessAddressPage, address)
            .success
            .value
            .set(ContactEmailAddressPage, "test@test.com")
            .success
            .value
            .set(TelephoneNumberQuestionPage, true)
            .success
            .value
            .set(ContactTelephoneNumberPage, "99")
            .success
            .value
            .set(HaveSecondContactPage, true)
            .success
            .value
            .set(SecondaryContactNamePage, "xx")
            .success
            .value
            .set(SecondaryContactEmailAddressPage, "test@test.com")
            .success
            .value
            .set(SecondaryContactTelephoneQuestionPage, true)
            .success
            .value
            .set(SecondaryContactTelephoneNumberPage, "99")
            .success
            .value
            .set(RegistrationTypePage, Individual)
            .success
            .value

          result.get(BusinessWithoutIDNamePage) mustBe None
          result.get(BusinessAddressPage) mustBe None
          result.get(ContactEmailAddressPage) mustBe None
          result.get(TelephoneNumberQuestionPage) mustBe None
          result.get(ContactTelephoneNumberPage) mustBe None
          result.get(HaveSecondContactPage) mustBe None
          result.get(SecondaryContactNamePage) mustBe None
          result.get(SecondaryContactEmailAddressPage) mustBe None
          result.get(SecondaryContactTelephoneQuestionPage) mustBe None
          result.get(SecondaryContactTelephoneNumberPage) mustBe None
      }
    }
  }
}
