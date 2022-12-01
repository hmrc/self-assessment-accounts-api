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

import com.typesafe.config.ConfigFactory
import mocks.MockAppConfig
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.routing.Router
import support.UnitSpec

class VersionRoutingMapSpec extends UnitSpec with MockAppConfig with GuiceOneAppPerSuite {

  val defaultRouter: Router                         = mock[Router]
  val v1Routes: v1.Routes                           = app.injector.instanceOf[v1.Routes]
  val v2Routes: v2.Routes                           = app.injector.instanceOf[v2.Routes]
  val v1WithCodingOutRoutes: v1WithCodingOut.Routes = app.injector.instanceOf[v1WithCodingOut.Routes]

  "map" when {
    "routing to v1" when {
      def test(isCodingOutEnabled: Boolean, routes: Any): Unit = {
        s"coding out feature switch is $isCodingOutEnabled" should {
          s"route to ${routes.toString}" in {
            MockAppConfig.featureSwitches.returns(Configuration(ConfigFactory.parseString(s"""
                 |coding-out.enabled = $isCodingOutEnabled
                 |""".stripMargin)))
            val versionRoutingMap: VersionRoutingMapImpl =
              VersionRoutingMapImpl(defaultRouter, v1Routes, v2Routes, v1WithCodingOutRoutes, mockAppConfig)

            versionRoutingMap.map(Version1) shouldBe routes
          }
        }
      }

      Seq((true, v1WithCodingOutRoutes), (false, v1Routes)).foreach(args => (test _).tupled(args))
    }
    "routing a v2 request" should {
      "route to v2.routes" in {
        MockAppConfig.featureSwitches.returns(Configuration(ConfigFactory.parseString(s"""
             |version-2.enabled = true
             |""".stripMargin)))
        val versionRoutingMap: VersionRoutingMapImpl = VersionRoutingMapImpl(defaultRouter, v1Routes, v2Routes, v1WithCodingOutRoutes, mockAppConfig)
        versionRoutingMap.map(Version2) shouldBe v2Routes
      }
    }
  }

}
