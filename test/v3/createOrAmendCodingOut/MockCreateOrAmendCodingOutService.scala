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

package v3.createOrAmendCodingOut

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import shared.controllers.RequestContext
import shared.models.errors.ErrorWrapper
import shared.models.outcomes.ResponseWrapper
import v3.createOrAmendCodingOut.model.request.CreateOrAmendCodingOutRequestData

import scala.concurrent.{ExecutionContext, Future}

trait MockCreateOrAmendCodingOutService extends TestSuite with MockFactory {

  val mockCreateOrAmendCodingOutService: CreateOrAmendCodingOutService = mock[CreateOrAmendCodingOutService]

  object MockCreateOrAmendCodingOutService {

    def amend(request: CreateOrAmendCodingOutRequestData): CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[Unit]]]] = {
      (mockCreateOrAmendCodingOutService
        .amend(_: CreateOrAmendCodingOutRequestData)(_: RequestContext, _: ExecutionContext))
        .expects(request, *, *)
    }

  }

}
