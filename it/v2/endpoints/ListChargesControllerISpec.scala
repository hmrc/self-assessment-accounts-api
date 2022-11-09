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

package v2.endpoints

import api.stubs.{AuditStub, AuthStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.fixtures.ListChargesFixture._
import v1.stubs.DownstreamStub

class ListChargesControllerISpec extends IntegrationBaseSpec {

  private trait Test {
    val nino                 = "AA123456A"
    val from: Option[String] = Some("2018-10-01")
    val to: Option[String]   = Some("2019-10-01")

    def uri: String = s"/$nino/charges"

    def desUrl: String = s"/enterprise/02.00.00/financial-data/NINO/$nino/ITSA"

    def setupStubs(): StubMapping

    def request: WSRequest = {

      val queryParams = Seq("from" -> from, "to" -> to)
        .collect { case (k, Some(v)) =>
          (k, v)
        }
      setupStubs()
      buildRequest(uri)
        .addQueryStringParameters(queryParams: _*)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  "Calling the list charges endpoint" should {
    "return a 404" when {
      "a request is made using V2" in new Test {

        val desQueryParams: Map[String, String] = Map(
          "dateFrom"                   -> from.get,
          "dateTo"                     -> to.get,
          "onlyOpenItems"              -> "false",
          "includeLocks"               -> "true",
          "calculateAccruedInterest"   -> "true",
          "removePOA"                  -> "true",
          "customerPaymentInformation" -> "true",
          "includeStatistical"         -> "false"
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, desUrl, desQueryParams, OK, fullDesListChargesMultipleResponse)
        }

        val response: WSResponse = await(request.get)

        response.status shouldBe NOT_FOUND
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe Json.parse("""
            |  {"code":"MATCHING_RESOURCE_NOT_FOUND","message":"Matching resource not found"}
            |""".stripMargin)
      }
    }

  }

}
