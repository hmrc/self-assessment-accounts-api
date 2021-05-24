/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.connectors

import config.AppConfig
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import v1.connectors.DownstreamUri.{DesUri, IfsUri}
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

trait BaseDownstreamConnector extends Logging {
  val http: HttpClient
  val appConfig: AppConfig

  val CORRELATION_ID = "CorrelationId"

  private[connectors] def desHeaderCarrier(additionalHeaders: Seq[String] = Seq.empty)(implicit hc: HeaderCarrier, correlationId: String): HeaderCarrier = {

    HeaderCarrier(
      extraHeaders = hc.extraHeaders ++
        // Contract headers
        Seq(
          "Authorization" -> s"Bearer ${appConfig.desToken}",
          "Environment" -> appConfig.desEnv,
          "CorrelationId" -> correlationId
        ) ++
        // Other headers (i.e Gov-Test-Scenario, Content-Type)
        hc.headers(additionalHeaders ++ appConfig.desEnvironmentHeaders.getOrElse(Seq.empty))
    )

    def ifsHeaderCarrier(implicit hc: HeaderCarrier, correlationId: String): HeaderCarrier = {

      HeaderCarrier(
        extraHeaders = hc.extraHeaders ++

          Seq(
            "Authorization" -> s"Bearer ${appConfig.ifsToken}",
            "Environment" -> appConfig.ifsEnv,
            "CorrelationId" -> correlationId
          ) ++
          hc.headers(additionalHeaders ++ appConfig.desEnvironmentHeaders.getOrElse(Seq.empty))
      )
    }

    def post[Body: Writes, Resp](body: Body, uri: DownstreamUri[Resp])(implicit ec: ExecutionContext,
                                                                       hc: HeaderCarrier,
                                                                       httpReads: HttpReads[DownstreamOutcome[Resp]],
                                                                       correlationId: String): Future[DownstreamOutcome[Resp]] = {

      def doPost(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
        http.POST(s"${appConfig.desBaseUrl}/${uri.value}", body)
      }

      doPost(desHeaderCarrier(Seq("Content-Type")))
    }

    def get[Resp](uri: DownstreamUri[Resp])(implicit ec: ExecutionContext,
                                            hc: HeaderCarrier,
                                            httpReads: HttpReads[DownstreamOutcome[Resp]],
                                            correlationId: String): Future[DownstreamOutcome[Resp]] = {

      def doGet(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] =
        http.GET(s"${appConfig.desBaseUrl}/${uri.value}")

      doGet(desHeaderCarrier())
    }

    def get[Resp](uri: DownstreamUri[Resp], queryParams: Seq[(String, String)])(implicit ec: ExecutionContext,
                                                                                hc: HeaderCarrier,
                                                                                httpReads: HttpReads[DownstreamOutcome[Resp]],
                                                                                correlationId: String): Future[DownstreamOutcome[Resp]] = {

      def doGet(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
        http.GET(s"${appConfig.desBaseUrl}/${uri.value}")
      }

      doGet(desHeaderCarrier())
    }

    def put[Body: Writes, Resp](body: Body, uri: DownstreamUri[Resp])(implicit ec: ExecutionContext,
                                                                      hc: HeaderCarrier,
                                                                      httpReads: HttpReads[DownstreamOutcome[Resp]],
                                                                      correlationId: String): Future[DownstreamOutcome[Resp]] = {

      def doPut(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
        http.PUT(url = s"${appConfig.desBaseUrl}/${uri.value}", body)
      }

      doPut(desHeaderCarrier(Seq("Content-Type")))
    }

    def delete[Resp](uri: DownstreamUri[Resp])(implicit ec: ExecutionContext,
                                               hc: HeaderCarrier,
                                               httpReads: HttpReads[DownstreamOutcome[Resp]],
                                               correlationId: String): Future[DownstreamOutcome[Resp]] = {

      def doDelete(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
        http.DELETE(s"${appConfig.desBaseUrl}/${uri.value}")
      }

      doDelete(desHeaderCarrier())
    }

    private def getBackendUri[Resp](uri: DownstreamUri[Resp]): String = uri match {
      case DesUri(value) => s"${appConfig.desBaseUrl}/$value"
      case IfsUri(value) => s"${appConfig.ifsBaseUrl}/$value"
    }

    private def getBackendHeaders[Resp](uri: DownstreamUri[Resp], hc: HeaderCarrier, correlationId: String): HeaderCarrier = uri match {
      case DesUri(_) => desHeaderCarrier(Seq(correlationId))
      case IfsUri(_) => ifsHeaderCarrier(hc, correlationId)
    }

  }
}