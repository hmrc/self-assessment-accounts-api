/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.fixtures

import play.api.libs.json.{JsValue, Json}
import v1.models.response.listPayments.{ListPaymentsResponse, Payment}

object ListPaymentsFixture {

  val desSuccessResponse: String =
    """
      |{
      |   "idType":"NINO",
      |   "idValue":"AB123456C",
      |   "regimeType":"ITSA",
      |   "businessPartner":"1122334455",
      |   "paymentDetails":[
      |      {
      |         "paymentLot":"123456789012",
      |         "paymentLotItem":"123456",
      |         "paymentReference":"1594",
      |         "paymentAmount":11.99,
      |         "paymentMethod":"A",
      |         "valueDate":"2019-02-26",
      |         "sapClearingDocsDetails":[
      |            {
      |               "chargeReference":"XM002610011594",
      |               "sapDocNumber":"1040000872",
      |               "sapDocItem":"0026",
      |               "sapDocSubItem":"1",
      |               "periodKey":"16RL",
      |               "periodKeyDescription":"2016/17 \"month 12\" RTI",
      |               "taxPeriodStartDate":"2010-03-27",
      |               "taxPeriodEndDate":"2010-06-27",
      |               "dueDate":"2010-07-27",
      |               "chargeType":"1481",
      |               "mainType":"0001",
      |               "amount":12345678912.02,
      |               "clearedAmount":345678912.02,
      |               "mainTransaction":"1234",
      |               "subTransaction":"5678",
      |               "contractAccountCategory":"02",
      |               "contractAccount":"ABC",
      |               "contractObjectType":"ABCD",
      |               "contractObject":"00000003000000002757"
      |            },
      |            {
      |               "chargeReference":"XM002610011560",
      |               "sapDocNumber":"1040000899",
      |               "sapDocItem":"1562",
      |               "sapDocSubItem":"2",
      |               "periodKey":"16RR",
      |               "periodKeyDescription":"2016/17 month 10 RTI",
      |               "taxPeriodStartDate":"2010-04-21",
      |               "taxPeriodEndDate":"2010-09-20",
      |               "dueDate":"2010-11-29",
      |               "chargeType":"1489",
      |               "mainType":"0099",
      |               "amount":12345678999.92,
      |               "clearedAmount":345678992.92,
      |               "mainTransaction":"1299",
      |               "subTransaction":"5609",
      |               "contractAccountCategory":"99",
      |               "contractAccount":"ABS",
      |               "contractObjectType":"ABED",
      |               "contractObject":"00000003000000002799"
      |            }
      |         ]
      |      },
      |      {
      |         "paymentLot":"223456789012",
      |         "paymentLotItem":"123456",
      |         "paymentReference":"XM002610011789",
      |         "paymentAmount":12.99,
      |         "paymentMethod":"B",
      |         "valueDate":"2019-02-27",
      |         "sapClearingDocsDetails":[
      |            {
      |               "chargeReference":"XM002610011595",
      |               "sapDocNumber":"1040000672",
      |               "sapDocItem":"6599",
      |               "sapDocSubItem":"1",
      |               "periodKey":"16TL",
      |               "periodKeyDescription":"2016/17 month 09 RTI",
      |               "taxPeriodStartDate":"2012-03-27",
      |               "taxPeriodEndDate":"2012-06-27",
      |               "dueDate":"2012-07-27",
      |               "chargeType":"1561",
      |               "mainType":"0991",
      |               "amount":6345678912.02,
      |               "clearedAmount":245678912.02,
      |               "mainTransaction":"9234",
      |               "subTransaction":"3678",
      |               "contractAccountCategory":"06",
      |               "contractAccount":"PBC",
      |               "contractObjectType":"ZBCD",
      |               "contractObject":"00000003000000033757"
      |            }
      |         ]
      |      }
      |   ]
      |}
      |""".stripMargin

  val desSuccessResponseNoPayments: String =
    """
      |{
      |   "idType":"NINO",
      |   "idValue":"AB123456C",
      |   "regimeType":"ITSA",
      |   "businessPartner":"1122334455",
      |   "paymentDetails": [
      |   ]
      |}
      |""".stripMargin

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |  "payments": [
      |    {
      |      "id": "123456789012-123456",
      |      "amount": 11.99,
      |      "method": "A",
      |      "transactionDate": "2019-02-26"
      |    },
      |    {
      |      "id": "223456789012-123456",
      |      "amount": 12.99,
      |      "method": "B",
      |      "transactionDate": "2019-02-27"
      |    }
      |  ]
      |}""".stripMargin)

  val mtdResponseObj = ListPaymentsResponse(
    payments = Seq(Payment(Some("123456789012-123456"), Some(BigDecimal(11.99)), Some("A"), Some("2019-02-26")),
      Payment(Some("223456789012-123456"), Some(BigDecimal(12.99)), Some("B"), Some("2019-02-27"))))

  val emptyResponseObj = ListPaymentsResponse(Seq.empty[Payment])
}
