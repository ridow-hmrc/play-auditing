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

import com.networknt.schema.regex.JoniRegularExpressionFactory
import com.networknt.schema.{SchemaLocation, SchemaRegistry}
import play.api.libs.json.*
import tools.jackson.databind.{JsonNode, ObjectMapper}

import java.net.URI
import java.time.LocalDate
import scala.jdk.CollectionConverters.*
import scala.quoted.*
import scala.util.Random

object JsonValidatorMacro:

  private val cipSchemaAnnotation                  = "uk.gov.hmrc.audit.http.validation.CipAuditEventSchema"
  private val nonValidatedAnnotation               = "uk.gov.hmrc.audit.http.validation.CipAuditEventUnvalidated"
  inline def nonvalidatedFormat[T]: AuditFormat[T] = ${ unvalidatedFormatImpl[T] }

  def unvalidatedFormatImpl[T: Type](using Quotes): Expr[AuditFormat[T]] =
    import quotes.reflect.*
    val typeSymbol = TypeRepr.of[T].typeSymbol
    if !typeSymbol.annotations.exists(_.tpe.show == nonValidatedAnnotation) then
      report.errorAndAbort(s"${typeSymbol.name} must be annotated with @$nonValidatedAnnotation")

    val format = JsMacroImpl.format[T]

    '{
      new AuditFormat[T]($format)
    }

  inline def generateValidatedJson[T]: AuditFormat[T] = ${ generateImpl[T] }

  def generateImpl[T: Type](using Quotes): Expr[AuditFormat[T]] =
    import quotes.reflect.*

    def buildJson(tpe: TypeRepr): JsValue =
      val sym = tpe.typeSymbol

      tpe.asType match
        case '[String] =>
          JsString(Random.alphanumeric.take(8).mkString)
        case '[Int] =>
          JsNumber(Random.nextInt(100))
        case '[Boolean] =>
          JsBoolean(Random.nextBoolean())
        case '[Double] =>
          JsNumber(Random.nextDouble())
        case '[LocalDate] =>
          JsString(LocalDate.now.toString)
        case '[Option[t]] =>
          buildJson(TypeRepr.of[t])
        case '[List[t]] =>
          JsArray(List(buildJson(TypeRepr.of[t])))
        case '[Set[t]] =>
          JsArray(Seq(buildJson(TypeRepr.of[t])))
        case _ if tpe.typeSymbol.flags.is(Flags.Enum) =>
          val sym   = tpe.typeSymbol
          val cases = sym.children
          if cases.isEmpty then report.errorAndAbort(s"Enum ${sym.name} has no cases.")

          val randomCaseSymbol = cases(Random.nextInt(cases.size))

          if !randomCaseSymbol.isClassDef then JsString(randomCaseSymbol.name)
          else buildJson(tpe.memberType(randomCaseSymbol))
        case t if sym.isClassDef && sym.flags.is(Flags.Case) =>
          val fields = sym.caseFields
          val body   = fields
            .map { field =>
              val name     = field.name
              val fieldTpe = tpe.memberType(field)
              name -> buildJson(fieldTpe)
            }
          JsObject(body)
        case _ => report.errorAndAbort(s"Unsupported type: ${tpe.show}")

    val generatedJson = buildJson(TypeRepr.of[T])

    val schema = loadSchema[T]

    if !isValidJson(generatedJson.toString, schema) then report.errorAndAbort(s"AuditEvent does not match CIP Schema")
    val format: Expr[OFormat[T]] = JsMacroImpl.format[T]
    '{
      new AuditFormat[T]($format)
    }

  private def loadSchema[T: Type](using Quotes): URI = {
    import quotes.reflect.*
    val typeSymbol = TypeRepr.of[T].typeSymbol
    val annot      = typeSymbol.annotations.find(_.tpe.show == cipSchemaAnnotation).getOrElse {
      report.errorAndAbort(s"${typeSymbol.name} must be annotated with @$cipSchemaAnnotation")
    }

    val schemaFile: String = annot match {
      case Apply(Select(New(_), _), List(NamedArg("schemaFile", Literal(c)))) => c.value.toString
      case _ => report.errorAndAbort("Could not read schemaFile path from annotation.")
    }
    URI.create(s"resource:$schemaFile")
  }

  private def isValidJson(json: String, schemaUri: URI)(using Quotes): Boolean = {
    import com.networknt.schema.SchemaRegistryConfig
    import quotes.reflect.*
    val schemaRegistryConfig: SchemaRegistryConfig = SchemaRegistryConfig
      .builder()
      .regularExpressionFactory(JoniRegularExpressionFactory.getInstance())
      .build()

    val schemaLocation     = SchemaLocation.of(schemaUri.toString)
    val schemaRegistry     = SchemaRegistry.builder().schemaRegistryConfig(schemaRegistryConfig).build()
    val schema             = schemaRegistry.getSchema(schemaLocation)
    val objMapper          = new ObjectMapper()
    val jsonnode: JsonNode = objMapper.readTree(json)
    val errors             = schema.validate(jsonnode).asScala

    errors.foreach(f => {
      report.info(s"Failed CIP validation: ${f.toString}")
    })
    errors.isEmpty
  }
