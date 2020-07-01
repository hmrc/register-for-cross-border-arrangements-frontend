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

import models.{Address, Country, UserAnswers}
import pages.behaviours.PageBehaviours
import org.scalacheck.Arbitrary.arbitrary


class DoYouLiveInTheUKPageSpec extends PageBehaviours {

  val address: Address = Address("", "", None, None, None, Country("", "", ""))

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

          result.get(WhatIsYourAddressPage) must not be defined
      }
    }

    "must remove non-UK address when user changes answer to 'No'" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val result = answers
            .set(SelectAddressPage, "Some UK address")
            .success
            .value
            .set(WhatIsYourAddressUkPage, address)
            .success
            .value
            .set(DoYouLiveInTheUKPage, false)
            .success
            .value

          result.get(SelectAddressPage) must not be defined
          result.get(WhatIsYourAddressUkPage) must not be defined
      }
    }
  }
}
