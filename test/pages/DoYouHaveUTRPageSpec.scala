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

package pages

import java.time.LocalDate

import models.RegistrationType.Individual
import models.{Address, BusinessType, Country, Name, UniqueTaxpayerReference, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import uk.gov.hmrc.domain.Generator

class DoYouHaveUTRPageSpec extends PageBehaviours {

  "DoYouHaveUTRPage" - {

    beRetrievable[Boolean](DoYouHaveUTRPage)

    beSettable[Boolean](DoYouHaveUTRPage)

    beRemovable[Boolean](DoYouHaveUTRPage)

    "must remove possible answers from Individual journeys when user changes answer to 'Yes'" in {
      val address: Address = Address("", "", None, None, None, Country("", "", ""))

      forAll(arbitrary[UserAnswers]) {
        answers =>
          val result = answers
            .set(RegistrationTypePage, Individual)
            .success.value
            .set(DoYouHaveANationalInsuranceNumberPage, true)
            .success.value
            .set(NinoPage, new Generator().nextNino)
            .success.value
            .set(NamePage, Name("Fist", "Last"))
            .success.value
            .set(NonUkNamePage, Name("Fist", "Last"))
            .success.value
            .set(DateOfBirthPage, LocalDate.now())
            .success.value
            .set(DoYouLiveInTheUKPage, false)
            .success.value
            .set(IndividualUKPostcodePage, "ZZ1 1ZZ")
            .success.value
            .set(SelectAddressPage, "Some UK address")
            .success.value
            .set(WhatIsYourAddressPage, address)
            .success.value
            .set(WhatIsYourAddressUkPage, address)
            .success.value
            .set(DoYouHaveUTRPage, true)
            .success.value

          result.get(RegistrationTypePage) must not be defined
          result.get(DoYouHaveANationalInsuranceNumberPage) must not be defined
          result.get(NinoPage) must not be defined
          result.get(NamePage) must not be defined
          result.get(NonUkNamePage) must not be defined
          result.get(DateOfBirthPage) must not be defined
          result.get(DoYouLiveInTheUKPage) must not be defined
          result.get(IndividualUKPostcodePage) must not be defined
          result.get(SelectAddressPage) must not be defined
          result.get(WhatIsYourAddressPage) must not be defined
          result.get(WhatIsYourAddressUkPage) must not be defined
      }
    }

    "must remove Business without ID answer from Business without ID journey when user changes answer to 'Yes'" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val result = answers
            .set(BusinessWithoutIDNamePage, "Business Name")
            .success.value
            .set(DoYouHaveUTRPage, true)
            .success.value

          result.get(BusinessWithoutIDNamePage) must not be defined
      }
    }

    "must remove Business with ID answers when user changes answer to 'No'" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val result = answers
            .set(BusinessTypePage, BusinessType.CorporateBody)
            .success.value
            .set(CorporationTaxUTRPage, UniqueTaxpayerReference("0123456789"))
            .success.value
            .set(SelfAssessmentUTRPage, UniqueTaxpayerReference("0123456789"))
            .success.value
            .set(BusinessNamePage, "Business name")
            .success.value
            .set(ConfirmBusinessPage, true)
            .success.value
            .set(DoYouHaveUTRPage, false)
            .success.value

          result.get(BusinessTypePage) must not be defined
          result.get(CorporationTaxUTRPage) must not be defined
          result.get(SelfAssessmentUTRPage) must not be defined
          result.get(BusinessNamePage) must not be defined
          result.get(ConfirmBusinessPage) must not be defined
      }
    }
  }
}
