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

import java.time.LocalDate

import pages.{DateOfBirthPage, NamePage}
import play.api.libs.json.{Json, OWrites, Reads}
import play.api.libs.json._

trait BusinessMatchingSubmission

  case class IndividualMatchingSubmission(regime: String,
                                          requiresNameMatch: Boolean,
                                          isAnAgent: Boolean,
                                          individual: Individual) extends BusinessMatchingSubmission

object IndividualMatchingSubmission {
  def apply(userAnswers: UserAnswers): Option[IndividualMatchingSubmission] =
    for {
      name <- userAnswers.get(NamePage)
      dob <- userAnswers.get(DateOfBirthPage)
    } yield {
      IndividualMatchingSubmission("Don't know",
      requiresNameMatch = true,
      isAnAgent = false,
      Individual( name, dob))}

  implicit val format = Json.format[IndividualMatchingSubmission]
}

/*
  case class OrganisationMatchingSubmission(regime: String,
                                            requiresNameMatch: Boolean,
                                            isAnAgent: Boolean,
                                            organisation: Organisation) extends BusinessMatchingSubmission
*/

  case class Individual(name: Name, dateOfBirth: LocalDate)
  object Individual {
    implicit lazy val writes: OWrites[Individual] = OWrites[Individual] {
      individual =>
        Json.obj(
          "firstName" -> individual.name.firstName,
          "lastName" -> individual.name.secondName,
          "dateOfBirth" -> individual.dateOfBirth.toString
        )
    }

    implicit lazy val reads: Reads[Individual] = {
      import play.api.libs.functional.syntax._
      (
        (__ \ "firstName").read[String] and
          (__ \ "lastName").read[String] and
          (__ \ "dateOfBirth").read[LocalDate]
        )((firstName, secondName, dob) => Individual(Name(firstName, secondName), dob))
    }
  }


/*
  //orgName between 1 and 105 "^[a-zA-Z0-9 '&\\/]{1,105}$"
  case class Organisation(organisationName: String, organisationType: BusinessType)
  object OrganisationMatchingSubmission {
    def apply(userAnswers: UserAnswers): Option[OrganisationMatchingSubmission] =
      for {
        name <- userAnswers.get(NamePage)
        dob <- userAnswers.get(DateOfBirthPage)
      } yield IndividualMatchingSubmission("Don't know",
        requiresNameMatch = true,
        isAnAgent = false,
        Individual( name, dob))
  }*/
