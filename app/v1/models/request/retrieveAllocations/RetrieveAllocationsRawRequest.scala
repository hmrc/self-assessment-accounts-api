package v1.models.request.retrieveAllocations

import v1.models.request.RawData

case class RetrieveAllocationsRawRequest(nino: String, paymentId: String) extends RawData
