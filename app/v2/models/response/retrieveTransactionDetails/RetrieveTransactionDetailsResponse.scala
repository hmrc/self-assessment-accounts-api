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

package v2.models.response.retrieveTransactionDetails

import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class RetrieveTransactionDetailsResponse(transactionItems: Seq[TransactionItem])

object RetrieveTransactionDetailsResponse extends HateoasLinks {

  implicit val reads: Reads[RetrieveTransactionDetailsResponse] =
    (JsPath \ "financialDetails")
      .read[Seq[TransactionItem]]
      .map(items => RetrieveTransactionDetailsResponse(items.filterNot(_ == TransactionItem.empty)))

  implicit val writes: OWrites[RetrieveTransactionDetailsResponse] =
    Json.writes[RetrieveTransactionDetailsResponse]

  implicit object RetrieveTransactionDetailsLinksFactory
    extends HateoasLinksFactory[RetrieveTransactionDetailsResponse, RetrieveTransactionDetailsHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveTransactionDetailsHateoasData): Seq[Link] = {
      import data._
      paymentId match {
        case Some(pid) =>
          Seq(
            retrieveTransactionDetails(appConfig, nino, transactionId, isSelf = true),
            retrievePaymentAllocations(appConfig, nino, pid, isSelf = false))
        case None => Seq(retrieveTransactionDetails(appConfig, nino, transactionId, isSelf = true))
      }
    }

  }

}

case class RetrieveTransactionDetailsHateoasData(nino: String, transactionId: String, paymentId: Option[String]) extends HateoasData