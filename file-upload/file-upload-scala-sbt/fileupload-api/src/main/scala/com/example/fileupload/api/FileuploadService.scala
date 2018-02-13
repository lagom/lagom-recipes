package com.example.fileupload.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{ Service, ServiceCall }

trait FileuploadService extends Service {

  /**
    * Invoke using:
     <code> curl -X POST -H "Content-Type: text/plain" -d  "hello world" http://localhost:9000/api/echo </code>
    */
  def uppercaseEcho(): ServiceCall[String, String]

  def uploadFile(): ServiceCall[NotUsed, String]

  override final def descriptor = {
    import Service._
    named("fileupload")
      .withCalls(
        pathCall("/api/echo", uppercaseEcho _),

        // Uploading a file using multi-part forms requires using POST. Because we don't want Lagom
        // to parse the payload of the request, the incoming type of uploadFile() is `NotUsed`. That
        // is a problem: Lagom will map all `ServiceCall[NotUsed, ...]` to Method.GET unless
        // otherwise specified. Therefore we must use a `restCall(Method.POST,...)` when uploading a file.
        restCall(Method.POST, "/api/files", uploadFile _)

      )
      .withAutoAcl(true)
  }
}
