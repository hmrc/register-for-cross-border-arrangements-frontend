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

package models.readSubscription

import models.ResponseCommon
import play.api.libs.json._

case class ResponseDetailForReadSubscription(subscriptionID: String,
                                             tradingName: Option[String],
                                             isGBUser: Boolean,
                                             primaryContact: PrimaryContact,
                                             secondaryContact: Option[SecondaryContact]
)

object ResponseDetailForReadSubscription {
  implicit val format: OFormat[ResponseDetailForReadSubscription] = Json.format[ResponseDetailForReadSubscription]
}

case class ReadSubscriptionForDACResponse(responseCommon: ResponseCommon, responseDetail: ResponseDetailForReadSubscription)

object ReadSubscriptionForDACResponse {
  implicit val format: OFormat[ReadSubscriptionForDACResponse] = Json.format[ReadSubscriptionForDACResponse]
}

case class DisplaySubscriptionForDACResponse(displaySubscriptionForDACResponse: ReadSubscriptionForDACResponse)

object DisplaySubscriptionForDACResponse {
  implicit val format: OFormat[DisplaySubscriptionForDACResponse] = Json.format[DisplaySubscriptionForDACResponse]
}
