package com.example.jpacrud.application;

import com.example.jpacrud.api.EntityResource;
import com.example.jpacrud.domain.model.Entity;
import com.example.jpacrud.domain.model.Part;
import org.pcollections.HashTreePSet;

import static java.util.stream.Collectors.toList;

/**
 * Mapper DTO &hArr; DOMAIN.
 */
final class Mappers {

    private Mappers() {
    }

    static EntityResource toEntityResource(Entity template) {
        return EntityResource.of(
                template.getId(),
                template.getProperty(),
                HashTreePSet.from(template.getParts().stream().map(Part::getName).collect(toList())));
    }

}
