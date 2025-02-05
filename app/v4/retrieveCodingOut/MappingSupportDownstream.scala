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

package v4.retrieveCodingOut

import shared.controllers.EndpointLogContext
import shared.models.domain.TaxYear
import shared.models.domain.TaxYear.currentTaxYear
import shared.models.errors.{ErrorWrapper, InternalError}
import shared.models.outcomes.ResponseWrapper
import shared.services.DownstreamResponseMappingSupport
import shared.utils.Logging
import v4.retrieveCodingOut.def1.model.response.Def1_RetrieveCodingOutResponse
import v4.retrieveCodingOut.model.response.RetrieveCodingOutResponse

trait MappingSupportDownstream extends DownstreamResponseMappingSupport {
  self: Logging =>

  final def validateCodingOutResponse(desResponseWrapper: ResponseWrapper[RetrieveCodingOutResponse],
                                      taxYear: TaxYear): Either[ErrorWrapper, ResponseWrapper[RetrieveCodingOutResponse]] = {

    implicit val endpointLogContext: EndpointLogContext =
      EndpointLogContext(
        controllerName = "RetrieveCodingOutController",
        endpointName = "retrieveCodingOut"
      )

    desResponseWrapper.responseData match {
      case retrieveCodingOutDetailsResponse: Def1_RetrieveCodingOutResponse
        if taxYear.year < currentTaxYear.year && idsMissing(retrieveCodingOutDetailsResponse) =>
        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
            s"Error response received with CorrelationId")

        Left(ErrorWrapper(desResponseWrapper.correlationId, InternalError))

      case _ =>
        Right(desResponseWrapper)
    }
  }

  def idsMissing(response: Def1_RetrieveCodingOutResponse): Boolean = {
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
