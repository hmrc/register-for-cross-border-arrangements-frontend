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
import play.api.data.Forms._
import models.BusinessName

class BusinessNameFormProvider @Inject() extends Mappings {
    val businessNameRegex = """^[A-Za-z0-9&\/\\'\s]*$"""

   def apply(): Form[BusinessName] = Form(
     mapping(
      "businessName" -> text("businessName.error.businessName.required")
        .verifying(regexp(businessNameRegex, "businessName.error.businessName.invalid"))
        .verifying(maxLength(105, "businessName.error.businessName.length"))
    )(BusinessName.apply)(BusinessName.unapply)
   )
 }