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

package models

import base.SpecBase
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{ContactNamePage, DoYouHaveUTRPage}

class UserAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks {

  "updateUserAnswersIfValueChanged" - {

    "must leave UserAnswers unchanged if the value does not change" in {

      val gen1 = arbitrary[String]
      val gen2 = arbitrary[Boolean]

      forAll(gen1, gen2) {
        (nameString, boolVal) =>
          val userAnswers = UserAnswers(userAnswersId)
            .set(DoYouHaveUTRPage, boolVal)
            .success
            .value
            .set(ContactNamePage, nameString)
            .success
            .value

          val result = UserAnswersHelper.updateUserAnswersIfValueChanged(userAnswers, DoYouHaveUTRPage, boolVal)

          whenReady(result) {
            ua =>
              ua.equals(userAnswers) mustBe true
          }
      }
    }

    "must run an implemented cleanup if the value is changed" in {

      val gen  = arbitrary[String]
      val gen2 = arbitrary[Boolean]

      forAll(gen, gen2) {
        (nameString, boolVal) =>
          val userAnswers = UserAnswers(userAnswersId)
            .set(DoYouHaveUTRPage, boolVal)
            .success
            .value
            .set(ContactNamePage, nameString)
            .success
            .value

          val result = UserAnswersHelper.updateUserAnswersIfValueChanged(userAnswers, DoYouHaveUTRPage, !boolVal)

          whenReady(result) {
            ua =>
              ua.get(ContactNamePage) mustBe None
              ua.get(DoYouHaveUTRPage) mustBe Some(!boolVal)
          }
      }
    }
  }
}
