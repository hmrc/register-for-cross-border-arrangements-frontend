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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class SoleTraderNameFormProviderSpec extends StringFieldBehaviours {

  val requiredFirstNameKey = "soleTraderName.error.firstName.required"
  val lengthFirstNameKey   = "soleTraderName.error.firstName.length"
  val invalidFirstNameKey  = "soleTraderName.error.firstName.invalid"

  val requiredSecondNameKey = "soleTraderName.error.secondName.required"
  val lengthSecondNameKey   = "soleTraderName.error.secondName.length"
  val invalidSecondNameKey  = "soleTraderName.error.secondName.invalid"

  val form = new SoleTraderNameFormProvider()()

  val firstNameMaxLength  = 35
  val secondNameMaxLength = 35

  ".firstName" - {

    val fieldName = "firstName"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(firstNameMaxLength)
    )

    behave like fieldWithMaxLengthAlpha(
      form,
      fieldName,
      maxLength = firstNameMaxLength,
      lengthError = FormError(fieldName, lengthFirstNameKey)
    )

    behave like fieldWithNonEmptyWhitespace(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredFirstNameKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredFirstNameKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "jjdjdj£%^&kfkf",
      FormError(fieldName, invalidFirstNameKey)
    )
  }

  ".secondName" - {

    val fieldName = "secondName"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(secondNameMaxLength)
    )

    behave like fieldWithMaxLengthAlpha(
      form,
      fieldName,
      maxLength = secondNameMaxLength,
      lengthError = FormError(fieldName, lengthSecondNameKey)
    )

    behave like fieldWithNonEmptyWhitespace(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredSecondNameKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredSecondNameKey)
    )

    behave like fieldWithInvalidData(
      form,
      fieldName,
      "jjdjdj£%^&kfkf",
      FormError(fieldName, invalidSecondNameKey)
    )
  }
}
