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

import helpers.JsonFixtures._
import org.scalatest.{FreeSpec, MustMatchers}
import play.api.libs.json.Json

import scala.util.matching.Regex

class RegisterWithIDRequestSpec extends FreeSpec with MustMatchers {

  "RegisterWithIDRequest" - {
    "marshal from Json Registration with ID" in {
      Json.parse(registerWithIDPayload).validate[PayloadRegisterWithID].get mustBe registrationWithRequest
    }

    "marshal to json" in {
      Json.toJson(registrationWithRequest) mustBe registerWithIDJson
    }

    "response common must generate correct values to spec" in {
      val requestCommon = RequestCommon.forService

      val ackRefLength = requestCommon.acknowledgementReference.length
      ackRefLength >= 1 && ackRefLength <= 32 mustBe true

      requestCommon.regime mustBe "DAC"

      val date: Regex = raw"[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z".r
      date.findAllIn(requestCommon.receiptDate).toList.nonEmpty mustBe true
    }
  }
}
