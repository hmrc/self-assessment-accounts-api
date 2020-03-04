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

package v1.models.response.listCharges

import cats.Functor
import config.AppConfig
import play.api.libs.json._
import v1.hateoas.{HateoasLinks, HateoasListLinksFactory}
import v1.models.hateoas.{HateoasData, Link}


case class ListChargesResponse[I](charges: Seq[I])

object ListChargesResponse extends HateoasLinks {

  implicit def reads[I: Reads]: Reads[ListChargesResponse[I]] =
    implicitly(JsPath \ "charges").read[Seq[I]].map(ListChargesResponse(_))

  implicit def writes[I: Writes]: OWrites[ListChargesResponse[I]] =
    Json.writes[ListChargesResponse[I]]

  implicit object LinksFactory extends HateoasListLinksFactory[ListChargesResponse, Charge, ListChargesHateoasData] {

    override def itemLinks(appConfig: AppConfig, data: ListChargesHateoasData, item: Charge): Seq[Link] = Seq(
      retrieveChargeHistory(appConfig, data.nino, item.id.getOrElse(""), isSelf = false)
    )

    override def links(appConfig: AppConfig, data: ListChargesHateoasData): Seq[Link] = Seq(
      listCharges(appConfig, data.nino, isSelf = true),
      retrieveTransactions(appConfig, data.nino, isSelf = false)
    )
  }

  implicit object ResponseFunctor extends Functor[ListChargesResponse] {
    override def map[A, B](fa: ListChargesResponse[A])(f: A => B): ListChargesResponse[B] =
      ListChargesResponse(fa.charges.map(f))
  }
}

case class ListChargesHateoasData(nino: String) extends HateoasData