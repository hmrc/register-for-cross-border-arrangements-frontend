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

package models

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels._

sealed trait RegistrationType

object RegistrationType extends Enumerable.Implicits {

  case object Business extends WithName("business") with RegistrationType
  case object Individual extends WithName("individual") with RegistrationType

  val values: Seq[RegistrationType] = Seq(
    Business,
    Individual
  )

  def radios(form: Form[_])(implicit messages: Messages): Seq[Radios.Item] = {
    val field = form("registrationType")
    Seq(
      Radios.Item("business", msg"registrationType.business", Business.toString, field.values.contains(Business.toString)),
      Radios.Item("individual", msg"registrationType.individual", Individual.toString, field.values.contains(Individual.toString))
    )
  }

  implicit val enumerable: Enumerable[RegistrationType] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )
}
