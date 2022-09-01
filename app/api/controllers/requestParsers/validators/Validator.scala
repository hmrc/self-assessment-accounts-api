package api.controllers.requestParsers.validators

import api.models.request.RawData

trait Validator[A <: RawData] {

  type ValidationLevel[T] = T => List[MtdError]

  def validate(data: A): List[MtdError]

  def run(validationSet: List[A => List[List[MtdError]]], data: A): List[MtdError] = {

    validationSet match {
      case Nil => List()
      case thisLevel :: remainingLevels =>
        thisLevel(data).flatten match {
          case x if x.isEmpty  => run(remainingLevels, data)
          case x if x.nonEmpty => x
        }
    }
  }

  def flattenErrors(errors: List[List[MtdError]]): List[MtdError] = {
    errors.flatten
      .groupBy(_.message)
      .map { case (_, errors) =>
        val baseError = errors.head.copy(paths = Some(Seq.empty[String]))
        errors.fold(baseError)((error1, error2) => {
          val paths: Option[Seq[String]] = for {
            error1Paths <- error1.paths
            error2Paths <- error2.paths
          } yield {
            error1Paths ++ error2Paths
          }
          error1.copy(paths = paths)
        })
      }
      .toList
  }

}
