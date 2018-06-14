package com.example.hello.api

import java.io.{ InputStream, OutputStream }

import akka.NotUsed
import akka.util.ByteString
import com.example.helloworld.helloworld.HelloReply
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer.{ NegotiatedDeserializer, NegotiatedSerializer }
import com.lightbend.lagom.scaladsl.api.deser.StrictMessageSerializer
import com.lightbend.lagom.scaladsl.api.transport.MessageProtocol
import com.lightbend.lagom.scaladsl.api.{ Service, ServiceCall }

import scala.collection.immutable

trait HelloService extends Service {

  def helloPB(id: String): ServiceCall[NotUsed, HelloReply]

  // These two implicits collaborate to build the actual message serializer
  // required implicitly in line 28.
  // The reason why we have the serialization split in two is to remove
  // all the duplication into `PBSerializer.pbSerializer` and use small,
  // specialized serializers for each type as subclasses of the
  // `PbToMessage[T]` trait below.
  // This is the same pattern used by default to support JSON serialization
  // via play-json.
  private implicit val hrs = MessagePBSerializers.helloReplySerializer
  private implicit val responseSerializer = PBSerializer.pbSerializer

  override final def descriptor = {
    import Service._
    named("hello")
      .withCalls(
        pathCall("/api/hello-pb/:id", helloPB _) // there's a 2nd set of arguments here.
      )
      .withAutoAcl(true)
  }
}

trait PbToMessage[Message] {
  def read(asInputStream: InputStream): Message
  def write(m: Message, outputStream: OutputStream)
}

object MessagePBSerializers {
  implicit val helloReplySerializer = new PbToMessage[HelloReply] {
    override def read(asInputStream: InputStream): HelloReply = HelloReply.parseDelimitedFrom(asInputStream).get
    override def write(m: HelloReply, outputStream: OutputStream): Unit = m.writeTo(outputStream)
  }
}

object PBSerializer {

  def pbSerializer[Message](implicit format: PbToMessage[Message]): StrictMessageSerializer[Message] =
    new StrictMessageSerializer[Message] {
      final private val serializer = {
        new NegotiatedSerializer[Message, ByteString]() {
          override def protocol: MessageProtocol = MessageProtocol(Some("application/octet-stream"))
          def serialize(m: Message) = {
            val builder = ByteString.createBuilder
            format.write(m, builder.asOutputStream)
            builder.result
          }
        }
      }
      final private val deserializer = {
        new NegotiatedDeserializer[Message, ByteString] {
          override def deserialize(bytes: ByteString) = format.read(bytes.iterator.asInputStream)
        }
      }
      override def serializerForRequest = serializer
      override def deserializer(protocol: MessageProtocol) = deserializer
      override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol]) = serializer
    }
}
