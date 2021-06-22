/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators

import config.AppConfig
import mocks.MockAppConfig
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.json.{JsObject, JsPath, Json}
import support.UnitSpec
import utils.CurrentDateTime
import v1.mocks.MockCurrentDateTime
import v1.models.errors._
import v1.models.request.createOrAmendCodingOut.CreateOrAmendCodingOutRawRequest

class CreateOrAmendCodingOutValidatorSpec extends UnitSpec {

  private val validNino           = "AA111111A"
  private val validTaxYear        = "2021-22"
  private val invalidNino         = "not a nino"
  private val invalidTaxYear      = "not a tax year"
  private val invalidTaxYearRange = "2021-25"

  private val validJson = Json.parse(
    """
      |{
      |  "taxCodeComponents": {
      |    "payeUnderpayment": [
      |      {
      |        "amount": 123.45,
      |        "id": 12345
      |      }
      |    ],
      |    "selfAssessmentUnderpayment": [
      |      {
      |        "amount": 123.45,
      |        "id": 12345
      |      }
      |    ],
      |    "debt": [
      |      {
      |        "amount": 123.45,
      |        "id": 12345
      |      }
      |    ],
      |    "inYearAdjustment": {
      |      "amount": 123.45,
      |      "id": 12345
      |    }
      |  }
      |}
    """.stripMargin
  ).as[JsObject]

  private val invalidJson = Json.parse(
    """
      |{
      |  "taxCodeComponents": {
      |    "payeUnderpayment": [
      |      {
      |        "amount": 123.455,
      |        "id": -12345
      |      }
      |    ],
      |    "selfAssessmentUnderpayment": [
      |      {
      |        "amount": 123498394893843.4,
      |        "id": 12345.35
      |      }
      |    ],
      |    "debt": [
      |      {
      |        "amount": -123.45,
      |        "id": 123453456789098765434567897654567890987654
      |      }
      |    ],
      |    "inYearAdjustment": {
      |      "amount": 11111111111111111111111111111123.45,
      |      "id": -12345
      |    }
      |  }
      |}
    """.stripMargin
  )

  private val emptyJson = Json.parse(
    """
      |{}
    """.stripMargin
  )

  private def removeFieldFromJson(json: JsObject, field: String): JsObject = {
    val path = JsPath \ "taxCodeComponents" \ field
    path.prune(json).get
  }

  class Test extends MockCurrentDateTime with MockAppConfig {

    implicit val dateTimeProvider: CurrentDateTime = mockCurrentDateTime
    val dateTimeFormatter: DateTimeFormatter       = DateTimeFormat.forPattern("yyyy-MM-dd")
    implicit val appConfig: AppConfig              = mockAppConfig
    val validator                                  = new CreateOrAmendCodingOutValidator()

    MockCurrentDateTime.getCurrentDate
      .returns(DateTime.parse("2022-07-11", dateTimeFormatter))
      .anyNumberOfTimes()

    MockAppConfig.minimumPermittedTaxYear returns 2022
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, validTaxYear, validJson)) shouldBe Nil
      }

      "a valid request is supplied missing payeUnderpayment" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, validTaxYear, removeFieldFromJson(validJson, "payeUnderpayment"))) shouldBe Nil
      }

      "a valid request is supplied missing selfAssessmentUnderpayment" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, validTaxYear, removeFieldFromJson(validJson, "selfAssessmentUnderpayment"))) shouldBe Nil
      }

      "a valid request is supplied missing debt" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, validTaxYear, removeFieldFromJson(validJson, "debt"))) shouldBe Nil
      }

      "a valid request is supplied missing inYearAdjustment" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, validTaxYear, removeFieldFromJson(validJson, "inYearAdjustment"))) shouldBe Nil
      }
    }

    "return a NinoFormatError" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(invalidNino, validTaxYear, validJson)) shouldBe List(NinoFormatError)
      }
    }

    "return a TaxYearFormatError" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, invalidTaxYear, validJson)) shouldBe List(TaxYearFormatError)
      }
    }

    "return a RuleIncorrectOrEmptyBodyError" when {
      "an empty body is supplied" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, validTaxYear, emptyJson)) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "an empty taxCodeComponents is supplied" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, validTaxYear, Json.obj("taxCodeComponents" -> Json.obj()))) shouldBe List(
          RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/taxCodeComponents"))))
      }
      "sequences inside taxCodeComponents are empty" in new Test {
        validator.validate(
          CreateOrAmendCodingOutRawRequest(
            validNino,
            validTaxYear,
            Json.parse("""
              |{
              |  "taxCodeComponents": {
              |    "payeUnderpayment": [],
              |    "selfAssessmentUnderpayment": [],
              |    "debt": []
              |  }
              |}
              |""".stripMargin)
          )) shouldBe List(
          RuleIncorrectOrEmptyBodyError.copy(
            paths = Some(
              Seq(
                "/taxCodeComponents/payeUnderpayment",
                "/taxCodeComponents/selfAssessmentUnderpayment",
                "/taxCodeComponents/debt"
              ))))
      }
    }

    "return a RuleTaxYearRangeInvalidError" when {
      "the tax year range is invalid" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, invalidTaxYearRange, validJson)) shouldBe List(
          RuleTaxYearRangeInvalidError
        )
      }
    }

    "return a RuleTaxYearNotSupportedError" when {
      "the tax year supplied is not supported" in new Test  {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, "2020-21", validJson)) shouldBe List(RuleTaxYearNotSupportedError)
      }
    }

    "return a RuleTaxYearNotEndedError" when {
      "the tax year supplied has not ended" in new Test  {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, "2022-23", validJson)) shouldBe List(RuleTaxYearNotEndedError)
      }
    }

    "return NinoFormatError and TaxYearFormatError" when {
      "an invalid tax year and an invalid nino is supplied" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(invalidNino, invalidTaxYear, validJson)) shouldBe List(
          NinoFormatError,
          TaxYearFormatError
        )
      }
    }

    "return ValueFormatError and IdFormatError" when {
      "a request with invalid fields is supplied" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, validTaxYear, invalidJson)) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq(
            "/taxCodeComponents/payeUnderpayment/0/amount",
            "/taxCodeComponents/selfAssessmentUnderpayment/0/amount",
            "/taxCodeComponents/debt/0/amount",
            "/taxCodeComponents/inYearAdjustment/amount"
          ))),
          IdFormatError.copy(paths = Some(Seq(
            "/taxCodeComponents/payeUnderpayment/0/id",
            "/taxCodeComponents/selfAssessmentUnderpayment/0/id",
            "/taxCodeComponents/debt/0/id",
            "/taxCodeComponents/inYearAdjustment/id"
          )))
        )
      }
    }
  }
}