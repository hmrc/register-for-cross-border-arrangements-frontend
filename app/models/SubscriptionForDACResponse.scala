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

import play.api.libs.json.{Json, OFormat, OWrites, Reads, __}


case class ResponseDetailForDACSubscription(subscriptionID: String)
object ResponseDetailForDACSubscription {
  implicit val format: OFormat[ResponseDetailForDACSubscription] = Json.format[ResponseDetailForDACSubscription]
}

case class SubscriptionForDACResponse(responseCommon: ResponseCommon, responseDetail: ResponseDetailForDACSubscription)
object SubscriptionForDACResponse {
  implicit val reads: Reads[SubscriptionForDACResponse] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "responseCommon").read[ResponseCommon] and
        (__ \ "responseDetail").read[ResponseDetailForDACSubscription]
    )((responseCommon, responseDetail) => SubscriptionForDACResponse(responseCommon, responseDetail))
  }

  implicit val writes: OWrites[SubscriptionForDACResponse] = Json.writes[SubscriptionForDACResponse]
}

case class CreateSubscriptionForDACResponse(createSubscriptionForDACResponse: SubscriptionForDACResponse)
object CreateSubscriptionForDACResponse {
  implicit val format: OFormat[CreateSubscriptionForDACResponse] = Json.format[CreateSubscriptionForDACResponse]
}