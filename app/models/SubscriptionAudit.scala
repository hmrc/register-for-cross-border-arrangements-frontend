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

import play.api.libs.json.Json

case class SubscriptionAudit(SAFEID: String,
                             tradingName: Option[String],
                             isGBUser: Boolean,
                             primaryContact: PrimaryContact,
                             secondaryContact: Option[SecondaryContact]
)

object SubscriptionAudit {

  def fromRequestDetail(details: RequestDetail): SubscriptionAudit =
    SubscriptionAudit(
      details.IDNumber,
      details.tradingName,
      details.isGBUser,
      details.primaryContact,
      details.secondaryContact
    )

  implicit val format = Json.format[SubscriptionAudit]
}
