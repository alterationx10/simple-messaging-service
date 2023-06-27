package works.scala.sss.api.services

import com.rabbitmq.client.Connection
import com.rabbitmq.http.client.Client
import works.scala.sss.api.models.{
  DelayedMessage,
  PublishMessageRequest,
  PublishMessageResponse
}
import zio.*
import works.scala.sss.rmq.RMQ
import works.scala.sss.extensions.Extensions.*

trait PublishingService:
  def publishMessage(
      topic: String,
      publishMessageRequest: PublishMessageRequest
  ): Task[PublishMessageResponse]

object PublishingServiceImpl:
  val layer: ZLayer[Connection & Client, Nothing, PublishingService] =
    ZLayer {
      for {
        amqp <- ZIO.service[Connection]
        http <- ZIO.service[Client]
      } yield PublishingServiceImpl(amqp, http)
        .asInstanceOf[PublishingService]
    }

case class PublishingServiceImpl(rmqConnection: Connection, rmqClient: Client)
    extends PublishingService:
  override def publishMessage(
      topic: String,
      publishMessageRequest: PublishMessageRequest
  ): Task[PublishMessageResponse] =
    ZIO.scoped {
      // TODO validations
      rmqConnection.scopedOp { chan =>
        chan.publishMessageRequest(
          DelayedMessage(
            topic,
            sendAt = publishMessageRequest.sendAt,
            payload = publishMessageRequest.payload
          )
        )
        PublishMessageResponse()
      }
    }
