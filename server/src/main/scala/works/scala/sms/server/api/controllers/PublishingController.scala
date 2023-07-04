package works.scala.sms.server.api.controllers

import works.scala.sms.server.api.models.{
  ApiError,
  PublishMessageRequest,
  PublishMessageResponse
}
import works.scala.sms.server.api.services.PublishingService
import zio.*
import zio.http.*
import zio.http.codec.HttpCodec
import zio.http.codec.HttpCodec.{Status, string}
import zio.http.endpoint.*
import HttpCodec.*
import works.scala.sms.server.api.controllers.PublishingController.publishMessage
import zio.http.endpoint.EndpointMiddleware.None

object PublishingController extends EndpointAggregator:
  val layer: ZLayer[PublishingService, Nothing, PublishingController] = ZLayer {
    ZIO.serviceWith[PublishingService](PublishingController.apply)
  }
  val publishMessage                                                  =
    Endpoint
      .post("publish" / string("topic"))
      .in[PublishMessageRequest]
      .out[PublishMessageResponse]
      .outError[ApiError](Status.InternalServerError)

  override val endpoints: List[Endpoint[_, _, _, None]] = List(
    publishMessage
  )

case class PublishingController(publishingService: PublishingService)
    extends BaseController:

  override val routes: List[Routes[Any, ApiError, EndpointMiddleware.None]] =
    List(
      publishMessage.implement((topic, in) =>
        publishingService.publishMessage(topic, in).handleErrors
      )
    )
