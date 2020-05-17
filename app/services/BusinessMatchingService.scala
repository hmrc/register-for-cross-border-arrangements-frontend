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
import models.{IndividualMatchingSubmission, UserAnswers}
import pages.NinoPage
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

}
