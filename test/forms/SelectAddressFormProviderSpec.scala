package forms

import forms.behaviours.OptionFieldBehaviours
import models.SelectAddress
import play.api.data.FormError

class SelectAddressFormProviderSpec extends OptionFieldBehaviours {

  val form = new SelectAddressFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "selectAddress.error.required"

    behave like optionsField[SelectAddress](
      form,
      fieldName,
      validValues  = SelectAddress.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
