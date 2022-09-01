package api.models.domain

import play.api.libs.json.{JsObject, Writes}

object EmptyJsonBody {
  implicit val writes: Writes[EmptyJsonBody.type] = (_: EmptyJsonBody.type) => JsObject.empty
}
