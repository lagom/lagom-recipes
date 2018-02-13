package com.example.fileupload.impl

import com.example.fileupload.api._
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, Matchers }

class FileuploadServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup) { ctx =>
    new FileuploadApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[FileuploadService]

  override protected def afterAll() = server.stop()

  "FileUpload service" should {

    "respond in upper case any payload received in /api/echo" in {
      // arrange
      val postPayload = "this is not uppercase"
      val serviceCall = client.uppercaseEcho()

      // act
      val eventualResponse = serviceCall.invoke(postPayload)

      // assert
      eventualResponse.map {
        _ should ===("THIS IS NOT UPPERCASE")
      }

    }
  }
}
