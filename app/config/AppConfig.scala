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

package config

import com.typesafe.config.Config

import javax.inject.{Inject, Singleton}
import play.api.{ConfigLoader, Configuration}
import routing.Version
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait AppConfig {

  def desBaseUrl: String
  def desEnv: String
  def desToken: String
  def desEnvironmentHeaders: Option[Seq[String]]

  def mtdIdBaseUrl: String

  def ifs1BaseUrl: String
  def ifs1Env: String
  def ifs1Token: String
  def ifs1EnvironmentHeaders: Option[Seq[String]]

  def ifs2BaseUrl: String
  def ifs2Env: String
  def ifs2Token: String
  def ifs2EnvironmentHeaders: Option[Seq[String]]

  def apiGatewayContext: String

  def minimumPermittedTaxYear: Int

  def apiStatus(version: Version): String
  def featureSwitch: Option[Configuration]
  def endpointsEnabled(version: Version): Boolean

  def confidenceLevelConfig: ConfidenceLevelConfig
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
  val ifs2BaseUrl: String = config.baseUrl("ifs2")
  val ifs2Env: String = config.getString("microservice.services.ifs2.env")
  val ifs2Token: String = config.getString("microservice.services.ifs2.token")
  val ifs2EnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.ifs2.environmentHeaders")

  val minimumPermittedTaxYear: Int = config.getInt("minimumPermittedTaxYear")

  // API Config
  val apiGatewayContext: String                   = config.getString("api.gateway.context")
  def apiStatus(version: Version): String         = config.getString(s"api.${version.name}.status")
  def featureSwitch: Option[Configuration]        = configuration.getOptional[Configuration](s"feature-switch")
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
