package works.scala.sms.server.api.controllers

import com.rabbitmq.client.Connection
import works.scala.sms.server.api.models.{ApiError, MessageConsumeResponse}
import works.scala.sms.server.api.services.ConsumerService
import zio.http.ChannelEvent.*
import zio.http.*
import zio.stream.*
import zio.*
import zio.Duration.*
import zio.http.ChannelEvent.UserEvent.HandshakeComplete
import works.scala.sms.server.extensions.Extensions.*
import works.scala.sms.server.rmq.RMQ
import zio.http.codec.HttpCodec
import zio.http.codec.HttpCodec.{Status, string}
import zio.http.endpoint.*
import HttpCodec.*
import works.scala.sms.server.api.controllers.ConsumerController.consumeOne
import zio.http.endpoint.EndpointMiddleware.None

import java.util.UUID
import scala.language.postfixOps

object ConsumerController extends EndpointAggregator:

  val layer: ZLayer[ConsumerService & Connection, Nothing, ConsumerController] =
    ZLayer {
      for {
        svc  <- ZIO.service[ConsumerService]
        conn <- ZIO.service[Connection]
      } yield ConsumerController(svc, conn)
    }

  val consumeOne =
    Endpoint
      .get("messages" / string("subscription"))
      .out[MessageConsumeResponse]
      .outError[ApiError](Status.InternalServerError)

  override val endpoints: List[Endpoint[_, _, _, None]] = List(
    consumeOne
  )

case class ConsumerController(
    consumerService: ConsumerService,
    rmqConnection: Connection
) extends BaseController:

  val socketApp = Http.collectZIO[Request] {
    case req @ Method.GET -> Root / "stream" / subscription =>
      consumerService
        .handleWs(
          subscription,
          req.url.queryParams
            .get("preFetch")
            .map(_.mkString.toInt)
            .getOrElse(1),
          req.url.queryParams
            .get("autoAck")
            .map(_.mkString.toBoolean)
            .getOrElse(false)
        )
        .toResponse
  }

  override val routes: List[Routes[Any, ApiError, EndpointMiddleware.None]] =
    List(
      consumeOne.implement(sub =>
        consumerService.ackingConsume(sub).handleErrors
      )
    )
