package works.scala.sms.server.api.models

import zio.schema.Schema
import zio.schema.derived
import zio.json.JsonCodec

case class Topic(name: String) derives JsonCodec, Schema
