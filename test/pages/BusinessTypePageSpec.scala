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

import models.{BusinessType, Name, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class BusinessTypePageSpec extends PageBehaviours {

  "BusinessTypePage" - {

    beRetrievable[BusinessType](BusinessTypePage)

    beSettable[BusinessType](BusinessTypePage)

    beRemovable[BusinessType](BusinessTypePage)

    "must remove business name when user changes answer to 'Sole proprietor'" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val result = answers
            .set(BusinessNamePage, "Business name")
            .success
            .value
            .set(BusinessTypePage, BusinessType.NotSpecified)
            .success
            .value

          result.get(BusinessNamePage) must not be defined
      }
    }

    "must remove sole trader name when user answer is not 'Sole proprietor'" in {
      forAll(arbitrary[UserAnswers]) {
        answers =>
          val result = answers
            .set(SoleTraderNamePage, Name("FirstName", "LastName"))
            .success
            .value
            .set(BusinessTypePage, BusinessType.CorporateBody)
            .success
            .value

          result.get(SoleTraderNamePage) must not be defined
      }
    }
  }
}
