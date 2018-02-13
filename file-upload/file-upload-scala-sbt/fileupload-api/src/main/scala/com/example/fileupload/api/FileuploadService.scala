package com.example.fileupload.api

import com.lightbend.lagom.scaladsl.api.{ Service, ServiceCall }

trait FileuploadService extends Service {

  /**
    * Invoke using:
     <code> curl -X POST -H "Content-Type: text/plain" -d  "hello world" http://localhost:9000/api/echo </code>
    */
  def uppercaseEcho(): ServiceCall[String, String]

  override final def descriptor = {
    import Service._
    named("fileupload")
      .withCalls(
        pathCall("/api/echo", uppercaseEcho _)
      )
      .withAutoAcl(true)
  }
}
