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

package shared.controllers

import io.swagger.v3.parser.OpenAPIV3Parser
import play.api.http.Status
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import support.IntegrationBaseSpec

import scala.util.Try

class DocumentationControllerISpec extends IntegrationBaseSpec {

  private val apiDefinitionJson = Json.parse(s"""
      |{
      |  "api":{
      |    "name":"Self Assessment Accounts (MTD)",
      |    "description":"An API for retrieving accounts data for Self Assessment",
      |    "context":"accounts/self-assessment",
      |    "categories":["INCOME_TAX_MTD"],
      |    "versions":[
      |      {
      |        "version":"2.0",
      |        "status":"RETIRED",
      |        "endpointsEnabled":false
      |      },
      |      {
      |        "version":"3.0",
      |        "status":"BETA",
      |        "endpointsEnabled":true
      |      }
      |    ]
      |  }
      |}
    """.stripMargin)

  "GET /api/definition" should {
    "return a 200 with the correct response body" in {
      val response: WSResponse = await(buildRequest("/api/definition").get())
      response.status shouldBe Status.OK
      Json.parse(response.body) shouldBe apiDefinitionJson
    }
  }

  "an OAS documentation request" must {
    s"return the documentation for 3.0" in {
      val response = get(s"/api/conf/3.0/application.yaml")

      val body         = response.body
      val parserResult = Try(new OpenAPIV3Parser().readContents(body)).getOrElse(fail("openAPI couldn't read contents"))

      val openAPI = Option(parserResult.getOpenAPI).getOrElse(fail("openAPI wasn't defined"))
      openAPI.getOpenapi shouldBe "3.0.3"
      withClue(s"If v3.0 endpoints are enabled in application.conf, remove the [test only] from this test: ") {
        openAPI.getInfo.getTitle shouldBe "Self Assessment Accounts (MTD)"
      }
      openAPI.getInfo.getVersion shouldBe "3.0"
    }

    s"return the documentation with the correct accept header for version 3.0" in {
      val response = get(s"/api/conf/3.0/common/headers.yaml")
      val body     = response.body

      val headerRegex = """(?s).*?application/vnd\.hmrc\.(\d+\.\d+)\+json.*?""".r
      val header      = headerRegex.findFirstMatchIn(body)
      header.isDefined shouldBe true

      val versionFromHeader = header.get.group(1)
      versionFromHeader shouldBe "3.0"

    }
  }

  private def get(path: String): WSResponse = {
    val response: WSResponse = await(buildRequest(path).get())
    response.status shouldBe OK
    response
  }

}
