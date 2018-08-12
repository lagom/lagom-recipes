package com.lightbend.lagom.recipes.cbpanel.api;

public class User {
    
    String name;
    Integer id;
    String location;
    
    public User(String name, Integer id, String location) {
        this.name = name;
        this.id = id;
        this.location = location;
    }
}
