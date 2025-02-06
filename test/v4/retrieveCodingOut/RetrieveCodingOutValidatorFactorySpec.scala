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

package v4.retrieveCodingOut

import config.MockSaAccountsConfig
import shared.utils.UnitSpec
import v4.retrieveCodingOut.def1.Def1_RetrieveCodingOutValidator

class RetrieveCodingOutValidatorFactorySpec extends UnitSpec with MockSaAccountsConfig {

  private val validNino = "AA123456A"
  private val validTaxYear = "2019-20"
  private val source = Some("hmrcHeld")

  private val validatorFactory = new RetrieveCodingOutValidatorFactory

  "running a validation" should {
    "return the parsed domain object" when {
      "given a valid request" in {
        MockedSaAccountsConfig.minimumPermittedTaxYear returns 2020

        val result = validatorFactory.validator(validNino, validTaxYear, source, mockSaAccountsConfig)
        result shouldBe a[Def1_RetrieveCodingOutValidator]

      }
    }
  }
}
