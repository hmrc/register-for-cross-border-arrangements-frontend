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

package helpers

import java.time.LocalDate

import base.SpecBase

class DateHelperSpec extends SpecBase {

  "DateHelper" - {

    "calling formatDateToString" - {

      "must return '5 June 1977' if given date 1977-6-5" in {

        val pastDate = LocalDate.of(1977, 6, 5)
        DateHelper.formatDateToString(pastDate) mustBe "5 June 1977"

      }

      "must return '29 September 2239' if given date 2239-9-29" in {

        val futureDate = LocalDate.of(2239, 9, 29)
        DateHelper.formatDateToString(futureDate) mustBe "29 September 2239"

      }
    }
  }
}
