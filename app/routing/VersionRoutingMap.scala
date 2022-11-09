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

package routing

import com.google.inject.ImplementedBy
import config.{AppConfig, FeatureSwitch}

import javax.inject.Inject
import play.api.routing.Router
import utils.Logging

@ImplementedBy(classOf[VersionRoutingMapImpl])
trait VersionRoutingMap {
  val defaultRouter: Router

  val map: Map[Version, Router]

  final def versionRouter(version: Version): Option[Router] = map.get(version)
}

case class VersionRoutingMapImpl @Inject() (defaultRouter: Router,
                                            v1Routes: v1.Routes,
                                            v2Routes: v2.Routes,
                                            v1WithCodingOutRoutes: v1WithCodingOut.Routes,
                                            appConfig: AppConfig)
    extends VersionRoutingMap
    with Logging {

  private lazy val featureSwitch: FeatureSwitch = FeatureSwitch(appConfig.featureSwitch)
  private lazy val isCodingOutEnabled           = featureSwitch.isCodingOutEnabled

  if (isCodingOutEnabled) logger.info("Coding Out feature switch is enabled") else logger.info("Coding Out feature switch is disabled")

  val map: Map[Version, Router] = Map(
    Version1 -> {
      if (isCodingOutEnabled) v1WithCodingOutRoutes else v1Routes
    },
    Version2 -> v2Routes
  )

}
