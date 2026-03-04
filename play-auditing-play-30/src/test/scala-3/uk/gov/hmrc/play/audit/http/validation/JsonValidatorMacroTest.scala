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

package uk.gov.hmrc.play.audit.http.validation

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.*
import org.scalatest.{Inside, Inspectors}

import scala.language.postfixOps

class JsonValidatorMacroTest extends AnyFunSuite with Inside with Inspectors with Matchers:

  test("missing annotation") {
    """import uk.gov.hmrc.play.audit.http.validation.JsonValidatorMacro
      |import play.api.libs.json.{Json, OFormat}

      |case class Subscription(name: String, age: Int, address: Address)

      |case class Address(street: String, postcode: String)

      |object Address:
      |  implicit val format: OFormat[Address] = Json.format[Address]

      |object Subscription:
      |  implicit val format: AuditFormat[Subscription] = JsonValidatorMacro.generateValidatedJson[Subscription]""".stripMargin shouldNot compile
  }

  test("Schema file not found") {
    """
    |import play.api.libs.json.Json
    |import play.api.libs.json.OFormat
    |import uk.gov.hmrc.audit.http.validation.CipAuditEventSchema

    |@CipAuditEventSchema(schemaFile = "/subscription-schema-not-found.json")
    |case class Subscription(name: String, age: Int, address: Address)

    |case class Address(street: String, postcode: String)

    |object Address:
    |  implicit val format: OFormat[Address] = Json.format[Address]

    |object Subscription:
    |  implicit val format: AuditFormat[Subscription] = JsonValidatorMacro.generateValidatedJson[Subscription]""".stripMargin shouldNot compile
  }

  test("Missing field") {
    """
      |
    |import play.api.libs.json.Json
    |import play.api.libs.json.OFormat
    |import uk.gov.hmrc.audit.http.validation.CipAuditEventSchema

    |@CipAuditEventSchema(schemaFile = "/subscription-schema.json")
    |case class Subscription(name: String, age: Int, address: Address, list: List[String], set: Set[String])

    |case class Address(street: String)

    |object Address:
    |  implicit val format: OFormat[Address] = Json.format[Address]

    |object Subscription:
    |  implicit val format: AuditFormat[Subscription] = JsonValidatorMacro.generateValidatedJson[Subscription]""".stripMargin shouldNot compile
  }

  test("no compilation errors") {
    """

      |import uk.gov.hmrc.play.audit.http.validation.JsonValidatorMacro
      |import play.api.libs.json.{Json, OFormat}
      |import uk.gov.hmrc.audit.http.validation.CipAuditEventSchema

      |@CipAuditEventSchema(schemaFile = "/subscription-schema.json")
      |case class Subscription(name: String, age: Int, list: List[String], set: Set[String], address: Address)

      |case class Address(street: String, postcode: String)

      |object Address:
      |  implicit val format: OFormat[Address] = Json.format[Address]

      |object Subscription:
      |  implicit val format: AuditFormat[Subscription] = JsonValidatorMacro.generateValidatedJson[Subscription]""".stripMargin should compile
  }

  test("additional property") {
    """
      |
      |import uk.gov.hmrc.play.audit.http.validation.{JsonValidatorMacro}
      |import play.api.libs.json.{Json, OFormat}
      |import uk.gov.hmrc.audit.http.validation.CipAuditEventSchema

      |@CipAuditEventSchema(schemaFile = "/subscription-schema.json")
      |case class Subscription(name: String, age: Int, list: List[String], set: Set[String], address: Address)

      |case class Address(street: String, postcode: String, country: Option[String])

      |object Address:
      |  implicit val format: OFormat[Address] = Json.format[Address]

      |object Subscription:
      |  implicit val format: AuditFormat[Subscription] = JsonValidatorMacro.generateValidatedJson[Subscription]""".stripMargin shouldNot compile
  }
