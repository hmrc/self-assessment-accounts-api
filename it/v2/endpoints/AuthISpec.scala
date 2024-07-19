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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.http.Status.NO_CONTENT
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import support.IntegrationBaseSpec

class AuthISpec extends IntegrationBaseSpec {

  private trait Test {
    val nino    = "AA123456A"
    val taxYear = "2022-23"

    def downstreamUri: String = s"/income-tax/accounts/self-assessment/collection/tax-code/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def uri: String = s"/$nino/$taxYear/collection/tax-code "
  }

  "Calling the sample endpoint" when {

    "MTD ID lookup fails with a 500" should {

      "return 500" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.error(nino, Status.INTERNAL_SERVER_ERROR)
        }

        val response: WSResponse = await(request().delete())
        response.status shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "MTD ID lookup fails with a 403" should {

      "return 403" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.error(nino, Status.FORBIDDEN)
        }

        val response: WSResponse = await(request().delete())
        response.status shouldBe Status.FORBIDDEN
      }
    }
  }

  "MTD ID lookup succeeds and the user is authorised" should {

    "return 200" in new Test {
      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        MtdIdLookupStub.ninoFound(nino)
        DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, NO_CONTENT)
      }

      val response: WSResponse = await(request().delete())
      response.status shouldBe Status.NO_CONTENT
    }
  }

  "MTD ID lookup succeeds but the user is NOT logged in" should {

    "return 403" in new Test {
      override val nino: String = "AA123456A"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        MtdIdLookupStub.ninoFound(nino)
        AuthStub.unauthorisedNotLoggedIn()
      }

      val response: WSResponse = await(request().delete())
      response.status shouldBe Status.FORBIDDEN
    }
  }

  "MTD ID lookup succeeds but the user is NOT authorised" should {

    "return 403" in new Test {
      override val nino: String = "AA123456A"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        MtdIdLookupStub.ninoFound(nino)
        AuthStub.unauthorisedOther()
      }

      val response: WSResponse = await(request().delete())
      response.status shouldBe Status.FORBIDDEN
    }
  }

}
