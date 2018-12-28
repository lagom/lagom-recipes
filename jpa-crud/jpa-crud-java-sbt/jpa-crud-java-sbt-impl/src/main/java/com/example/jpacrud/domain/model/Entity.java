package com.example.jpacrud.domain.model;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Domain Entity.
 */
public class Entity {

    private String id;

    private String property;

    private Set<Part> parts = newHashSet();

    // for JPA
    protected Entity() {}

    public Entity(String id, String property) {
        this.id = id;
        this.property = property;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Set<Part> getParts() {
        return parts;
    }

    public void setParts(Set<Part> parts) {
        this.parts = parts;
    }
}
