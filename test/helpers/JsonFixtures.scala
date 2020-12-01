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

package helpers

import models.{AddressResponse, ContactDetails, IndividualResponse, PayloadRegisterWithID, PayloadRegistrationWithIDResponse, RegisterWithIDRequest, RegisterWithIDResponse, RequestCommon, RequestParameters, RequestWithIDDetails, ResponseCommon, ResponseDetail, ReturnParameters, WithIDIndividual}
import play.api.libs.json.{JsObject, JsString, Json}

object JsonFixtures {

  val registerWithIDPayload: String =
    """
      |{
      |"registerWithIDRequest": {
      |"requestCommon": {
      |"regime": "DAC",
      |"receiptDate": "2016-08-16T15:55:30Z",
      |"acknowledgementReference": "ec031b045855445e96f98a569ds56cd2",
      |"requestParameters": [
      |{
      |"paramName": "REGIME",
      |"paramValue": "DAC"
      |}
      |]
      |},
      |"requestDetail": {
      |"IDType": "NINO",
      |"IDNumber": "0123456789",
      |"requiresNameMatch": true,
      |"isAnAgent": false,
      |"individual": {
      |"firstName": "Fred",
      |"middleName": "Flintstone",
      |"lastName": "Flint",
      |"dateOfBirth": "1999-12-20"
      |}
      |}
      |}
      |}""".stripMargin

  val registrationWithRequest: PayloadRegisterWithID = PayloadRegisterWithID(RegisterWithIDRequest(
    RequestCommon("2016-08-16T15:55:30Z", "DAC", "ec031b045855445e96f98a569ds56cd2",
      Some(Seq(RequestParameters("REGIME", "DAC")))),
    RequestWithIDDetails(
      "NINO",
      "0123456789",
      requiresNameMatch = true,
      isAnAgent = false,
      WithIDIndividual("Fred", Some("Flintstone"), "Flint", "1999-12-20")))
  )

  val registerWithIDJson: JsObject = Json.obj(
    "registerWithIDRequest" -> Json.obj(
      "requestCommon" -> Json.obj(
        "regime" -> "DAC",
        "receiptDate" -> "2016-08-16T15:55:30Z",
        "acknowledgementReference" -> "ec031b045855445e96f98a569ds56cd2",
        "requestParameters" -> Json.arr( Json.obj(
          "paramName" -> "REGIME",
          "paramValue" -> "DAC"
        ))
      ),
      "requestDetail" -> Json.obj(
        "IDType" -> "NINO",
        "IDNumber" -> "0123456789",
        "requiresNameMatch" -> true,
        "isAnAgent" -> false,
        "individual" -> Json.obj(
          "firstName" -> "Fred",
          "middleName" -> "Flintstone",
          "lastName" -> "Flint",
          "dateOfBirth" -> "1999-12-20"
        )
      )
    )
  )

  val withIDResponse: String =
    """
      |{
      |"registerWithIDResponse": {
      |"responseCommon": {
      |"status": "OK",
      |"statusText": "Sample status text",
      |"processingDate": "2016-08-16T15:55:30Z",
      |"returnParameters": [
      |{
      |"paramName":
      |"SAP_NUMBER",
      |"paramValue":
      |"0123456789"
      |}
      |]
      |},
      |"responseDetail": {
      |"SAFEID": "XE0000123456789",
      |"ARN": "WARN8764123",
      |"isEditable": true,
      |"isAnAgent": false,
      |"isAnIndividual": true,
      |"individual": {
      |"firstName": "Ron",
      |"middleName":
      |"Madisson",
      |"lastName": "Burgundy",
      |"dateOfBirth":
      |"1980-12-12"
      |},
      |"address": {
      |"addressLine1": "100 Parliament Street",
      |"addressLine4": "London",
      |"postalCode": "SW1A 2BQ",
      |"countryCode": "GB"
      |},
      |"contactDetails": {
      |"phoneNumber":
      |"1111111",
      |"mobileNumber":
      |"2222222",
      |"faxNumber":
      |"1111111",
      |"emailAddress":
      |"test@test.org"
      |}
      |}
      |}
      |}""".stripMargin

  val payloadModel: PayloadRegistrationWithIDResponse = {
    PayloadRegistrationWithIDResponse(
      RegisterWithIDResponse(
        ResponseCommon(
          "OK",
          Some("Sample status text"),
          "2016-08-16T15:55:30Z",
          Some(Seq(ReturnParameters("SAP_NUMBER", "0123456789")))
        ),
        Some(
          ResponseDetail(
            "XE0000123456789",
            Some("WARN8764123"),
            isEditable = true,
            isAnAgent = false,
            isAnIndividual = true,
            isAnASAgent = None,
            partnerDetails = IndividualResponse(
              "Ron",
              Some("Madisson"),
              "Burgundy",
              Some("1980-12-12")
            ),
            address = AddressResponse("100 Parliament Street",
              None,
              None,
              Some("London"),
              Some("SW1A 2BQ"),
              "GB"
            ),
            contactDetails = ContactDetails(
              Some("1111111"),
              Some("2222222"),
              Some("1111111"),
              Some("test@test.org")
            )
          )
        )
      )
    )
    }

  val withIDResponseJson: JsObject = Json.obj(
    "registerWithIDResponse" -> Json.obj(
      "responseCommon" -> Json.obj(
        "status" -> "OK",
        "statusText" -> "Sample status text",
        "processingDate" -> "2016-08-16T15:55:30Z",
        "returnParameters" -> Json.arr(
          Json.obj(
            "paramName" -> "SAP_NUMBER",
          "paramValue" -> "0123456789"
          )
        )
      ),
      "responseDetail" -> Json.obj(
  "SAFEID" -> "XE0000123456789",
        "ARN" -> "WARN8764123",
        "isEditable" -> true,
        "isAnAgent" -> false,
        "isAnIndividual" -> true,
        "individual" -> Json.obj(
          "firstName" -> "Ron",
          "middleName" -> "Madisson",
          "lastName" -> "Burgundy",
          "dateOfBirth" -> "1980-12-12"
        ),
      "address" -> Json.obj(
        "addressLine1" -> "100 Parliament Street",
        "addressLine4" -> "London",
        "postalCode" -> "SW1A 2BQ",
        "countryCode" -> "GB"
      ),
      "contactDetails" -> Json.obj(
        "phoneNumber" -> "1111111",
        "mobileNumber" -> "2222222",
        "faxNumber" -> "1111111",
        "emailAddress" -> "test@test.org"
      )
    )
  )
  )

  val requestCouldNotBeProcessedResponse: String =
    """
      |{
      |  "errorDetail": {
      |    "source": "Back End",
      |    "timestamp": "2020-11-11T13:19:52.307Z",
      |    "errorMessage": "Request could not be processed",
      |    "errorCode": "503",
      |    "correlationId": "36147652-e594-94a4-a229-23f28e20e841",
      |    "sourceFaultDetail": {
      |      "detail": [
      |        "001 - Request could not be processed"
      |      ]
      |    }
      |  }
      |}""".stripMargin

  val badRequestResponse: String =
    """
      |{
      |  "errorDetail": {
      |    "timestamp" : "2017-02-14T12:58:44Z",
      |    "correlationId": "c181e730-2386-4359-8ee0-f911d6e5f3bc",
      |    "errorCode": "400",
      |    "errorMessage": "Invalid ID",
      |    "source": "Back End",
      |    "sourceFaultDetail":{
      |      "detail":[
      |        "001 - Regime missing or invalid"
      |      ]
      |    }
      | }
      |}""".stripMargin


  /*********************For creating an EIS subscription below *********************/

  def jsonPayloadForInd(firstName: JsString,
                        lastName:JsString,
                        primaryEmail: JsString): String = {
    s"""
       |{
       |  "createSubscriptionForDACRequest": {
       |    "requestCommon": {
       |      "regime": "DAC",
       |      "receiptDate": "2020-09-23T16:12:11Z",
       |      "acknowledgementReference": "AB123c",
       |      "originatingSystem": "MDTP",
       |      "requestParameters": [{
       |        "paramName":"Name",
       |        "paramValue":"Value"
       |      }]
       |    },
       |    "requestDetail": {
       |      "IDType": "idType",
       |      "IDNumber": "idNumber",
       |      "isGBUser": true,
       |      "primaryContact": {
       |        "individual": {
       |          "firstName": $firstName,
       |          "lastName": $lastName
       |        },
       |        "email": $primaryEmail
       |      }
       |    }
       |  }
       |}
       |""".stripMargin
  }

  def jsonPayloadForOrg(organisationName: JsString,
                        primaryEmail: JsString,
                        phone: JsString): String = {
    s"""
      |{
      |  "createSubscriptionForDACRequest": {
      |    "requestCommon": {
      |      "regime": "DAC",
      |      "receiptDate": "2020-09-23T16:12:11Z",
      |      "acknowledgementReference": "AB123c",
      |      "originatingSystem": "MDTP",
      |      "requestParameters": [{
      |        "paramName":"Name",
      |        "paramValue":"Value"
      |      }]
      |    },
      |    "requestDetail": {
      |      "IDType": "idType",
      |      "IDNumber": "idNumber",
      |      "isGBUser": true,
      |      "primaryContact": {
      |        "organisation": {
      |          "organisationName": $organisationName
      |        },
      |        "email": $primaryEmail,
      |        "phone": $phone,
      |        "mobile": $phone
      |      }
      |    }
      |  }
      |}
      |""".stripMargin
  }

  def jsonPayloadForIndWithSecondaryContact(firstName: JsString,
                                            lastName:JsString,
                                            organisationName: JsString,
                                            primaryEmail: JsString,
                                            secondaryEmail: JsString,
                                            phone: JsString): String = {
    s"""
       |{
       |  "createSubscriptionForDACRequest": {
       |    "requestCommon": {
       |      "regime": "DAC",
       |      "receiptDate": "2020-09-23T16:12:11Z",
       |      "acknowledgementReference": "AB123c",
       |      "originatingSystem": "MDTP"
       |    },
       |    "requestDetail": {
       |      "IDType": "idType",
       |      "IDNumber": "idNumber",
       |      "isGBUser": true,
       |      "primaryContact": {
       |        "individual": {
       |          "firstName": $firstName,
       |          "lastName": $lastName
       |        },
       |        "email": $primaryEmail
       |      },
       |      "secondaryContact": {
       |        "organisation": {
       |          "organisationName": $organisationName
       |        },
       |        "email": $secondaryEmail,
       |        "phone": $phone,
       |        "mobile": $phone
       |      }
       |    }
       |  }
       |}
       |""".stripMargin
  }

  def jsonPayloadForOrgWithSecondaryContact(firstName: JsString,
                                            lastName:JsString,
                                            organisationName: JsString,
                                            primaryEmail: JsString,
                                            secondaryEmail: JsString,
                                            phone: JsString): String = {
    s"""
       |{
       |  "createSubscriptionForDACRequest": {
       |    "requestCommon": {
       |      "regime": "DAC",
       |      "receiptDate": "2020-09-23T16:12:11Z",
       |      "acknowledgementReference": "AB123c",
       |      "originatingSystem": "MDTP"
       |    },
       |    "requestDetail": {
       |      "IDType": "idType",
       |      "IDNumber": "idNumber",
       |      "isGBUser": true,
       |      "primaryContact": {
       |        "organisation": {
       |          "organisationName": $organisationName
       |        },
       |        "email": $primaryEmail,
       |        "phone": $phone,
       |        "mobile": $phone
       |      },
       |      "secondaryContact": {
       |        "individual": {
       |          "firstName": $firstName,
       |          "lastName": $lastName
       |        },
       |        "email": $secondaryEmail
       |      }
       |    }
       |  }
       |}
       |""".stripMargin
  }

  def indRequestJson(firstName: String,
                     lastName:String,
                     primaryEmail: String): JsObject = {
    Json.obj(
      "createSubscriptionForDACRequest" -> Json.obj(
        "requestCommon" -> Json.obj(
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
        ),
        "requestDetail" -> Json.obj(
          "IDType" -> "idType",
          "IDNumber" -> "idNumber",
          "isGBUser" -> true,
          "primaryContact" -> Json.obj(
            "individual" -> Json.obj(
              "firstName" -> firstName,
              "lastName" -> lastName
            ),
            "email" -> primaryEmail
          )
        )
      )
    )
  }

  def orgRequestJson(organisationName: String,
                     primaryEmail: String,
                     phone: String): JsObject = {
    Json.obj(
      "createSubscriptionForDACRequest" -> Json.obj(
        "requestCommon" -> Json.obj(
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
        ),
        "requestDetail" -> Json.obj(
          "IDType" -> "idType",
          "IDNumber" -> "idNumber",
          "isGBUser" -> true,
          "primaryContact" -> Json.obj(
            "organisation" -> Json.obj(
              "organisationName" -> organisationName
            ),
            "email" -> primaryEmail,
            "phone" -> phone,
            "mobile" -> phone
          )
        )
      )
    )
  }

  def indWithSecondaryContactJson(firstName: String,
                                  lastName:String,
                                  organisationName: String,
                                  primaryEmail: String,
                                  secondaryEmail: String,
                                  phone: String): JsObject = {
    Json.obj(
      "createSubscriptionForDACRequest" -> Json.obj(
        "requestCommon" -> Json.obj(
          "regime" -> "DAC",
          "receiptDate" -> "2020-09-23T16:12:11Z",
          "acknowledgementReference" -> "AB123c",
          "originatingSystem" -> "MDTP"
        ),
        "requestDetail" -> Json.obj(
          "IDType" -> "idType",
          "IDNumber" -> "idNumber",
          "isGBUser" -> true,
          "primaryContact" -> Json.obj(
            "individual" -> Json.obj(
              "firstName" -> firstName,
              "lastName" -> lastName
            ),
            "email" -> primaryEmail
          ),
          "secondaryContact" -> Json.obj(
            "organisation" -> Json.obj(
              "organisationName" -> organisationName
            ),
            "email" -> secondaryEmail,
            "phone" -> phone,
            "mobile" -> phone
          )
        )
      )
    )
  }

  def orgWithSecondaryContactJson(firstName: String,
                                  lastName:String,
                                  organisationName: String,
                                  primaryEmail: String,
                                  secondaryEmail: String,
                                  phone: String): JsObject = {
    Json.obj(
      "createSubscriptionForDACRequest" -> Json.obj(
        "requestCommon" -> Json.obj(
          "regime" -> "DAC",
          "receiptDate" -> "2020-09-23T16:12:11Z",
          "acknowledgementReference" -> "AB123c",
          "originatingSystem" -> "MDTP"
        ),
        "requestDetail" -> Json.obj(
          "IDType" -> "idType",
          "IDNumber" -> "idNumber",
          "isGBUser" -> true,
          "primaryContact" -> Json.obj(
            "organisation" -> Json.obj(
              "organisationName" -> organisationName
            ),
            "email" -> primaryEmail,
            "phone" -> phone,
            "mobile" -> phone
          ),
          "secondaryContact" -> Json.obj(
            "individual" -> Json.obj(
              "firstName" -> firstName,
              "lastName" -> lastName
            ),
            "email" -> secondaryEmail
          )
        )
      )
    )
  }

  val registerWithoutIDResponse: String =
    """
      |{
      |  "registerWithoutIDResponse": {
      |    "responseCommon": {
      |      "status": "OK",
      |      "statusText": "Success",
      |      "processingDate": "2020-09-01T01:00:00Z"
      |    },
      |    "responseDetail": {
      |      "SAFEID": "XE0000123456789"
      |    }
      |  }
      |}
      |""".stripMargin

  val registerWithoutIDResponseJson: JsObject = Json.obj(
    "registerWithoutIDResponse" -> Json.obj(
      "responseCommon" -> Json.obj(
        "status" -> "OK",
        "statusText" -> "Success",
        "processingDate" -> "2020-09-01T01:00:00Z"
      ),
      "responseDetail" -> Json.obj(
        "SAFEID" -> "XE0000123456789"
      )
    )
  )

}
