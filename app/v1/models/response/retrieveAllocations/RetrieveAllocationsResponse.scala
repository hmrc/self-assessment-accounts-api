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

package v1.models.response.retrieveAllocations

import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads, Writes}
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class RetrieveAllocationsResponse[I](amount: Option[BigDecimal],
                                       method: Option[String],
                                       transactionDate: Option[String],
                                       allocations: Seq[I]) // TODO: This also needs wrapping in HATEOAS

object RetrieveAllocationsResponse extends HateoasLinks {

  implicit def writes[I: Writes]: OWrites[RetrieveAllocationsResponse[I]] = Json.writes[RetrieveAllocationsResponse[I]]

  implicit def reads[I: Reads]: Reads[RetrieveAllocationsResponse[I]] = (
    (JsPath \ "paymentDetails" \\ "paymentAmount").readNullable[BigDecimal] and
      (JsPath \ "paymentDetails" \\ "paymentMethod").readNullable[String] and
      (JsPath \ "paymentDetails" \\ "valueDate").readNullable[String] and
      (JsPath \ "paymentDetails").readNullable[Seq[I]]
        .map {
          case Some(notEmpty) => notEmpty
          case _ => Seq.empty[I]
        }
    ) (RetrieveAllocationsResponse.apply _)

  implicit object RetrieveAllocationsLinksFactory extends HateoasLinksFactory[RetrieveAllocationsResponse, RetrieveAllocationsHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveAllocationsHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrievePaymentAllocations(appConfig, nino, paymentId, isSelf = true)
      )
    }
  }

}

case class RetrieveAllocationsHateoasData(nino: String, paymentId: String) extends HateoasData