package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class IInformation==IinformationsetnFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "iInformation==Iinformationsetn.error.required"
  val lengthKey = "iInformation==Iinformationsetn.error.length"
  val maxLength = exit

  val form = new IInformation==IinformationsetnFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
