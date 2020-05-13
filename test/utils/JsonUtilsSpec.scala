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

package utils

import play.api.libs.json.{JsValue, Json, OWrites, Reads}
import support.UnitSpec

class JsonUtilsSpec extends UnitSpec {

  case class TestClass(field1: String, field2: String)

  object TestClass extends JsonUtils {
    implicit val reads: Reads[TestClass] = Json.reads[TestClass]
    implicit val writes: OWrites[TestClass] = Json.writes[TestClass].removeField("field2")
    implicit val wrongWrites: OWrites[TestClass] = Json.writes[TestClass].removeField("field3")
  }

  private val testData: TestClass = TestClass("value1", "value2")

  private val removedFieldJson: JsValue = Json.parse(
    """
      |{
      |   "field1": "value1"
      |}
    """.stripMargin
  )

  private val fullJson: JsValue = Json.parse(
    """
      |{
      |   "field1": "value1",
      |   "field2": "value2"
      |}
    """.stripMargin
  )

  "JsonUtils" when {
    "removeField" should {
      "remove that field if it is present" in {
        Json.toJson(testData)(TestClass.writes) shouldBe removedFieldJson
      }

      "do nothing if the specified field does not exist" in {
        Json.toJson(testData)(TestClass.wrongWrites) shouldBe fullJson
      }
    }
  }
}
