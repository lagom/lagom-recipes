package com.example.fileupload.impl

import java.io.File
import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{ FileIO, Sink }
import akka.stream.{ IOResult, Materializer }
import akka.util.ByteString
import com.example.fileupload.api.FileuploadService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.server.PlayServiceCall
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo

import scala.concurrent.{ ExecutionContext, Future }

/**
  * Implementation of the FileuploadService.
  */
class FileuploadServiceImpl(
                             actionBuilder: DefaultActionBuilder,
                             playBodyParsers: PlayBodyParsers
                           )(implicit mat: Materializer,
                             exCtx: ExecutionContext
                           ) extends FileuploadService {

  override def uppercaseEcho() = ServiceCall { input =>
    Future.successful(input.toUpperCase)
  }


  override def uploadFile(): ServiceCall[NotUsed, String] = PlayServiceCall {
    callback: (ServiceCall[NotUsed, String] => EssentialAction) =>

      // (1) Create a multi-part body parser with the Accumulator (see the implementation of 'fileHandler()' below)
      //
      // Play provides a varied set of body parsers. In our case we need 'multipartFormData'
      // but might also use 'json', 'raw', ... . We complete the build of the bodyParser providing
      // the fileHandler we created below.
      val bodyParser: BodyParser[MultipartFormData[FilePath]] = playBodyParsers.multipartFormData(fileHandler)

      // (2) Create Play's Action.
      //
      // Play handles incoming request via Action (aka EssentialAction). Lagom is built on top of
      // Play and automatically creates an Action for each ServiceCall. But, when implementing
      // a ServiceCall using a PlayServiceCall like we disable the automatic creation and so we
      // have to create the Play Action ourselves. The callback argument above allows converting
      // a regular ServiceCall into an Action so that can mix Pal and Lagom coding styles.
      //
      // So, we create an 'async' action with a multi-part body parser that uses our own file handling.
      // The missing bit on an `actionBuilder.async(bodyParser)` is a function
      // `Request => Future[Result]` (that is regular Play API). In this example we create
      // a requestHandling(callback)(request) method which given a `Request` provides the `Future[String]`
      //
      // Note: Building a `requestHandling(...)` and using the provided `callback` is not a required step
      // and you could implement all your ServiceCall using only Play features.
      actionBuilder.async(bodyParser)(requestHandling(callback))
  }

  // (2.extra) Using the callback provided by the lagom framework we can implement
  // our logic using a regular Lagom ServiceCall. The provided `request` will contain the
  // information generated in the custom body parser (in our case a `FilePath`).
  private def requestHandling(
                               callback: ServiceCall[NotUsed, String] => EssentialAction
                             )(
                               request: Request[MultipartFormData[FilePath]]
                             ): Future[Result] = {
    val wrappedAction: EssentialAction = callback(
      ServiceCall { notused =>
        val files: Seq[FilePart[FilePath]] = request.body.files
        Future.successful(files.mkString("Uploaded[", ", ", "]"))
      })
    wrappedAction(request).run()
  }

  // A fileHandler will build an Accumulator for each FileInfo in the request.
  // Accumulator is Play's abstraction over Akka's Sink. The Accumulator is
  // converting a ByteString of each FileInfo in the multipart request into a FilePart[FilePath]
  // that contains the metadata once the bytes have moved from the request into it's
  // final storage. FilePath is a very simple case class we added below. FilePath wraps
  // the java.io.File.
  private def fileHandler(fileInfo: FileInfo): Accumulator[ByteString, FilePart[FilePath]] = {

    val file = tempFile()

    // (1.1) Create a Sink
    // This Sink is the key aspect of the recipe. This example uses a Akka Streams FileIO
    // Sink that stores bytes into a File, but you could use a Sink for S3, or a Sink
    // to a Blob on your preferred DB or ...
    val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(file.toPath)

    // (1.2) Create a Play's Accumulator around the Sink.
    val acc: Accumulator[ByteString, IOResult] = Accumulator(sink)
    acc.map {
      case akka.stream.IOResult(bytesWriten, status) =>
        // TODO: check if status.success is true if you want, or capture the
        // fileInfo.filename or whatever. In this case, we'll just return
        // the FilePart containing the absolute path to the file that was written.
        FilePart(fileInfo.partName, fileInfo.fileName, fileInfo.contentType, FilePath(file))
    }
  }

  // DON'T DO THIS in production!
  // create a temp file on a relative folder that may not exist.
  // DON'T DO THIS in production! Don't store files locally, instead use a DB, networked file
  // system with data replication, a remote blob storage, ...
  private def tempFile(): File = {
    val file = new java.io.File("./target/file-upload-data/uploads", UUID.randomUUID().toString).getAbsoluteFile
    file.getParentFile.mkdirs()
    file
  }

}

case class FilePath(file: File) {
  val absolutePath: String = file.getAbsolutePath
}