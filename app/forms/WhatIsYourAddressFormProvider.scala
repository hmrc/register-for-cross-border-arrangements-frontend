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

import forms.mappings.Mappings
import javax.inject.Inject
import models.{Address, Country}
import play.api.data.Form
import play.api.data.Forms._
import utils.RegexConstants

class WhatIsYourAddressFormProvider @Inject() extends Mappings with RegexConstants {

  val addressLineLength = 35

  def apply(countryList: Seq[Country]): Form[Address] = Form(
    mapping(
      "addressLine1" -> validatedText(
        "whatIsYourAddress.error.addressLine1.required",
        "whatIsYourAddress.error.addressLine1.invalid",
        "whatIsYourAddress.error.addressLine1.length",
        apiAddressRegex,
        addressLineLength
      ),
      "addressLine2" -> validatedOptionalText("whatIsYourAddress.error.addressLine2.invalid",
                                              "whatIsYourAddress.error.addressLine2.length",
                                              apiAddressRegex,
                                              addressLineLength
      ),
      "addressLine3" -> validatedText(
        "whatIsYourAddress.error.addressLine3.required",
        "whatIsYourAddress.error.addressLine3.invalid",
        "whatIsYourAddress.error.addressLine3.length",
        apiAddressRegex,
        addressLineLength
      ),
      "addressLine4" -> validatedOptionalText("whatIsYourAddress.error.addressLine4.invalid",
                                              "whatIsYourAddress.error.addressLine4.length",
                                              apiAddressRegex,
                                              addressLineLength
      ),
      "postCode" -> optionalPostcode(
        requiredKey = "whatIsYourAddress.error.postcode.required",
        lengthKey = "whatIsYourAddress.error.postcode.length",
        countryFieldName = "country"
      ),
      "country" -> text("whatIsYourAddress.error.country.required")
        .verifying("whatIsYourAddress.error.country.required", value => countryList.exists(_.code == value))
        .transform[Country](value => countryList.find(_.code == value).get, _.code)
    )(Address.apply)(Address.unapply)
  )

}
