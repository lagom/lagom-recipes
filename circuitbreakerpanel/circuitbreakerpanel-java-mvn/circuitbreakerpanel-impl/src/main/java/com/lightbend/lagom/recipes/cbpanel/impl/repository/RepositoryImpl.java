package com.lightbend.lagom.recipes.cbpanel.impl.repository;

import java.util.NoSuchElementException;

public class RepositoryImpl implements Repository {
    
    @Override
    public String getSomethingRandom(String userName) {
        if (userName.length() > 20) {
            throw new IllegalArgumentException();
            
        } else if (userName.length() == 1) {
            throw new NoSuchElementException();
        } else {
            return String.format("Welcome user %s", userName);
        }
    }
}
