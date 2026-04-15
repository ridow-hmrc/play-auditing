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
import uk.gov.hmrc.play.audit.http.validation.AuditFormat
import uk.gov.hmrc.audit.http.validation.CipAuditEventUnvalidated

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object AuditEventSchema {


  private val unvalidatedSchemaAnnotation               = classOf[CipAuditEventUnvalidated].getCanonicalName()

  def unvalidatedFormat[T]: AuditFormat[T] = macro nonvalidatedFormatImpl[T]

  def nonvalidatedFormatImpl[T: c.WeakTypeTag](c: Context): c.Expr[AuditFormat[T]] = {
    import c.universe._

    val tpe = weakTypeOf[T]
    val symbol = tpe.typeSymbol

    val hasAnnotation = symbol.annotations.exists { ann =>
      ann.tree.tpe.toString.contains("CipAuditEventUnvalidated")
    }

    if (!hasAnnotation) {
      c.abort(c.enclosingPosition, s"${symbol.name} must be annotated with @@CipAuditEventUnvalidated")
    }

    val formatTree = q"play.api.libs.json.Json.format[$tpe]"

    c.Expr[AuditFormat[T]](q"new AuditFormat[$tpe]($formatTree)")
  }
}