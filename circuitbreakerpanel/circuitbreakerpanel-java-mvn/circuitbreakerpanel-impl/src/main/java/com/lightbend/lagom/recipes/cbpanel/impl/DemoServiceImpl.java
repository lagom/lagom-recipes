package com.lightbend.lagom.recipes.cbpanel.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.client.CircuitBreakersPanel;
import com.lightbend.lagom.recipes.cbpanel.api.DemoService;
import com.lightbend.lagom.recipes.cbpanel.impl.repository.Repository;

import javax.inject.Inject;

public class DemoServiceImpl implements DemoService {
    
    private Repository repository;
    
    private CircuitBreakersPanel circuitBreakerPanel;
    
    @Inject
    public DemoServiceImpl(Repository repository, CircuitBreakersPanel circuitBreakerPanel) {
        this.repository = repository;
        this.circuitBreakerPanel = circuitBreakerPanel;
    }
    
    
    /**
     * The CircuitBreakerPanel#withCircuitBreaker method accepts as string as the
     * circuitBreaker name ( configuration for which it picks from the application.conf)
     * As a second parameter it accepts a supplier in which you can make your call to the
     * external service
     */
    @Override
    public ServiceCall<NotUsed, String> getHelloOrFail(String userName) {

        return request -> circuitBreakerPanel.withCircuitBreaker("breakerA",
                () -> repository.getSomethingRandom(userName));
    }
}
