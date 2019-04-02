package com.example.jpacrud.domain.model;

/**
 * Part of Entity.
 */
public class Part {

    private String name;

    // for JPA
    protected Part() {}

    public Part(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
