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

package v3.retrieveChargeHistoryByChargeReference.def1.model.response

import api.hateoas.{HateoasData, HateoasLinks, HateoasLinksFactory, Link}
import config.AppConfig
import play.api.libs.json.{Json, OFormat}

case class RetrieveCodingOutResponse(taxCodeComponents: Option[TaxCodeComponentsObject],
                                     unmatchedCustomerSubmissions: Option[UnmatchedCustomerSubmissionsObject])

object RetrieveCodingOutResponse extends HateoasLinks {

  implicit val format: OFormat[RetrieveCodingOutResponse] = Json.format[RetrieveCodingOutResponse]

  implicit object RetrieveCodingOutLinksFactory extends HateoasLinksFactory[RetrieveCodingOutResponse, RetrieveCodingOutHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveCodingOutHateoasData): Seq[Link] = {
      import data._
      Seq(
        createOrAmendCodingOut(appConfig, nino, taxYear),
        retrieveCodingOut(appConfig, nino, taxYear),
        deleteCodingOut(appConfig, nino, taxYear)
      )
    }

  }

}

case class RetrieveCodingOutHateoasData(nino: String, taxYear: String) extends HateoasData
