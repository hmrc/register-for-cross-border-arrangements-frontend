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

import java.time.LocalDate

import pages._
import play.api.libs.json.{Json, OWrites, Reads, _}

trait IndividualAndBusinessMatchingSubmission

case class IndividualMatchingSubmission(regime: String, requiresNameMatch: Boolean, isAnAgent: Boolean, individual: Individual)
    extends IndividualAndBusinessMatchingSubmission

object IndividualMatchingSubmission {

  def apply(userAnswers: UserAnswers): Option[IndividualMatchingSubmission] =
    for {
      name <- userAnswers.get(NamePage)
      dob  <- userAnswers.get(DateOfBirthPage)
    } yield IndividualMatchingSubmission("DAC", requiresNameMatch = true, isAnAgent = false, Individual(name, dob))

  implicit val format: OFormat[IndividualMatchingSubmission] = Json.format[IndividualMatchingSubmission]
}

case class Individual(name: Name, dateOfBirth: LocalDate)

object Individual {

  implicit lazy val writes: OWrites[Individual] = OWrites[Individual] {
    individual =>
      Json.obj(
        "firstName"   -> individual.name.firstName,
        "lastName"    -> individual.name.secondName,
        "dateOfBirth" -> individual.dateOfBirth.toString
      )
  }

  implicit lazy val reads: Reads[Individual] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "firstName").read[String] and
        (__ \ "lastName").read[String] and
        (__ \ "dateOfBirth").read[LocalDate]
    )(
      (firstName, secondName, dob) => Individual(Name(firstName, secondName), dob)
    )
  }
}

//orgName between 1 and 105 "^[a-zA-Z0-9 '&\\/]{1,105}$"
case class Organisation(organisationName: String, organisationType: BusinessType)

object Organisation {

  implicit lazy val writes: OWrites[Organisation] = OWrites[Organisation] {
    organisation =>
      Json.obj(
        "organisationName" -> organisation.organisationName,
        "organisationType" -> OrganisationType(organisation.organisationType).value
      )
  }

  implicit lazy val reads: Reads[Organisation] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "organisationName").read[String] and
        (__ \ "organisationType").read[BusinessType]
    )(
      (organisationName, organisationType) => Organisation(organisationName, organisationType)
    )
  }

}

case class BusinessMatchingSubmission(regime: String, requiresNameMatch: Boolean, isAnAgent: Boolean, organisation: Organisation)
    extends IndividualAndBusinessMatchingSubmission

object BusinessMatchingSubmission {

  def apply(userAnswers: UserAnswers): Option[BusinessMatchingSubmission] =
    for {
      businessName <- getBusinessName(userAnswers)
      businessType <- userAnswers.get(BusinessTypePage)
    } yield BusinessMatchingSubmission(regime = "DAC", requiresNameMatch = true, isAnAgent = false, Organisation(businessName, businessType))

  private def getBusinessName(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(BusinessTypePage) match {
      case Some(BusinessType.NotSpecified) =>
        userAnswers.get(SoleTraderNamePage).map {
          name => s"${name.firstName} ${name.secondName}"
        }
      case _ => userAnswers.get(BusinessNamePage)
    }

  implicit val format: OFormat[BusinessMatchingSubmission] = Json.format[BusinessMatchingSubmission]

}
