/*
 * 
 */
package com.lightbend.lagom.recipes.mixedpersistence.hellostream.impl;

import akka.Done;
import akka.stream.javadsl.Flow;
import com.lightbend.lagom.recipes.mixedpersistence.hello.api.HelloEvent;
import com.lightbend.lagom.recipes.mixedpersistence.hello.api.HelloService;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * This subscribes to the HelloService event stream.
 */
public class HelloStreamSubscriber {

  @Inject
  public HelloStreamSubscriber(HelloService helloService, HelloStreamRepository repository) {
    // Create a subscriber
    helloService.helloEvents().subscribe()
      // And subscribe to it with at least once processing semantics.
      .atLeastOnce(
        // Create a flow that emits a Done for each message it processes
        Flow.<HelloEvent>create().mapAsync(1, event -> {

          if (event instanceof HelloEvent.GreetingMessageChanged) {
            HelloEvent.GreetingMessageChanged messageChanged = (HelloEvent.GreetingMessageChanged) event;
            // Update the message
            return repository.updateMessage(messageChanged.getName(), messageChanged.getMessage());

          } else {
            // Ignore all other events
            return CompletableFuture.completedFuture(Done.getInstance());
          }
        })
      );

  }
}
