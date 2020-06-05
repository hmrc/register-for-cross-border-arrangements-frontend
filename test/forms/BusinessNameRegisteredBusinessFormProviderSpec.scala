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

class BusinessNameRegisteredBusinessFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "businessName.registered.error.required"
  val lengthKey = "businessName.registered.error.length"
  val invalidKey = "businessName.registered.error.invalid"
  val businessNameRegex = "^[a-zA-Z0-9 '&\\/]{1,105}$"
  val maxLength = 105

  val form = new BusinessNameRegisteredBusinessFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLengthAndInvalid(
      form,
      fieldName,
      maxLength = maxLength,
      invalidError = FormError(fieldName, invalidKey, Seq(businessNameRegex)),
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like fieldWithNonEmptyWhitespace(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
