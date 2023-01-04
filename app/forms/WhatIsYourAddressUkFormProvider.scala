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
import models.{Address, Country}
import play.api.data.Form
import play.api.data.Forms._
import utils.RegexConstants

import javax.inject.Inject

class WhatIsYourAddressUkFormProvider @Inject() extends Mappings with RegexConstants {

  val addressLineMaxLength = 35

  def apply(countryList: Seq[Country]): Form[Address] = Form(
    mapping(
      "addressLine1" -> validatedText(
        "whatIsYourUkAddress.error.addressLine1.required",
        "whatIsYourUkAddress.error.addressLine1.invalid",
        "whatIsYourUkAddress.error.addressLine1.length",
        apiAddressRegex,
        addressLineMaxLength
      ),
      "addressLine2" -> validatedOptionalText("whatIsYourUkAddress.error.addressLine2.invalid",
                                              "whatIsYourUkAddress.error.addressLine2.length",
                                              apiAddressRegex,
                                              addressLineMaxLength
      ),
      "addressLine3" -> validatedText(
        "whatIsYourUkAddress.error.addressLine3.required",
        "whatIsYourUkAddress.error.addressLine3.invalid",
        "whatIsYourUkAddress.error.addressLine3.length",
        apiAddressRegex,
        addressLineMaxLength
      ),
      "addressLine4" -> validatedOptionalText("whatIsYourUkAddress.error.addressLine4.invalid",
                                              "whatIsYourUkAddress.error.addressLine4.length",
                                              apiAddressRegex,
                                              addressLineMaxLength
      ),
      "postCode" -> addressPostcode("whatIsYourUkAddress.error.postcode.invalid", regexPostcode, "whatIsYourUkAddress.error.postcode.required"),
      "country" -> text("whatIsYourUkAddress.error.country.required")
        .verifying("whatIsYourAddress.error.country.required", value => countryList.exists(_.code == value))
        .transform[Country](value => countryList.find(_.code == value).get, _.code)
    )(Address.apply)(Address.unapply)
  )

}
