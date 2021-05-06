/*
 * Copyright 2021 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.OK
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.fixtures.ListChargesFixture.fullDesListChargesMultipleResponse
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class CodingOutFeatureSwitchISpec extends IntegrationBaseSpec {
  override def servicesConfig: Map[String, String] = super.servicesConfig + ("feature-switch.coding-out.enabled" -> "false")

  private trait Test {
    val nino = "AA123456A"
    val from = "2018-10-01"
    val to = "2019-10-01"

    def uri: String = s"/$nino/charges"

    def desUrl: String = s"/enterprise/02.00.00/financial-data/NINO/$nino/ITSA"

    def setupStubs(): StubMapping

    def request: WSRequest = {

      val queryParams = Seq("from" -> from, "to" -> to)
      setupStubs()
      buildRequest(uri)
        .addQueryStringParameters(queryParams: _*)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "custom routing" when {
    "the coding-out feature switch is off" should {
      "use the v1 routes package" in new Test {
        val desQueryParams: Map[String, String] = Map(
          "dateFrom" -> from,
          "dateTo" -> to,
          "onlyOpenItems" -> "false",
          "includeLocks" -> "true",
          "calculateAccruedInterest" -> "true",
          "removePOA" -> "true",
          "customerPaymentInformation" -> "true",
          "includeStatistical" -> "false"
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, desQueryParams, OK, fullDesListChargesMultipleResponse)
        }

        val response: WSResponse = await(request.get)

        response.status shouldBe OK
      }
    }
  }
}
