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
import models.{CreateSubscriptionForDACRequest, CreateSubscriptionForDACResponse, SubscriptionForDACRequest, SubscriptionInfo, UserAnswers}
import org.slf4j.LoggerFactory
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionConnector @Inject()(val config: FrontendAppConfig, val http: HttpClient) {

  private val logger = LoggerFactory.getLogger(getClass)

  def createEnrolment(userAnswers: UserAnswers)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {

    val submissionUrl = s"${config.businessMatchingUrl}/enrolment/create-enrolment"
    http.PUT[SubscriptionInfo, HttpResponse](submissionUrl, SubscriptionInfo.createSubscriptionInfo(userAnswers))
  }

  def createSubscription(userAnswers: UserAnswers)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CreateSubscriptionForDACResponse] = {

    val submissionUrl = s"${config.businessMatchingUrl}/subscription/create-dac-subscription"

    try {
      http.POST[CreateSubscriptionForDACRequest, HttpResponse](
        submissionUrl,
        CreateSubscriptionForDACRequest(SubscriptionForDACRequest.createSubscription(userAnswers))
      ).flatMap {
        case response if response.status equals OK =>
          Future.successful(response.json.as[CreateSubscriptionForDACResponse])
        case response =>
          logger.warn(s"Unable to create a subscription to ETMP. ${response.status} response status")
          Future.failed(new HttpException(response.body, response.status))
      }
    } catch {
      case e: Exception =>
        logger.warn("Unable to create an ETMP subscription", e)
        Future.failed(e)
    }
  }
}
