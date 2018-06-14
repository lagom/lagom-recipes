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
    
    private CircuitBreakersPanel circuitBreakerPanel;
    
    @Inject
    public DemoServiceImpl(Repository repository, CircuitBreakersPanel circuitBreakerPanel) {
        this.repository = repository;
        this.circuitBreakerPanel = circuitBreakerPanel;
    }
    
    @Override
    public ServiceCall<NotUsed, String> getHelloOrFail(String userName) {
        return request -> circuitBreakerPanel.withCircuitBreaker("breakerA",
                () -> repository.getSomethingRandom(userName));
    }
}
