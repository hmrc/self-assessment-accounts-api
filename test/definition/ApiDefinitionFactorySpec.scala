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

package definition

import api.mocks.MockHttpClient
import config.ConfidenceLevelConfig
import definition.APIStatus.{ALPHA, BETA}
import definition.Versions.{VERSION_1, VERSION_2}
import mocks.MockAppConfig
import support.UnitSpec
import uk.gov.hmrc.auth.core.ConfidenceLevel

class ApiDefinitionFactorySpec extends UnitSpec {

  class Test extends MockHttpClient with MockAppConfig {
    val apiDefinitionFactory = new ApiDefinitionFactory(mockAppConfig)
    MockAppConfig.apiGatewayContext returns "api.gateway.context"
  }

  private val confidenceLevel: ConfidenceLevel = ConfidenceLevel.L200

  "definition" when {
    "called" should {
      "return a valid Definition case class" in new Test {
        MockAppConfig.featureSwitch returns None
        MockAppConfig.apiStatus(VERSION_1) returns "ALPHA"
        MockAppConfig.apiStatus(VERSION_2) returns "BETA"
        MockAppConfig.endpointsEnabled(VERSION_1) returns false anyNumberOfTimes ()
        MockAppConfig.endpointsEnabled(VERSION_2) returns true anyNumberOfTimes ()

        MockAppConfig.confidenceLevelCheckEnabled returns ConfidenceLevelConfig(
          definitionEnabled = true,
          authValidationEnabled = true) anyNumberOfTimes ()

        private val readScope  = "read:self-assessment"
        private val writeScope = "write:self-assessment"

        apiDefinitionFactory.definition shouldBe
          Definition(
            scopes = Seq(
              Scope(
                key = readScope,
                name = "View your Self Assessment information",
                description = "Allow read access to self assessment data",
                confidenceLevel
              ),
              Scope(
                key = writeScope,
                name = "Change your Self Assessment information",
                description = "Allow write access to self assessment data",
                confidenceLevel
              )
            ),
            api = APIDefinition(
              name = "Self Assessment Accounts (MTD)",
              description = "An API for retrieving accounts data for Self Assessment",
              context = "api.gateway.context",
              categories = Seq("INCOME_TAX_MTD"),
              versions = Seq(
                APIVersion(
                  version = VERSION_1,
                  status = ALPHA,
                  endpointsEnabled = false
                ),
                APIVersion(
                  version = VERSION_2,
                  status = BETA,
                  endpointsEnabled = true
                )
              ),
              requiresTrust = None
            )
          )
      }
    }
  }

  "confidenceLevel" when {
    Seq(
      (true, ConfidenceLevel.L200),
      (false, ConfidenceLevel.L50)
    ).foreach { case (definitionEnabled, cl) =>
      s"confidence-level-check.definition.enabled is $definitionEnabled in config" should {
        s"return $cl" in new Test {
          MockAppConfig.confidenceLevelCheckEnabled returns ConfidenceLevelConfig(definitionEnabled = definitionEnabled, authValidationEnabled = true)
          apiDefinitionFactory.confidenceLevel shouldBe cl
        }
      }
    }
  }

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {
      Seq(
        (VERSION_1, ALPHA),
        (VERSION_2, BETA)
      ).foreach { case (version, status) =>
        s"return the correct $status for $version " in new Test {
          MockAppConfig.apiStatus(version) returns status.toString
          apiDefinitionFactory.buildAPIStatus(version) shouldBe status
        }
      }
    }

    "the 'apiStatus' parameter is present and invalid" should {
      Seq(VERSION_1, VERSION_2).foreach { version =>
        s"default to alpha for $version " in new Test {
          MockAppConfig.apiStatus(version) returns "ALPHO"
          apiDefinitionFactory.buildAPIStatus(version) shouldBe ALPHA
        }
      }
    }

  }

}
