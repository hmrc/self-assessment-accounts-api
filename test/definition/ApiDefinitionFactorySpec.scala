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

package definition

import api.config.MockAppConfig
import api.connectors.MockHttpClient
import cats.implicits.catsSyntaxValidatedId
import config.ConfidenceLevelConfig
import config.Deprecation.NotDeprecated
import definition.APIStatus.{ALPHA, BETA}
import play.api.Configuration
import routing.{Version2, Version3}
import support.UnitSpec
import uk.gov.hmrc.auth.core.ConfidenceLevel

class ApiDefinitionFactorySpec extends UnitSpec {

  private val confidenceLevel: ConfidenceLevel = ConfidenceLevel.L200

  class Test extends MockHttpClient with MockAppConfig {
    val apiDefinitionFactory = new ApiDefinitionFactory(mockAppConfig)
    MockAppConfig.apiGatewayContext returns "api.gateway.context"
  }

  "definition" when {
    "called" should {
      "return a valid Definition case class" in new Test {
        MockAppConfig.featureSwitches returns Configuration.empty
        Seq(Version2, Version3).foreach { version =>
          MockAppConfig.apiStatus(version) returns "BETA"
          MockAppConfig.endpointsEnabled(version).returns(true).anyNumberOfTimes()
          MockAppConfig.deprecationFor(version).returns(NotDeprecated.valid).anyNumberOfTimes()
        }

        MockAppConfig.confidenceLevelCheckEnabled
          .returns(ConfidenceLevelConfig(confidenceLevel = confidenceLevel, definitionEnabled = true, authValidationEnabled = true))
          .anyNumberOfTimes()

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
                  version = Version2,
                  status = BETA,
                  endpointsEnabled = true
                ),
                APIVersion(
                  version = Version3,
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
      (true, ConfidenceLevel.L250, ConfidenceLevel.L250),
      (true, ConfidenceLevel.L200, ConfidenceLevel.L200),
      (false, ConfidenceLevel.L200, ConfidenceLevel.L50)
    ).foreach { case (definitionEnabled, configCL, expectedDefinitionCL) =>
      s"confidence-level-check.definition.enabled is $definitionEnabled and confidence-level = $configCL" should {
        s"return confidence level $expectedDefinitionCL" in new Test {
          MockAppConfig.confidenceLevelCheckEnabled returns ConfidenceLevelConfig(
            confidenceLevel = configCL,
            definitionEnabled = definitionEnabled,
            authValidationEnabled = true)
          apiDefinitionFactory.confidenceLevel shouldBe expectedDefinitionCL
        }
      }
    }
  }

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {
      Seq(
        (Version2, BETA),
        (Version3, BETA)
      ).foreach { case (version, status) =>
        s"return the correct $status for $version " in new Test {
          MockAppConfig.apiStatus(version) returns status.toString
          MockAppConfig.deprecationFor(version).returns(NotDeprecated.valid).anyNumberOfTimes()
          apiDefinitionFactory.buildAPIStatus(version) shouldBe status
        }
      }
    }

    "the 'apiStatus' parameter is present and invalid" should {
      Seq(Version2, Version3).foreach { version =>
        s"default to alpha for $version " in new Test {
          MockAppConfig.apiStatus(version) returns "ALPHO"
          MockAppConfig.deprecationFor(version).returns(NotDeprecated.valid).anyNumberOfTimes()
          apiDefinitionFactory.buildAPIStatus(version) shouldBe ALPHA
        }
      }
    }

    "the 'deprecatedOn' parameter is missing for a deprecated version" should {
      Seq(Version2, Version3).foreach { version =>
        s"throw exception for $version" in new Test {
          MockAppConfig.apiStatus(version) returns "DEPRECATED"
          MockAppConfig
            .deprecationFor(version)
            .returns(s"deprecatedOn date is required for a deprecated version $version".invalid)
            .anyNumberOfTimes()

          val exception: Exception = intercept[Exception] {
            apiDefinitionFactory.buildAPIStatus(version)
          }

          val exceptionMessage: String = exception.getMessage
          exceptionMessage shouldBe s"deprecatedOn date is required for a deprecated version $version"
        }
      }
    }

  }

}
