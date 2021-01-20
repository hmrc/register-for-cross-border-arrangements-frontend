/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.requests.UserRequest
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, credentialRole}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[UserRequest, AnyContent] with ActionFunction[Request, UserRequest]

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: FrontendAppConfig,
                                               val parser: BodyParsers.Default
                                             )
                                             (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authorised(AuthProviders(GovernmentGateway) and ConfidenceLevel.L50)
      .retrieve(Retrievals.internalId and Retrievals.allEnrolments and affinityGroup and credentialRole) {
        case _ ~ _ ~ Some(Agent) ~ _ =>
          Future.successful(Redirect(routes.UnauthorisedAgentController.onPageLoad()))
        case _ ~ enrolments ~ _ ~ Some(Assistant) if !enrolments.enrolments.exists(_.key == "HMRC-DAC6-ORG")  =>
          Future.successful(Redirect(routes.UnauthorisedAssistantController.onPageLoad()))
        case Some(internalID) ~ enrolments ~ _ ~ _ =>
          block(UserRequest(enrolments, internalID, request))
        case _ => throw new UnauthorizedException("Unable to retrieve internal Id")
    } recover {
      case _: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: InsufficientEnrolments => Redirect(routes.UnauthorisedController.onPageLoad())
      case _: InsufficientConfidenceLevel => Redirect(routes.UnauthorisedController.onPageLoad())
      case _: UnsupportedAuthProvider => Redirect(routes.UnauthorisedController.onPageLoad())
      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad())
      case _: UnauthorizedException => Redirect(routes.UnauthorisedController.onPageLoad())
    }
  }
}
