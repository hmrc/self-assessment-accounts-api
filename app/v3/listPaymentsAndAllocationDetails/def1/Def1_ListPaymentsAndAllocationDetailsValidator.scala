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

package v3.listPaymentsAndAllocationDetails.def1

import cats.data.Validated
import cats.data.Validated.*
import cats.implicits.*
import common.errors.*
import common.resolvers.ResolveStringPattern
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveDateRange, ResolveNino}
import shared.models.errors.*
import v3.listPaymentsAndAllocationDetails.def1.model.request.Def1_ListPaymentsAndAllocationDetailsRequestData
import v3.listPaymentsAndAllocationDetails.model.request.ListPaymentsAndAllocationDetailsRequestData

import javax.inject.Singleton

@Singleton
class Def1_ListPaymentsAndAllocationDetailsValidator(nino: String,
                                                     fromDate: Option[String],
                                                     toDate: Option[String],
                                                     paymentLot: Option[String],
                                                     paymentLotItem: Option[String])
    extends Validator[ListPaymentsAndAllocationDetailsRequestData] {

  private val minYear = 1900
  private val maxYear = 2099

  private val resolvePaymentLot     = new ResolveStringPattern("^[0-9A-Za-z]{1,12}".r, PaymentLotFormatError)
  private val resolvePaymentLotItem = new ResolveStringPattern("^[0-9A-Za-z]{1,6}".r, PaymentLotItemFormatError)

  private val resolveDateRange = ResolveDateRange(FromDateFormatError, ToDateFormatError, RangeToDateBeforeFromDateError)
    .withYearsLimitedTo(minYear, maxYear)

  def validate: Validated[Seq[MtdError], Def1_ListPaymentsAndAllocationDetailsRequestData] = {

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
      ).mapN(Def1_ListPaymentsAndAllocationDetailsRequestData.apply)
    }
  }

  private def validateMissingDate(fromDate: Option[String], toDate: Option[String]): Validated[Seq[MtdError], Option[(String, String)]] =
    (fromDate, toDate) match {
      case (None, None)           => Valid(None)
      case (Some(from), Some(to)) => Valid(Some((from, to)))
      case (Some(_), None)        => invalid(RuleMissingToDateError)
      case (None, Some(_))        => invalid(MissingFromDateError)
    }

  private def validateMissingPaymentData(paymentLot: Option[String], paymentLotItem: Option[String]): Validated[Seq[MtdError], Unit] =
    (paymentLot, paymentLotItem) match {
      case (None, Some(_)) => invalid(MissingPaymentLotError)
      case (Some(_), None) => invalid(MissingPaymentLotItemError)
      case _               => Valid(())
    }

}
