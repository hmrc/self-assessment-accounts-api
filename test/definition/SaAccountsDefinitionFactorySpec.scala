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

import cats.implicits.catsSyntaxValidatedId
import play.api.Configuration
import shared.config.Deprecation.NotDeprecated
import shared.config.MockSharedAppConfig
import shared.definition.APIStatus.BETA
import shared.definition._
import shared.mocks.MockHttpClient
import shared.routing.{Version2, Version3}
import shared.utils.UnitSpec

class SaAccountsDefinitionFactorySpec extends UnitSpec {
  
  class Test extends MockHttpClient with MockSharedAppConfig {
    MockedSharedAppConfig.apiGatewayContext returns "api.gateway.context"
    val apiDefinitionFactory = new SaAccountsDefinitionFactory(mockSharedAppConfig)
  }

  "definition" when {
    "called" should {
      "return a valid Definition case class" in new Test {

        MockedSharedAppConfig.featureSwitchConfig returns Configuration.empty
        Seq(Version2, Version3).foreach { version =>
          MockedSharedAppConfig.apiStatus(version) returns "BETA"
          MockedSharedAppConfig.endpointsEnabled(version).returns(true).anyNumberOfTimes()
          MockedSharedAppConfig.deprecationFor(version).returns(NotDeprecated.valid).anyNumberOfTimes()
        }

        apiDefinitionFactory.definition shouldBe
          Definition(
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

}
