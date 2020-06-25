package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.libs.json.{JsError, JsString, Json}

class SelectAddressSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks with OptionValues {

  "SelectAddress" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(SelectAddress.values)

      forAll(gen) {
        selectAddress =>

          JsString(selectAddress.toString).validate[SelectAddress].asOpt.value mustEqual selectAddress
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!SelectAddress.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[SelectAddress] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(SelectAddress.values)

      forAll(gen) {
        selectAddress =>

          Json.toJson(selectAddress) mustEqual JsString(selectAddress.toString)
      }
    }
  }
}
