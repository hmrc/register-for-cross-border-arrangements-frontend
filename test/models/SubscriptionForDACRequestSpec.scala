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
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{BusinessNamePage, BusinessTypePage, ContactEmailAddressPage, SelfAssessmentUTRPage}
import play.api.libs.json.Json

class SubscriptionForDACRequestSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks with Generators {

  val userAnswers: UserAnswers = UserAnswers("userAnswersId")
    .set(BusinessTypePage, BusinessType.Partnership).success.value
    .set(SelfAssessmentUTRPage, UniqueTaxpayerReference("0123456789")).success.value
    .set(BusinessNamePage, "Pizza for you").success.value
    .set(ContactEmailAddressPage, "email@email.com").success.value

  val requestParameter = Seq(RequestParameter("Name", "Value"))
  val primaryContact: PrimaryContact = PrimaryContact(
    ContactInformationForOrganisation(OrganisationDetails("Pizza for you"), "email@email.com", None, None)
  )
  val secondaryContact: SecondaryContact = SecondaryContact(
    ContactInformationForIndividual(IndividualDetails("Fairy", None, "Liquid"), "email2@email.com", None, None)
  )

  val requestCommon: RequestCommon = RequestCommon(
    regime = "DAC",
    receiptDate = "2020-09-23T16:12:11Z",
    acknowledgementReference = "AB123c",
    originatingSystem = "MDTP",
    requestParameters = Some(requestParameter),
    paramName = "paramName",
    paramValue = "paramValue"
  )

  val requestDetail: RequestDetail = RequestDetail(
    idType = "idType",
    idNumber = "idNumber",
    tradingName = None,
    isGBUser = true,
    primaryContact = primaryContact,
    secondaryContact = None)

  "SubscriptionForDACRequest" - {
    "must serialise values - not displaying null fields for requestParameters and contact numbers" in {

      val json = Json.obj(
        "requestCommon" -> Json.obj(
          "regime" -> "DAC",
          "receiptDate" -> "2020-09-23T16:12:11Z",
          "acknowledgementReference" -> "AB123c",
          "originatingSystem" -> "MDTP",
          "paramName" -> "paramName",
          "paramValue" -> "paramValue"
        ),
        "requestDetail" -> Json.obj(
          "idType" -> "idType",
          "idNumber" -> "idNumber",
          "isGBUser" -> true,
          "primaryContact" -> Json.obj(
            "organisation" -> Json.obj(
              "organisationName" -> "Pizza for you"
            ),
            "email" -> "email@email.com",
          ),
          "secondaryContact" -> Json.obj(
            "individual" -> Json.obj(
              "firstName" -> "Fairy",
              "lastName" -> "Liquid"
            ),
            "email" -> "email2@email.com"
          )
        )
      )


      val dacRequest = SubscriptionForDACRequest(
        requestCommon = requestCommon.copy(requestParameters = None),
        requestDetail = requestDetail.copy(secondaryContact = Some(secondaryContact))
      )

      Json.toJson(dacRequest) mustBe json
    }

    "must serialise requestCommon" in {

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
        ),
        "paramName" -> "paramName",
        "paramValue" -> "paramValue"
      )

      Json.toJson(requestCommon) mustBe json
    }

    "must serialise requestDetail - not displaying null fields for secondary contact" in {

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
          "mobile" -> "07111222333"
        )
      )

      val completePrimaryContact = PrimaryContact(
        ContactInformationForOrganisation(OrganisationDetails("Pizza for you"), "email@email.com", Some("0191 111 2222"), Some("07111222333"))
      )

      val requestDetailUpdated = requestDetail.copy(primaryContact = completePrimaryContact)

      Json.toJson(requestDetailUpdated) mustBe json
    }

  }
}
