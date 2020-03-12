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

package v1.models.response.retrieveTransaction

import cats.Functor
import config.AppConfig
import play.api.libs.json._
import v1.controllers.requestParsers.validators.validations.PaymentIdValidation
import v1.hateoas.{HateoasLinks, HateoasListLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class RetrieveTransactionsResponse[I](transactions: Seq[I])


object RetrieveTransactionsResponse extends HateoasLinks {

  implicit def reads[I: Reads]: Reads[RetrieveTransactionsResponse[I]] =
    implicitly(JsPath \ "transactions").read[Seq[I]].map(items => RetrieveTransactionsResponse(items.filterNot(_ == TransactionItem.empty)))

  implicit def writes[I: Writes]: OWrites[RetrieveTransactionsResponse[I]] =
    Json.writes[RetrieveTransactionsResponse[I]]

  implicit object LinksFactory extends HateoasListLinksFactory[RetrieveTransactionsResponse, TransactionItem, RetrieveTransactionsHateoasData] {

    override def itemLinks(appConfig: AppConfig, data: RetrieveTransactionsHateoasData, item: TransactionItem): Seq[Link] = {
      val id = item.id.getOrElse("")

      val isPayment = PaymentIdValidation.validate(id) == Nil

      if (isPayment) {
        Seq(retrievePaymentAllocations(appConfig, data.nino, id, isSelf = false))
      } else {
        Seq(retrieveChargeHistory(appConfig, data.nino, id, isSelf = false))
      }
    }

    override def links(appConfig: AppConfig, data: RetrieveTransactionsHateoasData): Seq[Link] = Seq(
      retrieveTransactions(appConfig, data.nino, isSelf = true),
      listPayments(appConfig, data.nino, isSelf = false),
      listCharges(appConfig, data.nino, isSelf = false)
    )
  }

  implicit object ResponseFunctor extends Functor[RetrieveTransactionsResponse] {
    override def map[A, B](fa: RetrieveTransactionsResponse[A])(f: A => B): RetrieveTransactionsResponse[B] =
      RetrieveTransactionsResponse(fa.transactions.map(f))
  }

}

case class RetrieveTransactionsHateoasData(nino: String) extends HateoasData


