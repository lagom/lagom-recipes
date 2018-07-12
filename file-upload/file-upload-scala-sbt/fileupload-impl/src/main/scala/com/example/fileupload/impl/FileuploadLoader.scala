package com.example.fileupload.impl

import akka.cluster.Cluster
import akka.management.cluster.ClusterHttpManagementRoutes
import com.example.fileupload.api.FileUploadService
import com.example.play.controllers.FileUploadController
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.cluster.ClusterComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.playjson.{EmptyJsonSerializerRegistry, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.routing.AkkaHttpPlayRouter
import router.Routes

class FileUploadLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new FileUploadApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator

    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new FileUploadApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[FileUploadService])
}

abstract class FileUploadApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with ClusterComponents
    with AhcWSComponents {

  override def jsonSerializerRegistry: JsonSerializerRegistry = EmptyJsonSerializerRegistry

  lazy implicit val ac = actorSystem
  lazy val clusterHttpRouter = AkkaHttpPlayRouter("/cluster", ClusterHttpManagementRoutes(Cluster(ac)))

  lazy val fileUploadRouter = new Routes(
    httpErrorHandler,
    new FileUploadController(controllerComponents)
  )

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer =
    serverFor[FileUploadService](wire[FileUploadServiceImpl])
      .plusRouter(clusterHttpRouter)
      .plusRouter(fileUploadRouter.withPrefix("/io"))




}
