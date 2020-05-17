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

package controllers.auth

import com.google.inject.Inject
import config.FrontendAppConfig
import models.requests.UserRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.auth.core.Enrolments

import scala.concurrent.{ExecutionContext, Future}

class NotEnrolledForDAC6Action @Inject()(config:FrontendAppConfig)(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[AuthenticatedRequest, UserRequest] {

  override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, UserRequest[A]]] =

    if (request.enrolments.exists(_.key == "DAC6")) {
      Future.successful(Left(Redirect(config.dacFrontendUrl)))
    } else {
      Future.successful(Right(UserRequest(Enrolments(request.enrolments), request.request)))
    }
}
