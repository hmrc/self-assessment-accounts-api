/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.retrieveItsaPenalties.model.response

import api.utils.UnitSpec
import api.utils.enums.EnumJsonSpecSupport
import v4.retrieveItsaPenalties.model.response.ExpiryReason.*

class ExpiryReasonSpec extends UnitSpec with EnumJsonSpecSupport {

  testDeserialization[ExpiryReason](
    ("APP", appeal),
    ("FAP", `submission-frequency-change`),
    ("ICR", `obligations-reversed`),
    ("MAN", `hmrc-removed`),
    ("NAT", `natural-expiry`),
    ("NLT", `penalty-removed`),
    ("POC", `expiry-conditions-met`),
    ("RES", `hmrc-reset`)
  )

  testSerialization[ExpiryReason](
    (appeal, "appeal"),
    (`submission-frequency-change`, "submission-frequency-change"),
    (`obligations-reversed`, "obligations-reversed"),
    (`hmrc-removed`, "hmrc-removed"),
    (`natural-expiry`, "natural-expiry"),
    (`penalty-removed`, "penalty-removed"),
    (`expiry-conditions-met`, "expiry-conditions-met"),
    (`hmrc-reset`, "hmrc-reset")
  )

}
