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

package models

import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json._
import uk.gov.hmrc.viewmodels._

sealed trait SelectAddress

object SelectAddress extends Enumerable.Implicits {
//TODO Delete since it is no longer being used
  case object Address1 extends WithName("address 1") with SelectAddress
  case object Address3 extends WithName("address 3") with SelectAddress

  val values: Seq[SelectAddress] = Seq(
    Address1,
    Address3
  )

  def radios(form: Form[_])(implicit messages: Messages): Seq[Radios.Item] = {

    val field = form("value")
    val items = Seq(
      Radios.Radio(msg"selectAddress.address 1", Address1.toString),
      Radios.Radio(msg"selectAddress.address 3", Address3.toString)
    )

    Radios(field, items)
  }

  implicit val enumerable: Enumerable[SelectAddress] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
