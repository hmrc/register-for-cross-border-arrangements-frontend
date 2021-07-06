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

package models

import java.time.format.DateTimeFormatter

import pages._
import play.api.libs.json._

case class PayloadRegisterWithID(registerWithIDRequest: RegisterWithIDRequest)

object PayloadRegisterWithID {
  implicit val format: Format[PayloadRegisterWithID] = Json.format[PayloadRegisterWithID]

  def createIndividualSubmission(userAnswers: UserAnswers, identifierName: String, identifierValue: String): Option[PayloadRegisterWithID] =
    for {
      individual <- RequestWithIDDetails.createIndividualSubmission(userAnswers, identifierName, identifierValue)
    } yield PayloadRegisterWithID(
      RegisterWithIDRequest(
        RequestCommon.forService,
        individual
      )
    )

  def createBusinessSubmission(userAnswers: UserAnswers, identifierName: String, identifierValue: String): Option[PayloadRegisterWithID] =
    for {
      request <- RequestWithIDDetails.createBusinessSubmission(userAnswers, identifierName, identifierValue)
    } yield PayloadRegisterWithID(
      RegisterWithIDRequest(
        RequestCommon.forService,
        request
      )
    )
}

case class RegisterWithIDRequest(requestCommon: RequestCommon, requestDetail: RequestWithIDDetails)

object RegisterWithIDRequest {
  implicit val format: Format[RegisterWithIDRequest] = Json.format[RegisterWithIDRequest]
}

sealed trait PartnerDetails

case class WithIDIndividual(
  firstName: String,
  middleName: Option[String],
  lastName: String,
  dateOfBirth: String
) extends PartnerDetails

object WithIDIndividual {
  implicit val format = Json.format[WithIDIndividual]
}

case class WithIDOrganisation(
  organisationName: String,
  organisationType: String
) extends PartnerDetails

object WithIDOrganisation {
  implicit val format = Json.format[WithIDOrganisation]
}

case class RequestWithIDDetails(
  IDType: String,
  IDNumber: String,
  requiresNameMatch: Boolean,
  isAnAgent: Boolean,
  partnerDetails: PartnerDetails
)

object RequestWithIDDetails {
  val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE

  implicit lazy val requestWithIDDetailsReads: Reads[RequestWithIDDetails] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "IDType").read[String] and
        (__ \ "IDNumber").read[String] and
        (__ \ "requiresNameMatch").read[Boolean] and
        (__ \ "isAnAgent").read[Boolean] and
        (__ \ "individual").readNullable[WithIDIndividual] and
        (__ \ "organisation").readNullable[WithIDOrganisation]
    )(
      (idType, idNumber, requiresNameMatch, isAnAgent, individual, organisation) =>
        (individual, organisation) match {
          case (Some(_), Some(_)) => throw new Exception("Request details cannot have both and organisation or individual element")
          case (Some(ind), _)     => RequestWithIDDetails(idType, idNumber, requiresNameMatch, isAnAgent, ind)
          case (_, Some(org))     => RequestWithIDDetails(idType, idNumber, requiresNameMatch, isAnAgent, org)
          case (None, None)       => throw new Exception("Request Details must have either an organisation or individual element")
        }
    )
  }

  implicit lazy val requestWithIDDetailsWrites: OWrites[RequestWithIDDetails] = OWrites[RequestWithIDDetails] {
    case RequestWithIDDetails(idType, idNumber, requiresNameMatch, isAnAgent, individual @ WithIDIndividual(_, _, _, _)) =>
      Json.obj(
        "IDType"            -> idType,
        "IDNumber"          -> idNumber,
        "requiresNameMatch" -> requiresNameMatch,
        "isAnAgent"         -> isAnAgent,
        "individual"        -> individual
      )
    case RequestWithIDDetails(idType, idNumber, requiresNameMatch, isAnAgent, organisation @ WithIDOrganisation(_, _)) =>
      Json.obj(
        "IDType"            -> idType,
        "IDNumber"          -> idNumber,
        "requiresNameMatch" -> requiresNameMatch,
        "isAnAgent"         -> isAnAgent,
        "organisation"      -> organisation
      )
  }

  def createIndividualSubmission(userAnswers: UserAnswers, identifierName: String, identifierValue: String): Option[RequestWithIDDetails] =
    for {
      name <- userAnswers.get(NamePage).orElse(userAnswers.get(SoleTraderNamePage))
      dob  <- userAnswers.get(DateOfBirthPage)
    } yield RequestWithIDDetails(
      identifierName,
      identifierValue,
      requiresNameMatch = true,
      isAnAgent = false, //This may change
      WithIDIndividual(name.firstName, None, name.secondName, dob.format(dateFormat))
    )

  def createBusinessSubmission(userAnswers: UserAnswers, identifierName: String, identifierValue: String): Option[RequestWithIDDetails] =
    for {
      businessName <- getBusinessName(userAnswers)
      businessType <- userAnswers.get(BusinessTypePage)
    } yield RequestWithIDDetails(
      identifierName,
      identifierValue,
      requiresNameMatch = true,
      isAnAgent = false, //This may change
      WithIDOrganisation(businessName, toEnumeratedBusinessType(businessType))
    )

  private def toEnumeratedBusinessType(businessType: BusinessType) = businessType match {
    case BusinessType.NotSpecified       => "0000"
    case BusinessType.Partnership        => "0001"
    case BusinessType.LimitedLiability   => "0002"
    case BusinessType.CorporateBody      => "0003"
    case BusinessType.UnIncorporatedBody => "0004"
  }

  private def getBusinessName(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(BusinessTypePage) match {
      case Some(BusinessType.NotSpecified) =>
        userAnswers.get(SoleTraderNamePage).map {
          name => s"${name.firstName} ${name.secondName}"
        }
      case _ => userAnswers.get(BusinessNamePage)
    }
}
