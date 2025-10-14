/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.retrieveChargeHistoryByTransactionId

import shared.config.{ConfigFeatureSwitches, SharedAppConfig}
import shared.connectors.DownstreamUri.{HipUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v3.retrieveChargeHistoryByTransactionId.model.request.RetrieveChargeHistoryByTransactionIdRequestData
import v3.retrieveChargeHistoryByTransactionId.model.response.RetrieveChargeHistoryResponse
import shared.utils.DateUtils.isoDateTimeStamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveChargeHistoryByTransactionIdConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig)
    extends BaseDownstreamConnector {

  def retrieveChargeHistoryByTransactionId(request: RetrieveChargeHistoryByTransactionIdRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveChargeHistoryResponse]] = {

    import request._

    val additionalContractHeaders: Seq[(String, String)] = List(
      "X-Message-Type"        -> "ETMPGetChargeHistory",
      "X-Originating-System"  -> "MDTP",
      "X-Receipt-Type"        -> "ITSA",
      "X-Receipt-Date"        -> isoDateTimeStamp,
      "X-Transmitting-System" -> "HIP"
    )

    val hipQueryParams: Seq[(String, String)] =
      List(
        "idType"            -> "NINO",
        "idValue"           -> nino.value,
        "sapDocumentNumber" -> transactionId.toString
      )

    val IfsQueryParams = Seq("docNumber" -> transactionId.toString)

    val (downStreamUri, queryParams) = if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1554")) {
      (
        HipUri[RetrieveChargeHistoryResponse](
          path = "etmp/RESTAdapter/itsa/taxpayer/GetChargeHistory",
          additionalContractHeaders
        ),
        hipQueryParams
      )
    } else {
      (
        IfsUri[RetrieveChargeHistoryResponse](s"cross-regime/charges/NINO/$nino/ITSA"),
        IfsQueryParams
      )

    }
    get(downStreamUri, queryParams)
  }

}
