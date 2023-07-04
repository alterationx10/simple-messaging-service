package works.scala.sms.cli

import works.scala.sms.server.api.controllers.{
  ConsumerController,
  PublishingController,
  SubscriptionController,
  TopicController
}
import zio.*
import zio.cli.*
import zio.http.*
import zio.http.endpoint.*
import zio.http.endpoint.cli.*

object Main extends ZIOCliDefault:
  override def cliApp: CliApp[Any with ZIOAppArgs with Scope, Any, Any] =
    HttpCliApp
      .fromEndpoints(
        name = "simple-messaging-service",
        version = "0.0.0",
        summary = HelpDoc.Span.text("Interact with the sms api"),
        footer = HelpDoc.p("ScalaWorks"),
        host = "localhost",
        port = 9000,
        endpoints = Chunk.from(
          TopicController.endpoints ++
            SubscriptionController.endpoints ++
            PublishingController.endpoints ++
            ConsumerController.endpoints
        )
      )
      .cliApp
