/*
 * Copyright 2022 HM Revenue & Customs
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

package pages

import models.UserAnswers

import scala.util.Try

object PageLists {

  val removePage: (Try[UserAnswers], QuestionPage[_]) => Try[UserAnswers] =
    (ua: Try[UserAnswers], page: QuestionPage[_]) =>
      ua.flatMap(
        x => x.remove(page)
      )

  val allAfterRegistrationTypePages = List(
    BusinessAddressPage,
    BusinessWithoutIDNamePage,
    ContactEmailAddressPage,
    ContactNamePage,
    TelephoneNumberQuestionPage,
    ContactTelephoneNumberPage,
    HaveSecondContactPage,
    SecondaryContactNamePage,
    SecondaryContactEmailAddressPage,
    SecondaryContactTelephoneQuestionPage,
    SecondaryContactTelephoneNumberPage,
    DoYouHaveANationalInsuranceNumberPage,
    NamePage,
    NonUkNamePage,
    DateOfBirthPage,
    DoYouLiveInTheUKPage,
    IndividualUKPostcodePage,
    NinoPage,
    SelectAddressPage,
    SelectedAddressLookupPage,
    WhatIsYourAddressPage,
    WhatIsYourAddressUkPage,
    PostCodePage
  )

  val allAfterHaveUTRPages = List(RegistrationTypePage,
                                  BusinessTypePage,
                                  CorporationTaxUTRPage,
                                  SelfAssessmentUTRPage,
                                  BusinessNamePage,
                                  ConfirmBusinessPage,
                                  SoleTraderNamePage
  ) ++ allAfterRegistrationTypePages

  val allPages = List(DoYouHaveUTRPage) ++ allAfterHaveUTRPages
}
