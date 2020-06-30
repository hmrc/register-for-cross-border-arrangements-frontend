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
import models.{BusinessMatchingSubmission, IndividualMatchingSubmission, UniqueTaxpayerReference}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class BusinessMatchingConnector @Inject()(val config: FrontendAppConfig, val http: HttpClient) {

  def sendIndividualMatchingInformation(nino: Nino, individualSubmission: IndividualMatchingSubmission)
                                       (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
      val submissionUrl = s"${config.businessMatchingUrl}/matching/individual/$nino"
      http.POST[IndividualMatchingSubmission, HttpResponse](submissionUrl, individualSubmission)
  }

  def sendBusinessMatchingInformation(utr: UniqueTaxpayerReference, businessSubmission: BusinessMatchingSubmission)
                                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val submissionUrl = s"${config.businessMatchingUrl}/matching/organisation/${utr.uniqueTaxPayerReference}"
    http.POST[BusinessMatchingSubmission, HttpResponse](submissionUrl, businessSubmission)
  }

}
