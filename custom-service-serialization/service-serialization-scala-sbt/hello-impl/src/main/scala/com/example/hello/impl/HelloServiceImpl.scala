package com.example.hello.impl

import com.example.hello.api.HelloService
import com.example.helloworld.helloworld.HelloReply
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.{ ExecutionContext, Future }

/**
  * Implementation of the HelloService.
  */
class HelloServiceImpl()(implicit exctx: ExecutionContext) extends HelloService {

  override def helloPB(id: String) = ServiceCall { _ =>
    Future.successful(HelloReply(id, 42, "termination.-.-.-."))
  }
}
