package com.example.fileupload.impl

import com.example.fileupload.api.FileuploadService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class FileuploadLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new FileuploadApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new FileuploadApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[FileuploadService])
}

abstract class FileuploadApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[FileuploadService](wire[FileuploadServiceImpl])
}
