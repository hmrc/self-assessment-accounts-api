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

package api.controllers.requestParsers.validators.validations

import api.models.errors.{DocNumberFormatError, MtdError}

object DocNumberValidation {

  val MAX_LENGTH = 12

  def validate(docNumber: Option[String]): List[MtdError] =
    docNumber
      .map { docNumber =>
        val invalid = docNumber.isEmpty || docNumber.length > MAX_LENGTH

        if (invalid) List(DocNumberFormatError) else Nil
      }
      .getOrElse(Nil)

}
