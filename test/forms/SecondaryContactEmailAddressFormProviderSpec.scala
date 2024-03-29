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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class SecondaryContactEmailAddressFormProviderSpec extends StringFieldBehaviours {

  val form = new SecondaryContactEmailAddressFormProvider()()

  val requiredKey = "secondaryContactEmailAddress.error.required"
  val lengthKey   = "secondaryContactEmailAddress.error.length"
  val invalidKey  = "secondaryContactEmailAddress.error.email.invalid"
  val maxLength   = 254

  ".email" - {

    val fieldName = "email"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validEmailAddress
    )

    behave like fieldWithMaxLengthEmail(
      form,
      fieldName,
      maxLength = maxLength,
      FormError(fieldName, lengthKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "jjdjdj£%^&kfkf",
      FormError(fieldName, invalidKey)
    )
  }
}
