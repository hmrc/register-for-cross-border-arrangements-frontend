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

package models

import play.api.libs.json._

sealed trait PartnerDetailsResponse

case class IndividualResponse(
  firstName: String,
  middleName: Option[String],
  lastName: String,
  dateOfBirth: Option[String]
) extends PartnerDetailsResponse

object IndividualResponse {
  implicit val format: Format[IndividualResponse] = Json.format[IndividualResponse]
}

case class OrganisationResponse(
  organisationName: String,
  isAGroup: Boolean,
  organisationType: Option[String],
  code: Option[String]
) extends PartnerDetailsResponse

object OrganisationResponse {
  implicit val format: Format[OrganisationResponse] = Json.format[OrganisationResponse]
}

case class AddressResponse(
  addressLine1: String,
  addressLine2: Option[String],
  addressLine3: Option[String],
  addressLine4: Option[String],
  postalCode: Option[String],
  countryCode: String
)

object AddressResponse {
  implicit val format: Format[AddressResponse] = Json.format[AddressResponse]
}

case class ResponseDetail(
  SAFEID: String,
  ARN: Option[String],
  isEditable: Boolean,
  isAnAgent: Boolean,
  isAnASAgent: Option[Boolean],
  isAnIndividual: Boolean,
  partnerDetails: PartnerDetailsResponse,
  address: AddressResponse,
  contactDetails: ContactDetails
)

object ResponseDetail {

  implicit lazy val responseDetailsWrites: Writes[ResponseDetail] = Writes[ResponseDetail] {
    case ResponseDetail(safeid,
                        arn,
                        isEditable,
                        isAnAgent,
                        isAnASAgent,
                        isAnIndividual,
                        individual @ IndividualResponse(_, _, _, _),
                        address,
                        contactDetails
        ) =>
      Json.obj(
        "SAFEID"         -> safeid,
        "ARN"            -> arn,
        "isEditable"     -> isEditable,
        "isAnAgent"      -> isAnAgent,
        "isAnASAgent"    -> isAnASAgent,
        "isAnIndividual" -> isAnIndividual,
        "individual"     -> individual,
        "address"        -> address,
        "contactDetails" -> contactDetails
      )

    case ResponseDetail(safeid,
                        arn,
                        isEditable,
                        isAnAgent,
                        isAnASAgent,
                        isAnIndividual,
                        organisation @ OrganisationResponse(_, _, _, _),
                        address,
                        contactDetails
        ) =>
      Json.obj(
        "SAFEID"         -> safeid,
        "ARN"            -> arn,
        "isEditable"     -> isEditable,
        "isAnAgent"      -> isAnAgent,
        "isAnASAgent"    -> isAnASAgent,
        "isAnIndividual" -> isAnIndividual,
        "organisation"   -> organisation,
        "address"        -> address,
        "contactDetails" -> contactDetails
      )
  }

  implicit lazy val responseDetailsReads: Reads[ResponseDetail] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "SAFEID").read[String] and
        (__ \ "ARN").readNullable[String] and
        (__ \ "isEditable").read[Boolean] and
        (__ \ "isAnAgent").read[Boolean] and
        (__ \ "isAnASAgent").readNullable[Boolean] and
        (__ \ "isAnIndividual").read[Boolean] and
        (__ \ "individual").readNullable[IndividualResponse] and
        (__ \ "organisation").readNullable[OrganisationResponse] and
        (__ \ "address").read[AddressResponse] and
        (__ \ "contactDetails").read[ContactDetails]
    )(
      (safeid, arn, isEditable, isAnAgent, isAnASAgent, isAnIndividual, individual, organisation, address, contactDetails) =>
        (individual, organisation) match {
          case (Some(_), Some(_)) => throw new Exception("Response details cannot have both and organisation or individual element")
          case (Some(ind), _)     => ResponseDetail(safeid, arn, isEditable, isAnAgent, isAnASAgent, isAnIndividual, ind, address, contactDetails)
          case (_, Some(org))     => ResponseDetail(safeid, arn, isEditable, isAnAgent, isAnASAgent, isAnIndividual, org, address, contactDetails)
          case (None, None)       => throw new Exception("Response Details must have either an organisation or individual element")
        }
    )
  }
}

case class RegisterWithIDResponse(
  responseCommon: ResponseCommon,
  responseDetail: Option[ResponseDetail]
)

object RegisterWithIDResponse {
  implicit val format: Format[RegisterWithIDResponse] = Json.format[RegisterWithIDResponse]
}

case class PayloadRegistrationWithIDResponse(registerWithIDResponse: RegisterWithIDResponse)

object PayloadRegistrationWithIDResponse {
  implicit val format: Format[PayloadRegistrationWithIDResponse] = Json.format[PayloadRegistrationWithIDResponse]
}
