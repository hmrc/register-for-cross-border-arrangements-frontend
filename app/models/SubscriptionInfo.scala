/*
 * Copyright 2021 HM Revenue & Customs
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

import pages._
import play.api.libs.json.{Json, OFormat}

case class SubscriptionInfo(safeID: String,
                            saUtr: Option[String] = None,
                            ctUtr: Option[String] = None,
                            nino: Option[String] = None,
                            nonUkPostcode: Option[String] = None,
                            dac6Id: String
)

object SubscriptionInfo {
  implicit val format: OFormat[SubscriptionInfo] = Json.format[SubscriptionInfo]

  def createSubscriptionInfo(userAnswers: UserAnswers): SubscriptionInfo =
    SubscriptionInfo(
      safeID = getSafeID(userAnswers),
      saUtr = getSaUtrIfProvided(userAnswers),
      ctUtr = getCtUtrIfProvided(userAnswers),
      nino = getNinoIfProvided(userAnswers),
      nonUkPostcode = getNonUkPostCodeIfProvided(userAnswers),
      dac6Id = getSubscriptionId(userAnswers)
    )

  private def getSafeID(userAnswers: UserAnswers): String =
    userAnswers.get(SafeIDPage) match {
      case Some(id) => id
      case None     => throw new Exception("Safe ID can't be retrieved")
    }

  private def getSubscriptionId(userAnswers: UserAnswers): String =
    userAnswers.get(SubscriptionIDPage) match {
      case Some(id) => id
      case None     => throw new Exception("Subscription ID can't be retrieved")
    }

  private def getNinoIfProvided(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(NinoPage) match {

      case Some(nino) => Some(nino.nino)
      case _          => None
    }

  private def getSaUtrIfProvided(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(SelfAssessmentUTRPage) match {

      case Some(utr) => Some(utr.uniqueTaxPayerReference)
      case _         => None
    }

  private def getCtUtrIfProvided(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(CorporationTaxUTRPage) match {

      case Some(utr) => Some(utr.uniqueTaxPayerReference)
      case _         => None
    }

  private def getNonUkPostCodeIfProvided(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(WhatIsYourAddressPage) match {

      case Some(address) => address.postCode
      case _             => None
    }

}
