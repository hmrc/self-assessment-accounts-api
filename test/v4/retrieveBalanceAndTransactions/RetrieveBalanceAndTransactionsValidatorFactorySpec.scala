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

package v4.retrieveBalanceAndTransactions

import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v4.retrieveBalanceAndTransactions.def1.Def1_RetrieveBalanceAndTransactionsValidator
import v4.retrieveBalanceAndTransactions.def1.model.RequestFixture._

class RetrieveBalanceAndTransactionsValidatorFactorySpec extends UnitSpec with JsonErrorValidators {

  private val validatorFactory = new RetrieveBalanceAndTransactionsValidatorFactory

  "running a validation" should {
    "return the parsed domain object" when {
      "given a valid request" in {
        val result = validatorFactory.validator(
          validNino,
          Some(validDocNumber),
          Some(validFromDate),
          Some(validToDate),
          Some("true"),
          Some("true"),
          Some("true"),
          Some("true"),
          Some("true"),
          Some("true"))

        result shouldBe a[Def1_RetrieveBalanceAndTransactionsValidator]

      }
    }

  }

}
