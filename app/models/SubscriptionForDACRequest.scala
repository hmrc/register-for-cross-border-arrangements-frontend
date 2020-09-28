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
import play.api.libs.json.{Format, JsValue, Json, OWrites, Reads, __}

import scala.util.Random


case class OrganisationTest(organisationName: String)
object OrganisationTest {
  def apply(userAnswers: UserAnswers): OrganisationTest = {
    userAnswers.get(BusinessNamePage) match {
      case Some(name) => new OrganisationTest(name)
      case None => throw new Exception("Organisation name can't be empty when creating a subscription")
    }
  }

  implicit val format = Json.format[OrganisationTest]
}

case class IndividualTest(firstName: String,
                          middleName: Option[String],
                          lastName: String)
object IndividualTest {
  def apply(userAnswers: UserAnswers): IndividualTest = {
    userAnswers.get(ContactNamePage) match {
      case Some(name) => new IndividualTest(name.firstName, None, name.secondName)
      case None => throw new Exception("Individual name can't be empty when creating a subscription")
    }
  }

  implicit val format = Json.format[IndividualTest]
}


object ContactInformation {
  def apply(`class`: String, data: JsValue): ContactInformation = {
    (`class` match {
      case "ContactInformationForIndividual" =>
        Json.fromJson[ContactInformationForIndividual](data)(ContactInformationForIndividual.format)
      case "ContactInformationForOrganisation" =>
        Json.fromJson[ContactInformationForOrganisation](data)(ContactInformationForOrganisation.format)
    }).get
  }

  def unapply(contactInformation: ContactInformation): Option[(String, JsValue)] = {
    val (prod: Product, jsValue) = contactInformation match {
      case contactInformationForIndividual: ContactInformationForIndividual =>
        (contactInformationForIndividual, Json.toJson(contactInformationForIndividual)(ContactInformationForIndividual.format))
      case contactInformationForOrganisation: ContactInformationForOrganisation =>
        (contactInformationForOrganisation, Json.toJson(contactInformationForOrganisation)(ContactInformationForOrganisation.format))
    }
    Some(prod.productPrefix -> jsValue)
  }

  implicit val format = Json.format[ContactInformation]
}

sealed trait ContactInformation
case class ContactInformationForIndividual(individual: IndividualTest,
                                           email: String,
                                           phone: Option[String],
                                           mobile: Option[String]) extends ContactInformation
object ContactInformationForIndividual {
  implicit val format = Json.format[ContactInformationForIndividual]
}

case class ContactInformationForOrganisation(organisation: OrganisationTest,
                                             email: String,
                                             phone: Option[String],
                                             mobile: Option[String]) extends ContactInformation
object ContactInformationForOrganisation {
  implicit val format = Json.format[ContactInformationForOrganisation]
}

case class PrimaryContact(contactInformation: ContactInformation)
object PrimaryContact {

  implicit val writes: OWrites[PrimaryContact] = OWrites[PrimaryContact] {
    primaryContact =>
      primaryContact.contactInformation match {
        case ContactInformationForIndividual(individual, email, phone, mobile) =>
          Json.obj(
            "contactInformation" -> Json.obj(
              "individual" -> Json.obj(
                "firstName" -> individual.firstName,
                "lastName" -> individual.lastName
              ),
              "email" -> email,
              "phone" -> phone,
              "mobile" -> mobile
            )
          )
        case ContactInformationForOrganisation(organisation, email, phone, mobile) =>
          Json.obj(
            "contactInformation" -> Json.obj(
              "organisation" -> organisation.organisationName
            ),
            "email" -> email,
            "phone" -> phone,
            "mobile" -> mobile
          )
      }
  }
}

case class SecondaryContact(contactInformation: ContactInformation)
object SecondaryContact {
  implicit val writes: OWrites[SecondaryContact] = OWrites[SecondaryContact] {
    secondaryContact =>
      secondaryContact.contactInformation match {
        case ContactInformationForOrganisation(organisation, email, phone, mobile) =>
          Json.obj(
            "contactInformation" -> Json.obj(
              "organisation" -> organisation.organisationName
            ),
            "email" -> email,
            "phone" -> phone,
            "mobile" -> mobile
          )
      }
  }
}

//TODO What type is requestParameters?
case class RequestCommon(regime: String,
                         receiptDate: String,
                         acknowledgementReference: String,
                         originatingSystem: String,
                         requestParameters: Option[Seq[String]],
                         paramName: String,
                         paramValue: String)

object RequestCommon {
  implicit val writes: OWrites[RequestCommon] = OWrites[RequestCommon] {
    requestCommon =>
      Json.obj(
        "regime" -> requestCommon.regime,
        "receiptDate" -> requestCommon.receiptDate,
        "acknowledgementReference" -> requestCommon.acknowledgementReference,
        "originatingSystem" -> requestCommon.originatingSystem,
        "requestParameters" -> requestCommon.requestParameters,
        "paramName" -> requestCommon.paramName,
        "paramValue" -> requestCommon.paramValue
      )
  }
}

case class RequestDetail(idType: String,
                         idNumber: String,
                         tradingName: Option[String],
                         isGBUser: Boolean,
                         primaryContact: PrimaryContact,
                         secondaryContact: Option[SecondaryContact])
object RequestDetail {
  implicit val writes = OWrites[RequestDetail] {
    requestDetails =>
      Json.obj(
        "idType" -> requestDetails.idType,
        "idNumber" -> requestDetails.idNumber,
        "tradingName" -> requestDetails.tradingName,
        "isGBUser" -> requestDetails.isGBUser,
        "primaryContact" -> requestDetails.primaryContact,
        "secondaryContact" -> requestDetails.secondaryContact
      )
  }
}


case class SubscriptionForDACRequest(requestCommon: RequestCommon, requestDetail: RequestDetail)
object SubscriptionForDACRequest {

  implicit val writes = OWrites[SubscriptionForDACRequest] {
    dacRequest =>
      Json.obj(
        "requestCommon" -> dacRequest.requestCommon,
        "requestDetail" -> dacRequest.requestDetail
      )
  }

  def createDacRequest(userAnswers: UserAnswers): SubscriptionForDACRequest = {

    SubscriptionForDACRequest(
      requestCommon = createRequestCommon,
      requestDetail = createRequestDetail(userAnswers)
    )
  }

  private def createRequestCommon: RequestCommon = {
    //Format: ISO 8601 YYYY-MM-DDTHH:mm:ssZ e.g. 2020-09-23T16:12:11Z
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    val r = new Random()
    val idSize: Int = 1 + r.nextInt(33) //Generate a size between 1 and 32
    val generateAcknowledgementReference: String = r.alphanumeric.take(idSize).mkString

    RequestCommon(
      regime = "DAC",
      receiptDate = ZonedDateTime.now().format(formatter),
      acknowledgementReference = generateAcknowledgementReference,
      originatingSystem = "DAC6",
      requestParameters = None,
      paramName = "",
      paramValue = ""
    )
  }

  private def createRequestDetail(userAnswers: UserAnswers): RequestDetail = {
    val isGBUser = userAnswers.get(NonUkNamePage) match {
      case Some(_) => false
      case None => true
    }

    RequestDetail(
      idType = getIdType(userAnswers),
      idNumber = "idNumber",
      tradingName = Some("tradingName"),
      isGBUser = isGBUser,
      primaryContact = createPrimaryContact(userAnswers),
      secondaryContact = createSecondaryContact(userAnswers)
    )
  }

  private def getIdType(userAnswers: UserAnswers): String = {
    (userAnswers.get(NinoPage), userAnswers.get(SelfAssessmentUTRPage), userAnswers.get(CorporationTaxUTRPage)) match {
      case (Some(nino), _, _) => nino.toString().capitalize
      case (_, Some(selfAssessmentUTRPage), _) => selfAssessmentUTRPage.uniqueTaxPayerReference.capitalize
      case (_, _, Some(corporationTaxUTRPage)) => corporationTaxUTRPage.uniqueTaxPayerReference.capitalize
    }
  }

  private def createPrimaryContact(userAnswers: UserAnswers): PrimaryContact = {
    userAnswers.get(BusinessTypePage) match {
      case Some(BusinessType.NotSpecified) => PrimaryContact(createContactInformation(userAnswers)) //TODO Is this still needed?
      case _ => PrimaryContact(createContactInformation(userAnswers))
    }
  }

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
        organisation = OrganisationTest(userAnswers),
        email = secondaryEmail,
        phone = secondaryContactNumber,
        mobile = secondaryContactNumber)
    } else {
      val email = userAnswers.get(ContactEmailAddressPage) match {
        case Some(email) => email
        case None => throw new Exception("Email can't be empty when creating a subscription")
      }

      val contactNumber = userAnswers.get(ContactTelephoneNumberPage)

      if (getIdType(userAnswers) == "NINO") {
        ContactInformationForIndividual(
          individual = IndividualTest(userAnswers),
          email = email,
          phone = contactNumber,
          mobile = contactNumber)
      } else {
        ContactInformationForOrganisation(
          organisation = OrganisationTest(userAnswers),
          email = email,
          phone = contactNumber,
          mobile = contactNumber)
      }
    }
  }

}
