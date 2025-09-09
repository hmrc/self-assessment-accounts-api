/*
 * Copyright 2025 HM Revenue & Customs
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

package config

import org.apache.commons.lang3.BooleanUtils
import play.api.Configuration
import play.api.mvc.Request
import shared.config.{FeatureSwitches, SharedAppConfig}

case class SaAccountsFeatureSwitches private[config] (protected val featureSwitchConfig: Configuration) extends FeatureSwitches {

  def isTemporalValidationEnabled(using request: Request[?]): Boolean = {
    if (isEnabled("allowTemporalValidationSuspension")) {
      request.headers.get("suspend-temporal-validations").forall(!BooleanUtils.toBoolean(_))
    } else {
      true
    }
  }

}

object SaAccountsFeatureSwitches {
  def apply()(using appConfig: SharedAppConfig): SaAccountsFeatureSwitches = SaAccountsFeatureSwitches(appConfig.featureSwitchConfig)
}
