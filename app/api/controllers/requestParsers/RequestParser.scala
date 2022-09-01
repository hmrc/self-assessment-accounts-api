package api.controllers.requestParsers

import api.controllers.requestParsers.validators.Validator
import api.models.errors.{BadRequestError, ErrorWrapper}
import utils.Logging

trait RequestParser[Raw <: RawData, Request] extends Logging {

  val validator: Validator[Raw]

  protected def requestFor(data: Raw): Request

  def parseRequest(data: Raw)(implicit correlationId: String): Either[ErrorWrapper, Request] = {
    validator.validate(data) match {
      case Nil =>
        logger.info(
          "[RequestParser][parseRequest] " +
            s"Validation successful for the request with CorrelationId: $correlationId")
        Right(requestFor(data))
      case err :: Nil =>
        logger.warn(
          "[RequestParser][parseRequest] " +
            s"Validation failed with ${err.code} error for the request with CorrelationId: $correlationId")
        Left(ErrorWrapper(correlationId, err, None))
      case errs =>
        logger.warn(
          "[RequestParser][parseRequest] " +
            s"Validation failed with ${errs.map(_.code).mkString(",")} error for the request with CorrelationId: $correlationId")
        Left(ErrorWrapper(correlationId, BadRequestError, Some(errs)))
    }
  }

}
