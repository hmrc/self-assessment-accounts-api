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

import api.hateoas.{HateoasFactory, HateoasLinksFactory, HateoasListLinksFactory}
import api.models.hateoas.{HateoasData, HateoasDataBuilder, HateoasWrapper}
import api.models.request.RawData
import cats.Functor
import play.api.http.Status.NO_CONTENT
import play.api.http.{HttpEntity, Status}
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{ResponseHeader, Result, Results}

import scala.language.higherKinds

case class ResultWrapper(httpStatus: Int, body: Option[JsValue]) {

  def toResult: Result = {
    body match {
      case Some(b) => Results.Status(httpStatus)(b)
      case None    => Result(header = ResponseHeader(httpStatus), body = HttpEntity.NoEntity)
    }
  }

}

trait ResultCreatorComponent[InputRaw <: RawData, Output] {

  def resultCreator: ResultCreator[InputRaw, Output]
}

trait ResultCreator[InputRaw <: RawData, Output] {

  def createResult(raw: InputRaw, output: Output): ResultWrapper
}

object ResultCreator {

  def noContent[InputRaw <: RawData, Output]: ResultCreator[InputRaw, Output] =
    (_: InputRaw, _: Output) => ResultWrapper(NO_CONTENT, None)

  def json[InputRaw <: RawData, Output](successStatus: Int = Status.OK)(implicit ws: Writes[Output]): ResultCreator[InputRaw, Output] =
    (_: InputRaw, output: Output) => ResultWrapper(successStatus, Some(Json.toJson(output)))

  def hateoasWrapping[InputRaw <: RawData, Output, HData <: HateoasData](hateoasFactory: HateoasFactory, successStatus: Int = Status.OK)(implicit
      linksFactory: HateoasLinksFactory[Output, HData],
      hateoasDataBuilder: HateoasDataBuilder[InputRaw, Output, HData],
      writes: Writes[HateoasWrapper[Output]]): ResultCreator[InputRaw, Output] =
    (raw: InputRaw, output: Output) => {
      val data: HData = hateoasDataBuilder.dataFor(raw, output)
      val wrapped     = hateoasFactory.wrap(output, data)

      ResultWrapper(successStatus, Some(Json.toJson(wrapped)))
    }

  def hateoasWrappingUsing[InputRaw <: RawData, Output, HData <: HateoasData](hateoasFactory: HateoasFactory, successStatus: Int = Status.OK)(
      data: Output => HData)(implicit
      linksFactory: HateoasLinksFactory[Output, HData],
      writes: Writes[HateoasWrapper[Output]]): ResultCreator[InputRaw, Output] =
    (_: InputRaw, output: Output) => {
      val wrapped = hateoasFactory.wrap(output, data(output))

      ResultWrapper(successStatus, Some(Json.toJson(wrapped)))
    }

  def hateoasListWrapping[InputRaw <: RawData, Output[_]: Functor, I, HData <: HateoasData](hateoasFactory: HateoasFactory,
                                                                                            successStatus: Int = Status.OK)(implicit
      linksFactory: HateoasListLinksFactory[Output, I, HData],
      hateoasDataBuilder: HateoasDataBuilder[InputRaw, Output[I], HData],
      writes: Writes[HateoasWrapper[Output[HateoasWrapper[I]]]]): ResultCreator[InputRaw, Output[I]] =
    (raw: InputRaw, output: Output[I]) => {
      val data: HData = hateoasDataBuilder.dataFor(raw, output)
      val wrapped     = hateoasFactory.wrapList(output, data)

      ResultWrapper(successStatus, Some(Json.toJson(wrapped)))
    }

  def hateoasListWrappingUsing[InputRaw <: RawData, Output[_]: Functor, I, HData <: HateoasData](hateoasFactory: HateoasFactory,
                                                                                                 successStatus: Int = Status.OK)(
      data: Output[I] => HData)(implicit
      linksFactory: HateoasListLinksFactory[Output, I, HData],
      writes: Writes[HateoasWrapper[Output[HateoasWrapper[I]]]]): ResultCreator[InputRaw, Output[I]] =
    (raw: InputRaw, output: Output[I]) => {
      val wrapped = hateoasFactory.wrapList(output, data(output))

      ResultWrapper(successStatus, Some(Json.toJson(wrapped)))
    }

}
