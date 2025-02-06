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

import shared.config.SharedAppConfig
import shared.definition._
import shared.routing.{Version3, Version4}

import javax.inject.{Inject, Singleton}

@Singleton
class SaAccountsDefinitionFactory @Inject()(protected val appConfig: SharedAppConfig) extends ApiDefinitionFactory {

  lazy val definition: Definition =
    Definition(
      api = APIDefinition(
        name = "Self Assessment Accounts (MTD)",
        description = "An API for retrieving accounts data for Self Assessment",
        context = appConfig.apiGatewayContext,
        categories = List("INCOME_TAX_MTD"),
        versions = List(
          APIVersion(
            version = Version3,
            status = buildAPIStatus(Version3),
            endpointsEnabled = appConfig.endpointsEnabled(Version3)
          ),
          APIVersion(
            version = Version4,
            status = buildAPIStatus(Version4),
            endpointsEnabled = appConfig.endpointsEnabled(Version4)
          )
        ),
        requiresTrust = None
      )
    )
}
