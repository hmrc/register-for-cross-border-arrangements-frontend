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

import play.api.libs.json._

sealed trait OrganisationType {
  def value: String
}

object OrganisationType {

  implicit val formats: Format[OrganisationType] = new Format[OrganisationType] {

    override def reads(json: JsValue): JsResult[OrganisationType] =
      json.asOpt[String] match {
        case Some("Partnership")         => JsSuccess(Partnership)
        case Some("LLP")                 => JsSuccess(LLP)
        case Some("Corporate Body")      => JsSuccess(CorporateBody)
        case Some("Unincorporated Body") => JsSuccess(UnincorporatedBody)
        case Some("Not Specified")       => JsSuccess(NotSpecified)
        case _                           => JsError("Invalid OrganisationType value")
      }
    override def writes(businessType: OrganisationType): JsValue = JsString(businessType.value)
  }

  def apply(businessType: BusinessType): OrganisationType = businessType match {
    case BusinessType.CorporateBody      => CorporateBody
    case BusinessType.LimitedLiability   => LLP
    case BusinessType.Partnership        => Partnership
    case BusinessType.UnIncorporatedBody => UnincorporatedBody
    case BusinessType.NotSpecified       => NotSpecified
  }
}

case object CorporateBody extends OrganisationType {
  def value = "Corporate Body"
}

case object LLP extends OrganisationType {
  def value = "LLP"
}

case object Partnership extends OrganisationType {
  def value = "Partnership"
}

case object UnincorporatedBody extends OrganisationType {
  def value = "Unincorporated Body"
}

case object NotSpecified extends OrganisationType {
  def value = "Not Specified"
}
