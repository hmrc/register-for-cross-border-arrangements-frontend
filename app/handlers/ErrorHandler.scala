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

package handlers

import config.FrontendAppConfig
import controllers.exceptions.SomeInformationIsMissingException
import play.api.http.HttpErrorHandler
import play.api.http.Status._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.{Logger, PlayException}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

// NOTE: There should be changes to bootstrap to make this easier, the API in bootstrap should allow a `Future[Html]` rather than just an `Html`
@Singleton
class ErrorHandler @Inject() (
  renderer: Renderer,
  val messagesApi: MessagesApi,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends HttpErrorHandler
    with I18nSupport {
  private val logger: Logger = Logger(this.getClass)

  override def onClientError(request: RequestHeader, statusCode: Int, message: String = ""): Future[Result] = {

    implicit val rh: RequestHeader = request

    statusCode match {
      case BAD_REQUEST =>
        renderer.render("badRequest.njk").map(BadRequest(_))
      case NOT_FOUND =>
        val json = Json.obj(
          "emailAddress" -> appConfig.emailEnquiries
        )
        renderer.render("notFound.njk", json).map(NotFound(_))
      case _ =>
        Future.successful(Redirect(controllers.routes.ProblemWithServiceController.onPageLoad()))
    }
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {

    logError(request, exception)
    exception match {
      case e: SomeInformationIsMissingException =>
        Future.successful(Redirect(controllers.routes.SomeInformationIsMissingController.onPageLoad()))
      case ApplicationException(result, _) =>
        Future.successful(result)
      case _ =>
        Future.successful(Redirect(controllers.routes.ProblemWithServiceController.onPageLoad()))
    }
  }

  private def logError(request: RequestHeader, ex: Throwable): Unit =
    logger.error(
      """
        |
        |! %sInternal server error, for (%s) [%s] ->
        | """.stripMargin.format(ex match {
                                   case p: PlayException => "@" + p.id + " - "
                                   case _                => ""
                                 },
                                 request.method,
                                 request.uri
      ),
      ex
    )
}
