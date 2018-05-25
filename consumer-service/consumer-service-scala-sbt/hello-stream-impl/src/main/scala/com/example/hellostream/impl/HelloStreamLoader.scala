package com.example.hellostream.impl

import com.example.hello.api.HelloService
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.api.{ LagomConfigComponent, ProvidesAdditionalConfiguration, Service, ServiceInfo }
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaClientComponents
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.playjson.ProvidesJsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.BuiltInComponentsFromContext
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.{ EssentialFilter, Results }
import play.api.routing.Router


class HelloStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new HelloStreamApplication(context) {
      override def serviceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new HelloStreamApplication(context) with LagomDevModeComponents

}

abstract class HelloStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with LagomKafkaClientComponents
    with AhcWSComponents {


  override lazy val srv: LagomServer = super.serverFor(serviceFactory)

  override lazy val router = Router.from {
    case _ => Action {
      Results.NotFound
    }
  }

  override lazy val serviceInfo: ServiceInfo = ServiceInfo("hello-stream-service", Map.empty)

  lazy val helloService = serviceClient.implement[HelloService]
  lazy val subscriber: HelloSubscriber = wire[HelloSubscriber]

}
