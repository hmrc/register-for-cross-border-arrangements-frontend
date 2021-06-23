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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class SubscriptionForDACResponseSpec extends SpecBase with ScalaCheckPropertyChecks {

  val returnParameters: ReturnParameters = ReturnParameters("Name", "Value")
  val responseCommon: ResponseCommon = ResponseCommon(status = "OK", statusText = None, processingDate = "2020-09-01T01:00:00Z", returnParameters = None)
  val responseDetail: ResponseDetailForDACSubscription = ResponseDetailForDACSubscription(subscriptionID = "XADAC0000123456")

  val dacResponse: CreateSubscriptionForDACResponse = CreateSubscriptionForDACResponse(
    SubscriptionForDACResponse(responseCommon = responseCommon, responseDetail = responseDetail)
  )
  val dacResponseFull: CreateSubscriptionForDACResponse = CreateSubscriptionForDACResponse(
    SubscriptionForDACResponse(
      responseCommon = responseCommon.copy(statusText = Some("status"), returnParameters = Some(Seq(returnParameters))),
      responseDetail = responseDetail)
  )

  "CreateSubscriptionForDACResponse" - {

    "must deserialise CreateSubscriptionForDACResponse" in {

      val jsonPayload: String =
        """
          |{
          |  "createSubscriptionForDACResponse": {
          |    "responseCommon": {
          |      "status": "OK",
          |      "statusText": "status",
          |      "processingDate": "2020-09-01T01:00:00Z",
          |      "returnParameters": [{
          |        "paramName":"Name",
          |        "paramValue":"Value"
          |      }]
          |    },
          |    "responseDetail": {
          |      "subscriptionID": "XADAC0000123456"
          |    }
          |  }
          |}
          |""".stripMargin

      Json.parse(jsonPayload).validate[CreateSubscriptionForDACResponse].get mustBe dacResponseFull

    }

    "must serialise CreateSubscriptionForDACResponse" in {
      val json = Json.obj(
        "createSubscriptionForDACResponse" -> Json.obj(
          "responseCommon" -> Json.obj(
            "status" -> "OK",
            "statusText" -> "status",
            "processingDate" -> "2020-09-01T01:00:00Z",
            "returnParameters" -> Json.arr(
              Json.obj(
                "paramName" -> "Name",
                "paramValue" -> "Value"
              )
            )
          ),
          "responseDetail" -> Json.obj(
            "subscriptionID" -> "XADAC0000123456"
          )
        )
      )

      Json.toJson(dacResponseFull) mustBe json
    }

    "must deserialise CreateSubscriptionForDACResponse - not including null values" in {

      val jsonPayload: String =
        """
          |{
          |  "createSubscriptionForDACResponse": {
          |    "responseCommon": {
          |      "status": "OK",
          |      "processingDate": "2020-09-01T01:00:00Z"
          |    },
          |    "responseDetail": {
          |      "subscriptionID": "XADAC0000123456"
          |    }
          |  }
          |}
          |""".stripMargin

      Json.parse(jsonPayload).validate[CreateSubscriptionForDACResponse].get mustBe dacResponse

    }

    "must serialise CreateSubscriptionForDACResponse - not including null values" in {
      val json = Json.obj(
        "createSubscriptionForDACResponse" -> Json.obj(
          "responseCommon" -> Json.obj(
            "status" -> "OK",
            "processingDate" -> "2020-09-01T01:00:00Z"
          ),
          "responseDetail" -> Json.obj(
            "subscriptionID" -> "XADAC0000123456"
          )
        )
      )

      Json.toJson(dacResponse) mustBe json
    }
  }
}
