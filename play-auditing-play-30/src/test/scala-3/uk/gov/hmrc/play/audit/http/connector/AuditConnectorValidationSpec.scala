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

package uk.gov.hmrc.play.audit.http.connector

import java.time.Instant
import org.apache.pekko.actor.ActorSystem
import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.scalatest._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsNull, JsObject, JsValue, Json}
import uk.gov.hmrc.audit.{DatastreamMetricsMock, HandlerResult}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.audit.http.config.{AuditingConfig, BaseUri, Consumer}
import uk.gov.hmrc.play.audit.http.connector.AuditResult._
import uk.gov.hmrc.play.audit.model.{DataCall, DataEvent, ExtendedDataEvent, MergedDataEvent}
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.OFormat
import uk.gov.hmrc.play.audit.http.validation.AuditFormat
import uk.gov.hmrc.audit.http.validation.CipAuditEventUnvalidated
import uk.gov.hmrc.play.audit.model.ValidatedDataEvent
import uk.gov.hmrc.play.audit.http.validation.AuditEventSchema

@CipAuditEventUnvalidated
case class MyExampleAudit(userType: String, vrn: String)

object MyExampleAudit{
  implicit val format: AuditFormat[MyExampleAudit] = AuditEventSchema.unvalidatedFormat
}
class AuditConnectorValidationSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with OneInstancePerTest
    with DatastreamMetricsMock {

  implicit val ec: ExecutionContext = RunInlineExecutionContext
  implicit val as: ActorSystem      = ActorSystem()

  private val consumer = Consumer(BaseUri("datastream-base-url", 8080, "http"))

  private val enabledConfig = AuditingConfig(
    consumer = Some(consumer),
    enabled = true,
    auditSource = "the-project-name",
    auditSentHeaders = false
  )
  private val enabledConfigWithProvider =
    enabledConfig.copy(auditProvider = Some("config-provider"))

  private val mockAuditChannel: AuditChannel = mock[AuditChannel]
  when(mockAuditChannel.send(any[String], any[JsValue])(any[ExecutionContext]))
    .thenReturn(Future.successful(HandlerResult.Success))

  private def createConnector(
      config: AuditingConfig,
      metricsKey: Option[String] = Some("play.the-project-name")
  ): AuditConnector =
    new AuditConnector {
      override def auditingConfig    = config
      override def auditChannel      = mockAuditChannel
      override def datastreamMetrics = mockDatastreamMetrics(metricsKey)
    }

  "sendValidatedEvent" should {
    val subscription =
      Subscription("John", 25, List("one"), Set("two"), Address("nowhere avenue", "AB12 3CD", Option("UK")))

    val validatedEvent = ValidatedDataEvent("source", "type", detail = subscription)

    "send a validated event" in {
      createConnector(enabledConfig).sendValidatedEvent(validatedEvent).futureValue shouldBe AuditResult.Success

      val captor = ArgumentCaptor.forClass(classOf[JsValue])

      verify(mockAuditChannel).send(any[String], captor.capture())(any[ExecutionContext])

      // val capturedValue = captor.getValue()
    }
  }
}
