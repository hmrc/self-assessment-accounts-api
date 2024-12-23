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

package v3.common.resolvers

import api.controllers.validators.resolvers.Resolver
import api.models.domain.MtdSource
import api.models.errors.{MtdError, SourceFormatError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

object ResolveSource extends Resolver[String, MtdSource] {

  def apply(value: String, maybeError: Option[MtdError], errorPath: Option[String]): Validated[Seq[MtdError], MtdSource] = {
    def useError = maybeError.getOrElse(SourceFormatError).maybeWithExtraPath(errorPath)

    MtdSource.parser
      .lift(value)
      .map(parsed => Valid(parsed))
      .getOrElse(Invalid(List(useError)))
  }

}
