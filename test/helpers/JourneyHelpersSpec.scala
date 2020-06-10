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
import helpers.JourneyHelpers._
import models.{BusinessType, RegistrationType, UserAnswers}
import pages.{BusinessTypePage, RegistrationTypePage}

class JourneyHelpersSpec extends SpecBase {

  "JourneyHelpers" - {

    "calling isOrganisationJourney" - {
      "must return true if it's an organisation (except sole trader)" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(BusinessTypePage, BusinessType.Partnership)
          .success
          .value

        val result = isOrganisationJourney(userAnswers)

        result mustBe true
      }

      "must return true if it's an organisation is registering" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(RegistrationTypePage, RegistrationType.Business)
          .success
          .value

        val result = isOrganisationJourney(userAnswers)

        result mustBe true
      }

      "must return false if business type is sole trader" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(BusinessTypePage, BusinessType.NotSpecified)
          .success
          .value

        val result = isOrganisationJourney(userAnswers)

        result mustBe false
      }

      "must return false if it's an individual is registering" in {
        val userAnswers = UserAnswers(userAnswersId)
          .set(RegistrationTypePage, RegistrationType.Individual)
          .success
          .value

        val result = isOrganisationJourney(userAnswers)

        result mustBe false
      }

      "must return false if it's an individual" in {
        val result = isOrganisationJourney(emptyUserAnswers)

        result mustBe false
      }

    }
  }

}
