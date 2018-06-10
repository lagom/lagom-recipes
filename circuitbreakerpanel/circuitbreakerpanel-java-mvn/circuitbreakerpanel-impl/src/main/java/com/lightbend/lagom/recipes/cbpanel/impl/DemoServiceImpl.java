package com.lightbend.lagom.recipes.cbpanel.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.client.CircuitBreakersPanel;
import com.lightbend.lagom.recipes.cbpanel.api.DemoService;
import com.lightbend.lagom.recipes.cbpanel.impl.repository.Repository;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class DemoServiceImpl implements DemoService {
    
    private Repository repository;
    
    private CircuitBreakersPanel circuitBreaker;
    
    @Inject
    public DemoServiceImpl(Repository repository, CircuitBreakersPanel circuitBreaker) {
        this.repository = repository;
        this.circuitBreaker = circuitBreaker;
    }
    
    @Override
    public ServiceCall<NotUsed, String> getHelloOrFail(String userName) {
        return request -> circuitBreaker.withCircuitBreaker("breakerA",
                () -> CompletableFuture.completedFuture(repository.getSomethingRandom(userName)));
    }
}
