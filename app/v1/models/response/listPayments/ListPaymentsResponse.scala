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

package v1.models.response.listPayments

import cats.Functor
import config.AppConfig
import play.api.libs.json._
import v1.hateoas.{HateoasLinks, HateoasListLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class ListPaymentsResponse[I](payments: Seq[I])

object ListPaymentsResponse extends HateoasLinks {

  implicit def reads[I: Reads]: Reads[ListPaymentsResponse[I]] =
    implicitly(JsPath \ "paymentDetails").read[Seq[I]].map(ListPaymentsResponse(_))

  implicit def writes[I: Writes]: OWrites[ListPaymentsResponse[I]] =
    Json.writes[ListPaymentsResponse[I]]

  implicit object LinksFactory extends HateoasListLinksFactory[ListPaymentsResponse, Payment, ListPaymentsHateoasData] {

    override def itemLinks(appConfig: AppConfig, data: ListPaymentsHateoasData, item: Payment): Seq[Link] =
      Seq(
        retrievePaymentAllocations(appConfig, data.nino, item.paymentId.getOrElse(""), isSelf = false)
      )

    override def links(appConfig: AppConfig, data: ListPaymentsHateoasData): Seq[Link] = {
      import data._
      Seq(
        listPayments(appConfig, nino, from, to, isSelf = true),
        listTransactions(appConfig, nino, from, to, isSelf = false)
      )
    }
  }

  implicit object ResponseFunctor extends Functor[ListPaymentsResponse] {
    override def map[A, B](fa: ListPaymentsResponse[A])(f: A => B): ListPaymentsResponse[B] =
      ListPaymentsResponse(fa.payments.map(f))
  }

}

case class ListPaymentsHateoasData(nino: String, from: String, to: String) extends HateoasData
