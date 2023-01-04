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

import base.SpecBase
import play.api.libs.json.{JsValue, Json}

class ErrorDetailSpec extends SpecBase {

  "ErrorDetail" - {

    "de-serialise json" in {

      val expectedResult = ErrorDetail("2022-01-19T09:13:34.453Z",
                                       None,
                                       "503",
                                       "Request could not be processed",
                                       "Back End",
                                       SourceDetail(Vector("001 - Request could not be processed"))
      )

      val json: JsValue =
        Json.parse("""
          |{"source":"Back End","timestamp":"2022-01-19T09:13:34.453Z","errorMessage":"Request could not be processed","sourceFaultDetail":{"detail":["001 - Request could not be processed"]},"errorCode":"503","correlationId":null}
          |""".stripMargin)

      json.as[ErrorDetail] mustBe expectedResult
    }

  }

}
