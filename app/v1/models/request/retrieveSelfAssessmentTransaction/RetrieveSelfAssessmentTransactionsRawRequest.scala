
package v1.models.request.retrieveSelfAssessmentTransaction

import v1.models.request.RawData

case class RetrieveTransactionsRawRequest(nino: String, from: Option[String], to: Option[String]) extends RawData
