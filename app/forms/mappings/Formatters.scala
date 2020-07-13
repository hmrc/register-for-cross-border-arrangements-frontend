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

package forms.mappings

import models.Enumerable
import play.api.data.FormError
import play.api.data.format.Formatter

import scala.util.control.Exception.nonFatalCatch
import play.api.Logger

trait Formatters extends Transforms {

  private[mappings] def stringFormatter(errorKey: String): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None | Some("") => Left(Seq(FormError(key, errorKey)))
        case Some(s) => Right(s)
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def stringTrimFormatter(errorKey: String): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
            case None => Left(Seq(FormError(key, errorKey)))
            case Some(s) => {s.trim match {
              case "" => Left(Seq(FormError(key, errorKey)))
              case s1  =>  Right (s1)
            }
        }
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def booleanFormatter(requiredKey: String, invalidKey: String): Formatter[Boolean] =
    new Formatter[Boolean] {

      private val baseFormatter = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .right.flatMap {
          case "true" => Right(true)
          case "false" => Right(false)
          case _ => Left(Seq(FormError(key, invalidKey)))
        }

      def unbind(key: String, value: Boolean) = Map(key -> value.toString)
    }

  private[mappings] def intFormatter(requiredKey: String, wholeNumberKey: String, nonNumericKey: String, args: Seq[String] = Seq.empty): Formatter[Int] =
    new Formatter[Int] {

      val decimalRegexp = """^-?(\d*\.\d*)$"""

      private val baseFormatter = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .right.map(_.replace(",", ""))
          .right.flatMap {
          case s if s.matches(decimalRegexp) =>
            Left(Seq(FormError(key, wholeNumberKey, args)))
          case s =>
            nonFatalCatch
              .either(s.toInt)
              .left.map(_ => Seq(FormError(key, nonNumericKey, args)))
        }

      override def unbind(key: String, value: Int) =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] val optionalStringFormatter: Formatter[Option[String]] = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] =
      Right(
        data
          .get(key)
          .filter(_.lengthCompare(0) > 0)
      )

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      Map(key -> value.getOrElse(""))
  }

  private[mappings] def enumerableFormatter[A](requiredKey: String, invalidKey: String)(implicit ev: Enumerable[A]): Formatter[A] =
    new Formatter[A] {

      private val baseFormatter = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], A] =
        baseFormatter.bind(key, data).right.flatMap {
          str =>
            ev.withName(str).map(Right.apply).getOrElse(Left(Seq(FormError(key, invalidKey))))
        }

      override def unbind(key: String, value: A): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }

  protected def validatedTextFormatter(requiredKey: String, invalidKey: String, lengthKey: String, regex: String, length: Int) = new Formatter[String] {
    private val dataFormatter: Formatter[String] = stringTrimFormatter(requiredKey)
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      dataFormatter
        .bind(key, data)
        .right.flatMap {
        case str if str.matches(regex) => {
          Right(str)
        }
        case str if str.length > length =>  {
          Left(Seq(FormError(key, lengthKey)))
        }
        case str => {
          Left(Seq(FormError(key, invalidKey)))
        }
      }
    }
    override def unbind(key: String, value: String): Map[String, String] = {
      Map(key -> value)
    }
  }

  protected def addressPostcodeFormatter(invalidKey: String, emptyKey: String = "postCode.error.required"): Formatter[Option[String]] = new Formatter[Option[String]] {

    private val regexPostcode = """^[A-Za-z]{1,2}[0-9Rr][0-9A-Za-z]?\s?[0-9][ABD-HJLNP-UW-Zabd-hjlnp-uw-z]{2}$"""

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val keydata = data.get(key)
      keydata match {
        case Some(s)
          if s.trim == "" =>
          Left(Seq(FormError(key, emptyKey)))
        case Some(s)
          if ! stripSpaces (s).matches (regexPostcode) =>
          Left (Seq (FormError (key, invalidKey) ) )
        case Some(s)
          if stripSpaces(s).matches(regexPostcode) => 
            Right(Some(validPostCodeFormat(stripSpaces(s))))
        case _ => Left(Seq(FormError(key, emptyKey)))
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = {
      Map(key -> value.getOrElse(""))
    }
  }


}
