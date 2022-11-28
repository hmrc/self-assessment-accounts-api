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

import scala.concurrent.{ExecutionContext, Future}

// FIXME need to handle:
// - nrs

trait RequestHandlerFactoryComponent {
  def requestHandlerFactory: RequestHandlerFactory
}

trait DefaultRequestHandlerFactoryComponent extends RequestHandlerFactoryComponent {
  self: ErrorHandlingComponent =>

  def requestHandlerFactory: RequestHandlerFactory = new RequestHandlerFactory {

    def withParser[InputRaw <: RawData, Input](parser: RequestParser[InputRaw, Input]): RequestHandlerFactory.ParserOnlyBuilder[InputRaw, Input] =
      RequestHandlerFactory.ParserOnlyBuilder(parser, errorHandling)

  }

}

trait RequestHandlerFactory {
  def withParser[InputRaw <: RawData, Input](parser: RequestParser[InputRaw, Input]): RequestHandlerFactory.ParserOnlyBuilder[InputRaw, Input]
}

object RequestHandlerFactory {

  // Intermediate class so that the compiler can separately capture the InputRaw and Input types here, and the Output type later
  case class ParserOnlyBuilder[InputRaw <: RawData, Input](parser: RequestParser[InputRaw, Input], defaultErrorHandling: ErrorHandling) {

    def withService[Output](
        serviceFunction: Input => Future[Either[ErrorWrapper, ResponseWrapper[Output]]]): RequestHandlerBuilder[InputRaw, Input, Output] =
      RequestHandlerBuilder(parser, serviceFunction, defaultErrorHandling)

  }

  case class RequestHandlerBuilder[InputRaw <: RawData, Input, Output](
                                                                        parser: RequestParser[InputRaw, Input],
                                                                        service: Input => Future[Either[ErrorWrapper, ResponseWrapper[Output]]],
                                                                        errorHandling: ErrorHandling,
                                                                        errorHandlerOverride: PartialFunction[ErrorWrapper, Result] = PartialFunction.empty,
                                                                        resultCreator: ResultCreator[InputRaw, Input, Output] = ResultCreator.noContent[InputRaw, Input, Output],
                                                                        auditHandler: Option[AuditHandler] = None
  ) {

    def withResultCreator(resultCreator: ResultCreator[InputRaw, Input, Output]): RequestHandlerBuilder[InputRaw, Input, Output] =
      copy(resultCreator = resultCreator)

    def withErrorHandlerOverride(errorHandlerOverride: PartialFunction[ErrorWrapper, Result]): RequestHandlerBuilder[InputRaw, Input, Output] =
      copy(errorHandlerOverride = errorHandlerOverride)

    def withAuditing(auditHandler: AuditHandler): RequestHandlerBuilder[InputRaw, Input, Output] =
      copy(auditHandler = Some(auditHandler))

    def createRequestHandler(implicit ec: ExecutionContext): RequestHandler[InputRaw, Input, Output] = {
      val combinedErrorHandling = new ErrorHandling {
        override def errorResultPF: PartialFunction[ErrorWrapper, Result] = errorHandlerOverride.orElse(errorHandling.errorResultPF)
      }

      new RequestHandler(parser, service, combinedErrorHandling, resultCreator, auditHandler)
    }

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
    def withHateoasResult[HData <: HateoasData](
        hateoasFactory: HateoasFactory)(data: (Input, Output) => HData, successStatus: Int = Status.OK)(implicit
        linksFactory: HateoasLinksFactory[Output, HData],
        writes: Writes[HateoasWrapper[Output]]): RequestHandlerBuilder[InputRaw, Input, Output] =
      withResultCreator(ResultCreator.hateoasWrapping(hateoasFactory, successStatus)(data))

  }

}
