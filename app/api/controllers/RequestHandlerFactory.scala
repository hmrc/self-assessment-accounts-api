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

import scala.concurrent.{ExecutionContext, Future}

object RequestHandlerFactory {

  def withParser[InputRaw <: RawData, Input](parser: RequestParser[InputRaw, Input]): RequestHandlerFactory.ParserOnlyBuilder[InputRaw, Input] =
    RequestHandlerFactory.ParserOnlyBuilder(parser, ErrorHandling.Default)

  // Intermediate class so that the compiler can separately capture the InputRaw and Input types here, and the Output type later
  case class ParserOnlyBuilder[InputRaw <: RawData, Input](parser: RequestParser[InputRaw, Input],
                                                           errorHandling: ErrorHandling = ErrorHandling.Default) {

    def withService[Output](
        serviceFunction: Input => Future[Either[ErrorWrapper, ResponseWrapper[Output]]]): RequestHandlerBuilder[InputRaw, Input, Output] =
      RequestHandlerBuilder(parser, serviceFunction, errorHandling)

  }

  case class RequestHandlerBuilder[InputRaw <: RawData, Input, Output](
      parser: RequestParser[InputRaw, Input],
      service: Input => Future[Either[ErrorWrapper, ResponseWrapper[Output]]],
      errorHandling: ErrorHandling,
      resultCreator: ResultCreator[InputRaw, Input, Output] = ResultCreator.noContent[InputRaw, Input, Output],
      auditHandler: Option[AuditHandler] = None
  ) {

    def withResultCreator(resultCreator: ResultCreator[InputRaw, Input, Output]): RequestHandlerBuilder[InputRaw, Input, Output] =
      copy(resultCreator = resultCreator)

    def withErrorHandling(errorHandling: ErrorHandling): RequestHandlerBuilder[InputRaw, Input, Output] =
      copy(errorHandling = errorHandling)

    def withAuditing(auditHandler: AuditHandler): RequestHandlerBuilder[InputRaw, Input, Output] =
      copy(auditHandler = Some(auditHandler))

    def createRequestHandler(implicit ec: ExecutionContext): RequestHandler[InputRaw, Input, Output] =
      new RequestHandler(parser, service, errorHandling, resultCreator, auditHandler)

    /** Shorthand for
      * {{{
      * withResultCreator(ResultCreator.plainJson(successStatus))
      * }}}
      */
    def withPlainJsonResult(successStatus: Int = Status.OK)(implicit ws: Writes[Output]): RequestHandlerBuilder[InputRaw, Input, Output] =
      withResultCreator(ResultCreator.plainJson(successStatus))

    /** Shorthand for
      * {{{
      * withResultCreator(ResultCreator.noContent)
      * }}}
      */
    def withNoContentResult(successStatus: Int = Status.OK): RequestHandlerBuilder[InputRaw, Input, Output] =
      withResultCreator(ResultCreator.noContent)

    /** Shorthand for
      * {{{
      * withResultCreator(ResultCreator.hateoasWrapping(hateoasFactory, successStatus)(data))
      * }}}
      */
    def withHateoasResultFrom[HData <: HateoasData](
        hateoasFactory: HateoasFactory)(data: (Input, Output) => HData, successStatus: Int = Status.OK)(implicit
        linksFactory: HateoasLinksFactory[Output, HData],
        writes: Writes[HateoasWrapper[Output]]): RequestHandlerBuilder[InputRaw, Input, Output] =
      withResultCreator(ResultCreator.hateoasWrapping(hateoasFactory, successStatus)(data))

    /** Shorthand for
      * {{{
      * withResultCreator(ResultCreator.hateoasWrapping(hateoasFactory, successStatus)((_,_) => data))
      * }}}
      */
    def withHateoasResult[HData <: HateoasData](hateoasFactory: HateoasFactory)(data: HData, successStatus: Int = Status.OK)(implicit
        linksFactory: HateoasLinksFactory[Output, HData],
        writes: Writes[HateoasWrapper[Output]]): RequestHandlerBuilder[InputRaw, Input, Output] =
      withResultCreator(ResultCreator.hateoasWrapping(hateoasFactory, successStatus)((_, _) => data))

  }

}
