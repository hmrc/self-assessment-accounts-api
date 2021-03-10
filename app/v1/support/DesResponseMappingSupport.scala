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

package v1.support

import utils.Logging
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.response.listCharges.ListChargesResponse
import v1.models.response.listPayments.ListPaymentsResponse
import v1.models.response.retrieveTransactionDetails.RetrieveTransactionDetailsResponse

trait DesResponseMappingSupport {
  self: Logging =>

  final def validateListPaymentsSuccessResponse[T](desResponseWrapper: ResponseWrapper[T]): Either[ErrorWrapper, ResponseWrapper[T]] = {
    desResponseWrapper.responseData match {
      case listPaymentsResponse: ListPaymentsResponse[_] if listPaymentsResponse.payments.isEmpty =>
        Left(ErrorWrapper(desResponseWrapper.correlationId, NoPaymentsFoundError, None))
      case _ => Right(desResponseWrapper)
    }
  }

  final def validateListChargesSuccessResponse[T](desResponseWrapper: ResponseWrapper[T]): Either[ErrorWrapper, ResponseWrapper[T]] = {
    desResponseWrapper.responseData match {
      case listChargesResponse: ListChargesResponse[_] if listChargesResponse.charges.isEmpty =>
        Left(ErrorWrapper(desResponseWrapper.correlationId, NoChargesFoundError, None))
      case _ => Right(desResponseWrapper)
    }
  }

  final def validateTransactionDetailsResponse[T](desResponseWrapper: ResponseWrapper[T]): Either[ErrorWrapper, ResponseWrapper[T]] = {
    desResponseWrapper.responseData match {
      case retrieveDetailsResponse: RetrieveTransactionDetailsResponse if retrieveDetailsResponse.transactionItems.isEmpty =>
        Left(ErrorWrapper(desResponseWrapper.correlationId, NoTransactionDetailsFoundError, None))
      case _ => Right(desResponseWrapper)
    }
  }

  final def mapDesErrors[D](errorCodeMap: PartialFunction[String, MtdError])(desResponseWrapper: ResponseWrapper[DesError])(
    implicit logContext: EndpointLogContext): ErrorWrapper = {

    lazy val defaultErrorCodeMapping: String => MtdError = { code =>
      logger.warn(s"[${logContext.controllerName}] [${logContext.endpointName}] - No mapping found for error code $code")
      DownstreamError
    }

    desResponseWrapper match {
      case ResponseWrapper(correlationId, DesErrors(error :: Nil)) =>
        ErrorWrapper(correlationId, errorCodeMap.applyOrElse(error.code, defaultErrorCodeMapping), None)

      case ResponseWrapper(correlationId, DesErrors(errorCodes)) =>
        val mtdErrors = errorCodes.map(error => errorCodeMap.applyOrElse(error.code, defaultErrorCodeMapping))

        if (mtdErrors.contains(DownstreamError)) {
          logger.warn(
            s"[${logContext.controllerName}] [${logContext.endpointName}] [CorrelationId - $correlationId]" +
              s" - downstream returned ${errorCodes.map(_.code).mkString(",")}. Revert to ISE")
          ErrorWrapper(correlationId, DownstreamError, None)
        } else {
          ErrorWrapper(correlationId, BadRequestError, Some(mtdErrors))
        }

      case ResponseWrapper(correlationId, OutboundError(error, errors)) =>
        ErrorWrapper(correlationId, error, errors)
    }
  }
}
