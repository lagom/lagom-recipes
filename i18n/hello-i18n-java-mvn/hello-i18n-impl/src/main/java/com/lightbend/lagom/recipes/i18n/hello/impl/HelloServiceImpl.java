package com.lightbend.lagom.recipes.i18n.hello.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.recipes.i18n.hello.api.HelloService;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the HelloService.
 */
public class HelloServiceImpl implements HelloService {

    @Inject
    public HelloServiceImpl() {
    }

    @Override
    public ServiceCall<NotUsed, String> hello(String id) {
        return request -> CompletableFuture.completedFuture("Hello, " + id + "!");
    }

}
