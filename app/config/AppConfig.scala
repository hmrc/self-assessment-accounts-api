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

import com.typesafe.config.Config

import javax.inject.{Inject, Singleton}
import play.api.{ConfigLoader, Configuration}
import routing.Version
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait AppConfig {

  // DES Config
  def desBaseUrl: String
  def desEnv: String
  def desToken: String
  def desEnvironmentHeaders: Option[Seq[String]]

  lazy val desDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = desBaseUrl, env = desEnv, token = desToken, environmentHeaders = desEnvironmentHeaders)

  // IFS1 Config
  def ifs1BaseUrl: String
  def ifs1Env: String
  def ifs1Token: String
  def ifs1EnvironmentHeaders: Option[Seq[String]]

  lazy val ifs1DownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = ifs1BaseUrl, env = ifs1Env, token = ifs1Token, environmentHeaders = ifs1EnvironmentHeaders)

  // IFS2 Config
  def ifs2BaseUrl: String
  def ifs2Env: String
  def ifs2Token: String
  def ifs2EnvironmentHeaders: Option[Seq[String]]

  lazy val ifs2DownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = ifs2BaseUrl, env = ifs2Env, token = ifs2Token, environmentHeaders = ifs2EnvironmentHeaders)

  // TYS IFS Config
  def tysIfsBaseUrl: String
  def tysIfsEnv: String
  def tysIfsToken: String
  def tysIfsEnvironmentHeaders: Option[Seq[String]]

  lazy val taxYearSpecificIfsDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = tysIfsBaseUrl, env = tysIfsEnv, token = tysIfsToken, environmentHeaders = tysIfsEnvironmentHeaders)

  // API Config
  def apiGatewayContext: String
  def apiStatus(version: Version): String
  def featureSwitches: Configuration
  def endpointsEnabled(version: Version): Boolean
  def confidenceLevelConfig: ConfidenceLevelConfig

  def mtdIdBaseUrl: String
  def minimumPermittedTaxYear: Int
}

@Singleton
class AppConfigImpl @Inject() (config: ServicesConfig, configuration: Configuration) extends AppConfig {

  val mtdIdBaseUrl: String = config.baseUrl("mtd-id-lookup")

  // DES config
  val desBaseUrl: String                         = config.baseUrl("des")
  val desEnv: String                             = config.getString("microservice.services.des.env")
  val desToken: String                           = config.getString("microservice.services.des.token")
  val desEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.des.environmentHeaders")

  // IFS1 config
  val ifs1BaseUrl: String                         = config.baseUrl("ifs1")
  val ifs1Env: String                             = config.getString("microservice.services.ifs1.env")
  val ifs1Token: String                           = config.getString("microservice.services.ifs1.token")
  val ifs1EnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.ifs1.environmentHeaders")

  // IFS2 config
  val ifs2BaseUrl: String                         = config.baseUrl("ifs2")
  val ifs2Env: String                             = config.getString("microservice.services.ifs2.env")
  val ifs2Token: String                           = config.getString("microservice.services.ifs2.token")
  val ifs2EnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.ifs2.environmentHeaders")

  // Tax Year Specific (TYS) IFS Config
  val tysIfsBaseUrl: String                         = config.baseUrl("tys-ifs")
  val tysIfsEnv: String                             = config.getString("microservice.services.tys-ifs.env")
  val tysIfsToken: String                           = config.getString("microservice.services.tys-ifs.token")
  val tysIfsEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.tys-ifs.environmentHeaders")

  val minimumPermittedTaxYear: Int = config.getInt("minimumPermittedTaxYear")

  // API Config
  val apiGatewayContext: String                   = config.getString("api.gateway.context")
  def apiStatus(version: Version): String         = config.getString(s"api.${version.name}.status")
  def featureSwitches: Configuration              = configuration.getOptional[Configuration](s"feature-switch").getOrElse(Configuration.empty)
  def endpointsEnabled(version: Version): Boolean = config.getBoolean(s"api.$version.endpoints.enabled")

  val confidenceLevelConfig: ConfidenceLevelConfig = configuration.get[ConfidenceLevelConfig](s"api.confidence-level-check")
}

case class ConfidenceLevelConfig(definitionEnabled: Boolean, authValidationEnabled: Boolean)

object ConfidenceLevelConfig {

  implicit val configLoader: ConfigLoader[ConfidenceLevelConfig] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)
    ConfidenceLevelConfig(
      definitionEnabled = config.getBoolean("definition.enabled"),
      authValidationEnabled = config.getBoolean("auth-validation.enabled")
    )
  }

}
