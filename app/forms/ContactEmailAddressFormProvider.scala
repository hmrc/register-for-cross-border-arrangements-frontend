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

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class ContactEmailAddressFormProvider @Inject() extends Mappings {

  private val emailRegex = "^(?:[a-zA-Z0-9!#$%&*+\\/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&*+\\/=?^_`{|}~-]+)*)" +
    "@(?:[a-zA-Z0-9!#$%&*+\\/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&*+\\/=?^_`{|}~-]+)*)$"

  def apply(): Form[String] =
    Form(
      "email" -> text("contactEmailAddress.error.required")
          .verifying(regexp(emailRegex, "contactEmailAddress.error.email.invalid"))
        .verifying(maxLength(254, "contactEmailAddress.error.length"))
    )
}