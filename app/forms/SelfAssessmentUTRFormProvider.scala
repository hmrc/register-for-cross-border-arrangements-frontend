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

package forms

import forms.mappings.Mappings
import javax.inject.Inject
import models.UniqueTaxpayerReference
import play.api.data.Form
import play.api.data.Forms._
import utils.RegexConstants

class SelfAssessmentUTRFormProvider @Inject() extends Mappings with RegexConstants {

  private val length = 10

  def apply(): Form[UniqueTaxpayerReference] = Form(
    mapping(
      "selfAssessmentUTR" -> validatedFixedLengthText("selfAssessmentUTR.error.required",
                                                      "selfAssessmentUTR.error.invalid",
                                                      "selfAssessmentUTR.error.length",
                                                      utrRegex,
                                                      length
      )
    )(UniqueTaxpayerReference.apply)(UniqueTaxpayerReference.unapply)
  )
}
