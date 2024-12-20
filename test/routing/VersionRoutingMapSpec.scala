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

package routing

import config.MockAppConfig
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.routing.Router
import support.UnitSpec

class VersionRoutingMapSpec extends UnitSpec with MockAppConfig with GuiceOneAppPerSuite {

  val defaultRouter: Router = mock[Router]
  val v3Routes: v3.Routes   = app.injector.instanceOf[v3.Routes]

  "map" when {

    "routing a v3 request" should {
      "route to v3.routes" in {
        val versionRoutingMap: VersionRoutingMapImpl =
          VersionRoutingMapImpl(defaultRouter, v3Routes, mockAppConfig)

        versionRoutingMap.map(Version3) shouldBe v3Routes
      }

    }
  }

}
