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

package helpers

import models.{BusinessType, CheckMode, Mode, RegistrationType, UserAnswers}
import pages.{BusinessTypePage, QuestionPage, RegistrationTypePage}
import play.api.libs.json.Reads

object JourneyHelpers {

  def isOrganisationJourney(ua: UserAnswers): Boolean =
    (ua.get(BusinessTypePage), ua.get(RegistrationTypePage)) match {
      case (Some(BusinessType.NotSpecified), _) => false
      case (_, Some(RegistrationType.Business)) => true
      case (Some(_), _)                         => true
      case _                                    => false
    }

  def redirectToSummary[T](value: T, page: QuestionPage[T], mode: Mode, ua: UserAnswers)(implicit rds: Reads[T]): Boolean =
    ua.get(page) match {
      case Some(ans) if (ans == value) && (mode == CheckMode) => true
      case _                                                  => false
    }

}
