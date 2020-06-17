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

package services

import connectors.BusinessMatchingConnector
import javax.inject.Inject
import models.{BusinessDetails, BusinessMatchingSubmission, IndividualMatchingSubmission, UserAnswers}
import pages.{CorporationTaxUTRPage, NinoPage, SelfAssessmentUTRPage}
import play.api.http.Status._
import play.api.libs.json.JsResult.Exception
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class BusinessMatchingService @Inject()(businessMatchingConnector: BusinessMatchingConnector) {

  def sendIndividualMatchingInformation(userAnswers: UserAnswers)
                                       (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[HttpResponse]] = {

    (IndividualMatchingSubmission(userAnswers), userAnswers.get(NinoPage)) match {
      case (Some(individualSubmission), Some(nino)) =>
        businessMatchingConnector.sendIndividualMatchingInformation(nino, individualSubmission).map(Some(_))
      case _ => Future.successful(None)
    }
  }

  def sendBusinessMatchingInformation(userAnswers: UserAnswers)
                                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[BusinessDetails]] = {

    val utr = (userAnswers.get(SelfAssessmentUTRPage), userAnswers.get(CorporationTaxUTRPage)) match {
      case (Some(utr), _) => utr
      case (_, Some(utr)) => utr
    }

      businessMatchingConnector.sendBusinessMatchingInformation(
        utr,
        BusinessMatchingSubmission(userAnswers).get
      ).map {
        response =>
          response.status match {
            case OK => validateJsonForBusiness(response.json)
            case NOT_FOUND => None
            case _ => None
          }
    }
  }

  private def validateJsonForBusiness(value: JsValue): Option[BusinessDetails] = {
      value.validate[BusinessDetails] match {
        case JsSuccess(details, _) => Some(details)
        case JsError(_) => throw Exception(JsError(s"Error encountered retrieving business matching record."))
      }
  }
}
