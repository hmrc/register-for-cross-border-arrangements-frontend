/*
 * Copyright 2022 HM Revenue & Customs
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

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success

import scala.concurrent.Future

class AuditServiceSpec extends SpecBase {

  val mockAuditConnector = mock[AuditConnector]
  val eventDetail        = Json.obj("test-param1" -> "test-value-1")

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuditConnector].toInstance(mockAuditConnector)
    )
    .build()

  val auditService = injector.instanceOf[AuditService]

  "Auditservice must" - {
    "call the audit connector and sendExtendedEvent" in {
      when(mockAuditConnector.sendExtendedEvent(any())(any(), any())).thenReturn(Future.successful(Success))
      val request = FakeRequest(GET, "/")
      auditService.sendAuditEvent("dummy app", eventDetail, "transactionName", "path")(hc, request)
      verify(mockAuditConnector, times(1)).sendExtendedEvent(any())(any(), any())
    }
  }

}
