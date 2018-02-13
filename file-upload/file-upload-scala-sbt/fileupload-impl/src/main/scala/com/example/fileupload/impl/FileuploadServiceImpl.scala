package com.example.fileupload.impl

import com.example.fileupload.api.FileuploadService
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.Future

/**
  * Implementation of the FileuploadService.
  */
class FileuploadServiceImpl() extends FileuploadService {

  override def uppercaseEcho() = ServiceCall { input =>
    Future.successful(input.toUpperCase)
  }
}
