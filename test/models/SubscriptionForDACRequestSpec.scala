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

import generators.Generators
import helpers.JsonFixtures._
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class SubscriptionForDACRequestSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks with Generators {

  val requestParameter = Seq(RequestParameter("Name", "Value"))
  val primaryContactForInd: PrimaryContact = PrimaryContact(
    ContactInformationForIndividual(IndividualDetails("Fairy", None, "Liquid"), "email2@email.com", None, None)
  )
  val primaryContactForOrg: PrimaryContact = PrimaryContact(
    ContactInformationForOrganisation(OrganisationDetails("Pizza for you"), "email@email.com", Some("0191 111 2222"), Some("07111111111"))
  )
  val secondaryContactForInd: SecondaryContact = SecondaryContact(
    ContactInformationForOrganisation(OrganisationDetails("Pizza for you"), "email@email.com", Some("0191 111 2222"), Some("07111111111"))
  )
  val secondaryContactForOrg: SecondaryContact = SecondaryContact(
    ContactInformationForIndividual(IndividualDetails("Fairy", None, "Liquid"), "email2@email.com", None, None)
  )

  val requestCommon: RequestCommonForSubscription = RequestCommonForSubscription(
    regime = "DAC",
    receiptDate = "2020-09-23T16:12:11Z",
    acknowledgementReference = "AB123c",
    originatingSystem = "MDTP",
    requestParameters = Some(requestParameter)
  )

  val requestDetail: RequestDetail = RequestDetail(
    idType = "idType",
    idNumber = "idNumber",
    tradingName = None,
    isGBUser = true,
    primaryContact = primaryContactForOrg,
    secondaryContact = None)


  val indRequest: CreateSubscriptionForDACRequest = CreateSubscriptionForDACRequest(
    SubscriptionForDACRequest(
      requestCommon = requestCommon,
      requestDetail = requestDetail.copy(primaryContact = primaryContactForInd))
  )

  val orgRequest: CreateSubscriptionForDACRequest = CreateSubscriptionForDACRequest(
    SubscriptionForDACRequest(
      requestCommon = requestCommon,
      requestDetail = requestDetail)
  )

  val indWithSecondaryContact: CreateSubscriptionForDACRequest = CreateSubscriptionForDACRequest(
    SubscriptionForDACRequest(
      requestCommon = requestCommon.copy(requestParameters = None),
      requestDetail = requestDetail.copy(primaryContact = primaryContactForInd, secondaryContact = Some(secondaryContactForInd)))
  )

  val orgWithSecondaryContact: CreateSubscriptionForDACRequest = CreateSubscriptionForDACRequest(
    SubscriptionForDACRequest(
      requestCommon = requestCommon.copy(requestParameters = None),
      requestDetail = requestDetail.copy(secondaryContact = Some(secondaryContactForOrg)))
  )

  "CreateSubscriptionForDACRequest" - {

    "must deserialise CreateSubscriptionForDACRequest (with Request parameters) for an individual without a secondary contact" in {
      Json.parse(jsonPayloadForInd).validate[CreateSubscriptionForDACRequest].get mustBe indRequest
    }

    "must deserialise CreateSubscriptionForDACRequest (with Request parameters) for an organisation without a secondary contact" in {
      Json.parse(jsonPayloadForOrg).validate[CreateSubscriptionForDACRequest].get mustBe orgRequest
    }

    "must deserialise CreateSubscriptionForDACRequest for an individual with a secondary contact" in {
      Json.parse(jsonPayloadForIndWithSecondaryContact).validate[CreateSubscriptionForDACRequest].get mustBe indWithSecondaryContact
    }

    "must deserialise CreateSubscriptionForDACRequest for an organisation with a secondary contact" in {
      Json.parse(jsonPayloadForOrgWithSecondaryContact).validate[CreateSubscriptionForDACRequest].get mustBe orgWithSecondaryContact
    }

    "must serialise subscription request for individual - exclude null fields for optional contact details" in {
      Json.toJson(indRequest) mustBe indRequestJson
    }

    "must serialise subscription request for organisation - exclude null fields for optional contact details" in {
      Json.toJson(orgRequest) mustBe orgRequestJson
    }

    "must serialise subscription request for individual - exclude null fields for requestParameters and contact numbers" in {
      Json.toJson(indWithSecondaryContact) mustBe indWithSecondaryContactJson
    }

    "must serialise subscription request for organisation - exclude null fields for requestParameters and contact numbers" in {
      Json.toJson(orgWithSecondaryContact) mustBe orgWithSecondaryContactJson
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

    "must serialise RequestDetail - not displaying null fields for secondary contact" in {

      val json = Json.obj(
        "idType" -> "idType",
        "idNumber" -> "idNumber",
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

      Json.toJson(requestDetail) mustBe json
    }

  }
}
