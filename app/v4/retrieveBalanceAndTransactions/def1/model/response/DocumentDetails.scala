/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.retrieveBalanceAndTransactions.def1.model.response

import api.models.domain.TaxYear
import api.utils.{EmptinessChecker, EmptyPathsResult}
import common.models.ChargeClassification
import play.api.libs.functional.syntax.*
import play.api.libs.json.*

case class LastClearing(lastClearingDate: Option[String], lastClearingReason: Option[String], lastClearedAmount: Option[BigDecimal])

object LastClearing {
  given EmptinessChecker[LastClearing] = EmptinessChecker.derived
  given format: OFormat[LastClearing]  = Json.format[LastClearing]
}

case class LatePaymentInterest(latePaymentInterestId: Option[String],
                               accruingInterestAmount: Option[BigDecimal],
                               interestRate: Option[BigDecimal],
                               interestStartDate: Option[String],
                               interestEndDate: Option[String],
                               interestAmount: Option[BigDecimal],
                               interestDunningLockAmount: Option[BigDecimal],
                               interestOutstandingAmount: Option[BigDecimal])

object LatePaymentInterest {
  given EmptinessChecker[LatePaymentInterest] = EmptinessChecker.derived

  given Reads[LatePaymentInterest] =
    (
      (JsPath \ "latePaymentInterestID").readNullable[String] and
        (JsPath \ "accruingInterestAmount").readNullable[BigDecimal] and
        (JsPath \ "interestRate").readNullable[BigDecimal] and
        (JsPath \ "interestFromDate").readNullable[String] and
        (JsPath \ "interestEndDate").readNullable[String] and
        (JsPath \ "latePaymentInterestAmount").readNullable[BigDecimal] and
        (JsPath \ "lpiWithDunningLock").readNullable[BigDecimal] and
        (JsPath \ "interestOutstandingAmount").readNullable[BigDecimal]
    )(LatePaymentInterest.apply)

  given OWrites[LatePaymentInterest] = Json.writes[LatePaymentInterest]
}

case class ReducedCharge(chargeType: Option[String], documentNumber: Option[String], amendmentDate: Option[String], taxYear: Option[String])

object ReducedCharge {
  given EmptinessChecker[ReducedCharge] = EmptinessChecker.derived

  given Reads[ReducedCharge] =
    (
      (JsPath \ "chargeTypeReducedCharge").readNullable[String] and
        (JsPath \ "documentNumberReducedCharge").readNullable[String] and
        (JsPath \ "amendmentDateReducedCharge").readNullable[String] and
        (JsPath \ "taxYearReducedCharge")
          .readNullable[String]
          .map(maybeTaxYear =>
            maybeTaxYear.map { year =>
              val ty = TaxYear.fromDownstream(year)
              ty.asMtd
            })
    )(ReducedCharge.apply)

  given OWrites[ReducedCharge] = Json.writes[ReducedCharge]
}

case class DocumentDetails(taxYear: Option[String],
                           documentId: String,
                           formBundleNumber: Option[String],
                           creditReason: Option[String],
                           documentDate: String,
                           documentText: Option[String],
                           documentDueDate: Option[String],
                           documentDescription: Option[String],
                           chargeClassification: Option[ChargeClassification],
                           originalAmount: BigDecimal,
                           outstandingAmount: BigDecimal,
                           lastClearing: Option[LastClearing],
                           isChargeEstimate: Boolean,
                           isCodedOut: Boolean,
                           paymentLot: Option[String],
                           paymentLotItem: Option[String],
                           effectiveDateOfPayment: Option[String],
                           latePaymentInterest: Option[LatePaymentInterest],
                           totalStandoverAmount: Option[BigDecimal],
                           collectableAmount: Option[BigDecimal],
                           amountCodedOut: Option[BigDecimal],
                           reducedCharge: Option[ReducedCharge],
                           poaRelevantAmount: Option[BigDecimal])

object DocumentDetails {
  val informationCode: Option[String] => Boolean = _.exists(_.nonEmpty)

  private def taxYear(maybeValue: Option[String]): Option[String] = maybeValue.flatMap {
    case year if year == "9999" => None
    case year =>
      val ty = TaxYear.fromDownstream(year)
      Some(ty.asMtd)
  }

  private def replaceWithNoneIfEmpty[A](maybeA: Option[A])(using emptinessChecker: EmptinessChecker[A]): Option[A] =
    maybeA.flatMap { a =>
      if (emptinessChecker.findEmptyPaths(a) == EmptyPathsResult.CompletelyEmpty) None else Some(a)
    }

  given Reads[DocumentDetails] = for {
    taxYear              <- (JsPath \ "taxYear").readNullable[String].map(taxYear)
    documentId           <- (JsPath \ "documentID").read[String]
    formBundleNumber     <- (JsPath \ "formBundleNumber").readNullable[String]
    creditReason         <- (JsPath \ "creditReason").readNullable[String]
    documentDate         <- (JsPath \ "documentDate").read[String]
    documentText         <- (JsPath \ "documentText").readNullable[String]
    documentDueDate      <- (JsPath \ "documentDueDate").readNullable[String]
    documentDescription  <- (JsPath \ "documentDescription").readNullable[String]
    chargeClassification <- (JsPath \ "chargeClassification").readNullable[ChargeClassification]
    originalAmount       <- (JsPath \ "totalAmount").read[BigDecimal]
    outstandingAmount    <- (JsPath \ "documentOutstandingAmount").read[BigDecimal]
    lastClearing         <- JsPath.readNullable[LastClearing].map(replaceWithNoneIfEmpty[LastClearing])
    isChargeEstimate <- (JsPath \ "statisticalFlag").read[String].flatMap {
      case "Y" => Reads.pure(true)
      case "N" => Reads.pure(false)
      case x   => Reads.failed(s"expected 'Y' or 'N' but was `$x`")
    }
    isCodedOut             <- (JsPath \ "informationCode").readNullable[String].map(informationCode)
    paymentLot             <- (JsPath \ "paymentLot").readNullable[String]
    paymentLotItem         <- (JsPath \ "paymentLotItem").readNullable[String]
    effectiveDateOfPayment <- (JsPath \ "effectiveDateOfPayment").readNullable[String]
    latePaymentInterest    <- JsPath.readNullable[LatePaymentInterest].map(replaceWithNoneIfEmpty[LatePaymentInterest])
    totalStandoverAmount   <- (JsPath \ "totalSoAmt").readNullable[BigDecimal]
    collectableAmount      <- (JsPath \ "collectableAmt").readNullable[BigDecimal]
    amountCodedOut         <- (JsPath \ "amountCodedOut").readNullable[BigDecimal]
    reducedCharge          <- JsPath.readNullable[ReducedCharge].map(replaceWithNoneIfEmpty[ReducedCharge])
    poaRelevantAmount      <- (JsPath \ "poaRelevantAmount").readNullable[BigDecimal]
  } yield {
    DocumentDetails(
      taxYear = taxYear,
      documentId = documentId,
      formBundleNumber = formBundleNumber,
      creditReason = creditReason,
      documentDate = documentDate,
      documentText = documentText,
      documentDueDate = documentDueDate,
      documentDescription = documentDescription,
      chargeClassification = chargeClassification,
      originalAmount = originalAmount,
      outstandingAmount = outstandingAmount,
      lastClearing = lastClearing,
      isChargeEstimate = isChargeEstimate,
      isCodedOut = isCodedOut,
      paymentLot = paymentLot,
      paymentLotItem = paymentLotItem,
      effectiveDateOfPayment = effectiveDateOfPayment,
      latePaymentInterest = latePaymentInterest,
      totalStandoverAmount = totalStandoverAmount,
      collectableAmount = collectableAmount,
      amountCodedOut = amountCodedOut,
      reducedCharge = reducedCharge,
      poaRelevantAmount = poaRelevantAmount
    )
  }

  given OWrites[DocumentDetails] = Json.writes[DocumentDetails]
}
