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

package forms

import forms.mappings.Mappings
import javax.inject.Inject
import models.Name
import play.api.data.Form
import play.api.data.Forms.mapping
import utils.RegexConstants

class NonUkNameFormProvider @Inject() extends Mappings with RegexConstants {

  private val maxLength = 35

  def apply(): Form[Name] =
    Form(
      mapping(
        "firstName" -> validatedText("nonUkName.error.firstName.required",
                                     "nonUkName.error.firstName.invalid",
                                     "nonUkName.error.firstName.length",
                                     apiNameRegex,
                                     maxLength
        ),
        "secondName" -> validatedText("nonUkName.error.secondName.required",
                                      "nonUkName.error.secondName.invalid",
                                      "nonUkName.error.secondName.length",
                                      apiNameRegex,
                                      maxLength
        )
      )(Name.apply)(Name.unapply)
    )
}
