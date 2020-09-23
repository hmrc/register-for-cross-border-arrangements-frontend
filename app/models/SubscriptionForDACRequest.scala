package models

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import pages._
import play.api.libs.json.{Json, OWrites, Reads, __}

import scala.util.Random

case class SubscriptionForDACRequest(requestCommon: RequestCommon, requestDetail: RequestDetail)

object SubscriptionForDACRequest {
  implicit val format = Json.format[SubscriptionForDACRequest]

  def createDacRequest(userAnswers: UserAnswers): SubscriptionForDACRequest = {

    SubscriptionForDACRequest(
      requestCommon = createRequestCommon,
      requestDetail = createRequestDetail(userAnswers)
    )
  }

  private def createRequestCommon: RequestCommon = {
    //Format: ISO 8601 YYYY-MM-DDTHH:mm:ssZ e.g. 2020-09-23T16:12:11Z
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    //TODO Are we storing acknowledgementReference below? Do we need to check if they exist already? <- low chance
    val r = new Random()
    val idSize: Int = 1 + r.nextInt(33) //Generate a size between 1 and 32
    val generateAcknowledgementReference: String = r.alphanumeric.take(idSize).mkString

    RequestCommon(
      regime = "DAC",
      receiptDate = ZonedDateTime.now().format(formatter),
      acknowledgementReference = generateAcknowledgementReference,
      originatingSystem = "DAC6",
      requestParameters = None,
      paramName = "",
      paramValue = ""
    )
  }

  private def createRequestDetail(userAnswers: UserAnswers): RequestDetail = {
    val isGBUser = userAnswers.get(NonUkNamePage) match {
      case Some(_) => false
      case None => true
    }

    RequestDetail(
      idType = getIdType(userAnswers),
      idNumber = "idNumber",
      tradingName = Some("tradingName"),
      isGBUser = isGBUser,
      primaryContact = createPrimaryContact(userAnswers),
      secondaryContact = createSecondaryContact(userAnswers)
    )
  }

  private def getIdType(userAnswers: UserAnswers): String = {
    (userAnswers.get(NinoPage), userAnswers.get(SelfAssessmentUTRPage), userAnswers.get(CorporationTaxUTRPage)) match {
      case (Some(nino), _, _) => nino.toString().capitalize
      case (_, Some(selfAssessmentUTRPage), _) => selfAssessmentUTRPage.uniqueTaxPayerReference.capitalize
      case (_, _, Some(corporationTaxUTRPage)) => corporationTaxUTRPage.uniqueTaxPayerReference.capitalize
    }
  }

  private def createPrimaryContact(userAnswers: UserAnswers): PrimaryContact = {
    userAnswers.get(BusinessTypePage) match {
      case Some(BusinessType.NotSpecified) => PrimaryContact(createContactInformation(userAnswers))
      case _ => PrimaryContact(createContactInformation(userAnswers))
    }
  }

  private def createSecondaryContact(userAnswers: UserAnswers): Option[SecondaryContact] = {
    userAnswers.get(HaveSecondContactPage) match {
      case Some(true) => Some(SecondaryContact(createContactInformation(userAnswers, secondaryContact = true)))
      case _ => None
    }
  }

  private def createContactInformation(userAnswers: UserAnswers, secondaryContact: Boolean = false): ContactInformation = {

    if (secondaryContact) {
      val secondaryEmail = userAnswers.get(SecondaryContactEmailAddressPage) match {
        case Some(email) => email
        case None => "" //TODO Secondary email should be optional? In the API it isn't
      }

      val secondaryContactNumber = userAnswers.get(SecondaryContactTelephoneNumberPage)

      ContactInformation(
        email = secondaryEmail,
        phone = secondaryContactNumber,
        mobile = secondaryContactNumber,
        userType = Organisation(userAnswers))
    } else {
      val email = userAnswers.get(ContactEmailAddressPage) match {
        case Some(email) => email
        case None => throw new Exception("Email can't be empty when creating a subscription")
      }

      val contactNumber = userAnswers.get(ContactTelephoneNumberPage)

      if (getIdType(userAnswers) == "NINO") {
        ContactInformation(
          email = email,
          phone = contactNumber,
          mobile = contactNumber,
          userType = Individual(userAnswers))
      } else {
        ContactInformation(
          email = email,
          phone = contactNumber,
          mobile = contactNumber,
          userType = Organisation(userAnswers))
      }
    }
  }

}

trait IndividualOrBusinessSubscription

case class Organisation(name: String) extends IndividualOrBusinessSubscription

object Organisation {
  def apply(userAnswers: UserAnswers): Organisation = {
    userAnswers.get(BusinessNamePage) match {
      case Some(name) => new Organisation(name)
      case None => throw new Exception("Organisation name can't be empty when creating a subscription")
    }
  }

  implicit val format = Json.format[Organisation]
}

case class Individual(firstName: String,
                      middleName: Option[String],
                      lastName: String) extends IndividualOrBusinessSubscription

object Individual {
  def apply(userAnswers: UserAnswers): Individual = {
    userAnswers.get(ContactNamePage) match {
      case Some(name) => new Individual(name.firstName, None, name.secondName)
      case None => throw new Exception("Individual name can't be empty when creating a subscription")
    }
  }

  implicit val format = Json.format[RequestDetail]
}

case class ContactInformation(email: String,
                              phone: Option[String],
                              mobile: Option[String],
                              userType: IndividualOrBusinessSubscription)

object ContactInformation {
  implicit val format = Json.format[ContactInformation]
}

case class PrimaryContact(contactInformation: ContactInformation)

object PrimaryContact {
  implicit val format = Json.format[PrimaryContact]
}

case class SecondaryContact(contactInformation: ContactInformation)

object SecondaryContact {
  implicit val format = Json.format[SecondaryContact]
}

case class RequestCommon(regime: String,
                         receiptDate: String,
                         acknowledgementReference: String,
                         originatingSystem: String,
                         requestParameters: Option[Seq[_]],
                         paramName: String,
                         paramValue: String)
object RequestCommon {
  implicit val format = Json.format[RequestCommon]
}

case class RequestDetail(idType: String,
                         idNumber: String,
                         tradingName: Option[String],
                         isGBUser: Boolean,
                         primaryContact: PrimaryContact,
                         secondaryContact: Option[SecondaryContact])
object RequestDetail {
  implicit val format = Json.format[RequestDetail]
}

