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

package models

import base.SpecBase
import play.api.libs.json.{JsResultException, JsString, Json}

class OrganisationTypeSpec extends SpecBase {

  "OrganisationType reads correct BusinessType" - {
    "when a BusinessType is provided" in {
      OrganisationType(BusinessType.Partnership).value mustBe "Partnership"
      OrganisationType(BusinessType.CorporateBody).value mustBe "Corporate Body"
      OrganisationType(BusinessType.UnIncorporatedBody).value mustBe "Unincorporated Body"
      OrganisationType(BusinessType.LimitedLiability).value mustBe "LLP"
      OrganisationType(BusinessType.NotSpecified).value mustBe "Not Specified"
    }
  }
  "OrganisationType.formats.reads" - {
    "must generate the correct scala BusinessType instance from the JSON string representation" - {
      "when a valid value is provided" in {
        JsString("Partnership").as[OrganisationType](OrganisationType.formats) mustBe Partnership
        JsString("LLP").as[OrganisationType](OrganisationType.formats) mustBe LLP
        JsString("Corporate Body").as[OrganisationType](OrganisationType.formats) mustBe CorporateBody
        JsString("Unincorporated Body").as[OrganisationType](OrganisationType.formats) mustBe UnincorporatedBody
        JsString("Not Specified").as[OrganisationType](OrganisationType.formats) mustBe NotSpecified
      }
    }
    "must throw an JsResultException in response to an invalid string value" in {
      val ex = the[JsResultException] thrownBy JsString("April").as[OrganisationType](OrganisationType.formats)
      ex.getMessage must include("Invalid OrganisationType value")
    }
  }
  "OrganisationType.formats.writes" - {
    "must generate a single string JSON representation from the scala enum" - {
      "for each possible value" in {
        Json.toJson("Partnership") mustBe JsString("Partnership")
        Json.toJson("LLP") mustBe JsString("LLP")
        Json.toJson("Corporate Body") mustBe JsString("Corporate Body")
        Json.toJson("Unincorporated Body") mustBe JsString("Unincorporated Body")
        Json.toJson("Not Specified") mustBe JsString("Not Specified")
      }
    }
  }
}
