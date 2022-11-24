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

package api.controllers

import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.auth.UserDetails
import api.models.errors.ErrorWrapper
import api.models.request.RawData
import api.services.AuditService
import play.api.libs.json.JsValue
import cats.syntax.either._

import scala.concurrent.ExecutionContext

trait AuditHandlerComponent[InputRaw <: RawData] {
  def auditEventCreator: Option[AuditHandler[InputRaw]]
}

object AuditHandler {

  def apply[InputRaw <: RawData](auditService: AuditService, auditType: String, transactionName: String, requestBody: Option[JsValue] = None)(
      paramsCreator: InputRaw => Map[String, String]
  ): AuditHandler[InputRaw] = new AuditHandler(
    auditService = auditService,
    auditType = auditType,
    transactionName = transactionName,
    paramsCreator = paramsCreator,
    requestBody = requestBody
  )

}

class AuditHandler[InputRaw <: RawData](auditService: AuditService,
                                        auditType: String,
                                        transactionName: String,
                                        paramsCreator: InputRaw => Map[String, String],
                                        requestBody: Option[JsValue]) {
  import api.controllers.RequestContextImplicits._

  def performAudit(input: InputRaw, userDetails: UserDetails, httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]])(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Unit = {
    val auditEvent = {
      val auditResponse = AuditResponse(httpStatus, response.leftMap(ew => ew.auditErrors))

      val detail = GenericAuditDetail(
        userDetails = userDetails,
        params = paramsCreator(input),
        requestBody = requestBody,
        `X-CorrelationId` = ctx.correlationId,
        auditResponse = auditResponse
      )

      AuditEvent(auditType, transactionName, detail)
    }

    auditService.auditEvent(auditEvent)
  }

}
