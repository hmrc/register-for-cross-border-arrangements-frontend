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

package models

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

import pages._
import play.api.libs.json._


case class OrganisationDetails(organisationName: String)
object OrganisationDetails {
  def buildPrimaryContact(userAnswers: UserAnswers): OrganisationDetails = {
    userAnswers.get(ContactNamePage) match {
      case Some(name) => new OrganisationDetails(s"${name.firstName} ${name.secondName}")
      case _ => throw new Exception("Contact name page is empty when creating a subscription for organisation")
    }
  }

  def buildSecondaryContact(userAnswers: UserAnswers): OrganisationDetails = {
    userAnswers.get(SecondaryContactNamePage) match {
      case Some(name) => new OrganisationDetails(name)
      case None => throw new Exception("Secondary contact name page is empty after user answered 'Yes' to second contact")
    }
  }

  implicit val format: OFormat[OrganisationDetails] = Json.format[OrganisationDetails]
}

case class IndividualDetails(firstName: String,
                             middleName: Option[String],
                             lastName: String)
object IndividualDetails {
  def buildIndividualDetails(userAnswers: UserAnswers): IndividualDetails = {
    (userAnswers.get(NamePage), userAnswers.get(NonUkNamePage), userAnswers.get(SoleTraderNamePage)) match {
      case (Some(name), _, _) => new IndividualDetails(name.firstName, None, name.secondName)
      case (_, Some(nonUKName), _) => new IndividualDetails(nonUKName.firstName, None, nonUKName.secondName)
      case (_, _, Some(soleTraderName)) => new IndividualDetails(soleTraderName.firstName, None, soleTraderName.secondName)
      case _ => throw new Exception("Individual or sole trader name can't be empty when creating a subscription")
    }
  }

  implicit val format: OFormat[IndividualDetails] = Json.format[IndividualDetails]
}

sealed trait ContactInformation

case class ContactInformationForIndividual(individual: IndividualDetails,
                                           email: String,
                                           phone: Option[String],
                                           mobile: Option[String]) extends ContactInformation
object ContactInformationForIndividual {
  implicit val format: OFormat[ContactInformationForIndividual] = Json.format[ContactInformationForIndividual]
}

case class ContactInformationForOrganisation(organisation: OrganisationDetails,
                                             email: String,
                                             phone: Option[String],
                                             mobile: Option[String]) extends ContactInformation
object ContactInformationForOrganisation {
  implicit val format: OFormat[ContactInformationForOrganisation] = Json.format[ContactInformationForOrganisation]
}

case class PrimaryContact(contactInformation: ContactInformation)
object PrimaryContact {

  implicit lazy val reads: Reads[PrimaryContact] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "organisation").readNullable[OrganisationDetails] and
        (__ \ "individual").readNullable[IndividualDetails] and
        (__ \ "email").read[String] and
        (__ \ "phone").readNullable[String] and
        (__ \ "mobile").readNullable[String]
      ) (
      (organisation, individual, email, phone, mobile) => (organisation, individual) match {
        case (Some(_), Some(_)) => throw new Exception("PrimaryContact cannot have both and organisation or individual element")
        case (Some(org), _) => PrimaryContact(ContactInformationForOrganisation(org, email, phone, mobile))
        case (_, Some(ind)) => PrimaryContact(ContactInformationForIndividual(ind, email, phone, mobile))
        case (None, None) => throw new Exception("PrimaryContact must have either an organisation or individual element")
      }
    )
  }

  implicit lazy val writes: OWrites[PrimaryContact] = {
    case PrimaryContact(contactInformationForInd@ContactInformationForIndividual(_, _, _, _)) =>
      Json.toJsObject(contactInformationForInd)
    case PrimaryContact(contactInformationForOrg@ContactInformationForOrganisation(_, _, _, _)) =>
      Json.toJsObject(contactInformationForOrg)
  }
}

case class SecondaryContact(contactInformation: ContactInformation)
object SecondaryContact {

  implicit lazy val reads: Reads[SecondaryContact] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "organisation").readNullable[OrganisationDetails] and
        (__ \ "individual").readNullable[IndividualDetails] and
        (__ \ "email").read[String] and
        (__ \ "phone").readNullable[String] and
        (__ \ "mobile").readNullable[String]
      ) (
      (organisation, individual, email, phone, mobile) => (organisation, individual) match {
        case (Some(_), Some(_)) => throw new Exception("SecondaryContact cannot have both and organisation or individual element")
        case (Some(org), _) => SecondaryContact(ContactInformationForOrganisation(org, email, phone, mobile))
        case (_, Some(ind)) => SecondaryContact(ContactInformationForIndividual(ind, email, phone, mobile))
        case (None, None) => throw new Exception("SecondaryContact must have either an organisation or individual element")
      }
    )
  }

  implicit lazy val writes: OWrites[SecondaryContact] = {
    case SecondaryContact(contactInformationForInd@ContactInformationForIndividual(_, _, _, _)) =>
      Json.toJsObject(contactInformationForInd)
    case SecondaryContact(contactInformationForOrg@ContactInformationForOrganisation(_, _, _, _)) =>
      Json.toJsObject(contactInformationForOrg)
  }
}

case class RequestParameter(paramName: String,
                            paramValue: String)
object RequestParameter {
  implicit val format: OFormat[RequestParameter] = Json.format[RequestParameter]
}

case class RequestCommonForSubscription(regime: String,
                                        receiptDate: String,
                                        acknowledgementReference: String,
                                        originatingSystem: String,
                                        requestParameters: Option[Seq[RequestParameter]])

object RequestCommonForSubscription {
  implicit val format: OFormat[RequestCommonForSubscription] = Json.format[RequestCommonForSubscription]
}

case class RequestDetail(IDType: String,
                         IDNumber: String,
                         tradingName: Option[String],
                         isGBUser: Boolean,
                         primaryContact: PrimaryContact,
                         secondaryContact: Option[SecondaryContact])
object RequestDetail {

  implicit val reads: Reads[RequestDetail] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "IDType").read[String] and
      (__ \ "IDNumber").read[String] and
      (__ \ "tradingName").readNullable[String] and
      (__ \ "isGBUser").read[Boolean] and
      (__ \ "primaryContact").read[PrimaryContact] and
      (__ \ "secondaryContact").readNullable[SecondaryContact]
    )((idType, idNumber, tradingName, isGBUser, primaryContact, secondaryContact) =>
      RequestDetail(idType, idNumber, tradingName, isGBUser, primaryContact, secondaryContact))
  }

  implicit val writes: OWrites[RequestDetail] = Json.writes[RequestDetail]
}


case class SubscriptionForDACRequest(requestCommon: RequestCommonForSubscription,
                                     requestDetail: RequestDetail)
object SubscriptionForDACRequest {

  implicit val format: OFormat[SubscriptionForDACRequest] = Json.format[SubscriptionForDACRequest]

  def createSubscription(userAnswers: UserAnswers): SubscriptionForDACRequest = {

    SubscriptionForDACRequest(
      requestCommon = createRequestCommon,
      requestDetail = createRequestDetail(userAnswers)
    )
  }

  private def createRequestCommon: RequestCommonForSubscription = {
    //Format: ISO 8601 YYYY-MM-DDTHH:mm:ssZ e.g. 2020-09-23T16:12:11Z
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    //Generate a 32 chars UUID without hyphens
    val acknowledgementReference = UUID.randomUUID().toString.replace("-", "")

    RequestCommonForSubscription(
      regime = "DAC",
      receiptDate = ZonedDateTime.now().format(formatter),
      acknowledgementReference = acknowledgementReference,
      originatingSystem = "MDTP",
      requestParameters = None
    )
  }

  private def createRequestDetail(userAnswers: UserAnswers): RequestDetail = {
    val isGBUser = userAnswers.get(NonUkNamePage) match {
      case Some(_) => false
      case None => true
    }

    RequestDetail(
      IDType = "SAFE",
      IDNumber = getNumber(userAnswers),
      tradingName = None,
      isGBUser = isGBUser,
      primaryContact = createPrimaryContact(userAnswers),
      secondaryContact = createSecondaryContact(userAnswers)
    )
  }

  private def getNumber(userAnswers: UserAnswers): String = {
    userAnswers.get(SafeIDPage) match {
      case Some(id) => id
      case None => throw new Exception("Error retrieving ID number")
    }
  }

  private def createPrimaryContact(userAnswers: UserAnswers): PrimaryContact = PrimaryContact(createContactInformation(userAnswers))

  private def createSecondaryContact(userAnswers: UserAnswers): Option[SecondaryContact] = {
    userAnswers.get(HaveSecondContactPage) match {
      case Some(true) => Some(SecondaryContact(createContactInformation(userAnswers, secondaryContact = true)))
      case _ => None
    }
  }

  private def createContactInformation(userAnswers: UserAnswers, secondaryContact: Boolean = false): ContactInformation = {

    if (secondaryContact) {
      val secondaryEmail = userAnswers.get(SecondaryContactEmailAddressPage) match {
        case Some(email) => email
        case None => ""
      }

      val secondaryContactNumber = userAnswers.get(SecondaryContactTelephoneNumberPage)

      ContactInformationForOrganisation(
        organisation = OrganisationDetails.buildSecondaryContact(userAnswers),
        email = secondaryEmail,
        phone = secondaryContactNumber,
        mobile = None)
    } else {
      val email = userAnswers.get(ContactEmailAddressPage) match {
        case Some(email) => email
        case None => throw new Exception("Email can't be empty when creating a subscription")
      }

      val contactNumber = userAnswers.get(ContactTelephoneNumberPage)

      val individualOrSoleTrader =
        (userAnswers.get(RegistrationTypePage), userAnswers.get(BusinessTypePage)) match {
          case (Some(RegistrationType.Individual), _) => true
          case (_, Some(BusinessType.NotSpecified)) => true
          case _ => false
        }

      if (individualOrSoleTrader) {
        ContactInformationForIndividual(
          individual = IndividualDetails.buildIndividualDetails(userAnswers),
          email = email,
          phone = contactNumber,
          mobile = None)
      } else {
        ContactInformationForOrganisation(
          organisation = OrganisationDetails.buildPrimaryContact(userAnswers),
          email = email,
          phone = contactNumber,
          mobile = None)
      }
    }
  }

}

case class CreateSubscriptionForDACRequest(createSubscriptionForDACRequest: SubscriptionForDACRequest)

object CreateSubscriptionForDACRequest {
  implicit val format: OFormat[CreateSubscriptionForDACRequest] = Json.format[CreateSubscriptionForDACRequest]
}
