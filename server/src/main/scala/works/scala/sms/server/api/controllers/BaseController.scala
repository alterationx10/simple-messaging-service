package works.scala.sms.server.api.controllers

import caliban.GraphQL
import works.scala.sms.server.api.models.ApiError
import zio.*
import zio.http.endpoint.*

trait EndpointAggregator:
  val endpoints: List[Endpoint[?, ?, ?, EndpointMiddleware.None]] = List.empty

trait BaseController:
  val routes: List[Routes[Any, ApiError, EndpointMiddleware.None]] = List.empty
  val graphs: List[GraphQL[Any]]                                   = List.empty

  extension [T](task: Task[T])
    def handleErrors: ZIO[Any, ApiError, T]           =
      task.mapError(e => ApiError(e.getMessage()))
    def handleErrorsEither: Task[Either[ApiError, T]] =
      handleErrors.either
