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

package api.support

import api.controllers.EndpointLogContext
import api.models.domain.{TaxYear, TodaySupplier}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.DownstreamResponseMappingSupport
import utils.Logging
import v2.models.response.retrieveCodingOut.RetrieveCodingOutResponse
import v2.models.response.retrieveTransactionDetails.RetrieveTransactionDetailsResponse

trait MappingSupportDownstream extends DownstreamResponseMappingSupport {
  self: Logging =>

  final def validateTransactionDetailsResponse[T](desResponseWrapper: ResponseWrapper[T]): Either[ErrorWrapper, ResponseWrapper[T]] = {
    desResponseWrapper.responseData match {
      case retrieveDetailsResponse: RetrieveTransactionDetailsResponse if retrieveDetailsResponse.transactionItems.isEmpty =>
        Left(ErrorWrapper(desResponseWrapper.correlationId, NoTransactionDetailsFoundError, None))
      case _ => Right(desResponseWrapper)
    }
  }

  final def validateCodingOutResponse[T](desResponseWrapper: ResponseWrapper[RetrieveCodingOutResponse], taxYear: TaxYear)(implicit
      todaySupplier: TodaySupplier): Either[ErrorWrapper, ResponseWrapper[RetrieveCodingOutResponse]] = {

    implicit val endpointLogContext: EndpointLogContext =
      EndpointLogContext(
        controllerName = "RetrieveCodingOutController",
        endpointName = "retrieveCodingOut"
      )

    desResponseWrapper.responseData match {
      case retrieveCodingOutDetailsResponse: RetrieveCodingOutResponse if taxYear.isTaxYearComplete && idsMissing(retrieveCodingOutDetailsResponse) =>
        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
            s"Error response received with CorrelationId")

        Left(ErrorWrapper(desResponseWrapper.correlationId, InternalError))

      case _ =>
        Right(desResponseWrapper)
    }
  }

  def idsMissing(response: RetrieveCodingOutResponse): Boolean = {
    import response._

    lazy val taxCodeComponentsIdsMissing = taxCodeComponents.fold(false)(taxCodeComponents => {
      import taxCodeComponents._

      debt.fold(false)(debt => debt.exists(_.id.isEmpty)) ||
        inYearAdjustment.fold(false)(inYearAdjustment => inYearAdjustment.id.isEmpty) ||
        payeUnderpayment.fold(false)(payeUnderpayment => payeUnderpayment.exists(_.id.isEmpty)) ||
        selfAssessmentUnderpayment.fold(false)(selfAssessmentUnderpayment => selfAssessmentUnderpayment.exists(_.id.isEmpty))
    })

    lazy val unmatchedCustomerSubmissionsIdsMissing = unmatchedCustomerSubmissions.fold(false)(unmatchedCustomerSubmissions => {
      import unmatchedCustomerSubmissions._

      debt.fold(false)(debt => debt.exists(_.id.isEmpty)) ||
        inYearAdjustment.fold(false)(inYearAdjustment => inYearAdjustment.id.isEmpty) ||
        payeUnderpayment.fold(false)(payeUnderpayment => payeUnderpayment.exists(_.id.isEmpty)) ||
        selfAssessmentUnderpayment.fold(false)(selfAssessmentUnderpayment => selfAssessmentUnderpayment.exists(_.id.isEmpty))
    })

    taxCodeComponentsIdsMissing || unmatchedCustomerSubmissionsIdsMissing
  }

}