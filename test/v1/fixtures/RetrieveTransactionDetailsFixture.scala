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
import uk.gov.hmrc.domain.Nino
import v1.models.request.retrieveTransactionDetails.RetrieveTransactionDetailsParsedRequest
import v1.models.response.retrieveTransactionDetails.{RetrieveTransactionDetailsResponse, SubItem, TransactionItem}

object RetrieveTransactionDetailsFixture {

  val transactionId = "0001"
  val nino = "AA123456A"

  val requestData = RetrieveTransactionDetailsParsedRequest(Nino(nino), transactionId)

  private val subItem11 = SubItem(subItemId = Some("001"), amount= Some(100.11), clearingDate=Some("2021-01-31"),
    clearingReason= Some("Incoming payment"), paymentAmount=Some(100.11), paymentMethod=Some("BACS RECEIPTS"),
    paymentId=Some("P0101180112-000001"),outgoingPaymentMethod = None)

  private val subItem12 = SubItem(subItemId = Some("002"), amount= Some(-10.11), clearingDate=Some("2021-01-31"),
    clearingReason= Some("Outgoing payment - Paid"), paymentAmount=None, paymentMethod=None,
    paymentId=None, outgoingPaymentMethod = Some("Payable Order Repayment"))

  private val subItem21 = SubItem(subItemId = Some("001"), amount= Some(89.78), clearingDate=Some("2021-01-31"),
    clearingReason= Some("Incoming payment"), paymentAmount=Some(89.78), paymentMethod=Some("BACS RECEIPTS"),
    paymentId=Some("P0101180112-000001"),outgoingPaymentMethod = None)

  val mtdResponse = RetrieveTransactionDetailsResponse(
    transactionItems = Seq(TransactionItem(transactionItemId = Some("0001"), `type` = Some("National Insurance Class 2"),
      taxPeriodFrom = Some("2019-04-06"), taxPeriodTo = Some("2020-04-05"), originalAmount= Some(100.45),
      outstandingAmount = Some(10.23), dueDate = Some("2021-01-31"), paymentMethod = None,
      paymentId = None, subItems = Some(Seq(subItem11, subItem12))),
      TransactionItem(transactionItemId = Some("0002"), `type` = Some("National Insurance Class 4"),
        taxPeriodFrom = Some("2019-04-06"), taxPeriodTo = Some("2020-04-05"), originalAmount= Some(100.23),
        outstandingAmount = Some(10.45), dueDate = Some("2021-01-31"), paymentMethod = None,
        paymentId = None, subItems = Some(Seq(subItem21)))))

  val mtdJson = Json.parse(
    """
      |{
      |   "transactionItems":[
      |      {
      |         "transactionItemId":"0001",
      |         "type":"National Insurance Class 2",
      |         "taxPeriodFrom":"2019-04-06",
      |         "taxPeriodTo":"2020-04-05",
      |         "originalAmount":100.45,
      |         "outstandingAmount":10.23,
      |         "dueDate":"2021-01-31",
      |         "subItems":[
      |            {
      |               "subItemId":"001",
      |               "amount":100.11,
      |               "clearingDate":"2021-01-31",
      |               "clearingReason":"Incoming payment",
      |               "paymentAmount":100.11,
      |               "paymentMethod":"BACS RECEIPTS",
      |               "paymentId":"P0101180112-000001"
      |            },
      |            {
      |               "subItemId":"002",
      |               "amount":-10.11,
      |               "clearingDate":"2021-01-31",
      |               "clearingReason":"Outgoing payment - Paid",
      |               "outgoingPaymentMethod":"Payable Order Repayment"
      |            }
      |         ]
      |      },
      |      {
      |         "transactionItemId":"0002",
      |         "type":"National Insurance Class 4",
      |         "taxPeriodFrom":"2019-04-06",
      |         "taxPeriodTo":"2020-04-05",
      |         "originalAmount":100.23,
      |         "outstandingAmount":10.45,
      |         "dueDate":"2021-01-31",
      |         "subItems":[
      |            {
      |               "subItemId":"001",
      |               "amount":89.78,
      |               "clearingDate":"2021-01-31",
      |               "clearingReason":"Incoming payment",
      |               "paymentAmount":89.78,
      |               "paymentMethod":"BACS RECEIPTS",
      |               "paymentId":"P0101180112-000001"
      |            }
      |         ]
      |      }
      |   ]
      |}
    """.stripMargin)

  val desResponseWithMultipleTransactionItemForCharges: JsValue = Json.parse(
    """
      |{
      |  "transactionItems": [
      |    {
      |      "sapDocumentItemId": "0001",
      |      "type": "National Insurance Class 2",
      |      "taxPeriodFrom": "2019-04-06",
      |      "taxPeriodTo": "2020-04-05",
      |      "originalAmount": 100.45,
      |      "outstandingAmount": 10.23,
      |      "dueDate": "2021-01-31",
      |      "subItems": [
      |        {
      |          "subItemId": "001",
      |          "amount": 100.11,
      |          "clearingDate": "2021-01-31",
      |          "clearingReason": "Incoming payment",
      |          "paymentAmount": 100.11,
      |          "paymentMethod": "BACS RECEIPTS",
      |          "paymentLot": "P0101180112",
      |          "paymentLotItem": "000001"
      |        }
      |      ]
      |    },
      |    {
      |      "sapDocumentItemId": "0002",
      |      "type": "National Insurance Class 4",
      |      "taxPeriodFrom": "2019-04-06",
      |      "taxPeriodTo": "2020-04-05",
      |      "originalAmount": 100.23,
      |      "outstandingAmount": 10.45,
      |      "dueDate": "2021-01-31",
      |      "subItems": [
      |        {
      |          "subItemId": "001",
      |          "amount": 100.11,
      |          "clearingDate": "2021-01-31",
      |          "clearingReason": "Incoming payment",
      |          "paymentAmount": 100.11,
      |          "paymentMethod": "BACS RECEIPTS",
      |          "paymentLot": "P0101180112",
      |          "paymentLotItem": "000001"
      |        }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val mtdResponseWithMultipleTransactionItemForCharges: JsValue = Json.parse(
    """
      |{
      |  "transactionItems": [
      |    {
      |      "transactionItemId": "0001",
      |      "type": "National Insurance Class 2",
      |      "taxPeriodFrom": "2019-04-06",
      |      "taxPeriodTo": "2020-04-05",
      |      "originalAmount": 100.45,
      |      "outstandingAmount": 10.23,
      |      "dueDate": "2021-01-31",
      |      "subItems": [
      |        {
      |          "subItemId": "001",
      |          "amount": 100.11,
      |          "clearingDate": "2021-01-31",
      |          "clearingReason": "Incoming payment",
      |          "paymentAmount": 100.11,
      |          "paymentMethod": "BACS RECEIPTS",
      |          "paymentId": "P0101180112-000001"
      |        }
      |      ]
      |    },
      |    {
      |      "transactionItemId": "0002",
      |      "type": "National Insurance Class 4",
      |      "taxPeriodFrom": "2019-04-06",
      |      "taxPeriodTo": "2020-04-05",
      |      "originalAmount": 100.23,
      |      "outstandingAmount": 10.45,
      |      "dueDate": "2021-01-31",
      |      "subItems": [
      |        {
      |          "subItemId": "001",
      |          "amount": 100.11,
      |          "clearingDate": "2021-01-31",
      |          "clearingReason": "Incoming payment",
      |          "paymentAmount": 100.11,
      |          "paymentMethod": "BACS RECEIPTS",
      |          "paymentId": "P0101180112-000001"
      |        }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val desResponseWithMultipleSubItemForCharge: JsValue = Json.parse(
    """
      |{
      |  "sapDocumentItemId": "0001",
      |  "type": "National Insurance Class 2",
      |  "taxPeriodFrom": "2019-04-06",
      |  "taxPeriodTo": "2020-04-05",
      |  "originalAmount": 100.45,
      |  "outstandingAmount": 10.23,
      |  "dueDate": "2021-01-31",
      |  "subItems": [
      |    {
      |      "subItemId": "001",
      |      "amount": 100.11,
      |      "clearingDate": "2021-01-31",
      |      "clearingReason": "Incoming payment",
      |      "paymentAmount": 100.11,
      |      "paymentMethod": "BACS RECEIPTS",
      |      "paymentLot": "P0101180112",
      |      "paymentLotItem": "000001"
      |    },
      |    {
      |      "subItemId": "002",
      |      "amount": 100.11,
      |      "clearingDate": "2021-01-31",
      |      "clearingReason": "Incoming payment",
      |      "paymentAmount": 100.11,
      |      "paymentMethod": "BACS RECEIPTS",
      |      "paymentLot": "P0101180112",
      |      "paymentLotItem": "000001"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val desResponseWithMultipleSubItemForPayment: JsValue = Json.parse(
    """
      |{
      |  "sapDocumentItemId": "0001",
      |  "type": "Payment on account",
      |  "originalAmount": -5000.00,
      |  "outstandingAmount": 0.00,
      |  "dueDate": "2021-01-31",
      |  "paymentMethod":"BACS RECEIPTS",
      |  "paymentLot": "P0101180112",
      |  "paymentLotItem": "000001",
      |  "subItems":[
      |     {
      |       "subItemId":"001",
      |       "clearingDate":"2021-01-31",
      |       "clearingReason":"Payment allocation",
      |       "paymentAmount": -1100.00
      |     },
      |     {
      |       "subItemId":"002",
      |       "clearingDate":"2021-01-31",
      |       "clearingReason":"Payment allocation",
      |       "paymentAmount": -300.00
      |     }
      |  ]
      |}
    """.stripMargin
  )

  val mtdResponseWithMultipleSubItemForCharge: JsValue = Json.parse(
    """
      |{
      |  "transactionItemId": "0001",
      |  "type": "National Insurance Class 2",
      |  "taxPeriodFrom": "2019-04-06",
      |  "taxPeriodTo": "2020-04-05",
      |  "originalAmount": 100.45,
      |  "outstandingAmount": 10.23,
      |  "dueDate": "2021-01-31",
      |  "subItems": [
      |    {
      |      "subItemId": "001",
      |      "amount": 100.11,
      |      "clearingDate": "2021-01-31",
      |      "clearingReason": "Incoming payment",
      |      "paymentAmount": 100.11,
      |      "paymentMethod": "BACS RECEIPTS",
      |      "paymentId": "P0101180112-000001"
      |    },
      |    {
      |      "subItemId": "002",
      |      "amount": 100.11,
      |      "clearingDate": "2021-01-31",
      |      "clearingReason": "Incoming payment",
      |      "paymentAmount": 100.11,
      |      "paymentMethod": "BACS RECEIPTS",
      |      "paymentId": "P0101180112-000001"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val mtdResponseWithMultipleSubItemForPayment: JsValue = Json.parse(
    """
      |{
      |  "transactionItemId": "0001",
      |  "type": "Payment on account",
      |  "originalAmount": -5000.00,
      |  "outstandingAmount": 0.00,
      |  "dueDate": "2021-01-31",
      |  "paymentMethod":"BACS RECEIPTS",
      |  "paymentId":"P0101180112-000001",
      |  "subItems":[
      |     {
      |       "subItemId":"001",
      |       "clearingDate":"2021-01-31",
      |       "clearingReason":"Payment allocation",
      |       "paymentAmount": -1100.00
      |     },
      |     {
      |       "subItemId":"002",
      |       "clearingDate":"2021-01-31",
      |       "clearingReason":"Payment allocation",
      |       "paymentAmount": -300.00
      |     }
      |  ]
      |}
    """.stripMargin
  )

  val desResponseEmptySubItemsObject: JsValue = Json.parse(
    """
      |{
      |   "transactionItems": [
      |     {
      |       "sapDocumentItemId": "0001",
      |       "type": "National Insurance Class 2",
      |       "taxPeriodFrom": "2019-04-06",
      |       "taxPeriodTo": "2020-04-05",
      |       "originalAmount": 100.45,
      |       "outstandingAmount": 10.23,
      |       "dueDate": "2021-01-31",
      |       "subItems": []
      |     }
      |   ]
      |}
    """.stripMargin
  )

  val subItemResponseCharge: SubItem =
    SubItem(
      subItemId = Some("001"),
      amount = Some(100.11),
      clearingDate = Some("2021-01-31"),
      clearingReason = Some("Incoming payment"),
      outgoingPaymentMethod = None,
      paymentAmount = Some(100.11),
      paymentMethod = Some("BACS RECEIPTS"),
      paymentId = Some("P0101180112-000001")
    )



  val transactionItemResponseCharge: TransactionItem =
    TransactionItem(
      transactionItemId = Some("0001"),
      `type` = Some("National Insurance Class 2"),
      taxPeriodFrom = Some("2019-04-06"),
      taxPeriodTo = Some("2020-04-05"),
      originalAmount = Some(100.45),
      outstandingAmount = Some(10.23),
      dueDate = Some("2021-01-31"),
      paymentMethod = None,
      paymentId = None,
      subItems = Some(Seq(subItemResponseCharge))
    )

  val retrieveTransactionDetailsResponseCharge: RetrieveTransactionDetailsResponse =
    RetrieveTransactionDetailsResponse(
      transactionItems = Seq(transactionItemResponseCharge)
    )

  val retrieveSubItemsResponseCharge: TransactionItem =
    transactionItemResponseCharge.copy(
      subItems = Some(
        Seq(
          subItemResponseCharge
        )
      )
    )

  val subItemResponseCharge2: SubItem =
    SubItem(
      subItemId = Some("002"),
      amount = Some(100.11),
      clearingDate = Some("2021-01-31"),
      clearingReason = Some("Incoming payment"),
      outgoingPaymentMethod = None,
      paymentAmount = Some(100.11),
      paymentMethod = Some("BACS RECEIPTS"),
      paymentId = Some("P0101180112-000001")
    )

  val subItem2ResponseCharge: SubItem =
    SubItem(
      subItemId = Some("001"),
      amount = Some(100.11),
      clearingDate = Some("2021-01-31"),
      clearingReason = Some("Incoming payment"),
      outgoingPaymentMethod = None,
      paymentAmount = Some(100.11),
      paymentMethod = Some("BACS RECEIPTS"),
      paymentId = Some("P0101180112-000001")
    )

  val transactionItemResponseCharge2: TransactionItem =
    TransactionItem(
      transactionItemId = Some("0002"),
      `type` = Some("National Insurance Class 4"),
      taxPeriodFrom = Some("2019-04-06"),
      taxPeriodTo = Some("2020-04-05"),
      originalAmount = Some(100.23),
      outstandingAmount = Some(10.45),
      dueDate = Some("2021-01-31"),
      paymentMethod = None,
      paymentId = None,
      subItems = Some(Seq(subItemResponseCharge2))
    )

  val multipleTransactionItemResponseCharge: TransactionItem =
    TransactionItem(
      transactionItemId = Some("0002"),
      `type` = Some("National Insurance Class 4"),
      taxPeriodFrom = Some("2019-04-06"),
      taxPeriodTo = Some("2020-04-05"),
      originalAmount = Some(100.23),
      outstandingAmount = Some(10.45),
      dueDate = Some("2021-01-31"),
      paymentMethod = None,
      paymentId = None,
      subItems = Some(Seq(subItem2ResponseCharge))
    )

  val retrieveTransactionDetailsResponseChargeMultiple: RetrieveTransactionDetailsResponse =
    RetrieveTransactionDetailsResponse(
      transactionItems = Seq(
        transactionItemResponseCharge,
        multipleTransactionItemResponseCharge
      )
    )

  val retrieveSubItemsResponseChargeMultiple: TransactionItem =
    transactionItemResponseCharge.copy(
      subItems = Some(
        Seq(
          subItemResponseCharge,
          subItemResponseCharge2
        )
      )
    )

  val subItemResponsePayment: SubItem =
    SubItem(
      subItemId = Some("001"),
      amount = None,
      clearingDate = Some("2021-01-31"),
      clearingReason = Some("Payment allocation"),
      outgoingPaymentMethod = None,
      paymentAmount = Some(-1100),
      paymentMethod = None,
      paymentId = None
    )

  val transactionItemResponsePayment: TransactionItem =
    TransactionItem(
      transactionItemId = Some("0001"),
      `type` = Some("Payment on account"),
      taxPeriodFrom = None,
      taxPeriodTo = None,
      originalAmount = Some(-5000),
      outstandingAmount = Some(0),
      dueDate = Some("2021-01-31"),
      paymentMethod = Some("BACS RECEIPTS"),
      paymentId = Some("P0101180112-000001"),
      subItems = Some(Seq(subItemResponsePayment))
    )

  val retrieveTransactionDetailsResponsePayment: RetrieveTransactionDetailsResponse =
    RetrieveTransactionDetailsResponse(
      transactionItems = Seq(transactionItemResponsePayment)
    )

  val subItemResponsePayment2: SubItem =
    SubItem(
      subItemId = Some("002"),
      amount = None,
      clearingDate = Some("2021-01-31"),
      clearingReason = Some("Payment allocation"),
      outgoingPaymentMethod = None,
      paymentAmount = Some(-300.00),
      paymentMethod = None,
      paymentId = None
    )

  val transactionItemResponsePayment2: TransactionItem =
    TransactionItem(
      transactionItemId = Some("0002"),
      `type` = Some("Payment on account"),
      taxPeriodFrom = Some("2019-04-06"),
      taxPeriodTo = Some("2020-04-05"),
      originalAmount = Some(100.45),
      outstandingAmount = Some(10.23),
      dueDate = Some("2021-01-31"),
      paymentMethod = Some("BACS RECEIPTS"),
      paymentId = Some("P0101180113-000002"),
      subItems = Some(Seq(subItemResponsePayment))
    )

  val retrieveTransactionDetailsResponsePaymentMultiple: RetrieveTransactionDetailsResponse =
    RetrieveTransactionDetailsResponse(
      transactionItems = Seq(
        transactionItemResponsePayment,
        transactionItemResponsePayment2
      )
    )

  val retrieveSubItemsResponsePaymentMultiple: TransactionItem =
    transactionItemResponsePayment.copy(
      subItems = Some(
        Seq(
          subItemResponsePayment,
          subItemResponsePayment2
        )
      )
    )
}
