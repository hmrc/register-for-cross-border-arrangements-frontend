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

package controllers

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, SummaryList}
import utils.CheckYourAnswersHelper

import scala.concurrent.ExecutionContext

class CheckYourAnswersController @Inject()(
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val answers: Seq[SummaryList.Row] = buildBusinessDetails(request.userAnswers)
      val other: Seq[SummaryList.Row] = buildContactDetails(request.userAnswers)

      renderer.render(
        "check-your-answers.njk",
        Json.obj(
          "businessDetailsList" -> answers,
          "contactDetailsList" -> other,
        )
      ).map(Ok(_))
  }

  private def buildBusinessDetails(ua: UserAnswers) = {
    val helper = new CheckYourAnswersHelper(ua)

    //Business with ID, Individual with ID, Business without ID, Individual without ID and all scenarios
    val pagesToCheck = Tuple4(
      helper.businessType,
      helper.nino,
      helper.businessWithoutIDName,
      helper.nonUkName
    )

    pagesToCheck match {
      case (Some(_), None, None, None) =>
        //Business with ID
        Seq(
          helper.businessType,
          helper.doYouHaveUTR,
          helper.businessNamePage
        ).flatten
      case (None, Some(_), None, None) =>
        //Individual with ID
        Seq(
          helper.nino,
          helper.namePage,
          helper.dateOfBirth
        ).flatten
      case (None, None, Some(_), None) =>
        //Business without ID
        Seq(
          helper.businessWithoutIDName,
          helper.businessAddress
        ).flatten
      case (None, None, None, Some(_)) =>
        //Individual without ID
        Seq(
          helper.nonUkName,
          helper.dateOfBirth,
          helper.doYouLiveInTheUK,
          helper.individualUKPostcode,
          helper.whatIsYourAddress,
          helper.whatIsYourAddressUk
        ).flatten
      case _ => ???
    }

  }

  private def buildContactDetails(ua: UserAnswers) = {
    val helper = new CheckYourAnswersHelper(ua)

    val pagesToCheck = Tuple3(
      helper.contactName,
      helper.contactEmailAddress,
      helper.haveSecondContact
    )

    pagesToCheck match {
      case (Some(_), _, None) =>
        //Business. No second contact
        Seq(
          helper.contactName,
          helper.contactEmailAddress,
          helper.telephoneNumberQuestion,
          helper.contactTelephoneNumber,
          helper.haveSecondContact
        ).flatten
      case (None, Some(_), None) =>
        //Individual. No second contact
        Seq(
          helper.contactEmailAddress,
          helper.telephoneNumberQuestion,
          helper.contactTelephoneNumber,
          helper.haveSecondContact
        ).flatten
      case (_, _, Some(_)) =>
        //TODO Split into two for individual and business?
        //Have second contact
        Seq(
          helper.contactName,
          helper.contactEmailAddress,
          helper.telephoneNumberQuestion,
          helper.contactTelephoneNumber,
          helper.haveSecondContact,
          helper.secondaryContactName
        ).flatten
      case _ =>
        println("\n\n_______Here_______\n\n\n")
        ???
    }

  }
}
