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
import models.{Address, Country}
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._

class WhatIsYourAddressUkFormProvider @Inject() extends Mappings  {

  val addressLineLength = 50

   def apply(countryList: Seq[Country]): Form[Address] = Form(
     mapping(
      "addressLine1" -> textNonWhitespaceOnly("whatIsYourUkAddress.error.addressLine1.required")
        .verifying(maxLength(addressLineLength, "whatIsYourUkAddress.error.addressLine1.length")),
      "addressLine2" -> textNonWhitespaceOnly("whatIsYourUkAddress.error.addressLine2.required")
        .verifying(maxLength(addressLineLength, "whatIsYourUkAddress.error.addressLine2.length")),
       "addressLine3" -> optionalText().verifying(maxLength(addressLineLength, "whatIsYourUkAddress.error.addressLine3.length")),
       "addressLine4" -> optionalText().verifying(maxLength(addressLineLength, "whatIsYourUkAddress.error.addressLine4.length")),
       "postCode" -> addressPostcode("whatIsYourUkAddress.error.postcode.invalid", "whatIsYourUkAddress.error.postcode.required"),
       "country" ->  text("whatIsYourUkAddress.error.country.required")
         .verifying("whatIsYourAddress.error.country.required", value => countryList.exists(_.code == value))
         .transform[Country](value => countryList.find(_.code == value).get, _.code)
    )(Address.apply)(Address.unapply)
   )

}