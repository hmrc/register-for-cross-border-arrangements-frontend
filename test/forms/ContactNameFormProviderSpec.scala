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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ContactNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKeyFirstName = "contactName.error.required.firstName"
  val lengthKeyFirstName = "contactName.error.length.firstName"
  val invalidKeyFirstName = "contactName.error.invalid.firstName"

  val requiredKeyLastName = "contactName.error.required.lastName"
  val lengthKeyLastName = "contactName.error.length.lastName"
  val invalidKeyLastName = "contactName.error.invalid.lastName"

  val validRegex = """^[a-zA-Z &`\\-\\'^]*$"""
  val maxLength = 35

  val form = new ContactNameFormProvider()()

  ".firstName" - {

    val fieldName = "firstName"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLengthAndInvalid(
      form,
      fieldName,
      maxLength = maxLength,
      invalidError = FormError(fieldName, invalidKeyFirstName, Seq(validRegex)),
      lengthError = FormError(fieldName, lengthKeyFirstName, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKeyFirstName)
    )
  }

  ".lastName" - {

    val fieldName = "lastName"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLengthAndInvalid(
      form,
      fieldName,
      maxLength = maxLength,
      invalidError = FormError(fieldName, invalidKeyLastName, Seq(validRegex)),
      lengthError = FormError(fieldName, lengthKeyLastName, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKeyLastName)
    )
  }
}
