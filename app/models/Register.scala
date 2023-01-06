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

import controllers.exceptions.SomeInformationIsMissingException

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID
import models.RegistrationType.Business
import pages._
import play.api.libs.json._

case class NoIdOrganisation(organisationName: String)

object NoIdOrganisation {
  implicit val format: OFormat[NoIdOrganisation] = Json.format[NoIdOrganisation]
}

case class AddressNoId(addressLine1: String,
                       addressLine2: Option[String],
                       addressLine3: String,
                       addressLine4: Option[String],
                       postalCode: Option[String],
                       countryCode: String
)

object AddressNoId {

  def apply(address: Address): Option[AddressNoId] =
    Some(
      AddressNoId(
        addressLine1 = address.addressLine1,
        addressLine2 = address.addressLine2,
        addressLine3 = address.addressLine3,
        addressLine4 = address.addressLine4,
        postalCode = address.postCode,
        countryCode = address.country.code
      )
    )

  implicit lazy val writes: OWrites[AddressNoId] = OWrites[AddressNoId] {
    address =>
      Json.obj(
        "addressLine1" -> address.addressLine1,
        "addressLine2" -> address.addressLine2,
        "addressLine3" -> address.addressLine3,
        "addressLine4" -> address.addressLine4,
        "postalCode"   -> address.postalCode,
        "countryCode"  -> address.countryCode
      )
  }

  implicit lazy val reads: Reads[AddressNoId] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "addressLine1").read[String] and
        (__ \ "addressLine2").readNullable[String] and
        (__ \ "addressLine3").read[String] and
        (__ \ "addressLine4").readNullable[String] and
        (__ \ "postalCode").readNullable[String] and
        (__ \ "countryCode").read[String]
    )(
      (a1, a2, a3, a4, pc, cc) => AddressNoId(a1, a2, a3, a4, pc, cc)
    )
  }
}

case class ContactDetails(
  phoneNumber: Option[String],
  mobileNumber: Option[String],
  faxNumber: Option[String],
  emailAddress: Option[String]
)

object ContactBuilder {

  def buildContacts(userAnswers: UserAnswers): ContactDetails =
    userAnswers.get(TelephoneNumberQuestionPage) match {
      case Some(hasTelephoneNumber) =>
        hasTelephoneNumber match {
          case true =>
            userAnswers.get(ContactTelephoneNumberPage) match {
              case Some(telephoneNumber) => ContactDetails(Some(telephoneNumber), None, None, userAnswers.get(ContactEmailAddressPage))
              case None                  => throw new SomeInformationIsMissingException("User has answered yes to do you have a telephone number? but has failed to provide one")
            }
          case false => ContactDetails(None, None, None, userAnswers.get(ContactEmailAddressPage))
        }
      case None => throw new SomeInformationIsMissingException("Do you have a telephone number? question is missing")
    }
}

object ContactDetails {
  implicit val formats = Json.format[ContactDetails]
}

case class Identification(idNumber: String, issuingInstitution: String, issuingCountryCode: String)

object Identification {
  implicit val formats = Json.format[Identification]
}

case class RequestParameters(paramName: String, paramValue: String)

object RequestParameters {
  implicit val formats = Json.format[RequestParameters]
}

case class RequestCommon(
  receiptDate: String,
  regime: String,
  acknowledgementReference: String,
  requestParameters: Option[Seq[RequestParameters]]
)

object RequestCommon {
  implicit val format = Json.format[RequestCommon]

  def forService: RequestCommon = {
    val acknRef: String = UUID.randomUUID().toString.replaceAll("-", "") //uuids are 36 and spec demands 32
    //Format: ISO 8601 YYYY-MM-DDTHH:mm:ssZ e.g. 2020-09-23T16:12:11Z
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val dateTime: String = ZonedDateTime
      .now(ZoneId.of("UTC"))
      .format(formatter)
    RequestCommon(dateTime, "DAC", acknRef, None)
  }
}

case class RequestDetails(organisation: Option[NoIdOrganisation],
                          individual: Option[Individual],
                          address: AddressNoId,
                          contactDetails: ContactDetails,
                          identification: Option[Identification]
)

object RequestDetails {
  implicit val formats = Json.format[RequestDetails]
}

object Registration {

  def apply(userAnswers: UserAnswers): Option[RequestDetails] =
    userAnswers.get(RegistrationTypePage) match {
      case Some(models.RegistrationType.Individual) => IndRegistration(userAnswers)
      case Some(Business)                           => OrgRegistration(userAnswers)
      case _                                        => throw new SomeInformationIsMissingException("Cannot retrieve registration type")
    }
}

object OrgRegistration {

  def apply(userAnswers: UserAnswers): Option[RequestDetails] =
    for {
      organisationName <- getBusinessName(userAnswers)
      addressBusiness  <- userAnswers.get(BusinessAddressPage)
      address          <- AddressNoId(addressBusiness)
    } yield RequestDetails(
      Some(NoIdOrganisation(organisationName)),
      None,
      address,
      ContactBuilder.buildContacts(userAnswers),
      None
    )

  private def getBusinessName(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(BusinessTypePage) match {
      case Some(BusinessType.NotSpecified) =>
        userAnswers.get(SoleTraderNamePage).map {
          name => s"${name.firstName} ${name.secondName}"
        }
      case _ => userAnswers.get(BusinessWithoutIDNamePage)
    }
}

object IndRegistration {

  def apply(userAnswers: UserAnswers): Option[RequestDetails] = for {
    name       <- userAnswers.get(NonUkNamePage)
    dob        <- userAnswers.get(DateOfBirthPage)
    addressInd <- getAddress(userAnswers)
    address    <- AddressNoId(addressInd)
  } yield RequestDetails(
    None,
    Some(Individual(name, dob)),
    address,
    ContactBuilder.buildContacts(userAnswers),
    None
  )

  private def getAddress(userAnswers: UserAnswers): Option[Address] =
    userAnswers.get(DoYouLiveInTheUKPage) match {
      case Some(true)  => userAnswers.get(WhatIsYourAddressUkPage).orElse(toAddress(userAnswers))
      case Some(false) => userAnswers.get(WhatIsYourAddressPage)
      case _           => throw new SomeInformationIsMissingException("Cannot get address")
    }

  private def toAddress(userAnswers: UserAnswers) =
    userAnswers.get(SelectedAddressLookupPage) map {
      lookUp =>
        Address(
          lookUp.addressLine1.getOrElse(""),
          lookUp.addressLine2,
          lookUp.addressLine3
            .map(
              l => s"$l ${lookUp.town}"
            )
            .getOrElse(lookUp.town),
          lookUp.addressLine4,
          Some(lookUp.postcode),
          Country("valid", "GB", "United Kingdom")
        )
    }
}

case class RegisterWithoutIDRequest(requestCommon: RequestCommon, requestDetail: RequestDetails)

object RegisterWithoutIDRequest {
  implicit val format = Json.format[RegisterWithoutIDRequest]
}

case class Register(
  registerWithoutIDRequest: RegisterWithoutIDRequest
)

object Register {
  implicit val format = Json.format[Register]
}
