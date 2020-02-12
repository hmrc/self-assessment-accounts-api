package v1.models.request.retrieveAllocations

import uk.gov.hmrc.domain.Nino

case class RetrieveAllocationsParsedRequest(nino: Nino, paymentLot: String, paymentLotItem: String)
