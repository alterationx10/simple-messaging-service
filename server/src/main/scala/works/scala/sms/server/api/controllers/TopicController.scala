package works.scala.sms.server.api.controllers

import works.scala.sms.server.api.models.{
  ApiError,
  CreateTopicRequest,
  CreateTopicResponse,
  DeleteTopicResponse,
  GetTopicResponse,
  GetTopicsResponse
}
import works.scala.sms.server.api.services.TopicService
import works.scala.sms.server.api.models.*
import zio.*
import zio.http.*
import zio.http.codec.*
import zio.http.endpoint.*
import HttpCodec.*
import works.scala.sms.server.api.controllers.TopicController.*
import zio.http.endpoint.EndpointMiddleware.None

object TopicController                                 extends EndpointAggregator:
  val layer: ZLayer[TopicService, Nothing, TopicController] = ZLayer {
    ZIO.service[TopicService].map(TopicController.apply)
  }

  val getTopics =
    Endpoint
      .get("topics")
      .out[GetTopicsResponse]
      .outError[ApiError](Status.InternalServerError) ?? Doc.p("Get all topics")

  val getTopic =
    Endpoint
      .get("topics" / string("name"))
      .out[GetTopicResponse]
      .outError[ApiError](Status.InternalServerError) ?? Doc.p("Get a topic by name")

  val createTopic =
    Endpoint
      .post("topics")
      .in[CreateTopicRequest]
      .out[CreateTopicResponse]
      .outError[ApiError](Status.InternalServerError)

  val deleteTopic =
    Endpoint
      .delete("topics" / string("name"))
      .out[DeleteTopicResponse]
      .outError[ApiError](Status.InternalServerError)

  override val endpoints: List[Endpoint[_, _, _, None]] = List(
    getTopic,
    getTopics,
    createTopic,
    deleteTopic
  )
case class TopicController(topicService: TopicService) extends BaseController:

  override val routes: List[Routes[Any, ApiError, EndpointMiddleware.None]] =
    List(
      getTopics.implement(_ => topicService.getTopics.handleErrors),
      getTopic.implement(name => topicService.getTopic(name).handleErrors),
      createTopic.implement(in => topicService.createTopic(in).handleErrors),
      deleteTopic.implement(name => topicService.deleteTopic(name).handleErrors)
    )
