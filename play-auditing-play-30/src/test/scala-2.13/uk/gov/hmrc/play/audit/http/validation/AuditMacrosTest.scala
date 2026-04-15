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
import org.scalatest.matchers.should._
import org.scalatest.{Inside, Inspectors}

import scala.language.postfixOps

class AuditMacrosTest extends AnyFunSuite with Inside with Inspectors with Matchers {

    test("AuditMacros should create audit format"){



    }

    test("AuditMacros should fail if annotation is missing"){
        fail("Missing annotation")
    }


}
