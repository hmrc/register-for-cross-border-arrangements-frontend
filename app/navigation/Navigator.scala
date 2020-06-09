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
import helpers.JourneyHelpers._
import javax.inject.{Inject, Singleton}
import models.BusinessType._
import models.RegistrationType.{Individual, enumerable => _, reads => _, _}
import models.SecondaryContactPreference._
import models._
import pages._
import play.api.mvc.Call

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case RegistrationTypePage => registrationTypeRoutes
    case DoYouHaveUTRPage => doYouHaveUTRRoutes
    case BusinessTypePage => businessTypeRoutes
    case CorporationTaxUTRPage => businessNameRoutes
    case SelfAssessmentUTRPage => businessNameRoutes
    case DoYouHaveANationalInsuranceNumberPage => doYouHaveANationalInsuranceNumberRoutes
    case NinoPage => _ => Some(routes.NameController.onPageLoad(NormalMode))
    case NamePage => _ => Some(routes.DateOfBirthController.onPageLoad(NormalMode))
    case BusinessNamePage => _ => Some(routes.BusinessMatchingController.matchBusiness())
    case SoleTraderNamePage => _ => Some(routes.BusinessMatchingController.matchBusiness())
    case ConfirmBusinessPage => confirmBusinessRoutes
    case DateOfBirthPage => dateOfBirthRoutes
    case DoYouLiveInTheUKPage => doYouLiveInTheUKRoutes
    case NonUkNamePage => _ => Some(routes.DateOfBirthController.onPageLoad(NormalMode))
    case BusinessAddressPage => _ =>   Some(routes.ContactNameController.onPageLoad(NormalMode))
    case BusinessWithoutIDNamePage => _ => Some(routes.BusinessAddressController.onPageLoad(NormalMode))
    case WhatIsYourAddressUkPage => _ => Some(routes.ContactEmailAddressController.onPageLoad(NormalMode))
    case WhatIsYourAddressPage => _ => Some(routes.ContactNameController.onPageLoad(NormalMode))
    case IsThisYourBusinessPage => _ => Some(routes.IdentityConfirmedController.onPageLoad())
    case ContactNamePage => _ => Some(routes.ContactEmailAddressController.onPageLoad(NormalMode))
    case TelephoneNumberQuestionPage => telephoneNumberQuestionRoutes
    case ContactEmailAddressPage => _ => Some(routes.TelephoneNumberQuestionController.onPageLoad(NormalMode))
    case ContactTelephoneNumberPage => contactTelephoneNumberRoutes
    case HaveSecondContactPage => haveSecondContactRoutes
    case SecondaryContactNamePage => _ => Some(routes.SecondaryContactPreferenceController.onPageLoad(NormalMode))
    case SecondaryContactPreferencePage => secondaryContactPreferenceRoutes
    case SecondaryContactEmailAddressPage => _ => Some(routes.CheckYourAnswersController.onPageLoad())
    case _ => _ => Some(routes.IndexController.onPageLoad())
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  private def businessTypeRoutes(ua: UserAnswers): Option[Call] = {
    ua.get(BusinessTypePage) map {
      case BusinessType.CorporateBody | BusinessType.UnIncorporatedBody => routes.CorporationTaxUTRController.onPageLoad(NormalMode)
      case _ => routes.SelfAssessmentUTRController.onPageLoad(NormalMode)
    }
  }

  private def doYouHaveUTRRoutes(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveUTRPage) map {
      case true  => routes.BusinessTypeController.onPageLoad(NormalMode)
      case false => routes.RegistrationTypeController.onPageLoad(NormalMode)
    }

  private def businessNameRoutes(ua: UserAnswers): Option[Call] = {
    ua.get(BusinessTypePage) map {
      case BusinessType.NotSpecified => routes.SoleTraderNameController.onPageLoad(NormalMode)
      case BusinessType.Partnership => routes.BusinessNamePartnershipController.onPageLoad(NormalMode)
      case BusinessType.LimitedLiability | BusinessType.CorporateBody => routes.BusinessNameRegisteredBusinessController.onPageLoad(NormalMode)
      case BusinessType.UnIncorporatedBody => routes.BusinessNameOrganisationController.onPageLoad(NormalMode)
    }
  }

  private def confirmBusinessRoutes(ua: UserAnswers): Option[Call] =
    ua.get(ConfirmBusinessPage) map {
      case true  => routes.IdentityConfirmedController.onPageLoad()
      case false  => routes.BusinessNotConfirmedController.onPageLoad()
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
      case true  => routes.WhatIsYourAddressUkController.onPageLoad(NormalMode)
      case false => routes.WhatIsYourAddressController.onPageLoad(NormalMode)
    }

  private def registrationTypeRoutes(ua: UserAnswers): Option[Call] =
    ua.get(RegistrationTypePage) map {
      case Individual => routes.DoYouHaveANationalInsuranceNumberController.onPageLoad(NormalMode)
      case Business => routes.BusinessWithoutIDNameController.onPageLoad(NormalMode)
    }

  private def telephoneNumberQuestionRoutes(ua: UserAnswers): Option[Call] = {
    ua.get(TelephoneNumberQuestionPage) map {
      case true => routes.ContactTelephoneNumberController.onPageLoad(NormalMode)
      case false if isOrganisationJourney(ua) => routes.HaveSecondContactController.onPageLoad(NormalMode)
      case false => routes.CheckYourAnswersController.onPageLoad()
    }
  }

  private def haveSecondContactRoutes(ua: UserAnswers): Option[Call] =
    ua.get(HaveSecondContactPage) map {
      case true  => routes.SecondaryContactNameController.onPageLoad(NormalMode)
      case false => routes.CheckYourAnswersController.onPageLoad()
    }

  private def secondaryContactPreferenceRoutes(ua: UserAnswers): Option[Call] =
    ua.get(SecondaryContactPreferencePage) map {
      case set: Set[SecondaryContactPreference] if set.head == Telephone =>
        routes.ContactTelephoneNumberController.onPageLoad(NormalMode) //TODO change to SecondaryContactTelephone
      case set: Set[SecondaryContactPreference] if set.head == Email =>
        routes.SecondaryContactEmailAddressController.onPageLoad(NormalMode)
    }

  private def contactTelephoneNumberRoutes(ua: UserAnswers): Option[Call] = {
    (ua.get(RegistrationTypePage), ua.get(BusinessTypePage)) match {
      case (Some(Individual),_) => Some(routes.CheckYourAnswersController.onPageLoad())
      case (_,_) => Some(routes.HaveSecondContactController.onPageLoad(NormalMode))
    }
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
