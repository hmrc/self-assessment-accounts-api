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

package auth

import api.services.DownstreamStub
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSRequest, WSResponse}

class SelfAssessmentAccountsAuthMainAgentsOnlyISpec extends AuthMainAgentsOnlyISpec {

  val callingApiVersion = "3.0"

  val supportingAgentsNotAllowedEndpoint = "delete-coding-out"

  private val taxYear = "2022-23"

  override val mtdUrl: String = s"/$nino/$taxYear/collection/tax-code"

  def sendMtdRequest(request: WSRequest): WSResponse = await(request.delete())

  override val downstreamUri: String = s"/income-tax/accounts/self-assessment/collection/tax-code/$nino/$taxYear"

  val maybeDownstreamResponseJson: Option[JsValue] = None

  override val downstreamHttpMethod: DownstreamStub.HTTPMethod = DownstreamStub.DELETE

  override val expectedMtdSuccessStatus: Int = NO_CONTENT

}