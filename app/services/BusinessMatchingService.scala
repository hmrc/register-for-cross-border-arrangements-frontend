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

package services

import connectors.{RegistrationConnector, SubscriptionConnector}
import javax.inject.Inject
import models.readSubscription.DisplaySubscriptionForDACResponse
import models.{BusinessDetails, BusinessType, PayloadRegisterWithID, PayloadRegistrationWithIDResponse, UniqueTaxpayerReference, UserAnswers}
import pages.{BusinessTypePage, CorporationTaxUTRPage, NinoPage, SelfAssessmentUTRPage}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class BusinessMatchingService @Inject() (registrationConnector: RegistrationConnector, subscriptionConnector: SubscriptionConnector) {

  def sendIndividualMatchingInformation(userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[Exception, (Option[PayloadRegistrationWithIDResponse], Option[String], Option[DisplaySubscriptionForDACResponse])]] =
    userAnswers.get(NinoPage) match {
      case Some(nino) =>
        val payloadForIndividual = PayloadRegisterWithID.createIndividualSubmission(userAnswers, "NINO", nino.nino)
        payloadForIndividual match {
          case Some(request) =>
            registrationConnector.registerWithID(request).flatMap {
              response =>
                val safeId = retrieveSafeID(response)
                retrieveExistingSubscriptionDetails(safeId).map {
                  existingSubscription =>
                    Right((response, safeId, existingSubscription))
                }
            }
          case None =>
            Future.successful(Left(new Exception("Couldn't Create Payload for Register With ID")))
        }
      case _ => Future.successful(Left(new Exception("Missing Nino Answer")))
    }

  def sendBusinessMatchingInformation(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[(Option[BusinessDetails], Option[String], Option[DisplaySubscriptionForDACResponse])] = {

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

    callEndPoint(payload).flatMap {
      tup =>
        retrieveExistingSubscriptionDetails(tup._2).map {
          existingSubscription =>
            (tup._1, tup._2, existingSubscription)
        }
    }
  }

  def callEndPoint(
    payload: Option[PayloadRegisterWithID]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[(Option[BusinessDetails], Option[String])] =
    payload match {
      case Some(request) =>
        registrationConnector.registerWithID(request).map {
          response =>
            val safeId = retrieveSafeID(response)
            (response.flatMap(BusinessDetails.fromRegistrationMatch), safeId)
        }
      case _ => Future.successful((None, None))
    }

  def retrieveSafeID(payloadRegisterWithIDResponse: Option[PayloadRegistrationWithIDResponse]): Option[String] =
    payloadRegisterWithIDResponse.flatMap {
      _.registerWithIDResponse.responseDetail.map(_.SAFEID)
    }

  private def retrieveExistingSubscriptionDetails(
    safeId: Option[String]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[DisplaySubscriptionForDACResponse]] =
    if (safeId.isDefined) {
      subscriptionConnector.readSubscriptionDetails(safeId.get)
    } else Future(None)

}
