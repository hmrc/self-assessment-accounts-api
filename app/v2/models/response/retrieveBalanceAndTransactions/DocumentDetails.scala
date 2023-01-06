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

package v2.models.response.retrieveBalanceAndTransactions

import api.models.domain.TaxYear
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utils.{EmptinessChecker, EmptyPathsResult}

case class LastClearing(lastClearingDate: Option[String], lastClearingReason: Option[String], lastClearedAmount: Option[BigDecimal])

object LastClearing {
  implicit val emptinessChecker: EmptinessChecker[LastClearing] = EmptinessChecker.genericInstance
  implicit val format: OFormat[LastClearing]                    = Json.format[LastClearing]
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
  implicit val emptinessChecker: EmptinessChecker[LatePaymentInterest] = EmptinessChecker.genericInstance

  implicit val reads: Reads[LatePaymentInterest] =
    (
      (JsPath \ "latePaymentInterestID").readNullable[String] and
        (JsPath \ "accruingInterestAmount").readNullable[BigDecimal] and
        (JsPath \ "interestRate").readNullable[BigDecimal] and
        (JsPath \ "interestFromDate").readNullable[String] and
        (JsPath \ "interestEndDate").readNullable[String] and
        (JsPath \ "latePaymentInterestAmount").readNullable[BigDecimal] and
        (JsPath \ "lpiWithDunningLock").readNullable[BigDecimal] and
        (JsPath \ "interestOutstandingAmount").readNullable[BigDecimal]
    )(LatePaymentInterest.apply _)

  implicit val writes: OWrites[LatePaymentInterest] = Json.writes[LatePaymentInterest]
}

case class ReducedCharge(chargeType: Option[String], documentNumber: Option[String], amendmentDate: Option[String], taxYear: Option[String])

object ReducedCharge {
  implicit val emptinessChecker: EmptinessChecker[ReducedCharge] = EmptinessChecker.genericInstance

  implicit val reads: Reads[ReducedCharge] =
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
    )(ReducedCharge.apply _)

  implicit val writes: OWrites[ReducedCharge] = Json.writes[ReducedCharge]
}

case class DocumentDetails(taxYear: Option[String],
                           documentId: String,
                           formBundleNumber: Option[String],
                           creditReason: Option[String],
                           documentDate: String,
                           documentText: Option[String],
                           documentDueDate: String,
                           documentDescription: Option[String],
                           originalAmount: BigDecimal,
                           outstandingAmount: BigDecimal,
                           lastClearing: Option[LastClearing],
                           isChargeEstimate: Boolean,
                           isCodedOut: Boolean,
                           paymentLot: Option[String],
                           paymentLotItem: Option[String],
                           effectiveDateOfPayment: Option[String],
                           latePaymentInterest: Option[LatePaymentInterest],
                           amountCodedOut: Option[BigDecimal],
                           reducedCharge: Option[ReducedCharge])

object DocumentDetails {

  private def taxYear(maybeValue: Option[String]): Option[String] = maybeValue.flatMap {
    case year if year == "9999" => None
    case year =>
      val ty = TaxYear.fromDownstream(year)
      Some(ty.asMtd)
  }

  val informationCode: Option[String] => Boolean = _.exists(_.nonEmpty)

  private def replaceWithNoneIfEmpty[A](maybeA: Option[A])(implicit emptinessChecker: EmptinessChecker[A]): Option[A] =
    maybeA.flatMap { a =>
      if (emptinessChecker.findEmptyPaths(a) == EmptyPathsResult.CompletelyEmpty) None else Some(a)
    }

  implicit val reads: Reads[DocumentDetails] =
    (
      (JsPath \ "taxYear").readNullable[String].map(taxYear) and
        (JsPath \ "documentId").read[String] and
        (JsPath \ "formBundleNumber").readNullable[String] and
        (JsPath \ "creditReason").readNullable[String] and
        (JsPath \ "documentDate").read[String] and
        (JsPath \ "documentText").readNullable[String] and
        (JsPath \ "documentDueDate").read[String] and
        (JsPath \ "documentDescription").readNullable[String] and
        (JsPath \ "totalAmount").read[BigDecimal] and
        (JsPath \ "documentOutstandingAmount").read[BigDecimal] and
        JsPath.readNullable[LastClearing].map(replaceWithNoneIfEmpty[LastClearing]) and
        (JsPath \ "statisticalFlag").read[Boolean] and
        (JsPath \ "informationCode").readNullable[String].map(informationCode) and
        (JsPath \ "paymentLot").readNullable[String] and
        (JsPath \ "paymentLotItem").readNullable[String] and
        (JsPath \ "effectiveDateOfPayment").readNullable[String] and
        JsPath.readNullable[LatePaymentInterest].map(replaceWithNoneIfEmpty[LatePaymentInterest]) and
        (JsPath \ "amountCodedOut").readNullable[BigDecimal] and
        JsPath.readNullable[ReducedCharge].map(replaceWithNoneIfEmpty[ReducedCharge])
    )(DocumentDetails.apply _)

  implicit val writes: OWrites[DocumentDetails] = Json.writes[DocumentDetails]
}
