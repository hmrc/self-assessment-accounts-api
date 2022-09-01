package api.controllers.requestParsers.validators

import api.models.request.RawDataWithDateRange

trait ValidatorWithDateRange[T <: RawDataWithDateRange] extends Validator[T] {
  private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

  private def parameterFormatValidation: T => List[List[MtdError]] = (data: T) => {
    List(
      NinoValidation.validate(data.nino),
      data.from.map(DateFormatValidation.validate(_, FromDateFormatError)).getOrElse(Nil),
      data.to.map(DateFormatValidation.validate(_, ToDateFormatError)).getOrElse(Nil)
    )
  }

  private def parameterRuleValidation: T => List[List[MtdError]] = { data =>
    List(
      MissingParameterValidation.validate(data.from, MissingFromDateError),
      MissingParameterValidation.validate(data.to, MissingToDateError),
      (for {
        from <- data.from
        to   <- data.to
      } yield DateRangeValidation.validate(from, to)).getOrElse(Nil)
    )
  }

  override def validate(data: T): List[MtdError] = {
    run(validationSet, data).distinct
  }

}
