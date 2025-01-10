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

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import common.errors.ChargeReferenceFormatError
import shared.controllers.validators.resolvers.ResolverSupport
import shared.models.errors.MtdError
import v3.common.models.ChargeReference

case class ResolveChargeReference(error: Option[MtdError], path: Option[String]) extends ResolverSupport {

  private val chargeReferenceRegex = "^[A-Za-z]{2}[0-9]{12}$".r

  def apply(value: String): Validated[Seq[MtdError], ChargeReference] = resolver(value)

  val resolver: Resolver[String, ChargeReference] = value => {
    if (chargeReferenceRegex.matches(value))
      Valid(ChargeReference(value))
    else
      Invalid(List(error.getOrElse(ChargeReferenceFormatError).maybeWithExtraPath(path)))
  }


}

