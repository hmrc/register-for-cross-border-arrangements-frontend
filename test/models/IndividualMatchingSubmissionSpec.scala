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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsObject, JsSuccess, Json}

class IndividualMatchingSubmissionSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

   "IndividualMatchingSubmission mode" - {
     "must serialise" in {
       forAll(arbitrary[IndividualMatchingSubmission]) {
         individualMatchingSubmission =>
          val json = generateSubmissionJson(individualMatchingSubmission)
           json.validate[IndividualMatchingSubmission] mustEqual JsSuccess(individualMatchingSubmission)
       }
     }
   }

  def generateSubmissionJson(ims: IndividualMatchingSubmission) : JsObject =
    Json.obj("regime" -> ims.regime,
    "requiresNameMatch" -> ims.requiresNameMatch,
    "isAnAgent" -> ims.isAnAgent,
    "individual" -> Json.obj(
      "firstName" -> ims.individual.name.firstName ,
      "lastName" -> ims.individual.name.secondName,
      "dateOfBirth" -> ims.individual.dateOfBirth.toString
    ))
}
