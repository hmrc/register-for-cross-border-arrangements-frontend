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

import base.SpecBase
import generators.Generators
import helpers.JsonFixtures._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{ContactEmailAddressPage, ContactNamePage, DoYouLiveInTheUKPage, NonUkNamePage, SafeIDPage}
import play.api.libs.json.{JsString, Json}

import scala.util.matching.Regex

class SubscriptionForDACRequestSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  val requestParameter = Seq(RequestParameter("Name", "Value"))

  val requestCommon: RequestCommonForSubscription = RequestCommonForSubscription(
    regime = "DAC",
    receiptDate = "2020-09-23T16:12:11Z",
    acknowledgementReference = "AB123c",
    originatingSystem = "MDTP",
    requestParameters = Some(requestParameter)
  )

  private def requestDetail(primaryContact: PrimaryContact, secondaryContact: Option[SecondaryContact] = None): RequestDetail = {
    RequestDetail(
      IDType = "idType",
      IDNumber = "idNumber",
      tradingName = None,
      isGBUser = true,
      primaryContact = primaryContact,
      secondaryContact = secondaryContact)
  }

  "CreateSubscriptionForDACRequest" - {

    "must deserialise CreateSubscriptionForDACRequest (with Request parameters) for an individual without a secondary contact" in {

      forAll(validPersonalName, validPersonalName, validEmailAddress) {
        (firstName, lastName, primaryEmail) =>

          val primaryContactForInd: PrimaryContact = PrimaryContact(
            ContactInformationForIndividual(IndividualDetails(firstName, None, lastName), primaryEmail, None, None)
          )

          val indRequest: CreateSubscriptionForDACRequest = CreateSubscriptionForDACRequest(
            SubscriptionForDACRequest(
              requestCommon = requestCommon,
              requestDetail = requestDetail(primaryContactForInd))
          )

          val jsonPayload = jsonPayloadForInd(JsString(firstName), JsString(lastName), JsString(primaryEmail))

          Json.parse(jsonPayload).validate[CreateSubscriptionForDACRequest].get mustBe indRequest
      }
    }

    "must deserialise CreateSubscriptionForDACRequest (with Request parameters) for an organisation without a secondary contact" in {

      forAll(validBusinessName, validEmailAddress, validPhoneNumber) {
        (organisationName, primaryEmail, phoneNumber) =>

          val primaryContactForOrg: PrimaryContact = PrimaryContact(
            ContactInformationForOrganisation(OrganisationDetails(organisationName), primaryEmail, Some(phoneNumber), Some(phoneNumber))
          )

          val orgRequest: CreateSubscriptionForDACRequest = CreateSubscriptionForDACRequest(
            SubscriptionForDACRequest(
              requestCommon = requestCommon,
              requestDetail = requestDetail(primaryContact = primaryContactForOrg))
          )

          val jsonPayload = jsonPayloadForOrg(JsString(organisationName), JsString(primaryEmail), JsString(phoneNumber))

          Json.parse(jsonPayload).validate[CreateSubscriptionForDACRequest].get mustBe orgRequest
      }

    }

    "must deserialise CreateSubscriptionForDACRequest for an individual with a secondary contact" in {

      forAll(validPersonalName, validPersonalName, validBusinessName, validEmailAddress, validEmailAddress, validPhoneNumber) {
        (firstName, lastName, organisationName, primaryEmail, secondaryEmail, phoneNumber) =>

          val primaryContactForInd: PrimaryContact = PrimaryContact(
            ContactInformationForIndividual(IndividualDetails(firstName, None, lastName), primaryEmail, None, None)
          )

          val secondaryContactForInd: SecondaryContact = SecondaryContact(
            ContactInformationForOrganisation(OrganisationDetails(organisationName), secondaryEmail, Some(phoneNumber), Some(phoneNumber))
          )

          val indWithSecondaryContact: CreateSubscriptionForDACRequest = CreateSubscriptionForDACRequest(
            SubscriptionForDACRequest(
              requestCommon = requestCommon.copy(requestParameters = None),
              requestDetail = requestDetail(primaryContact = primaryContactForInd, secondaryContact = Some(secondaryContactForInd)))
          )

          val jsonPayload = jsonPayloadForIndWithSecondaryContact(JsString(firstName), JsString(lastName), JsString(organisationName),
            JsString(primaryEmail), JsString(secondaryEmail), JsString(phoneNumber))

          Json.parse(jsonPayload).validate[CreateSubscriptionForDACRequest].get mustBe indWithSecondaryContact
      }

    }

    "must deserialise CreateSubscriptionForDACRequest for an organisation with a secondary contact" in {

      forAll(validPersonalName, validPersonalName, validBusinessName, validEmailAddress, validEmailAddress, validPhoneNumber) {
        (firstName, lastName, organisationName, primaryEmail, secondaryEmail, phoneNumber) =>

          val primaryContactForOrg: PrimaryContact = PrimaryContact(
            ContactInformationForOrganisation(OrganisationDetails(organisationName), primaryEmail, Some(phoneNumber), Some(phoneNumber))
          )

          val secondaryContactForOrg: SecondaryContact = SecondaryContact(
            ContactInformationForIndividual(IndividualDetails(firstName, None, lastName), secondaryEmail, None, None)
          )

          val orgWithSecondaryContact: CreateSubscriptionForDACRequest = CreateSubscriptionForDACRequest(
            SubscriptionForDACRequest(
              requestCommon = requestCommon.copy(requestParameters = None),
              requestDetail = requestDetail(primaryContact = primaryContactForOrg, secondaryContact = Some(secondaryContactForOrg)))
          )

          val jsonPayload = jsonPayloadForOrgWithSecondaryContact(JsString(firstName), JsString(lastName), JsString(organisationName),
            JsString(primaryEmail), JsString(secondaryEmail), JsString(phoneNumber))

          Json.parse(jsonPayload).validate[CreateSubscriptionForDACRequest].get mustBe orgWithSecondaryContact
      }
    }

    "must serialise subscription request for individual - exclude null fields for optional contact details" in {

      forAll(validPersonalName, validPersonalName, validEmailAddress) {
        (firstName, lastName, primaryEmail) =>

          val primaryContactForInd: PrimaryContact = PrimaryContact(
            ContactInformationForIndividual(IndividualDetails(firstName, None, lastName), primaryEmail, None, None)
          )

          val indRequest: CreateSubscriptionForDACRequest = CreateSubscriptionForDACRequest(
            SubscriptionForDACRequest(
              requestCommon = requestCommon,
              requestDetail = requestDetail(primaryContact = primaryContactForInd))
          )

          Json.toJson(indRequest) mustBe indRequestJson(firstName, lastName, primaryEmail)
      }
    }

    "must serialise subscription request for organisation - exclude null fields for optional contact details" in {

      forAll(validBusinessName, validEmailAddress, validPhoneNumber) {
        (organisationName, primaryEmail, phoneNumber) =>

          val primaryContactForOrg: PrimaryContact = PrimaryContact(
            ContactInformationForOrganisation(OrganisationDetails(organisationName), primaryEmail, Some(phoneNumber), Some(phoneNumber))
          )

          val orgRequest: CreateSubscriptionForDACRequest = CreateSubscriptionForDACRequest(
            SubscriptionForDACRequest(
              requestCommon = requestCommon,
              requestDetail = requestDetail(primaryContact = primaryContactForOrg))
          )

          Json.toJson(orgRequest) mustBe orgRequestJson(organisationName, primaryEmail, phoneNumber)
      }
    }

    "must serialise subscription request for individual - exclude null fields for requestParameters and contact numbers" in {

      forAll(validPersonalName, validPersonalName, validBusinessName, validEmailAddress, validEmailAddress, validPhoneNumber) {
        (firstName, lastName, organisationName, primaryEmail, secondaryEmail, phoneNumber) =>

          val primaryContactForInd: PrimaryContact = PrimaryContact(
            ContactInformationForIndividual(IndividualDetails(firstName, None, lastName), primaryEmail, None, None)
          )

          val secondaryContactForInd: SecondaryContact = SecondaryContact(
            ContactInformationForOrganisation(OrganisationDetails(organisationName), secondaryEmail, Some(phoneNumber), Some(phoneNumber))
          )

          val indWithSecondaryContact: CreateSubscriptionForDACRequest = CreateSubscriptionForDACRequest(
            SubscriptionForDACRequest(
              requestCommon = requestCommon.copy(requestParameters = None),
              requestDetail = requestDetail(primaryContact = primaryContactForInd, secondaryContact = Some(secondaryContactForInd)))
          )

          Json.toJson(indWithSecondaryContact) mustBe
            indWithSecondaryContactJson(firstName, lastName, organisationName, primaryEmail, secondaryEmail, phoneNumber)
      }
    }

    "must serialise subscription request for organisation - exclude null fields for requestParameters and contact numbers" in {

      forAll(validPersonalName, validPersonalName, validBusinessName, validEmailAddress, validEmailAddress, validPhoneNumber) {
        (firstName, lastName, organisationName, primaryEmail, secondaryEmail, phoneNumber) =>

          val primaryContactForOrg: PrimaryContact = PrimaryContact(
            ContactInformationForOrganisation(OrganisationDetails(organisationName), primaryEmail, Some(phoneNumber), Some(phoneNumber))
          )

          val secondaryContactForOrg: SecondaryContact = SecondaryContact(
            ContactInformationForIndividual(IndividualDetails(firstName, None, lastName), secondaryEmail, None, None)
          )

          val requestDetail: RequestDetail = RequestDetail(
            IDType = "idType",
            IDNumber = "idNumber",
            tradingName = None,
            isGBUser = true,
            primaryContact = primaryContactForOrg,
            secondaryContact = None)

          val orgWithSecondaryContact: CreateSubscriptionForDACRequest = CreateSubscriptionForDACRequest(
            SubscriptionForDACRequest(
              requestCommon = requestCommon.copy(requestParameters = None),
              requestDetail = requestDetail.copy(secondaryContact = Some(secondaryContactForOrg)))
          )

          Json.toJson(orgWithSecondaryContact) mustBe
            orgWithSecondaryContactJson(firstName, lastName, organisationName, primaryEmail, secondaryEmail, phoneNumber)
      }
    }

    "must serialise RequestCommon" in {

      val json = Json.obj(
        "regime" -> "DAC",
        "receiptDate" -> "2020-09-23T16:12:11Z",
        "acknowledgementReference" -> "AB123c",
        "originatingSystem" -> "MDTP",
        "requestParameters" -> Json.arr(
          Json.obj(
            "paramName" -> "Name",
            "paramValue" -> "Value"
          )
        )
      )

      Json.toJson(requestCommon) mustBe json
    }

    "must have a request common per spec" in {
      val userAnswers = UserAnswers("")
      val updatedUserAnswers = userAnswers
        .set(SafeIDPage, "a").success.value
        .set(ContactEmailAddressPage, "hello").success.value
        .set(ContactNamePage, "Name Name").success.value

      val requestCommon = SubscriptionForDACRequest.createSubscription(updatedUserAnswers).requestCommon
      val ackRefLength = requestCommon.acknowledgementReference.length
      ackRefLength >= 1 && ackRefLength <= 32 mustBe true

      requestCommon.regime mustBe "DAC"

      val date: Regex = raw"[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z".r
      date.findAllIn(requestCommon.receiptDate).toList.nonEmpty mustBe true

      requestCommon.originatingSystem mustBe "MDTP"
    }

    "must serialise RequestDetail - not displaying null fields for secondary contact" in {

      val primaryContactForOrg: PrimaryContact = PrimaryContact(
        ContactInformationForOrganisation(OrganisationDetails("Pizza for you"), "email@email.com", Some("0191 111 2222"), Some("07111111111"))
      )

      val json = Json.obj(
        "IDType" -> "idType",
        "IDNumber" -> "idNumber",
        "isGBUser" -> true,
        "primaryContact" -> Json.obj(
          "organisation" -> Json.obj(
            "organisationName" -> "Pizza for you"
          ),
          "email" -> "email@email.com",
          "phone" -> "0191 111 2222",
          "mobile" -> "07111111111"
        )
      )

      Json.toJson(requestDetail(primaryContact = primaryContactForOrg)) mustBe json
    }

    "must create a request with the isGBUser flag set to true by DoYouLiveInTheUK" in {
        val userAnswers = UserAnswers("")
        val updatedUserAnswers = userAnswers
          .set(SafeIDPage, "a").success.value
          .set(ContactEmailAddressPage, "hello").success.value
          .set(ContactNamePage, "Name Name").success.value
          .set(DoYouLiveInTheUKPage, true).success.value
          .set(NonUkNamePage, Name("fred", "smith")).success.value

        val request = SubscriptionForDACRequest.createSubscription(updatedUserAnswers).requestDetail

        request.isGBUser mustBe true
      }

    "must create a request with the isGBUser flag set to false by DoYouLiveInTheUK" in {
      val userAnswers = UserAnswers("")
      val updatedUserAnswers = userAnswers
        .set(SafeIDPage, "a").success.value
        .set(ContactEmailAddressPage, "hello").success.value
        .set(ContactNamePage, "Name Name").success.value
        .set(DoYouLiveInTheUKPage, false).success.value
        .set(NonUkNamePage, Name("fred", "smith")).success.value

      val request = SubscriptionForDACRequest.createSubscription(updatedUserAnswers).requestDetail

      request.isGBUser mustBe false
    }

    "must create a request with the isGBUser flag set to false by NonUkName" in {
      val userAnswers = UserAnswers("")
      val updatedUserAnswers = userAnswers
        .set(SafeIDPage, "a").success.value
        .set(ContactEmailAddressPage, "hello").success.value
        .set(ContactNamePage, "Name Name").success.value
        .set(NonUkNamePage, Name("fred", "smith")).success.value

      val request = SubscriptionForDACRequest.createSubscription(updatedUserAnswers).requestDetail

      request.isGBUser mustBe false
    }

    "must create a request with the isGBUser flag set to true when criteria is missing" in {
      val userAnswers = UserAnswers("")
      val updatedUserAnswers = userAnswers
        .set(SafeIDPage, "a").success.value
        .set(ContactEmailAddressPage, "hello").success.value
        .set(ContactNamePage, "Name Name").success.value

      val request = SubscriptionForDACRequest.createSubscription(updatedUserAnswers).requestDetail

      request.isGBUser mustBe true
    }
  }
}
