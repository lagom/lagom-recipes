package com.example.jpacrud.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

/**
 * CRUD service for Entity
 */
public interface EntityService extends Service {

    /**
     * Get Entity.
     *
     * @param id identifier
     */
    ServiceCall<NotUsed, EntityResource> get(String id);

    /**
     * Create Entity.
     */
    ServiceCall<CreateEntityRequest, EntityResource> create();

    /**
     * Update Entity.
     *
     * @param id identifier
     */
    ServiceCall<UpdateEntityRequest, NotUsed> update(String id);

    /**
     * Delete Entity.
     *
     * @param id identifier
     */
    ServiceCall<NotUsed, NotUsed> delete(String id);

    @Override
    default Descriptor descriptor() {
        return named("entity")
                .withCalls(
                        restCall(Method.GET, "/entities/:id", this::get),
                        restCall(Method.POST, "/entities", this::create),
                        restCall(Method.PUT, "/entities/:id", this::update),
                        restCall(Method.DELETE, "/entities/:id", this::delete)
                )
                .withAutoAcl(true);
    }
}
