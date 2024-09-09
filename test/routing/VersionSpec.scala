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

package routing

import play.api.http.HeaderNames.ACCEPT
import play.api.libs.json._
import play.api.test.FakeRequest
import routing.Version.{VersionReads, VersionWrites}
import support.UnitSpec

class VersionSpec extends UnitSpec {

  "serialized to Json" must {
    "return the expected Json output for Version2" in {
      val version: Version = Version2
      val expected         = Json.parse(""" "2.0" """)
      val result           = Json.toJson(version)
      result shouldBe expected
    }

    "return the expected Json output for Version3" in {
      val version: Version = Version3
      val expected         = Json.parse(""" "3.0" """)
      val result           = Json.toJson(version)
      result shouldBe expected
    }
  }

  "Versions" when {
    "retrieved from a request header" must {
      "return an error if the version is unsupported" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.4.0+json"))) shouldBe Left(VersionNotFound)
      }

      "return an error if the Accept header value is invalid" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/XYZ.3.0+json"))) shouldBe Left(InvalidHeader)
      }

      "return the specified version Version2" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.2.0+json"))) shouldBe Right(Version2)
      }

      "return the specified version Version3" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.3.0+json"))) shouldBe Right(Version3)
      }
    }
  }

  "VersionReads" should {
    "successfully read Version2" in {
      val versionJson: JsValue      = JsString(Version2.name)
      val result: JsResult[Version] = VersionReads.reads(versionJson)

      result shouldEqual JsSuccess(Version2)
    }

    "successfully read Version3" in {
      val versionJson: JsValue      = JsString(Version3.name)
      val result: JsResult[Version] = VersionReads.reads(versionJson)

      result shouldEqual JsSuccess(Version3)
    }

    "return error for unrecognised version" in {
      val versionJson: JsValue      = JsString("UnknownVersion")
      val result: JsResult[Version] = VersionReads.reads(versionJson)

      result shouldBe a[JsError]
    }
  }

  "toString" should {
    "return the version name for Version2" in {
      val result = Version2.toString
      result shouldBe Version2.name
    }

    "return the version name for Version3" in {
      val result = Version3.toString
      result shouldBe Version3.name
    }
  }

}
