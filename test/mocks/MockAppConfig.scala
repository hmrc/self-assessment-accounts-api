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

package mocks

import config.{AppConfig, ConfidenceLevelConfig}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.Configuration
import routing.Version

trait MockAppConfig extends MockFactory {

  val mockAppConfig: AppConfig = mock[AppConfig]

  object MockAppConfig {
    // DES Config
    def desBaseUrl: CallHandler[String]                         = (mockAppConfig.desBaseUrl _: () => String).expects()
    def desToken: CallHandler[String]                           = (mockAppConfig.desToken _).expects()
    def desEnvironment: CallHandler[String]                     = (mockAppConfig.desEnv _).expects()
    def desEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (mockAppConfig.desEnvironmentHeaders _).expects()

    // IFS1 Config
    def ifs1BaseUrl: CallHandler[String]                         = (mockAppConfig.ifs1BaseUrl _: () => String).expects()
    def ifs1Token: CallHandler[String]                           = (mockAppConfig.ifs1Token _).expects()
    def ifs1Environment: CallHandler[String]                     = (mockAppConfig.ifs1Env _).expects()
    def ifs1EnvironmentHeaders: CallHandler[Option[Seq[String]]] = (mockAppConfig.ifs1EnvironmentHeaders _).expects()

    // IFS2 Config
    def ifs2BaseUrl: CallHandler[String]                         = (mockAppConfig.ifs2BaseUrl _: () => String).expects()
    def ifs2Token: CallHandler[String]                           = (mockAppConfig.ifs2Token _).expects()
    def ifs2Environment: CallHandler[String]                     = (mockAppConfig.ifs2Env _).expects()
    def ifs2EnvironmentHeaders: CallHandler[Option[Seq[String]]] = (mockAppConfig.ifs2EnvironmentHeaders _).expects()

    // TYS IFS Config
    def tysIfsBaseUrl: CallHandler[String]                         = (mockAppConfig.tysIfsBaseUrl _: () => String).expects()
    def tysIfsToken: CallHandler[String]                           = (mockAppConfig.tysIfsToken _).expects()
    def tysIfsEnv: CallHandler[String]                             = (mockAppConfig.tysIfsEnv _).expects()
    def tysIfsEnvironment: CallHandler[String]                     = (mockAppConfig.tysIfsEnv _).expects()
    def tysIfsEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (mockAppConfig.tysIfsEnvironmentHeaders _).expects()

    // MTD IF Lookup Config
    def mtdIdBaseUrl: CallHandler[String] = (mockAppConfig.mtdIdBaseUrl _: () => String).expects()

    // Business Rule Config
    def minimumPermittedTaxYear: CallHandler[Int] = (mockAppConfig.minimumPermittedTaxYear _).expects()

    // API Config
    def featureSwitches: CallHandler[Configuration]              = (mockAppConfig.featureSwitches _: () => Configuration).expects()
    def apiGatewayContext: CallHandler[String]                   = (mockAppConfig.apiGatewayContext _: () => String).expects()
    def apiStatus(status: Version): CallHandler[String]          = (mockAppConfig.apiStatus: Version => String).expects(status)
    def endpointsEnabled(version: Version): CallHandler[Boolean] = (mockAppConfig.endpointsEnabled: Version => Boolean).expects(version)

    def confidenceLevelCheckEnabled: CallHandler[ConfidenceLevelConfig] =
      (mockAppConfig.confidenceLevelConfig _: () => ConfidenceLevelConfig).expects()

  }

}
