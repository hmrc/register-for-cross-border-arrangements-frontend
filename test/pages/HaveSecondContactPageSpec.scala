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

package pages

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class HaveSecondContactPageSpec extends PageBehaviours {

  "HaveSecondContactPage" - {

    beRetrievable[Boolean](HaveSecondContactPage)

    beSettable[Boolean](HaveSecondContactPage)

    beRemovable[Boolean](HaveSecondContactPage)

    "must remove secondary contact details when user changes answer to 'No'" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val result = answers
            .set(SecondaryContactNamePage, "Contact name")
            .success
            .value
            .set(SecondaryContactTelephoneQuestionPage, true)
            .success
            .value
            .set(SecondaryContactEmailAddressPage, "email@email.com")
            .success
            .value
            .set(SecondaryContactTelephoneNumberPage, "07888888888")
            .success
            .value
            .set(HaveSecondContactPage, false)
            .success
            .value

          result.get(SecondaryContactNamePage) must not be defined
          result.get(SecondaryContactTelephoneQuestionPage) must not be defined
          result.get(SecondaryContactEmailAddressPage) must not be defined
          result.get(SecondaryContactTelephoneNumberPage) must not be defined
      }
    }
  }
}
