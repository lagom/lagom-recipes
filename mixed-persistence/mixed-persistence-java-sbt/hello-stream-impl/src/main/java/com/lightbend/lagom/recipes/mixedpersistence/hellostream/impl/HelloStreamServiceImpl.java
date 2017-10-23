/*
 * 
 */
package com.lightbend.lagom.recipes.mixedpersistence.hellostream.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.recipes.mixedpersistence.hello.api.HelloService;
import com.lightbend.lagom.recipes.mixedpersistence.hellostream.api.HelloStreamService;

import javax.inject.Inject;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Implementation of the HelloStreamService.
 */
public class HelloStreamServiceImpl implements HelloStreamService {

  private final HelloService helloService;
  private final HelloStreamRepository repository;

  @Inject
  public HelloStreamServiceImpl(HelloService helloService, HelloStreamRepository repository) {
    this.helloService = helloService;
    this.repository = repository;
  }

  @Override
  public ServiceCall<Source<String, NotUsed>, Source<String, NotUsed>> directStream() {
    return hellos -> completedFuture(
        hellos.mapAsync(8, name -> helloService.hello(name).invoke()));
  }

  @Override
  public ServiceCall<Source<String, NotUsed>, Source<String, NotUsed>> autonomousStream() {
    return hellos -> completedFuture(
        hellos.mapAsync(8, name -> repository.getMessage(name).thenApply( message ->
            String.format("%s, %s!", message.orElse("Hello"), name)
        ))
    );
  }
}
