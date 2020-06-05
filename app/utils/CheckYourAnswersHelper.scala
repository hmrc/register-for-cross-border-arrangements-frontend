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

package utils

import java.time.format.DateTimeFormatter

import controllers.routes
import models.{BusinessType, CheckMode, UserAnswers}
import pages._
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels.Text.Literal
import uk.gov.hmrc.viewmodels._
import utils.CheckYourAnswersHelper._

class CheckYourAnswersHelper(userAnswers: UserAnswers) {

  def secondaryContactName: Option[Row] = userAnswers.get(SecondaryContactNamePage) map {
    answer =>
      Row(
        key     = Key(msg"secondaryContactName.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.SecondaryContactNameController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"secondaryContactName.checkYourAnswersLabel"))
          )
        )
      )
  }


  def whatIsYourAddressUk: Option[Row] = userAnswers.get(WhatIsYourAddressUkPage) map {
    answer =>
      Row(
        key     = Key(msg"whatIsYourAddressUk.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(lit"$answer.lines"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.WhatIsYourAddressUkController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"whatIsYourAddressUk.checkYourAnswersLabel"))
                      )
        )
      )
  }

  def telephoneNumberQuestion: Option[Row] = userAnswers.get(TelephoneNumberQuestionPage) map {
    answer =>
      Row(
        key     = Key(msg"telephoneNumberQuestion.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.TelephoneNumberQuestionController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"telephoneNumberQuestion.checkYourAnswersLabel"))

          )
        )
      )
  }

  def confirmBusiness: Option[Row] = userAnswers.get(ConfirmBusinessPage) map {
    answer =>
      Row(
        key     = Key(msg"confirmBusiness.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.ConfirmBusinessController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"confirmBusiness.checkYourAnswersLabel"))
          )
        )
      )
  }

  def contactName: Option[Row] = userAnswers.get(ContactNamePage) map {
    answer =>
      Row(
        key     = Key(msg"contactName.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.ContactNameController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"contactName.checkYourAnswersLabel"))
          )
        )
      )
  }

  def contactEmailAddress: Option[Row] = userAnswers.get(ContactEmailAddressPage) map {
    answer =>
      Row(
        key     = Key(msg"contactEmailAddress.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.ContactEmailAddressController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"contactEmailAddress.checkYourAnswersLabel"))
          )
        )
      )
  }

  def businessAddress: Option[Row] = userAnswers.get(BusinessAddressPage) map {
    answer =>
      Row(
        key     = Key(msg"businessAddress.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.BusinessAddressController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"businessAddress.checkYourAnswersLabel"))
          )
        )
      )
  }

  def businessWithoutIDName: Option[Row] = userAnswers.get(BusinessWithoutIDNamePage) map {
    answer =>
      Row(
        key     = Key(msg"businessWithoutIDName.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.BusinessWithoutIDNameController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"businessWithoutIDName.checkYourAnswersLabel"))
          )
        )
      )
  }

  def businessNamePage: Option[Row] = {
    val returnUrl = userAnswers.get(BusinessTypePage) map {
      case BusinessType.NotSpecified => routes.SoleTraderNameController.onPageLoad(CheckMode).url
      case BusinessType.Partnership => routes.BusinessNamePartnershipController.onPageLoad(CheckMode).url
      case BusinessType.LimitedLiability | BusinessType.CorporateBody => routes.BusinessNameRegisteredBusinessController.onPageLoad(CheckMode).url
      case BusinessType.UnIncorporatedBody => routes.BusinessNameOrganisationController.onPageLoad(CheckMode).url
    } getOrElse("")

    userAnswers.get(BusinessNamePage) map {
      answer =>
        Row(
          key     = Key(msg"businessNamePage.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
          value   = Value(lit"$answer"),
          actions = List(
            Action(
              content            = msg"site.edit",
              href               = returnUrl,
              visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"businessNamePage.checkYourAnswersLabel"))
            )
          )
        )
    }
  }

  def individualUKPostcode: Option[Row] = userAnswers.get(IndividualUKPostcodePage) map {
    answer =>
      Row(
        key     = Key(msg"individualUKPostcode.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.IndividualUKPostcodeController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"individualUKPostcode.checkYourAnswersLabel"))
          )
        )
      )
  }

  def whatIsYourAddress: Option[Row] = userAnswers.get(WhatIsYourAddressPage) map {
    answer =>
      Row(
        key     = Key(msg"whatIsYourAddress.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(lit"${answer.lines}"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.WhatIsYourAddressController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"whatIsYourAddress.checkYourAnswersLabel"))
          )
        )
      )
  }

  def doYouLiveInTheUK: Option[Row] = userAnswers.get(DoYouLiveInTheUKPage) map {
    answer =>
      Row(
        key     = Key(msg"doYouLiveInTheUK.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.DoYouLiveInTheUKController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"doYouLiveInTheUK.checkYourAnswersLabel"))
          )
        )
      )
  }

  def nonUkName: Option[Row] = userAnswers.get(NonUkNamePage) map {
    answer =>
      Row(
        key     = Key(msg"nonUkName.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(lit"${answer.firstName} ${answer.secondName}"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.NonUkNameController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"nonUkName.checkYourAnswersLabel"))
          )
        )
      )
  }

  def dateOfBirth: Option[Row] = userAnswers.get(DateOfBirthPage) map {
    answer =>
      Row(
        key     = Key(msg"dateOfBirth.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(Literal(answer.format(dateFormatter))),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.DateOfBirthController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"dateOfBirth.checkYourAnswersLabel"))
          )
        )
      )
  }
            
  def doYouHaveUTR: Option[Row] = userAnswers.get(DoYouHaveUTRPage) map {
    answer =>
      Row(
        key     = Key(msg"doYouHaveUTR.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.DoYouHaveUTRController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"doYouHaveUTR.checkYourAnswersLabel"))
          )
        )
      )
  }

  def registrationType: Option[Row] = userAnswers.get(RegistrationTypePage) map {
    answer =>
      Row(
        key     = Key(msg"registrationType.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(msg"registrationType.$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.RegistrationTypeController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"registrationType.checkYourAnswersLabel")))))
  }

  def namePage: Option[Row] = userAnswers.get(NamePage) map {
    answer =>
      Row(
        key = Key(msg"name.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.NameController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"namePage.checkYourAnswersLabel"))
          )
        )
      )
  }

    def postCode: Option[Row] = userAnswers.get(PostCodePage) map {
      answer =>
        Row(
          key     = Key(msg"postCode.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
          value   = Value(lit"$answer"),
          actions = List(
            Action(
              content            = msg"site.edit",
              href               = routes.PostCodeController.onPageLoad(CheckMode).url,
              visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"postCode.checkYourAnswersLabel"))
          )
        )
      )
  }

  def nino: Option[Row] = userAnswers.get(NinoPage) map {
    answer =>
      Row(
        key     = Key(msg"nino.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.NinoController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"nino.checkYourAnswersLabel"))
          )
        )
      )
  }

  def doYouHaveANationalInsuranceNumber: Option[Row] = userAnswers.get(DoYouHaveANationalInsuranceNumberPage) map {
    answer =>
      Row(
        key = Key(msg"doYouHaveANationalInsuranceNumber.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.DoYouHaveANationalInsuranceNumberController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"doYouHaveANationalInsuranceNumber.checkYourAnswersLabel"))
          )
        )
      )
  }

  def uniqueTaxpayerReference: Option[Row] = userAnswers.get(UniqueTaxpayerReferencePage) map {
    answer =>
      Row(
        key     = Key(msg"uniqueTaxpayerReference.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(lit"${answer.uniqueTaxPayerReference}"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.UniqueTaxpayerReferenceController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"uniqueTaxpayerReference.checkYourAnswersLabel"))
          )
        )
      )
  }

  def businessType: Option[Row] = userAnswers.get(BusinessTypePage) map {
    answer =>
      Row(
        key     = Key(msg"businessType.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value   = Value(msg"businessType.$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.BusinessTypeController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"businessType.checkYourAnswersLabel"))
          )
        )
      )
  }

  private def yesOrNo(answer: Boolean): Content =
    if (answer) {
      msg"site.yes"
    } else {
      msg"site.no"
    }
}

object CheckYourAnswersHelper {

  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
}
