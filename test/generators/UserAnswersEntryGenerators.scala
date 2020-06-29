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

package generators

import java.time.LocalDate

import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import pages._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Nino
import wolfendale.scalacheck.regexp.RegexpGen

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {
  self: Generators =>

  implicit lazy val arbitrarySecondaryContactEmailAddressUserAnswersEntry: Arbitrary[(SecondaryContactEmailAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SecondaryContactEmailAddressPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySecondaryContactTelephoneNumberUserAnswersEntry: Arbitrary[(SecondaryContactTelephoneNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SecondaryContactTelephoneNumberPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactTelephoneNumberUserAnswersEntry: Arbitrary[(ContactTelephoneNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ContactTelephoneNumberPage.type]
        value <- RegexpGen.from("""^\+?[\d\s]+$""")
          .suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhatIsYourAddressUkUserAnswersEntry: Arbitrary[(WhatIsYourAddressUkPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[WhatIsYourAddressUkPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactEmailAddressUserAnswersEntry: Arbitrary[(ContactEmailAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ContactEmailAddressPage.type]
        value <- RegexpGen.from("^(?:[a-zA-Z0-9!#$%&*+\\/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&*+\\/=?^_`{|}~-]+)*)" +
          "@(?:[a-zA-Z0-9!#$%&*+\\/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&*+\\/=?^_`{|}~-]+)*)$")
          .suchThat(_.nonEmpty)
          .map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBusinessAddressUserAnswersEntry: Arbitrary[(BusinessAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BusinessAddressPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBusinessNamePageUserAnswersEntry: Arbitrary[(BusinessNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BusinessNamePage.type]
        value <- RegexpGen.from("^[a-zA-Z0-9 '&\\/]{1,105}$")
          .suchThat(_.nonEmpty)
          .map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryConfirmBusinessUserAnswersEntry: Arbitrary[(ConfirmBusinessPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ConfirmBusinessPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhatIsYourAddressUserAnswersEntry: Arbitrary[(WhatIsYourAddressPage.type, JsValue)] =
      Arbitrary {
      for {
        page  <- arbitrary[WhatIsYourAddressPage.type]
        value <- arbitrary[Address].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIndividualUKPostcodeUserAnswersEntry: Arbitrary[(IndividualUKPostcodePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[IndividualUKPostcodePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDoYouLiveInTheUKUserAnswersEntry: Arbitrary[(DoYouLiveInTheUKPage.type, JsValue)] =
      Arbitrary {
      for {
        page  <- arbitrary[DoYouLiveInTheUKPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDateOfBirthUserAnswersEntry: Arbitrary[(DateOfBirthPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[DateOfBirthPage.type]
        value <- arbitrary[LocalDate].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryNamePageUserAnswersEntry: Arbitrary[(NamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[NamePage.type]
        value <- arbitrary[Name].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySoleTraderNameUserAnswersEntry: Arbitrary[(SoleTraderNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SoleTraderNamePage.type]
        value <- arbitrary[Name].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDoYouHaveUTRUserAnswersEntry: Arbitrary[(DoYouHaveUTRPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[DoYouHaveUTRPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryRegistrationTypeUserAnswersEntry: Arbitrary[(RegistrationTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[RegistrationTypePage.type]
        value <- arbitrary[RegistrationType].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryRegistrationType: Arbitrary[RegistrationType] =
    Arbitrary {
      Gen.oneOf(RegistrationType.values.toSeq)
    }

  implicit lazy val arbitraryNinoUserAnswersEntry: Arbitrary[(NinoPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[NinoPage.type]
        value <- arbitrary[Nino].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDoYouHaveANationalInsuranceNumberUserAnswersEntry: Arbitrary[(DoYouHaveANationalInsuranceNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[DoYouHaveANationalInsuranceNumberPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPostCodeUserAnswersEntry: Arbitrary[(PostCodePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PostCodePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySelfAssessmentUTRPageUserAnswersEntry: Arbitrary[(SelfAssessmentUTRPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SelfAssessmentUTRPage.type]
        value <- arbitrary[UniqueTaxpayerReference].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCorporationTaxUTRUserAnswersEntry: Arbitrary[(CorporationTaxUTRPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CorporationTaxUTRPage.type]
        value <- arbitrary[UniqueTaxpayerReference].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBusinessTypeUserAnswersEntry: Arbitrary[(BusinessTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BusinessTypePage.type]
        value <- arbitrary[BusinessType].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBusinessNameUserAnswersEntry: Arbitrary[(BusinessWithoutIDNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BusinessWithoutIDNamePage.type]
        value <- arbitrary[String].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactNameUserAnswersEntry: Arbitrary[(ContactNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ContactNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHaveSecondContactUserAnswersEntry: Arbitrary[(HaveSecondContactPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HaveSecondContactPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }


  implicit lazy val arbitraryTelephoneNumberQuestionUserAnswersEntry: Arbitrary[(TelephoneNumberQuestionPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TelephoneNumberQuestionPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySecondaryContactNameUserAnswersEntry: Arbitrary[(SecondaryContactNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SecondaryContactNamePage.type]
        value <- RegexpGen.from("^[a-zA-Z0-9 '&\\\\/]{1,50}$")
                .suchThat(_.nonEmpty)
                .map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySecondaryContactPreferenceUserAnswersEntry: Arbitrary[(SecondaryContactPreferencePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SecondaryContactPreferencePage.type]
        value <- arbitrary[SecondaryContactPreference].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySelectAddressUserAnswersEntry: Arbitrary[(SelectAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SelectAddressPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }
}
