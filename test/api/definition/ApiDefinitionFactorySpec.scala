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

package api.definition

import api.config.Deprecation.NotDeprecated
import api.config.MockAppConfig
import api.definition.APIAccessType.PUBLIC
import api.definition.APIStatus.{ALPHA, BETA}
import api.mocks.MockHttpClient
import api.routing.*
import api.utils.UnitSpec
import cats.implicits.catsSyntaxValidatedId

import scala.language.reflectiveCalls

class ApiDefinitionFactorySpec extends UnitSpec {

  "definition" when {
    "called" should {
      "return a valid Definition case class" in new Test {
        MockedAppConfig.apiStatus(Version4) returns "BETA"
        MockedAppConfig.endpointsEnabled(Version4) returns true
        MockedAppConfig.deprecationFor(Version4).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedAppConfig.controlledAccessEnabled returns false

        apiDefinitionFactory.definition shouldBe
          Definition(
            api = APIDefinition(
              name = "Self Assessment Accounts (MTD)",
              description = "An API for retrieving accounts data for Self Assessment",
              context = "individuals/self-assessment/adjustable-summary",
              categories = Seq("INCOME_TAX_MTD"),
              versions = Seq(
                APIVersion(
                  version = Version4,
                  status = BETA,
                  access = PUBLIC,
                  endpointsEnabled = true
                )
              ),
              requiresTrust = None
            )
          )
      }
    }
  }

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {

      s"return the expected status" in new Test {
        MockedAppConfig.deprecationFor(Version9).returns(NotDeprecated.valid)
        MockedAppConfig.apiStatus(Version9).returns("BETA")

        val result: APIStatus = apiDefinitionFactory.buildAPIStatus(Version9)
        result shouldBe BETA
      }

    }

    "the 'apiStatus' parameter is present but invalid" should {
      s"default to alpha" in new Test {
        MockedAppConfig.deprecationFor(Version9).returns(NotDeprecated.valid)
        MockedAppConfig.apiStatus(Version9).returns("not-a-status")

        apiDefinitionFactory.buildAPIStatus(Version9) shouldBe ALPHA
      }
    }

    "the 'deprecatedOn' parameter is missing for a deprecated version" should {
      "throw an exception" in new Test {
        MockedAppConfig.apiStatus(Version9).returns("DEPRECATED")
        MockedAppConfig.deprecationFor(Version9).returns("deprecatedOn date is required for a deprecated version".invalid)

        val exception: Exception = intercept[Exception] {
          apiDefinitionFactory.buildAPIStatus(Version9)
        }

        val exceptionMessage: String = exception.getMessage
        exceptionMessage shouldBe "deprecatedOn date is required for a deprecated version"
      }
    }
  }

  "set the access level" when {
    "the controlled access flag is enabled" should {
      "to be CONTROLLED" in new Test {
        MockedAppConfig.endpointsEnabled(Version4)
        MockedAppConfig.apiStatus(Version4) returns "BETA"
        MockedAppConfig.deprecationFor(Version4).returns(NotDeprecated.valid).anyNumberOfTimes()

        MockedAppConfig.controlledAccessEnabled returns true

        apiDefinitionFactory.definition.api.versions.head.access shouldBe APIAccessType.CONTROLLED
      }
    }

    "the controlled access flag is disabled" should {
      "return PUBLIC" in new Test {
        MockedAppConfig.endpointsEnabled(Version4)
        MockedAppConfig.apiStatus(Version4) returns "BETA"
        MockedAppConfig.deprecationFor(Version4).returns(NotDeprecated.valid).anyNumberOfTimes()

        MockedAppConfig.controlledAccessEnabled returns false

        apiDefinitionFactory.definition.api.versions.head.access shouldBe APIAccessType.PUBLIC
      }
    }
  }

  class Test extends MockHttpClient with MockAppConfig {
    MockedAppConfig.apiGatewayContext returns "individuals/self-assessment/adjustable-summary"
    val apiDefinitionFactory = new ApiDefinitionFactory(mockAppConfig)
  }

}
