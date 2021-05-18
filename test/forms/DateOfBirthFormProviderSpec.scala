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

package forms

import forms.behaviours.DateBehaviours
import helpers.DateHelper
import helpers.DateHelper._
import play.api.data.FormError

import java.time.{LocalDate, ZoneId, ZonedDateTime}

class DateOfBirthFormProviderSpec extends DateBehaviours {

  val form = new DateOfBirthFormProvider()()

  ".value" - {

    val fieldName = "value"

    val validData = datesBetween(
      min = LocalDate.of(1909, 1, 1),
      max = ZonedDateTime.now(ZoneId.of("Europe/London")).toLocalDate
    )

    behave like dateField(form, "value", validData)

    behave like dateFieldWithMax(
      form = form,
      key = fieldName,
      max = today,
      formError = FormError(
        fieldName, "dateOfBirth.error.futureDate", Seq(DateHelper.formatDateToString(today))
      )
    )

    behave like dateFieldWithMin(
      form = form,
      key = fieldName,
      min = LocalDate.of(1900,1,1),
      formError = FormError(
        fieldName, "dateOfBirth.error.pastDate"
      )
    )

    behave like mandatoryDateField(form, "value", "dateOfBirth.error.required.all")
  }
}
