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

package v1.models.response.retrieveTransactionDetails

import config.AppConfig
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class RetrieveTransactionDetailsResponse(transactionItems: Seq[TransactionItem])

object RetrieveTransactionDetailsResponse extends HateoasLinks {

  implicit val reads: Reads[RetrieveTransactionDetailsResponse] =
   (JsPath \ "financialDetails").read[Seq[TransactionItem]]
     .map(items => RetrieveTransactionDetailsResponse(items.filterNot(_ == TransactionItem.empty)))

  implicit val writes: OWrites[RetrieveTransactionDetailsResponse] =
    Json.writes[RetrieveTransactionDetailsResponse]

  implicit object RetrieveTransactionDetailsLinksFactory extends HateoasLinksFactory[RetrieveTransactionDetailsResponse, RetrieveTransactionDetailsHateoasData]{
    override def links(appConfig: AppConfig, data: RetrieveTransactionDetailsHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveTransactionDetails(appConfig, nino, transactionId, isSelf = true),
        paymentId match {
          case Some(pid) => retrievePaymentAllocations(appConfig, nino, pid, isSelf = false)
          case None => retrieveChargeHistory(appConfig, nino, transactionId, isSelf = false)
        }
      )
    }
  }

}

case class RetrieveTransactionDetailsHateoasData(nino: String, transactionId: String, paymentId: Option[String]) extends HateoasData
