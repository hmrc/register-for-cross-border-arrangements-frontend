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

package services

import config.FrontendAppConfig
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditService @Inject() (appConfig: FrontendAppConfig, auditConnector: AuditConnector) {
  private val refererHeaderKey = "Referer"
  private val logger: Logger   = Logger(this.getClass)

  def sendAuditEvent(eventName: String, detail: JsValue, transactionName: String, path: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      ExtendedDataEvent(
        auditSource = appConfig.appName,
        auditType = eventName,
        detail = detail,
        tags = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails()
          ++ AuditExtensions.auditHeaderCarrier(hc).toAuditTags(transactionName, path)
      )
    ) map {
      ar: AuditResult =>
        ar match {
          case Failure(msg, ex) =>
            ex match {
              case Some(throwable) =>
                logger.warn(s"The attempt to issue audit event $eventName failed with message : $msg", throwable)
              case None =>
                logger.warn(s"The attempt to issue audit event $eventName failed with message : $msg")
            }
            ar
          case Disabled =>
            logger.warn(s"The attempt to issue audit event $eventName was unsuccessful, as auditing is currently disabled in config"); ar
          case _ => logger.debug(s"Audit event $eventName issued successsfully."); ar
        }
    }
}
