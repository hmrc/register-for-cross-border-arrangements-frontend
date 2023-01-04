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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class BusinessDetails(name: String, address: BusinessAddress)

object BusinessDetails {

  implicit lazy val reads: Reads[BusinessDetails] = (
    (JsPath \ "organisation" \ "organisationName").read[String] and
      (JsPath \ "address").read[BusinessAddress]
  )(BusinessDetails.apply _)

  implicit lazy val writes: Writes[BusinessDetails] = Json.writes[BusinessDetails]

  def fromRegistrationMatch(payload: PayloadRegistrationWithIDResponse): Option[BusinessDetails] = {
    val addressExtracted: Option[AddressResponse] =
      payload.registerWithIDResponse.responseDetail.map(_.address)

    val nameExtracted: Option[String] =
      payload.registerWithIDResponse.responseDetail.map {
        _.partnerDetails match {
          case individualResponse: IndividualResponse     => s"${individualResponse.firstName} ${individualResponse.lastName}"
          case organisationResponse: OrganisationResponse => organisationResponse.organisationName
        }
      }

    for {
      address <- addressExtracted
      name    <- nameExtracted
    } yield BusinessDetails(
      name,
      BusinessAddress.fromAddressResponse(address)
    )
  }
}

case class BusinessAddress(addressLine1: String,
                           addressLine2: Option[String],
                           addressLine3: Option[String],
                           addressLine4: Option[String],
                           postCode: String,
                           countryCode: String
) {

  def toAddress: Address =
    Address(
      addressLine1,
      addressLine2,
      addressLine3.getOrElse(""),
      addressLine4,
      Some(postCode),
      Country("valid", countryCode, "")
    )

}

object BusinessAddress {

  implicit lazy val reads: Reads[BusinessAddress] = (
    (JsPath \ "addressLine1").read[String] and
      (JsPath \ "addressLine2").readNullable[String] and
      (JsPath \ "addressLine3").readNullable[String] and
      (JsPath \ "addressLine4").readNullable[String] and
      (JsPath \ "postalCode").read[String] and
      (JsPath \ "countryCode").read[String]
  )(BusinessAddress.apply _)

  implicit lazy val writes: Writes[BusinessAddress] = Json.writes[BusinessAddress]

  def fromAddressResponse(addressResponse: AddressResponse): BusinessAddress =
    BusinessAddress(
      addressResponse.addressLine1,
      addressResponse.addressLine2,
      addressResponse.addressLine3,
      addressResponse.addressLine4,
      addressResponse.postalCode.getOrElse(""),
      addressResponse.countryCode
    )

}
