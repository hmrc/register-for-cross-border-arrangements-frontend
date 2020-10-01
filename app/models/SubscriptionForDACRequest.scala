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

import pages._
import play.api.libs.json._

import scala.util.Random


case class OrganisationDetails(organisationName: String)
object OrganisationDetails {
  def apply(userAnswers: UserAnswers): OrganisationDetails = {
    userAnswers.get(BusinessNamePage) match {
      case Some(name) => new OrganisationDetails(name)
      case None => throw new Exception("Organisation name can't be empty when creating a subscription")
    }
  }

  implicit val format: OFormat[OrganisationDetails] = Json.format[OrganisationDetails]
}

case class IndividualDetails(firstName: String,
                             middleName: Option[String],
                             lastName: String)
object IndividualDetails {
  def apply(userAnswers: UserAnswers): IndividualDetails = {
    userAnswers.get(ContactNamePage) match {
      case Some(name) => new IndividualDetails(name.firstName, None, name.secondName)
      case None => throw new Exception("Individual name can't be empty when creating a subscription")
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
    )((organisation, individual, email, phone, mobile) =>
      if (organisation.isDefined) {
        PrimaryContact(ContactInformationForOrganisation(organisation.get, email, phone, mobile))
      }
      else {
        PrimaryContact(ContactInformationForIndividual(individual.get, email, phone, mobile))
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
      )((organisation, individual, email, phone, mobile) =>
      if (organisation.isDefined) {
        SecondaryContact(ContactInformationForOrganisation(organisation.get, email, phone, mobile))
      }
      else {
        SecondaryContact(ContactInformationForIndividual(individual.get, email, phone, mobile))
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

case class RequestDetail(idType: String,
                         idNumber: String,
                         tradingName: Option[String],
                         isGBUser: Boolean,
                         primaryContact: PrimaryContact,
                         secondaryContact: Option[SecondaryContact])
object RequestDetail {

  implicit val reads: Reads[RequestDetail] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "idType").read[String] and
      (__ \ "idNumber").read[String] and
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

  implicit val reads: Reads[SubscriptionForDACRequest] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "createSubscriptionForDACRequest" \ "requestCommon").read[RequestCommonForSubscription] and
        (__ \ "createSubscriptionForDACRequest" \ "requestDetail").read[RequestDetail]
    )((requestCommon, requestDetail) => SubscriptionForDACRequest(requestCommon, requestDetail))
  }

  implicit val writes: OWrites[SubscriptionForDACRequest] = OWrites[SubscriptionForDACRequest] {
    dacRequest =>
      Json.obj(
        "createSubscriptionForDACRequest" -> Json.obj(
          "requestCommon" -> dacRequest.requestCommon,
          "requestDetail" -> dacRequest.requestDetail
        )
      )
  }

  def createEnrolment(userAnswers: UserAnswers): SubscriptionForDACRequest = {

    SubscriptionForDACRequest(
      requestCommon = createRequestCommon,
      requestDetail = createRequestDetail(userAnswers)
    )
  }

  private def createRequestCommon: RequestCommonForSubscription = {
    //Format: ISO 8601 YYYY-MM-DDTHH:mm:ssZ e.g. 2020-09-23T16:12:11Z
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    val r = new Random()
    val idSize: Int = 1 + r.nextInt(33) //Generate a size between 1 and 32
    val generateAcknowledgementReference: String = r.alphanumeric.take(idSize).mkString

    RequestCommonForSubscription(
      regime = "DAC",
      receiptDate = ZonedDateTime.now().format(formatter),
      acknowledgementReference = generateAcknowledgementReference,
      originatingSystem = "MDTP",
      requestParameters = None
    )
  }

  private def createRequestDetail(userAnswers: UserAnswers): RequestDetail = {
    val isGBUser = userAnswers.get(NonUkNamePage) match {
      case Some(_) => false
      case None => true
    }

    val (idType, idNumber) = getIdTypeAndNumber(userAnswers)

    RequestDetail(
      idType = idType,
      idNumber = idNumber,
      tradingName = None,
      isGBUser = isGBUser,
      primaryContact = createPrimaryContact(userAnswers),
      secondaryContact = createSecondaryContact(userAnswers)
    )
  }

  private def getIdTypeAndNumber(userAnswers: UserAnswers): (String, String) = {
    (userAnswers.get(NinoPage), userAnswers.get(SelfAssessmentUTRPage), userAnswers.get(CorporationTaxUTRPage)) match {
      case (Some(nino), _, _) => ("NINO", nino.toString().capitalize)
      case (_, Some(selfAssessmentUTRPage), _) => ("UTR", selfAssessmentUTRPage.uniqueTaxPayerReference)
      case (_, _, Some(corporationTaxUTRPage)) => ("UTR", corporationTaxUTRPage.uniqueTaxPayerReference)
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
        case None => "" //TODO Secondary email should be optional? In the API it isn't
      }

      val secondaryContactNumber = userAnswers.get(SecondaryContactTelephoneNumberPage)

      ContactInformationForOrganisation(
        organisation = OrganisationDetails(userAnswers),
        email = secondaryEmail,
        phone = secondaryContactNumber,
        mobile = secondaryContactNumber)
    } else {
      val email = userAnswers.get(ContactEmailAddressPage) match {
        case Some(email) => email
        case None => throw new Exception("Email can't be empty when creating a subscription")
      }

      val contactNumber = userAnswers.get(ContactTelephoneNumberPage)

      if (getIdTypeAndNumber(userAnswers)._1 == "NINO") {
        ContactInformationForIndividual(
          individual = IndividualDetails(userAnswers),
          email = email,
          phone = contactNumber,
          mobile = contactNumber)
      } else {
        ContactInformationForOrganisation(
          organisation = OrganisationDetails(userAnswers),
          email = email,
          phone = contactNumber,
          mobile = contactNumber)
      }
    }
  }

}
