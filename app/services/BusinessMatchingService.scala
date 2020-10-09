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

import connectors.RegistrationConnector
import javax.inject.Inject
import models.{BusinessDetails, BusinessType, PayloadRegisterWithID, PayloadRegistrationWithIDResponse, UniqueTaxpayerReference, UserAnswers}
import pages.{BusinessTypePage, CorporationTaxUTRPage, NinoPage, SelfAssessmentUTRPage}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class BusinessMatchingService @Inject()(registrationConnector: RegistrationConnector) {

  def sendIndividualMatchingInformation(userAnswers: UserAnswers)
                                       (implicit hc: HeaderCarrier,
                                        ec: ExecutionContext): Future[Either[Exception, (Option[PayloadRegistrationWithIDResponse], Option[String])]] =
    userAnswers.get(NinoPage) match {
      case Some(nino) =>
        val payloadForIndividual = PayloadRegisterWithID.createIndividualSubmission(userAnswers, "NINO", nino.nino)
        payloadForIndividual match {
          case Some(request) => registrationConnector.registerWithID(request).map {
            response =>
              val safeId = retrieveSafeID(response)
              Right((response, safeId))
          }
          case None =>
            Future.successful(Left(new Exception("Couldn't Create Payload for Register With ID"))            )
        }
      case _ => Future.successful(Left(new Exception("Missing Nino Answer")))
    }

  def sendBusinessMatchingInformation(userAnswers: UserAnswers)
                                     (implicit hc: HeaderCarrier, ec: ExecutionContext):  Future[(Option[BusinessDetails], Option[String])] = {

    val utr: UniqueTaxpayerReference = (userAnswers.get(SelfAssessmentUTRPage), userAnswers.get(CorporationTaxUTRPage)) match {
      case (Some(utr), _) => utr
      case (_, Some(utr)) => utr
    }

    //Note: ETMP data suggests sole trader business partner accounts are individual records
    val payload: Option[PayloadRegisterWithID] = userAnswers.get(BusinessTypePage) match {
      case Some(BusinessType.NotSpecified) =>
        PayloadRegisterWithID.createIndividualSubmission(userAnswers, "UTR", utr.uniqueTaxPayerReference)
      case _ =>
        PayloadRegisterWithID.createBusinessSubmission(userAnswers, "UTR", utr.uniqueTaxPayerReference)
    }

    callEndPoint(payload)
  }

  def callEndPoint(payload: Option[PayloadRegisterWithID])
                  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[(Option[BusinessDetails], Option[String])] = {
    payload match {
      case Some(request) => registrationConnector.registerWithID(request).map {
        response =>
          val safeId = retrieveSafeID(response)
          (response.flatMap(BusinessDetails.fromRegistrationMatch), safeId)
        //Do we need a logger message for failed extraction?
      }
      case _ => Future.successful((None, None))
    }
  }

  def retrieveSafeID(payloadRegisterWithIDResponse: Option[PayloadRegistrationWithIDResponse]): Option[String]  = {
    payloadRegisterWithIDResponse match {
      case Some(value) => value.registerWithIDResponse.responseDetail.map(_.SAFEID)
      case _ => throw new Exception("unable to retrieve SafeID")
    }
  }
}