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
import javax.inject.Inject
import models.{PayloadRegisterWithID, PayloadRegistrationWithIDResponse, Register, RegisterWithIDErrorResponse}
import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject() (val config: FrontendAppConfig, val http: HttpClient) {

  def sendWithoutIDInformation(registration: Register)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val submissionUrl = s"${config.businessMatchingUrl}/registration/02.00.00/noId"
    http.POST[Register, HttpResponse](submissionUrl, registration)
  }

  def registerWithID(
    registration: PayloadRegisterWithID
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[PayloadRegistrationWithIDResponse]] = {
    val submissionUrl = s"${config.businessMatchingUrl}/registration/02.00.00/withId"

    http.POST[PayloadRegisterWithID, HttpResponse](submissionUrl, registration) map {
      case responseMessage if is2xx(responseMessage.status) =>
        Some(responseMessage.json.as[PayloadRegistrationWithIDResponse])
      case responseMessage if responseMessage.status == NOT_FOUND =>
        None
      case responseMessage =>
        responseMessage.json.validate[RegisterWithIDErrorResponse] match {
          case JsSuccess(response, _) =>
            val errorDetail = response.errorDetail
            if (
              errorDetail.sourceFaultDetail.detail.contains("001 - Request could not be processed")
              && errorDetail.errorCode == "503"
            ) {
              None
            } else {
              throw new Exception("There has been an error during individual and business matching")
            }
          case JsError(_) =>
            //shove as a logger message into kibana for support
            throw new Exception("There has been an error. Unable to parse error detail and extract information")
        }
    }
  }
}
