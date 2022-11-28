/*
 * Copyright 2022 HM Revenue & Customs
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

package api.controllers

import api.controllers.requestParsers.RequestParser
import api.hateoas.{HateoasFactory, HateoasLinksFactory}
import api.models.errors.ErrorWrapper
import api.models.hateoas.{HateoasData, HateoasWrapper}
import api.models.outcomes.ResponseWrapper
import api.models.request.RawData
import play.api.http.Status
import play.api.libs.json.Writes
import play.api.mvc.Result

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

// FIXME need to handle:
// - nrs
// - logging context (requires class to automate - ok for mix-in but not for builder usage)

trait ParserOnlyBuilder[InputRaw <: RawData, Input] {

  def withService[Output](service: Input => Future[Either[ErrorWrapper, ResponseWrapper[Output]]]): RequestHandlerBuilder[InputRaw, Input, Output]

}

trait RequestHandlerBuilder[InputRaw <: RawData, Input, Output] {

  /** Shorthand for
    * {{{
    * withResultCreator(ResultCreator.json(successStatus))
    * }}}
    */
  def withPlainJsonResult(successStatus: Int = Status.OK)(implicit ws: Writes[Output]): RequestHandlerBuilder[InputRaw, Input, Output] =
    withResultCreator(ResultCreator.json(successStatus))

  /** Shorthand for
    * {{{
    * withResultCreator(ResultCreator.noContent)
    * }}}
    */
  def withNoContentResult(successStatus: Int = Status.OK): RequestHandlerBuilder[InputRaw, Input, Output] =
    withResultCreator(ResultCreator.noContent)

  /** Shorthand for
    * {{{
    * withResultCreator(ResultCreator.hateoasWrappingUsing(hateoasFactory, successStatus)(data))
    * }}}
    */
  def withHateoasResult[HData <: HateoasData](hateoasFactory: HateoasFactory)(data: Output => HData, successStatus: Int = Status.OK)(implicit
      linksFactory: HateoasLinksFactory[Output, HData],
      writes: Writes[HateoasWrapper[Output]]): RequestHandlerBuilder[InputRaw, Input, Output] =
    withResultCreator(ResultCreator.hateoasWrappingUsing(hateoasFactory, successStatus)(data))

  def withResultCreator(resultCreator: ResultCreator[InputRaw, Output]): RequestHandlerBuilder[InputRaw, Input, Output]

  def withAuditing(auditHandler: AuditHandler[InputRaw]): RequestHandlerBuilder[InputRaw, Input, Output]

  def withErrorHandling(errorHandling: PartialFunction[ErrorWrapper, Result]): RequestHandlerBuilder[InputRaw, Input, Output]
  def createRequestHandler(implicit ec: ExecutionContext): RequestHandler[InputRaw, Input, Output]
}

@Singleton
final class RequestHandlerFactory @Inject() (defaultErrorHandling: ErrorHandling = DefaultErrorHandling) {

  def withParser[InputRaw <: RawData, Input](parser: RequestParser[InputRaw, Input]): ParserOnlyBuilder[InputRaw, Input] =
    ParserOnlyBuilderImpl(parser)

  private case class ParserOnlyBuilderImpl[InputRaw <: RawData, Input](parser: RequestParser[InputRaw, Input])
      extends ParserOnlyBuilder[InputRaw, Input] {

    def withService[Output](
        serviceFunction: Input => Future[Either[ErrorWrapper, ResponseWrapper[Output]]]): RequestHandlerBuilder[InputRaw, Input, Output] =
      RequestHandlerBuilderImpl(parser, serviceFunction)

  }

  private case class RequestHandlerBuilderImpl[InputRaw <: RawData, Input, Output](
      parser: RequestParser[InputRaw, Input],
      service: Input => Future[Either[ErrorWrapper, ResponseWrapper[Output]]],
      errorHandling: PartialFunction[ErrorWrapper, Result] = PartialFunction.empty,
      resultCreator: ResultCreator[InputRaw, Output] = ResultCreator.noContent[InputRaw, Output],
      auditHandler: Option[AuditHandler[InputRaw]] = None
  ) extends RequestHandlerBuilder[InputRaw, Input, Output] {

    def withResultCreator(resultCreator: ResultCreator[InputRaw, Output]): RequestHandlerBuilder[InputRaw, Input, Output] =
      copy(resultCreator = resultCreator)

    def withErrorHandling(errorHandling: PartialFunction[ErrorWrapper, Result]): RequestHandlerBuilder[InputRaw, Input, Output] =
      copy(errorHandling = errorHandling)

    def withAuditing(auditHandler: AuditHandler[InputRaw]): RequestHandlerBuilder[InputRaw, Input, Output] =
      copy(auditHandler = Some(auditHandler))

    def createRequestHandler(implicit ec: ExecutionContext): RequestHandler[InputRaw, Input, Output] = {
      val combinedErrorHandling = new ErrorHandling {
        override def errorResultPF: PartialFunction[ErrorWrapper, Result] = errorHandling.orElse(defaultErrorHandling.errorResultPF)
      }

      new RequestHandler.Impl(parser, service, combinedErrorHandling, resultCreator, auditHandler)
    }

  }

}
