/*
 * Copyright 2026 HM Revenue & Customs
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

import uk.gov.hmrc.audit.http.validation.CipAuditEventSchema
import play.api.libs.json.OFormat
import uk.gov.hmrc.play.audit.http.validation.JsonValidatorMacro
import play.api.libs.json.Json
import uk.gov.hmrc.play.audit.http.validation.AuditFormat

@CipAuditEventSchema(schemaFile = "/subscription-schema.json")
case class Subscription(name: String, age: Int, list: List[String], set: Set[String], address: Address)

case class Address(street: String, postcode: String)

object Address:
  implicit val format: OFormat[Address] = Json.format[Address]

object Subscription:
  implicit val format: AuditFormat[Subscription] = JsonValidatorMacro.generateValidatedJson[Subscription]
