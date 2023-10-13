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

package v2.controllers.validators

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveDateRange, ResolveNino, ResolveStringPattern}
import api.models.errors._
import cats.data.Validated
import cats.data.Validated._
import cats.implicits._
import v2.models.request.listPaymentsAndAllocationDetails.ListPaymentsAndAllocationDetailsRequestData

import javax.inject.Singleton

@Singleton
class ListPaymentsAndAllocationDetailsValidatorFactory {

  private val minYear = 1900
  private val maxYear = 2100

  private val resolvePaymentLot     = new ResolveStringPattern("^[0-9A-Za-z]{1,12}".r, PaymentLotFormatError)
  private val resolvePaymentLotItem = new ResolveStringPattern("^[0-9A-Za-z]{1,6}".r, PaymentLotItemFormatError)

  def validator(nino: String,
                fromDate: Option[String],
                toDate: Option[String],
                paymentLot: Option[String],
                paymentLotItem: Option[String]): Validator[ListPaymentsAndAllocationDetailsRequestData] =
    new Validator[ListPaymentsAndAllocationDetailsRequestData] {

      private val resolveDateRange = ResolveDateRange
        .withLimits(minYear, maxYear, FromDateFormatError, ToDateFormatError, RangeToDateBeforeFromDateError)

      def validate: Validated[Seq[MtdError], ListPaymentsAndAllocationDetailsRequestData] = {

        validateMissingPaymentData(paymentLot, paymentLotItem) andThen { _ =>
          validateMissingDate(fromDate, toDate)
        } andThen { maybeFromAndTo =>
          (
            ResolveNino(nino),
            maybeFromAndTo
              .map { case (from, to) =>
                resolveDateRange(from -> to)
                  .map(Some(_))
              }
              .getOrElse(Valid(None)),
            resolvePaymentLot(paymentLot),
            resolvePaymentLotItem(paymentLotItem)
          ).mapN(ListPaymentsAndAllocationDetailsRequestData)
        }
      }

      private def validateMissingDate(fromDate: Option[String], toDate: Option[String]): Validated[Seq[MtdError], Option[(String, String)]] =
        (fromDate, toDate) match {
          case (None, None)           => Valid(None)
          case (Some(from), Some(to)) => Valid(Some((from, to)))
          case (Some(_), None)        => Invalid(List(RuleMissingToDateError))
          case (None, Some(_))        => Invalid(List(MissingFromDateError))
        }

      private def validateMissingPaymentData(paymentLot: Option[String], paymentLotItem: Option[String]): Validated[Seq[MtdError], Unit] =
        (paymentLot, paymentLotItem) match {
          case (None, Some(_)) => Invalid(List(MissingPaymentLotError))
          case (Some(_), None) => Invalid(List(MissingPaymentLotItemError))
          case _               => Valid(())
        }

    }

}
