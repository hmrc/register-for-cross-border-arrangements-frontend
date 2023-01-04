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

package navigation

import config.FrontendAppConfig
import controllers.routes
import helpers.JourneyHelpers._
import javax.inject.{Inject, Singleton}
import models.BusinessType._
import models.RegistrationType.{Individual, enumerable => _, reads => _, _}
import models._
import pages._
import play.api.mvc.Call

@Singleton
class Navigator @Inject() (appConfig: FrontendAppConfig) {

  private val normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case RegistrationTypePage                  => registrationTypeRoutes(NormalMode)
    case DoYouHaveUTRPage                      => doYouHaveUTRRoutes(NormalMode)
    case BusinessTypePage                      => businessTypeRoutes(NormalMode)
    case CorporationTaxUTRPage                 => businessNameRoutes(NormalMode)
    case SelfAssessmentUTRPage                 => businessNameRoutes(NormalMode)
    case DoYouHaveANationalInsuranceNumberPage => doYouHaveANationalInsuranceNumberRoutes(NormalMode)
    case NinoPage                              => _ => Some(routes.NameController.onPageLoad(NormalMode))
    case NamePage                              => _ => Some(routes.DateOfBirthController.onPageLoad(NormalMode))
    case BusinessNamePage                      => _ => Some(routes.BusinessMatchingController.matchBusiness())
    case SoleTraderNamePage                    => _ => Some(routes.DateOfBirthController.onPageLoad(NormalMode))
    case ConfirmBusinessPage                   => confirmBusinessRoutes(NormalMode)
    case DateOfBirthPage                       => dateOfBirthRoutes(NormalMode)
    case DoYouLiveInTheUKPage                  => doYouLiveInTheUKRoutes(NormalMode)
    case IndividualUKPostcodePage              => _ => Some(routes.SelectAddressController.onPageLoad(NormalMode))
    case SelectAddressPage                     => _ => Some(routes.ContactEmailAddressController.onPageLoad(NormalMode))
    case NonUkNamePage                         => _ => Some(routes.DateOfBirthController.onPageLoad(NormalMode))
    case BusinessAddressPage                   => _ => Some(routes.YourContactDetailsController.onPageLoad)
    case BusinessWithoutIDNamePage             => _ => Some(routes.DoYouHaveBusinessTradingNameController.onPageLoad(NormalMode))
    case DoYouHaveBusinessTradingNamePage      => doYouHaveBusinessTradingNameRoutes(NormalMode)
    case BusinessTradingNamePage               => _ => Some(routes.BusinessAddressController.onPageLoad(NormalMode))
    case WhatIsYourAddressUkPage               => _ => Some(routes.ContactEmailAddressController.onPageLoad(NormalMode))
    case WhatIsYourAddressPage                 => _ => Some(routes.ContactEmailAddressController.onPageLoad(NormalMode))
    case ContactNamePage                       => _ => Some(routes.ContactEmailAddressController.onPageLoad(NormalMode))
    case TelephoneNumberQuestionPage           => telephoneNumberQuestionRoutes(NormalMode)
    case ContactEmailAddressPage               => _ => Some(routes.TelephoneNumberQuestionController.onPageLoad(NormalMode))
    case ContactTelephoneNumberPage            => contactTelephoneNumberRoutes(NormalMode)
    case HaveSecondContactPage                 => haveSecondContactRoutes(NormalMode)
    case SecondaryContactNamePage              => _ => Some(routes.SecondaryContactEmailAddressController.onPageLoad(NormalMode))
    case SecondaryContactEmailAddressPage      => _ => Some(routes.SecondaryContactTelephoneQuestionController.onPageLoad(NormalMode))
    case SecondaryContactTelephoneQuestionPage => secondaryContactTelephoneQuestionRoute(NormalMode)
    case SecondaryContactTelephoneNumberPage   => _ => Some(routes.CheckYourAnswersController.onPageLoad)
    case SomeInformationIsMissingPage          => _ => Some(routes.DoYouHaveUTRController.onPageLoad(NormalMode))
    case _                                     => _ => Some(routes.IndexController.onPageLoad)
  }

  private val checkRouteMap: Page => UserAnswers => Option[Call] = {
    case DoYouHaveUTRPage                      => doYouHaveUTRRoutes(CheckMode)
    case RegistrationTypePage                  => registrationTypeRoutes(CheckMode)
    case BusinessWithoutIDNamePage             => _ => Some(routes.DoYouHaveBusinessTradingNameController.onPageLoad(CheckMode))
    case DoYouHaveBusinessTradingNamePage      => doYouHaveBusinessTradingNameRoutes(CheckMode)
    case BusinessTradingNamePage               => _ => Some(routes.BusinessAddressController.onPageLoad(CheckMode))
    case DoYouHaveANationalInsuranceNumberPage => doYouHaveANationalInsuranceNumberRoutes(CheckMode)
    case NinoPage                              => _ => Some(routes.NameController.onPageLoad(CheckMode))
    case NonUkNamePage                         => _ => Some(routes.DateOfBirthController.onPageLoad(CheckMode))
    case NamePage                              => _ => Some(routes.DateOfBirthController.onPageLoad(CheckMode))
    case DateOfBirthPage                       => dateOfBirthRoutes(CheckMode)
    case DoYouLiveInTheUKPage                  => doYouLiveInTheUKRoutes(CheckMode)
    case WhatIsYourAddressPage                 => _ => Some(routes.ContactEmailAddressController.onPageLoad(CheckMode))
    case IndividualUKPostcodePage              => _ => Some(routes.SelectAddressController.onPageLoad(CheckMode))
    case WhatIsYourAddressUkPage               => _ => Some(routes.ContactEmailAddressController.onPageLoad(CheckMode))
    case SelectAddressPage                     => _ => Some(routes.ContactEmailAddressController.onPageLoad(CheckMode))
    case BusinessTypePage                      => businessTypeRoutes(CheckMode)
    case CorporationTaxUTRPage                 => businessNameRoutes(CheckMode)
    case SelfAssessmentUTRPage                 => businessNameRoutes(CheckMode)
    case BusinessNamePage                      => _ => Some(routes.BusinessMatchingController.matchBusiness())
    case SoleTraderNamePage                    => _ => Some(routes.DateOfBirthController.onPageLoad(CheckMode))
    case ConfirmBusinessPage                   => confirmBusinessRoutes(CheckMode)
    case BusinessAddressPage                   => _ => Some(routes.ContactNameController.onPageLoad(CheckMode))
    case ContactNamePage                       => _ => Some(routes.ContactEmailAddressController.onPageLoad(CheckMode))
    case ContactEmailAddressPage               => _ => Some(routes.TelephoneNumberQuestionController.onPageLoad(CheckMode))
    case TelephoneNumberQuestionPage           => telephoneNumberQuestionRoutes(CheckMode)
    case ContactTelephoneNumberPage            => contactTelephoneNumberRoutes(CheckMode)
    case HaveSecondContactPage                 => haveSecondContactRoutes(CheckMode)
    case SecondaryContactNamePage              => _ => Some(routes.SecondaryContactEmailAddressController.onPageLoad(CheckMode))
    case SecondaryContactEmailAddressPage      => _ => Some(routes.SecondaryContactTelephoneQuestionController.onPageLoad(CheckMode))
    case SecondaryContactTelephoneQuestionPage => secondaryContactTelephoneQuestionRoute(CheckMode)
    case SecondaryContactTelephoneNumberPage   => _ => Some(routes.CheckYourAnswersController.onPageLoad)
    case _                                     => _ => Some(routes.CheckYourAnswersController.onPageLoad)

  }

  private def doYouHaveBusinessTradingNameRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveBusinessTradingNamePage) map {
      case true => routes.BusinessTradingNameController.onPageLoad(mode)
      case _    => routes.BusinessAddressController.onPageLoad(mode)
    }

  private def businessTypeRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(BusinessTypePage) map {
      case BusinessType.CorporateBody | BusinessType.UnIncorporatedBody => routes.CorporationTaxUTRController.onPageLoad(mode)
      case _                                                            => routes.SelfAssessmentUTRController.onPageLoad(mode)
    }

  private def businessNameRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(BusinessTypePage) map {
      case BusinessType.NotSpecified                                  => routes.SoleTraderNameController.onPageLoad(mode)
      case BusinessType.Partnership                                   => routes.BusinessNamePartnershipController.onPageLoad(mode)
      case BusinessType.LimitedLiability | BusinessType.CorporateBody => routes.BusinessNameRegisteredBusinessController.onPageLoad(mode)
      case BusinessType.UnIncorporatedBody                            => routes.BusinessNameOrganisationController.onPageLoad(mode)
    }

  private def confirmBusinessRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(ConfirmBusinessPage) map {
      case true =>
        ua.get(BusinessTypePage) match {
          case Some(BusinessType.NotSpecified) => routes.ContactEmailAddressController.onPageLoad(mode)
          case Some(_) =>
            mode match {
              case NormalMode => routes.YourContactDetailsController.onPageLoad
              case _          => routes.ContactNameController.onPageLoad(mode)
            }
          case None => routes.ContactEmailAddressController.onPageLoad(mode)
        }
      case false => routes.BusinessNotIdentifiedController.onPageLoad()
    }

  private def doYouHaveUTRRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveUTRPage) map {
      case true  => routes.BusinessTypeController.onPageLoad(mode)
      case false => routes.RegistrationTypeController.onPageLoad(mode)
    }

  private def doYouHaveANationalInsuranceNumberRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouHaveANationalInsuranceNumberPage) map {
      case true  => routes.NinoController.onPageLoad(mode)
      case false => routes.NonUkNameController.onPageLoad(mode)
    }

  private def dateOfBirthRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    if (ua.get(SoleTraderNamePage).isDefined) {
      Some(routes.BusinessMatchingController.matchBusiness())
    } else {
      ua.get(DoYouHaveANationalInsuranceNumberPage) map {
        case true  => routes.BusinessMatchingController.matchIndividual(mode)
        case false => routes.DoYouLiveInTheUKController.onPageLoad(mode)
      }
    }

  private def doYouLiveInTheUKRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(DoYouLiveInTheUKPage) map {
      case true  => routes.IndividualUKPostcodeController.onPageLoad(mode)
      case false => routes.WhatIsYourAddressController.onPageLoad(mode)
    }

  private def registrationTypeRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(RegistrationTypePage) map {
      case Individual => routes.DoYouHaveANationalInsuranceNumberController.onPageLoad(mode)
      case Business   => routes.BusinessWithoutIDNameController.onPageLoad(mode)
    }

  private def telephoneNumberQuestionRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(TelephoneNumberQuestionPage) map {
      case true                               => routes.ContactTelephoneNumberController.onPageLoad(mode)
      case false if isOrganisationJourney(ua) => routes.HaveSecondContactController.onPageLoad(mode)
      case false                              => routes.CheckYourAnswersController.onPageLoad
    }

  private def haveSecondContactRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(HaveSecondContactPage) map {
      case true  => routes.SecondaryContactNameController.onPageLoad(mode)
      case false => routes.CheckYourAnswersController.onPageLoad
    }

  private def contactTelephoneNumberRoutes(mode: Mode)(ua: UserAnswers): Option[Call] =
    if (isOrganisationJourney(ua)) {
      Some(routes.HaveSecondContactController.onPageLoad(mode))
    } else {
      Some(routes.CheckYourAnswersController.onPageLoad)
    }

  private def secondaryContactTelephoneQuestionRoute(mode: Mode)(ua: UserAnswers): Option[Call] =
    ua.get(SecondaryContactTelephoneQuestionPage) map {
      case true  => routes.SecondaryContactTelephoneNumberController.onPageLoad(mode)
      case false => routes.CheckYourAnswersController.onPageLoad
    }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers) match {
        case Some(call) => call
        case None       => routes.SessionExpiredController.onPageLoad
      }
    case CheckMode =>
      checkRouteMap(page)(userAnswers) match {
        case Some(call) => call
        case None       => routes.SessionExpiredController.onPageLoad
      }
  }
}
