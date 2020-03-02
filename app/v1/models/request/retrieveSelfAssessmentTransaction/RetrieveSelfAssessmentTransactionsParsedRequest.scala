
package v1.models.request.retrieveSelfAssessmentTransaction

import uk.gov.hmrc.domain.Nino

case class RetrieveTransactionsParsedRequest(nino: Nino, from: String, to: String)
