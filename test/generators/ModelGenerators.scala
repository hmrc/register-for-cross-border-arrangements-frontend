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

package generators

import models.{PayloadRegistrationWithIDResponse, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.alphaStr
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

trait ModelGenerators {
  self: Generators =>

  val regime = "DACSIX"

  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      for {
        state <- Gen.oneOf(Seq("Valid", "Invalid"))
        code  <- Gen.pick(2, 'A' to 'Z')
        name  <- arbitrary[String]
      } yield Country(state, code.mkString, name)
    }

  implicit lazy val arbitraryWhatIsYourAddress: Arbitrary[Address] =
    Arbitrary {
      for {
        addressLine1 <- arbitrary[String]
        addressLine2 <- arbitrary[Option[String]]
        addressLine3 <- arbitrary[String]
        addressLine4 <- arbitrary[Option[String]]
        postCode     <- arbitrary[Option[String]]
        country      <- arbitrary[Country]
      } yield Address(addressLine1, addressLine2, addressLine3, addressLine4, postCode, country)
    }

  //TODO: Pull out length
  implicit val arbitraryName: Arbitrary[Name] = Arbitrary {
    for {
      firstName  <- stringsWithMaxLength(50)
      secondName <- stringsWithMaxLength(50)
    } yield Name(firstName, secondName)
  }

  implicit val arbitraryNino: Arbitrary[Nino] = Arbitrary {
    for {
      prefix <- Gen.oneOf(Nino.validPrefixes)
      number <- Gen.choose(0, 999999)
      suffix <- Gen.oneOf(Nino.validSuffixes)
    } yield Nino(f"$prefix$number%06d$suffix")
  }

  implicit val arbitraryIndividualMatchingSubmission: Arbitrary[IndividualMatchingSubmission] = Arbitrary {
    for {
      name <- arbitrary[Name]
      dob  <- arbitrary[LocalDate]
    } yield IndividualMatchingSubmission(regime, requiresNameMatch = true, isAnAgent = false, Individual(name, dob))
  }

  implicit val arbitraryBusinessMatchingSubmission: Arbitrary[BusinessMatchingSubmission] = Arbitrary {
    for {
      businessName <- arbitrary[String]
      businessType <- arbitraryBusinessType.arbitrary
    } yield BusinessMatchingSubmission(regime, requiresNameMatch = true, isAnAgent = false, Organisation(businessName, businessType))
  }

  implicit val arbitraryEmailRequest: Arbitrary[EmailRequest] = Arbitrary {
    for {
      to          <- arbitrary[List[String]]
      id          <- arbitrary[String]
      contactName <- arbitrary[Map[String, String]]

    } yield EmailRequest(to, id, contactName)
  }

  implicit lazy val arbitraryUniqueTaxpayerReference: Arbitrary[UniqueTaxpayerReference] =
    Arbitrary {
      for {
        uniqueTaxPayerReference <- arbitrary[String]
      } yield UniqueTaxpayerReference(uniqueTaxPayerReference)
    }

  implicit lazy val arbitraryBusinessType: Arbitrary[BusinessType] =
    Arbitrary {
      Gen.oneOf(BusinessType.values.toSeq)
    }

  implicit val arbitraryRequestCommon: Arbitrary[RequestCommon] = Arbitrary {
    for {
      receiptDate              <- arbitrary[String]
      acknowledgementReference <- stringsWithMaxLength(32)

    } yield RequestCommon(
      receiptDate,
      regime = "DAC",
      acknowledgementReference,
      None
    )
  }

  implicit val arbitraryWithIDIndividual: Arbitrary[WithIDIndividual] = Arbitrary {
    for {
      firstName   <- alphaStr
      middleName  <- Gen.option(alphaStr)
      lastName    <- alphaStr
      dateOfBirth <- alphaStr
    } yield WithIDIndividual(firstName, middleName, lastName, dateOfBirth)
  }

  implicit val arbitraryWithIDOrganisation: Arbitrary[WithIDOrganisation] = Arbitrary {
    for {
      organisationName <- alphaStr
      organisationType <- alphaStr
    } yield WithIDOrganisation(organisationName, organisationType)
  }

  implicit val arbitraryRequestWithIDDetails: Arbitrary[RequestWithIDDetails] = Arbitrary {
    for {
      idType            <- alphaStr
      idNumber          <- alphaStr
      requiresNameMatch <- arbitrary[Boolean]
      isAnAgent         <- arbitrary[Boolean]
      partnerDetails    <- Gen.oneOf(arbitrary[WithIDIndividual], arbitrary[WithIDOrganisation])
    } yield RequestWithIDDetails(idType, idNumber, requiresNameMatch, isAnAgent, partnerDetails)
  }

  implicit val arbitraryPayloadRegisterWithID: Arbitrary[PayloadRegisterWithID] = Arbitrary {
    for {
      requestCommon        <- arbitrary[RequestCommon]
      requestWithIDDetails <- arbitrary[RequestWithIDDetails]
    } yield PayloadRegisterWithID(RegisterWithIDRequest(requestCommon, requestWithIDDetails))
  }

  implicit val arbitraryRegistration: Arbitrary[Register] = Arbitrary {
    for {
      requestCommon  <- arbitrary[RequestCommon]
      name           <- arbitrary[String]
      address        <- arbitrary[AddressNoId]
      contactDetails <- arbitrary[ContactDetails]
      identification <- Gen.option(arbitrary[Identification])
    } yield Register(
      RegisterWithoutIDRequest(
        requestCommon,
        RequestDetails(Some(NoIdOrganisation(name)), None, address = address, contactDetails = contactDetails, identification = identification)
      )
    )
  }

  implicit val arbitraryAddressNoId: Arbitrary[AddressNoId] = Arbitrary {
    for {
      addressLine1 <- arbitrary[String]
      addressLine2 <- Gen.option(arbitrary[String])
      addressLine3 <- arbitrary[String]
      addressLine4 <- Gen.option(arbitrary[String])
      postalCode   <- Gen.option(arbitrary[String])
      countryCode  <- arbitrary[String]
    } yield AddressNoId(
      addressLine1 = addressLine1,
      addressLine2 = addressLine2,
      addressLine3 = addressLine3,
      addressLine4 = addressLine4,
      postalCode = postalCode,
      countryCode = countryCode
    )
  }

  implicit val arbitraryContactDetails: Arbitrary[ContactDetails] = Arbitrary {
    for {
      phoneNumber  <- Gen.option(arbitrary[String])
      mobileNumber <- Gen.option(arbitrary[String])
      faxNumber    <- Gen.option(arbitrary[String])
      emailAddress <- Gen.option(arbitrary[String])
    } yield ContactDetails(
      phoneNumber = phoneNumber,
      mobileNumber = mobileNumber,
      faxNumber = faxNumber,
      emailAddress = emailAddress
    )
  }

  implicit val arbitraryIdentification: Arbitrary[Identification] = Arbitrary {
    for {
      idNumber           <- arbitrary[String]
      issuingInstitution <- arbitrary[String]
      issuingCountryCode <- arbitrary[String]
    } yield Identification(
      idNumber = idNumber,
      issuingInstitution = issuingInstitution,
      issuingCountryCode = issuingCountryCode
    )
  }

  implicit val arbitraryPayloadRegistrationWithIDResponse: Arbitrary[PayloadRegistrationWithIDResponse] = Arbitrary {
    for {
      registrationWithIDResponse <- arbitrary[RegisterWithIDResponse]
    } yield PayloadRegistrationWithIDResponse(registrationWithIDResponse)
  }

  implicit val arbitraryRegistrationWithIDResponse: Arbitrary[RegisterWithIDResponse] = Arbitrary {
    for {
      responseCommon  <- arbitrary[ResponseCommon]
      responseDetails <- Gen.option(arbitrary[ResponseDetail])
    } yield RegisterWithIDResponse(responseCommon, responseDetails)
  }

  implicit val arbitraryResponseCommon: Arbitrary[ResponseCommon] = Arbitrary {
    for {
      status           <- Gen.oneOf("OK", "NOT_OK")
      statusText       <- Gen.option(arbitrary[String])
      processingDate   <- arbitrary[String]
      returnParameters <- Gen.option(Gen.listOf(arbitrary[ReturnParameters]))
    } yield ResponseCommon(status, statusText, processingDate, returnParameters)
  }

  implicit val arbitraryReturnParameters: Arbitrary[ReturnParameters] = Arbitrary {
    for {
      paramName  <- arbitrary[String]
      paramValue <- arbitrary[String]
    } yield ReturnParameters(paramName, paramValue)
  }

  implicit val arbitraryResponseDetails: Arbitrary[ResponseDetail] = Arbitrary {
    for {
      safeid         <- arbitrary[String]
      arn            <- Gen.option(arbitrary[String])
      isEditable     <- arbitrary[Boolean]
      isAnAgent      <- arbitrary[Boolean]
      isAnASAgent    <- Gen.option(arbitrary[Boolean])
      isAnIndividual <- arbitrary[Boolean]
      partnerDetails <- Gen.oneOf(arbitrary[IndividualResponse], arbitrary[OrganisationResponse])
      address        <- arbitrary[AddressResponse]
      contactDetails <- arbitrary[ContactDetails]
    } yield ResponseDetail(safeid, arn, isEditable, isAnAgent, isAnASAgent, isAnIndividual, partnerDetails, address, contactDetails)
  }

  implicit val arbitraryIndividualResponse: Arbitrary[IndividualResponse] = Arbitrary {
    for {
      firstName   <- arbitrary[String]
      middleName  <- Gen.option(arbitrary[String])
      lastName    <- arbitrary[String]
      dateOfBirth <- Gen.option(arbitrary[String])
    } yield IndividualResponse(firstName, middleName, lastName, dateOfBirth)
  }

  implicit val arbitraryOrganisationResponse: Arbitrary[OrganisationResponse] = Arbitrary {
    for {
      organisationName <- arbitrary[String]
      isAGroup         <- arbitrary[Boolean]
      organisationType <- Gen.option(arbitrary[String])
      code             <- Gen.option(arbitrary[String])
    } yield OrganisationResponse(organisationName, isAGroup, organisationType, code)
  }

  implicit val arbitraryAddressResponse: Arbitrary[AddressResponse] = Arbitrary {
    for {
      addressLine1 <- arbitrary[String]
      addressLine2 <- Gen.option(arbitrary[String])
      addressLine3 <- Gen.option(arbitrary[String])
      addressLine4 <- Gen.option(arbitrary[String])
      postalCode   <- Gen.option(arbitrary[String])
      countryCode  <- arbitrary[String]
    } yield AddressResponse(addressLine1, addressLine2, addressLine3, addressLine4, postalCode, countryCode)
  }
}
