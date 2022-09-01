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

package v1.models.response.retrieveAllocations

import api.models.hateoas.{HateoasData, Link}
import cats.Functor
import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json._
import v1.hateoas.{HateoasLinks, HateoasListLinksFactory}
import v1.models.response.retrieveAllocations.detail.AllocationDetail

case class RetrieveAllocationsResponse[I](amount: Option[BigDecimal], method: Option[String], transactionDate: Option[String], allocations: Seq[I])

object RetrieveAllocationsResponse extends HateoasLinks {

  implicit def reads[I: Reads]: Reads[RetrieveAllocationsResponse[I]] = (
    (JsPath \ "paymentDetails" \\ "paymentAmount").readNullable[BigDecimal] and
      (JsPath \ "paymentDetails" \\ "paymentMethod").readNullable[String] and
      (JsPath \ "paymentDetails" \\ "valueDate").readNullable[String] and
      (JsPath \ "paymentDetails" \\ "sapClearingDocsDetails")
        .readNullable[Seq[I]]
        .map(_.map(_.filterNot(item => item == AllocationDetail.emptyAllocation)))
        .map {
          case Some(notEmpty) => notEmpty
          case _              => Seq.empty[I]
        }
  )(RetrieveAllocationsResponse.apply(_, _, _, _))

  implicit def writes[I: Writes]: OWrites[RetrieveAllocationsResponse[I]] =
    Json.writes[RetrieveAllocationsResponse[I]]

  implicit object LinksFactory extends HateoasListLinksFactory[RetrieveAllocationsResponse, AllocationDetail, RetrieveAllocationsHateoasData] {

    override def itemLinks(appConfig: AppConfig, data: RetrieveAllocationsHateoasData, item: AllocationDetail): Seq[Link] =
      Seq(
        retrieveChargeHistory(appConfig, data.nino, item.transactionId.getOrElse(""), isSelf = false),
        retrieveTransactionDetails(appConfig, data.nino, item.transactionId.getOrElse(""), isSelf = false)
      )

    override def links(appConfig: AppConfig, data: RetrieveAllocationsHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrievePaymentAllocations(appConfig, nino, paymentId, isSelf = true)
      )
    }

  }

  implicit object ResponseFunctor extends Functor[RetrieveAllocationsResponse] {

    override def map[A, B](fa: RetrieveAllocationsResponse[A])(f: A => B): RetrieveAllocationsResponse[B] =
      RetrieveAllocationsResponse(
        amount = fa.amount,
        method = fa.method,
        transactionDate = fa.transactionDate,
        allocations = fa.allocations.map(f)
      )

  }

}

case class RetrieveAllocationsHateoasData(nino: String, paymentId: String) extends HateoasData
