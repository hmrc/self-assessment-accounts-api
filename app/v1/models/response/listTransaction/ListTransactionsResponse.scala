/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.response.listTransaction

import cats.Functor
import config.AppConfig
import play.api.libs.json._
import v1.hateoas.{HateoasLinks, HateoasListLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class ListTransactionsResponse[I](transactions: Seq[I])

object ListTransactionsResponse extends HateoasLinks {

  implicit def reads[I: Reads]: Reads[ListTransactionsResponse[I]] =
    implicitly(JsPath \ "financialDetails").read[Seq[I]].map(items => ListTransactionsResponse(items.filterNot(_ == TransactionItem.empty)))

  implicit def writes[I: Writes]: OWrites[ListTransactionsResponse[I]] =
    Json.writes[ListTransactionsResponse[I]]

  implicit object ListTransactionsLinksFactory extends HateoasListLinksFactory[ListTransactionsResponse, TransactionItem, ListTransactionsHateoasData] {

    override def itemLinks(appConfig: AppConfig, data: ListTransactionsHateoasData, item: TransactionItem): Seq[Link] = {
      import data.nino

      val transactionId = item.transactionId.getOrElse("")

      if (item.paymentId.nonEmpty) {
        Seq(
          retrievePaymentAllocations(appConfig, nino, item.paymentId.get, isSelf = false),
          retrieveTransactionDetails(appConfig, nino, transactionId, isSelf = false)
        )
      } else {
        Seq(
          retrieveChargeHistory(appConfig, nino, transactionId, isSelf = false),
          retrieveTransactionDetails(appConfig, nino, transactionId, isSelf = false)
        )
      }
    }

    override def links(appConfig: AppConfig, data: ListTransactionsHateoasData): Seq[Link] = {
      import data._
      Seq(
        listTransactions(appConfig, nino, from, to, isSelf = true),
        listCharges(appConfig, nino, from, to, isSelf = false),
        listPayments(appConfig, nino, from, to, isSelf = false)
      )
    }
  }

  implicit object ResponseFunctor extends Functor[ListTransactionsResponse] {
    override def map[A, B](fa: ListTransactionsResponse[A])(f: A => B): ListTransactionsResponse[B] =
      ListTransactionsResponse(fa.transactions.map(f))
  }

}

case class ListTransactionsHateoasData(nino: String, from: String, to: String) extends HateoasData
