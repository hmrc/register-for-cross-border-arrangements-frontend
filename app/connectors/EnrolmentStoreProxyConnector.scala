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

package connectors

import config.FrontendAppConfig
import models.GroupIds
import play.api.Logging
import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnrolmentStoreProxyConnector @Inject() (val config: FrontendAppConfig, val http: HttpClient) extends Logging {

  def enrolmentExists(subscriptionID: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Boolean] = {
    val serviceEnrolmentPattern = s"HMRC-DAC6-ORG~DAC6ID~$subscriptionID"
    val submissionUrl           = s"${config.enrolmentStoreProxyUrl}/enrolment-store/enrolments/$serviceEnrolmentPattern/groups"

    http
      .GET[HttpResponse](
        submissionUrl
      )(rds = readRaw, hc = hc, ec = ec)
      .map {
        case response if response.status == NO_CONTENT => false
        case response if is2xx(response.status) =>
          response.json
            .asOpt[GroupIds]
            .exists(
              groupIds =>
                if (groupIds.principalGroupIds.nonEmpty) {
                  true
                } else {
                  false
                }
            )
        case response =>
          logger.warn(s"Enrolment response not formed. ${response.status} response status")
          throw new IllegalStateException()
      }

  }
}
