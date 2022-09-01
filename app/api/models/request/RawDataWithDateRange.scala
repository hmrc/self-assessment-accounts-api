package api.models.request

trait RawDataWithDateRange extends RawData {
  val nino: String
  val from: Option[String]
  val to: Option[String]
}
