package com.example.jpacrud.application;

import akka.NotUsed;
import akka.japi.Pair;
import com.example.jpacrud.api.CreateEntityRequest;
import com.example.jpacrud.api.EntityResource;
import com.example.jpacrud.api.EntityService;
import com.example.jpacrud.api.UpdateEntityRequest;
import com.example.jpacrud.domain.model.Entity;
import com.example.jpacrud.domain.model.EntityRepository;
import com.example.jpacrud.domain.model.Part;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;
import com.lightbend.lagom.javadsl.server.HeaderServiceCall;
import org.pcollections.HashTreePMap;

import java.util.UUID;
import java.util.function.Supplier;
import javax.inject.Inject;

import static akka.NotUsed.notUsed;
import static com.example.jpacrud.application.Mappers.toEntityResource;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.lightbend.lagom.javadsl.api.transport.MessageProtocol.fromContentTypeHeader;
import static com.lightbend.lagom.javadsl.api.transport.ResponseHeader.NO_CONTENT;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.length;
import static play.mvc.Http.Status.CREATED;

import static java.lang.String.format;
import static java.util.Optional.of;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toSet;

public class EntityServiceImpl implements EntityService {

    private static final MessageProtocol JSON = fromContentTypeHeader(of(JSON_UTF_8.toString()));

    private final EntityRepository templateRepository;

    @Inject
    public EntityServiceImpl(EntityRepository templateJpaRepository) {
        this.templateRepository = templateJpaRepository;
    }

    @Override
    public ServiceCall<NotUsed, EntityResource> get(String id) {
        return notUsed -> {
            validateUUID(id);
            return templateRepository.get(id)
                    .thenApply(template -> toEntityResource(template.orElseThrow(notFound(id))));
        };
    }

    @Override
    public HeaderServiceCall<CreateEntityRequest, EntityResource> create() {
        return (header, request) -> {
            validate(request);
            Entity entity = new Entity(
                    templateRepository.nextIdentity(),
                    request.getProperty()
            );
            if (request.getParts() != null) {
                entity.setParts(request.getParts().stream().map(Part::new).collect(toSet()));
            }
            ResponseHeader responseHeader = new ResponseHeader(CREATED, JSON, HashTreePMap.empty());
            return templateRepository.save(entity).thenApply(done -> Pair.create(responseHeader, toEntityResource(entity)));
        };
    }

    @Override
    public HeaderServiceCall<UpdateEntityRequest, NotUsed> update(String id) {
        return (header, request) -> {
            validateUUID(id);
            validate(request);
            return templateRepository.get(id)
                    .thenCompose(t -> {
                        Entity entity = t.orElseThrow(notFound(id));
                        entity.setProperty(request.getProperty());
                        if (request.getParts() != null) {
                            entity.setParts(request.getParts().stream().map(Part::new).collect(toSet()));
                        } else {
                            entity.setParts(newHashSet());
                        }
                        return templateRepository.save(entity).thenCompose(done -> completedFuture(Pair.create(NO_CONTENT, notUsed())));
                    });
        };
    }

    @Override
    public HeaderServiceCall<NotUsed, NotUsed> delete(String id) {
        return (header, notUsed) -> {
            validateUUID(id);
            return templateRepository.get(id).thenCompose(template -> {
                if (template.isPresent()) {
                    return templateRepository.remove(template.get()).thenCompose(done -> completedFuture(Pair.create(NO_CONTENT, notUsed())));
                } else {
                    return completedFuture(Pair.create(NO_CONTENT, notUsed()));
                }
            });
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void validateUUID(String uuid) {
        try {
            UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new BadRequest("ID is incorrect. The format of ID should be UUID.");
        }
    }

    private Supplier<NotFound> notFound(String id) {
        return () -> new NotFound(format("Entity with id='%s' is not found", id));
    }

    @SuppressWarnings("Duplicates")
    private void validate(CreateEntityRequest request) {
        if (request == null) throw new BadRequest("Request body can't be null");
        validateProperty(request.getProperty());
        if (request.getParts() != null) {
            for (String part : request.getParts()) validatePart(part);
        }
    }

    @SuppressWarnings("Duplicates")
    private void validate(UpdateEntityRequest request) {
        if (request == null) throw new BadRequest("Request body can't be null");
        validateProperty(request.getProperty());
        if (request.getParts() != null) {
            for (String part : request.getParts()) validatePart(part);
        }
    }

    private void validateProperty(String name) {
        if (isBlank(name)) throw new BadRequest("Property of entity can't be blank");
        if (length(name) > 100) throw new BadRequest("Length of property can't be more than 100 character");
    }

    private void validatePart(String part) {
        if (isBlank(part)) throw new BadRequest("Name of part can't be blank");
        if (length(part) > 100) throw new BadRequest("Length of part can't be more than 100 character");
    }

}
