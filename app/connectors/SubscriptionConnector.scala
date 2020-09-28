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

package connectors

import config.FrontendAppConfig
import javax.inject.Inject
import models.{SubscriptionForDACRequest, SubscriptionInfo, UserAnswers}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionConnector @Inject()(val config: FrontendAppConfig, val http: HttpClient) {

  def createSubscription(userAnswers: UserAnswers)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {

    val submissionUrl = s"${config.businessMatchingUrl}/enrolment/create-enrolment"
    http.PUT[SubscriptionInfo, HttpResponse](submissionUrl, SubscriptionInfo.createSubscriptionInfo(userAnswers))
  }

  def createEISSubscription(userAnswers: UserAnswers)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {

    val submissionUrl = s"${config.businessMatchingUrl}/subscription/create-dac-subscription"
    http.POST[SubscriptionForDACRequest, HttpResponse](submissionUrl, SubscriptionForDACRequest.createDacRequest(userAnswers))
  }
}
