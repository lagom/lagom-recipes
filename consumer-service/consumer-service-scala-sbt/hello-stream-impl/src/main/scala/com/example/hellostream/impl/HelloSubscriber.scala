package com.example.hellostream.impl

import akka.Done
import akka.stream.scaladsl.Flow
import com.example.hello.api.HelloService

/**
  * Implementation of the HelloStreamService.
  */
class HelloSubscriber(helloService: HelloService) {

  helloService.greetingsTopic().subscribe.atLeastOnce(
    Flow.fromFunction { msg =>
      println(s"Received $msg")
      Done
    }
  )

}
