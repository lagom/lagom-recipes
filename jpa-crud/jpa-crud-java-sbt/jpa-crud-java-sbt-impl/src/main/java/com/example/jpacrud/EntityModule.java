package com.example.jpacrud;

import com.example.jpacrud.api.EntityService;
import com.example.jpacrud.application.EntityServiceImpl;
import com.example.jpacrud.domain.model.EntityRepository;
import com.example.jpacrud.infrastructure.persistence.EntityRepositoryImpl;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * The module that binds the EntityService so that it can be served.
 */
public class EntityModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindService(EntityService.class, EntityServiceImpl.class);
        bind(EntityRepository.class).to(EntityRepositoryImpl.class);
    }
}
