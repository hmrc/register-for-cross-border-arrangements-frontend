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

package pages

import java.time.LocalDate

import models.{Name, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import uk.gov.hmrc.domain.Generator

class DoYouHaveANationalInsuranceNumberPageSpec extends PageBehaviours {

  "DoYouHaveANationalInsuranceNumberPage" - {

    beRetrievable[Boolean](DoYouHaveANationalInsuranceNumberPage)

    beSettable[Boolean](DoYouHaveANationalInsuranceNumberPage)

    beRemovable[Boolean](DoYouHaveANationalInsuranceNumberPage)

    "must remove all answers from no-nino journey when user changes answer to 'Yes'" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val result = answers
            .set(NonUkNamePage, Name("FirstName", "LastName"))
            .success
            .value
            .set(DoYouLiveInTheUKPage, true)
            .success
            .value
            .set(IndividualUKPostcodePage, "AA1 1AA")
            .success
            .value
            .set(SelectAddressPage, "Some UK address")
            .success
            .value
            .set(DateOfBirthPage, LocalDate.now())
            .success
            .value
            .set(DoYouHaveANationalInsuranceNumberPage, true)
            .success
            .value

          result.get(NonUkNamePage) must not be defined
          result.get(DoYouLiveInTheUKPage) must not be defined
          result.get(IndividualUKPostcodePage) must not be defined
          result.get(SelectAddressPage) must not be defined
          result.get(DateOfBirthPage) mustBe defined
      }
    }

    "must remove all answers from nino journey when user changes answer to 'No'" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val result = answers
            .set(NinoPage, new Generator().nextNino)
            .success
            .value
            .set(NamePage, Name("FirstName", "LastName"))
            .success
            .value
            .set(DateOfBirthPage, LocalDate.now())
            .success
            .value
            .set(DoYouHaveANationalInsuranceNumberPage, false)
            .success
            .value

          result.get(NinoPage) must not be defined
          result.get(NamePage) must not be defined
          result.get(DateOfBirthPage) mustBe defined
      }
    }
  }
}
