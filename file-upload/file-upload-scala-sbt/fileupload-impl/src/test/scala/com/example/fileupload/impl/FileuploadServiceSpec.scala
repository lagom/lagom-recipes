package com.example.fileupload.impl

import java.nio.file.{ Files, Path, Paths }

import akka.stream.scaladsl.{ FileIO, Source }
import com.example.fileupload.api._
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, Matchers }
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData.{ DataPart, FilePart }

class FileUploadServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  var ws: WSClient = null

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup) { ctx =>
    new FileUploadApplication(ctx) with LocalServiceLocator {
      ws = wsClient
    }
  }

  val client = server.serviceClient.implement[FileUploadService]

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

    "respond to cluster management request under /cluster/members" in {
      val eventualResponse = ws
          .url(server.playServer.httpPort.map { port => s"http://localhost:$port/cluster/members" }.get)
          .get()
      eventualResponse.map{ _.status shouldBe 200 }
    }

    "handle file uploads " in {

      // Create a java.nio.file.Path to the file we want to upload
      val originalPath: Path = Paths.get("fileupload-impl", "src", "test", "resources", "sampleFile.txt")
      assert(originalPath.toFile.exists())

      // pack the Path into a Play's FilePart. A FilePart represents a blob inside a MultipartForm.
      // You can also use DataPart's inside a MultipartForm when uploading data using Play-WS
      val filePart = FilePart(
        "multipart-filepart-1",
        "sampleFile.txt",
        Option("text/plain"),
        FileIO.fromPath(originalPath)
      )

      // To post multipart-form-encoded data a
      //      Source[play.api.mvc.MultipartFormData.Part[Source[ByteString, Any]], Any]
      // needs to be passed into post.
      val multipartFormParts = filePart :: DataPart("unused-key", "unused-value") :: List()

      val eventualResponse =
        ws
          .url(server.playServer.httpPort.map { port => s"http://localhost:$port/io/api/files" }.get)
          .post(Source(multipartFormParts))


      eventualResponse.map { resp =>
        // resp.body is a String of the format: "Uploaded[/path/to/file1, /path/toFile2]"
        // so a list of created files is returned. Even though the test only uploads one file
        // we can write an assertion for N files.
        val fileNames = resp.body.replace("Uploaded[", "").replace("]", "").split(",").map (_.trim)

        fileNames.map { fileName =>
          val uploadedPath = Paths.get(fileName)
          uploadedPath.toFile.length() should be(originalPath.toFile.length())
          Files.readAllBytes(uploadedPath) should be(Files.readAllBytes(originalPath))
        }.last
      }
    }
  }
}
