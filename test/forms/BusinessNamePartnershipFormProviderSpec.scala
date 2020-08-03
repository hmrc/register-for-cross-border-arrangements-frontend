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

class BusinessNamePartnershipFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "businessName.partnership.error.required"
  val lengthKey = "businessName.partnership.error.length"
  val invalidKey = "businessName.partnership.error.invalid"
  val maxLength = 105

  val form = new BusinessNamePartnershipFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validOrganisationName
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey)
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

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "jjdjdj£%^&kfkf",
      FormError(fieldName, invalidKey)
    )
  }
}
