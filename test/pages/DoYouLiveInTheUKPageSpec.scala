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

import models.{Address, Country, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class DoYouLiveInTheUKPageSpec extends PageBehaviours {

  val address: Address = Address("", None, "", None, None, Country("", "", ""))

  "DoYouLiveInTheUKPage" - {

    beRetrievable[Boolean](DoYouLiveInTheUKPage)

    beSettable[Boolean](DoYouLiveInTheUKPage)

    beRemovable[Boolean](DoYouLiveInTheUKPage)

    "must remove UK address when user changes answer to 'Yes'" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val result = answers
            .set(WhatIsYourAddressPage, address)
            .success
            .value
            .set(DoYouLiveInTheUKPage, true)
            .success
            .value

          result.get(WhatIsYourAddressPage) mustBe None
      }
    }

    "must remove non-UK address answers when user changes answer to 'No'" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val result = answers
            .set(IndividualUKPostcodePage, "AA1 1AA")
            .success
            .value
            .set(SelectAddressPage, "Some UK address")
            .success
            .value
            .set(WhatIsYourAddressUkPage, address)
            .success
            .value
            .set(DoYouLiveInTheUKPage, false)
            .success
            .value

          result.get(IndividualUKPostcodePage) mustBe None
          result.get(SelectAddressPage) mustBe None
          result.get(WhatIsYourAddressUkPage) mustBe None
      }
    }
  }
}
