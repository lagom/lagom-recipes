package com.lightbend.lagom.recipes.telemetry.endpoints.impl

import com.lightbend.lagom.recipes.telemetry.endpoints.api.EndpointMetricsTestService
import com.lightbend.lagom.scaladsl.api.{ Descriptor, ServiceLocator }
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class EndpointMetricsTestLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new EndpointMetricsTestApplication(context) {
      override lazy val serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new EndpointMetricsTestApplication(context) with LagomDevModeComponents

  override lazy val describeService: Option[Descriptor] =
    Some(readDescriptor[EndpointMetricsTestService])

}

abstract class EndpointMetricsTestApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer =
    serverFor[EndpointMetricsTestService](wire[EndpointMetricsTestServiceImpl])

}
