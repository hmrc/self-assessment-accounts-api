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

package api.fixtures.audit

import api.fixtures.audit.AuditResponseFixture._
import api.models.audit.GenericAuditDetailOld
import api.models.auth.UserDetails
import play.api.libs.json.{JsValue, Json}

object GenericAuditDetailFixture {

  val nino: String                         = "ZG903729C"
  val taxYear: String                      = "2019-20"
  val userType: String                     = "Agent"
  val agentReferenceNumber: Option[String] = Some("012345678")
  val userDetails: UserDetails             = UserDetails("", "Agent", agentReferenceNumber)
  val pathParams: Map[String, String]      = Map("nino" -> nino, "taxYear" -> taxYear)
  val requestBody: Option[JsValue]         = None
  val correlationId                        = "a1e8057e-fbbc-47a8-a8b478d9f015c253"

  val genericAuditDetailModelSuccess: GenericAuditDetailOld =
    GenericAuditDetailOld(
      userType = userType,
      agentReferenceNumber = agentReferenceNumber,
      params = Map("nino" -> nino, "taxYear" -> taxYear),
      requestBody = requestBody,
      auditResponse = auditResponseModelWithBody,
      `X-CorrelationId` = correlationId
    )

  val genericAuditDetailModelError: GenericAuditDetailOld =
    genericAuditDetailModelSuccess.copy(
      auditResponse = auditResponseModelWithErrors
    )

  val genericAuditDetailJsonSuccess: JsValue = Json.parse(
    s"""
       |{
       |   "userType" : "$userType",
       |   "agentReferenceNumber" : "${agentReferenceNumber.get}",
       |   "nino" : "$nino",
       |   "taxYear" : "$taxYear",
       |   "response" : {
       |     "httpStatus" : ${auditResponseModelWithBody.httpStatus},
       |     "body" : ${auditResponseModelWithBody.body.get}
       |   },
       |   "X-CorrelationId"  : "$correlationId"
       |}
    """.stripMargin
  )

  val genericAuditDetailJsonError: JsValue = Json.parse(
    s"""
       |{
       |   "userType" : "$userType",
       |   "agentReferenceNumber" : "${agentReferenceNumber.get}",
       |   "nino" : "$nino",
       |   "taxYear" : "$taxYear",
       |   "response" : $auditResponseJsonWithErrors,
       |   "X-CorrelationId"  : "$correlationId"
       |}
     """.stripMargin
  )

}
