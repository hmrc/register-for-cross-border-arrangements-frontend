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

package navigation

import controllers.routes
import javax.inject.{Inject, Singleton}
import models.RegistrationType.{Business, Individual}
import models._
import pages._
import play.api.mvc.Call

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case RegistrationTypePage => registrationTypeRoutes
    case DoYouHaveUTRPage => doYouHaveUTRPage
    case DoYouHaveANationalInsuranceNumberPage =>   doYouHaveANationalInsuranceNumberRoutes
    case NinoPage => _ => Some(routes.NameController.onPageLoad(NormalMode))
    case NamePage => _ => Some(routes.DateOfBirthController.onPageLoad(NormalMode))
    case DateOfBirthPage => dateOfBirthRoutes
    case DoYouLiveInTheUKPage => doYouLiveInTheUKRoutes
    case BusinessTypePage => _ => Some(routes.UniqueTaxpayerReferenceController.onPageLoad(NormalMode))
    case UniqueTaxpayerReferencePage => _ => Some(routes.PostCodeController.onPageLoad(NormalMode))
    case NonUkNamePage => _ => Some(routes.DateOfBirthController.onPageLoad(NormalMode))
    case BusinessAddressPage => _ =>   Some(routes.CheckYourAnswersController.onPageLoad())
    case BusinessWithoutIDNamePage => _ => Some(routes.WhatIsYourAddressController.onPageLoad(NormalMode))
    case _ => _ => Some(routes.IndexController.onPageLoad())
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  private def doYouHaveUTRPage(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveUTRPage) map {
      case true  => routes.BusinessTypeController.onPageLoad(NormalMode)
      case false => routes.RegistrationTypeController.onPageLoad(NormalMode)
    }

  private def doYouHaveANationalInsuranceNumberRoutes(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveANationalInsuranceNumberPage) map {
      case true  => routes.NinoController.onPageLoad(NormalMode)
      case false => routes.NonUkNameController.onPageLoad(NormalMode)
    }

  private def dateOfBirthRoutes(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveANationalInsuranceNumberPage) map {
      case true  => routes.BusinessMatchingController.matchIndividual()
      case false => routes.DoYouLiveInTheUKController.onPageLoad(NormalMode)
    }

  private def doYouLiveInTheUKRoutes(ua: UserAnswers): Option[Call] =
    ua.get(DoYouLiveInTheUKPage) map {
      case true  => routes.IndividualUKPostcodeController.onPageLoad(NormalMode)
      case false => routes.WhatIsYourAddressController.onPageLoad(NormalMode)
    }

  private def registrationTypeRoutes(ua: UserAnswers): Option[Call] =
    ua.get(RegistrationTypePage) map {
      case Individual => routes.DoYouHaveANationalInsuranceNumberController.onPageLoad(NormalMode)
      case Business => routes.BusinessWithoutIDNameController.onPageLoad(NormalMode)
    }


  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers) match {
        case Some(call) => call
        case None => routes.SessionExpiredController.onPageLoad()
      }
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
