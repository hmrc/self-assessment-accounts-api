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

package config.rewriters

import config.MockAppConfig
import com.github.jknack.handlebars.HandlebarsException
import support.UnitSpec

class EndpointSummaryGroupRewriterSpec extends UnitSpec with MockAppConfig {

  val rewriter = new EndpointSummaryGroupRewriter(mockAppConfig)

  "EndpointSummaryGroupRewriter" when {
    val checkAndRewrite = rewriter.rewriteGroupedEndpointSummaries

    "checking if rewrite is needed" should {
      "indicate rewrite needed for grouped endpoints yaml file" in {
        val result = checkAndRewrite.check("any-version", "coding_out.yaml")
        result shouldBe true
      }

      "indicate rewrite not needed for non-yaml file" in {
        val result = checkAndRewrite.check("any-version", "file.json")
        result shouldBe false
      }

      "indicate rewrite not needed for application.yaml file" in {
        val result = checkAndRewrite.check("any-version", "application.yaml")
        result shouldBe false
      }
    }

    "rewrite" should {
      "return the rewritten summaries when the 'maybeTestOnly' helper is present" in {
        MockAppConfig.endpointReleasedInProduction("2.0", "coding-out-create-and-amend") returns false
        MockAppConfig.endpointReleasedInProduction("2.0", "coding-out-retrieve") returns true
        MockAppConfig.endpointReleasedInProduction("2.0", "coding-out-delete") returns false

        val yaml =
          """
                  |put:
                  |  $ref: "./coding_out_create_and_amend.yaml"
                  |  summary: Create and Amend Coding Out{{#maybeTestOnly "2.0 coding-out-create-and-amend"}}{{/maybeTestOnly}}
                  |  security:
                  |    - User-Restricted:
                  |        - write:self-assessment
                  |
                  |
                  |get:
                  |  $ref: "./coding_out_retrieve.yaml"
                  |  summary: Retrieve Coding Out{{#maybeTestOnly "2.0 coding-out-retrieve"}}{{/maybeTestOnly}}
                  |  security:
                  |    - User-Restricted:
                  |        - read:self-assessment
                  |  parameters:
                  |    - $ref: './common/queryParameters.yaml#/components/parameters/source'
                  |
                  |delete:
                  |  $ref: "./coding_out_delete.yaml"
                  |  summary: Delete Coding Out{{#maybeTestOnly "2.0 coding-out-delete"}}{{/maybeTestOnly}}
                  |  security:
                  |    - User-Restricted:
                  |        - write:self-assessment
                  |
                  |""".stripMargin

        val expected =
          """
                  |put:
                  |  $ref: "./coding_out_create_and_amend.yaml"
                  |  summary: Create and Amend Coding Out [test only]
                  |  security:
                  |    - User-Restricted:
                  |        - write:self-assessment
                  |
                  |
                  |get:
                  |  $ref: "./coding_out_retrieve.yaml"
                  |  summary: Retrieve Coding Out
                  |  security:
                  |    - User-Restricted:
                  |        - read:self-assessment
                  |  parameters:
                  |    - $ref: './common/queryParameters.yaml#/components/parameters/source'
                  |
                  |delete:
                  |  $ref: "./coding_out_delete.yaml"
                  |  summary: Delete Coding Out [test only]
                  |  security:
                  |    - User-Restricted:
                  |        - write:self-assessment
                  |
                  |""".stripMargin

        val result = checkAndRewrite.rewrite(path = "/public/api/conf/2.0", filename = "coding_out.yaml", yaml)
        result shouldBe expected
      }

      "return the unchanged yaml when the 'maybeTestOnly' is not present" in {
        val yaml =
          """
            |put:
            |  $ref: "./coding_out_create_and_amend.yaml"
            |  summary: Create or Amend Coding Out Underpayments and Debt Amounts
            |  security:
            |    - User-Restricted:
            |        - write:self-assessment
            |
            |
            |get:
            |  $ref: "./coding_out_retrieve.yaml"
            |  summary: Retrieve Coding Out Status
            |  security:
            |    - User-Restricted:
            |        - read:self-assessment
            |  parameters:
            |    - $ref: './common/queryParameters.yaml#/components/parameters/source'
            |
            |delete:
            |  $ref: "./coding_out_delete.yaml"
            |  summary: Delete Coding Out
            |  security:
            |    - User-Restricted:
            |        - write:self-assessment
            |
            |""".stripMargin

        val result = checkAndRewrite.rewrite("/public/api/conf/2.0", "coding_out.yaml", yaml)
        result shouldBe yaml

      }

      "throw an exception when invalid endpoint details are provided" in {
        val endpointDetails = "invalidEndpointDetails"
        val yaml =
          s"""
             |put:
             |  $$ref: "./coding_out_create_and_amend.yaml"
             |  summary: Create and Amend Coding Out{{#maybeTestOnly "$endpointDetails"}}{{/maybeTestOnly}}
             |  security:
             |    - User-Restricted:
             |        - write:self-assessment
             |
             |""".stripMargin

        val exception = intercept[HandlebarsException] {
          checkAndRewrite.rewrite("/public/api/conf/1.0", "coding_out.yaml", yaml)
        }

        val cause = exception.getCause
        cause shouldBe a[IllegalArgumentException]
        cause.getMessage shouldBe
          s"Invalid endpoint details format: '$endpointDetails'. The endpoint details should consist of two space-separated parts: version and name."
      }
    }
  }

}
