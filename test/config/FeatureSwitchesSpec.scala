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

package config

import play.api.Configuration
import play.api.mvc.Headers
import play.api.test.FakeRequest
import support.UnitSpec

class FeatureSwitchesSpec extends UnitSpec {

  "isTemporalValidationEnabled" when {

    def configuration(enable: Boolean): Configuration =
      Configuration("allowTemporalValidationSuspension.enabled" -> enable)

    def requestWith(headers: Headers) =
      FakeRequest("GET", "someUrl", headers, None)

    def headers(suspend: String) = Headers("suspend-temporal-validations" -> suspend)

    "the suspension enabling feature switch is false" should {
      val featureSwitches = FeatureSwitches(configuration(false))

      "return true even if the suspend header is present and true" in {
        featureSwitches.isTemporalValidationEnabled(requestWith(headers(suspend = "true"))) shouldBe true
      }

      "return true if the suspend header is not present" in {
        featureSwitches.isTemporalValidationEnabled(requestWith(Headers())) shouldBe true
      }
    }

    "the suspension enabling feature switch is true" should {
      val featureSwitches = FeatureSwitches(configuration(true))

      "return false if the suspend header is present and true" in {
        featureSwitches.isTemporalValidationEnabled(requestWith(headers(suspend = "true"))) shouldBe false
      }

      "return true if the suspend header is present and false" in {
        featureSwitches.isTemporalValidationEnabled(requestWith(headers(suspend = "false"))) shouldBe true
      }

      "return true if the suspend header is not present" in {
        featureSwitches.isTemporalValidationEnabled(requestWith(Headers())) shouldBe true
      }

      "return true if the suspend header is not a valid boolean" in {
        featureSwitches.isTemporalValidationEnabled(requestWith(headers(suspend = "not a boolean"))) shouldBe true
      }
    }

  }

  "FeatureSwitches" should {
    "return true" when {
      "the feature switch is set to true" in {
        val config = Configuration(
          "random-feature-switch.enabled" -> true
        )

        val featureSwitches = FeatureSwitches(config)

        featureSwitches.isEnabled("random-feature-switch") shouldBe true
      }

      "the feature switch is not present in the config" in {
        val config = Configuration.empty

        val featureSwitches = FeatureSwitches(config)

        featureSwitches.isEnabled("random-feature-switch") shouldBe true
      }
    }

    "return false" when {
      "the feature switch is set to false" in {
        val config = Configuration(
          "random-feature-switch.enabled" -> false
        )

        val featureSwitches = FeatureSwitches(config)

        featureSwitches.isEnabled("random-feature-switch") shouldBe false
      }
    }
  }

}
