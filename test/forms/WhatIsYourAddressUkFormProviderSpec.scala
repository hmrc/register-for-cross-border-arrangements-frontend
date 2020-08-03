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
import models.Country
import play.api.data.FormError

class WhatIsYourAddressUkFormProviderSpec extends StringFieldBehaviours {

  val countries = Seq(Country("valid", "AD", "Andorra"))
  val form = new WhatIsYourAddressUkFormProvider()(countries)
  val maxLength = 35

  ".addressLine1" - {

    val fieldName = "addressLine1"
    val requiredKey = "whatIsYourUkAddress.error.addressLine1.required"
    val invalidKey = "whatIsYourUkAddress.error.addressLine1.invalid"
    val lengthKey = "whatIsYourUkAddress.error.addressLine1.length"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey)
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

  ".addressLine2" - {

    val fieldName = "addressLine2"
    val requiredKey = "whatIsYourUkAddress.error.addressLine2.required"
    val invalidKey = "whatIsYourUkAddress.error.addressLine2.invalid"
    val lengthKey = "whatIsYourUkAddress.error.addressLine2.length"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey)
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

  ".postCode" - {

    val fieldName = "postCode"
    val requiredKey = "whatIsYourUkAddress.error.postcode.required"
    val invalidKey = "whatIsYourUkAddress.error.postcode.invalid"
    val postCodeMaxLength = 8

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validPostCodes
    )

    behave like fieldWithValidatedRegex(
      form,
      fieldName,
      maxLength = postCodeMaxLength,
      invalidError = FormError(fieldName, Seq(invalidKey))
    )

   behave like mandatoryField(
     form,
     fieldName,
     requiredError = FormError(fieldName, requiredKey)
   )

  }
}
