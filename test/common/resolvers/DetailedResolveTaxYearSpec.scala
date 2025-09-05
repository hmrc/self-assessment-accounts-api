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

package common.resolvers

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import shared.models.domain.TaxYear
import shared.models.errors.{MtdError, RuleTaxYearNotEndedError, RuleTaxYearNotSupportedError}
import shared.utils.UnitSpec

class DetailedResolveTaxYearSpec extends UnitSpec {

  "DetailedResolveTaxYear" when {
    "a minimum tax year isn't specified" should {
      "accept a tax year < a reasonable minimum" in {
        val resolveTaxYear                            = DetailedResolveTaxYear()
        val result: Validated[Seq[MtdError], TaxYear] = resolveTaxYear("2010-11")
        result shouldBe Valid(TaxYear.fromMtd("2010-11"))
      }
    }

    "a minimum tax year is specified" should {
      val resolveTaxYear = DetailedResolveTaxYear(maybeMinimumTaxYear = Some(2019))

      "accept a tax year >= the minimum" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolveTaxYear("2018-19")
        result shouldBe Valid(TaxYear.fromMtd("2018-19"))
      }

      "reject a tax year < the minimum" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolveTaxYear("2017-18")
        result shouldBe Invalid(List(RuleTaxYearNotSupportedError))
      }
    }

    Seq(
      (true, "accept", Valid(TaxYear.fromMtd("2090-91"))),
      (false, "reject", Invalid(List(RuleTaxYearNotEndedError)))
    ).foreach { case (allowIncomplete, scenario, expectedResult) =>
      s"allowIncompleteTaxYear is $allowIncomplete" should {
        s"$scenario an incomplete tax year" in {
          val resolveTaxYear = DetailedResolveTaxYear(allowIncompleteTaxYear = allowIncomplete)
          val result         = resolveTaxYear("2090-91")
          result shouldBe expectedResult
        }
      }
    }
  }

}
