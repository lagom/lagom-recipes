package com.lightbend.lagom.recipes.cbpanel.impl.repository;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

public class Repository {
    
    public CompletableFuture<String> getSomethingRandom(String userName) {
        if (userName.length() > 20) {
            throw new IllegalArgumentException();
            
        } else if (userName.length() == 1) {
            throw new NoSuchElementException();
        } else {
            return CompletableFuture.completedFuture(String.format("Welcome user %s", userName));
        }
    }
}
