package com.example.jpacrud.infrastructure.persistence;

import com.example.jpacrud.domain.model.Entity;
import com.example.jpacrud.domain.model.EntityRepository;
import org.taymyr.play.repository.infrastructure.persistence.DatabaseExecutionContext;
import org.taymyr.play.repository.infrastructure.persistence.JPARepository;
import play.db.jpa.JPAApi;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.inject.Inject;

public class EntityRepositoryImpl extends JPARepository<Entity, String> implements EntityRepository {

    @Inject
    public EntityRepositoryImpl(@Nonnull JPAApi jpaApi, @Nonnull DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext, Entity.class);
    }

    @Override
    public String nextIdentity() {
        return UUID.randomUUID().toString();
    }

}
