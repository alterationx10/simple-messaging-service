package works.scala.sms.server.api.controllers

import works.scala.sms.server.api.controllers.SubscriptionController.*
import works.scala.sms.server.api.models.{
  ApiError,
  CreateSubscriptionRequest,
  CreateSubscriptionResponse,
  DeleteSubscriptionResponse,
  GetSubscriptionResponse,
  GetSubscriptionsResponse
}
import works.scala.sms.server.api.services.SubscriptionService
import zio.*
import zio.http.*
import zio.http.codec.HttpCodec
import zio.http.codec.HttpCodec.*
import zio.http.endpoint.*
import zio.http.endpoint.EndpointMiddleware.None

object SubscriptionController extends EndpointAggregator:

  val layer: ZLayer[SubscriptionService, Nothing, SubscriptionController] =
    ZLayer {
      ZIO.service[SubscriptionService].map(SubscriptionController.apply)
    }

  val createSubscription =
    Endpoint
      .post("subscriptions")
      .in[CreateSubscriptionRequest]
      .out[CreateSubscriptionResponse]
      .outError[ApiError](Status.InternalServerError)

  val getSubscription =
    Endpoint
      .get("subscriptions" / string("name"))
      .out[GetSubscriptionResponse]
      .outError[ApiError](Status.InternalServerError)

  val getSubscriptionz =
    Endpoint
      .get("subscriptions")
      .out[GetSubscriptionsResponse]
      .outError[ApiError](Status.InternalServerError)

  val deleteSubscription =
    Endpoint
      .delete("subscriptions" / string("name"))
      .out[DeleteSubscriptionResponse]
      .outError[ApiError](Status.InternalServerError)

  override val endpoints: List[Endpoint[_, _, _, None]] = List(
    getSubscription,
    getSubscriptionz,
    createSubscription,
    deleteSubscription
  )

case class SubscriptionController(subscriptionService: SubscriptionService)
    extends BaseController:
  import HttpCodec._

  override val routes: List[Routes[Any, ApiError, EndpointMiddleware.None]] =
    List(
      getSubscription.implement(name =>
        subscriptionService.getSubscription(name).handleErrors
      ),
      getSubscriptionz.implement(name =>
        subscriptionService.getSubscriptions().handleErrors
      ),
      createSubscription.implement(in =>
        subscriptionService.createSubscription(in).handleErrors
      ),
      deleteSubscription.implement(name =>
        subscriptionService.deleteSubscription(name).handleErrors
      )
    )
