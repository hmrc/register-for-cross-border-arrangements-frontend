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

package helpers

import base.SpecBase
import models.{BusinessType, UserAnswers}
import pages.BusinessTypePage

class JourneyHelpersSpec extends SpecBase {

  val journeyHelpers: JourneyHelpers = new JourneyHelpers

  "JourneyHelpers" - {

    "calling organisationJourney" - {
      "must return true if it's an organisation (except sole trader)" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(BusinessTypePage, BusinessType.Partnership)
          .success
          .value

        val result = journeyHelpers.organisationJourney(userAnswers)

        result mustBe true
      }

      "must return false if business type is sole trader" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(BusinessTypePage, BusinessType.NotSpecified)
          .success
          .value

        val result = journeyHelpers.organisationJourney(userAnswers)

        result mustBe false
      }

      "must return false if it's an individual" in {
        val result = journeyHelpers.organisationJourney(emptyUserAnswers)

        result mustBe false
      }

    }
  }

}