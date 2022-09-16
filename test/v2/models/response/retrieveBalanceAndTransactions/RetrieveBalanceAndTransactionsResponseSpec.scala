/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v2.models.response.retrieveBalanceAndTransactions

import play.api.libs.json.Json
import support.UnitSpec
import v2.fixtures.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsResponseFixture

class RetrieveBalanceAndTransactionsResponseSpec extends UnitSpec with RetrieveBalanceAndTransactionsResponseFixture {

  "RetrieveBalanceAndTransactionsResponse" when {
    "written to MTD JSON" must {
      "work" in {
        Json.toJson(responseModel) shouldBe responseMtdJson
      }
    }

    "read from downstream" must {
      "work" in {
        responseDownstreamJson.as[RetrieveBalanceAndTransactionsResponse] shouldBe responseModel
      }
    }
  }

}
