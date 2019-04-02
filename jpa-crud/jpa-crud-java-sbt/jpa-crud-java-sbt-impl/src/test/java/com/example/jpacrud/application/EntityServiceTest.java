package com.example.jpacrud.application;

import akka.NotUsed;
import com.example.jpacrud.api.CreateEntityRequest;
import com.example.jpacrud.api.EntityResource;
import com.example.jpacrud.api.EntityService;
import com.example.jpacrud.api.UpdateEntityRequest;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.testkit.ServiceTest.TestServer;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.pcollections.HashTreePSet;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static akka.NotUsed.notUsed;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.startServer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.pcollections.HashTreePSet.singleton;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;

@DisplayName("Test EntityService")
class EntityServiceTest {

    private static TestServer server;
    private static EntityService service;
    private static CreateEntityRequest entityRequest = CreateEntityRequest.builder()
            .property("I'm property")
            .parts(HashTreePSet.from(asList("part1", "part2")))
            .build();

    @BeforeAll
    static void beforeAll() {
        server = startServer(defaultSetup().withCluster(false));
        service = server.client(EntityService.class);
    }

    @AfterAll
    static void afterAll() {
        if (server != null) server.stop();
    }

    static <T> T eventually(CompletionStage<T> stage) throws InterruptedException, ExecutionException, TimeoutException {
        return stage.toCompletableFuture().get(5, SECONDS);
    }

    @Nested
    @DisplayName("creation entities")
    class CreateEntityTest {

        @Test
        @SuppressWarnings("unchecked")
        @DisplayName("correct entity should be create successfully")
        void shouldCreateEntity() throws InterruptedException, ExecutionException, TimeoutException {
            CreateEntityRequest request = entityRequest;
            EntityResource response = eventually(service.create().invoke(request));
            assertThat(response).isNotNull();
            SoftAssertions assertions = new SoftAssertions();
            assertions.assertThat(response.getId()).isNotNull();
            assertions.assertThat(response.getProperty()).isEqualTo(request.getProperty());
            assertions.assertThat(response.getParts()).containsExactly("part1", "part2");
            assertions.assertAll();
        }

        @Test
        @SuppressWarnings("unchecked")
        @DisplayName("entity without parts should be create successfully")
        void shouldCreateEntityWithoutTextsAndParts() throws InterruptedException, ExecutionException, TimeoutException {
            CreateEntityRequest request = CreateEntityRequest.builder()
                    .property("property")
                    .build();
            EntityResource response = eventually(service.create().invoke(request));
            assertThat(response).isNotNull();
            SoftAssertions assertions = new SoftAssertions();
            assertions.assertThat(response.getId()).isNotNull();
            assertions.assertThat(response.getProperty()).isEqualTo(request.getProperty());
            assertions.assertThat(response.getParts()).isEmpty();
            assertions.assertAll();
        }

        @Test
        @DisplayName("should throw an exception if entity without a property")
        void shouldThrowExceptionIfEntityWithoutName() {
            CreateEntityRequest request = CreateEntityRequest.builder().build();
            assertThatThrownBy(() -> eventually(service.create().invoke(request)))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BadRequest.class)
                    .hasMessageContaining("Property of entity can't be blank");
        }

        @Test
        @DisplayName("should throw an exception if length of property incorrect")
        void shouldThrowExceptionIfIncorrectLengthName() {
            CreateEntityRequest request = CreateEntityRequest.builder()
                    .property(RandomStringUtils.random(110))
                    .build();
            assertThatThrownBy(() -> eventually(service.create().invoke(request)))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BadRequest.class)
                    .hasMessageContaining("Length of property can't be more than 100 character");
        }

        @Test
        @DisplayName("should throw an exception if part is blank")
        void shouldThrowExceptionIfBlankPart() {
            CreateEntityRequest request = CreateEntityRequest.builder()
                    .property("property")
                    .parts(singleton(""))
                    .build();
            assertThatThrownBy(() -> eventually(service.create().invoke(request)))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BadRequest.class)
                    .hasMessageContaining("Name of part can't be blank");
        }

        @Test
        @DisplayName("should throw an exception if length of part incorrect")
        void shouldThrowExceptionIfIncorrectLengthPart() {
            CreateEntityRequest request = CreateEntityRequest.builder()
                    .property("property")
                    .parts(singleton(RandomStringUtils.random(110)))
                    .build();
            assertThatThrownBy(() -> eventually(service.create().invoke(request)))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BadRequest.class)
                    .hasMessageContaining("Length of part can't be more than 100 character");
        }

    }

    @Nested
    @SuppressWarnings("unchecked")
    @DisplayName("retrieving entities")
    class GetEntityTest extends EntityTest {

        @Test
        @DisplayName("retrieve entity by id")
        void shouldGetEntity() throws InterruptedException, ExecutionException, TimeoutException {
            EntityResource response = eventually(service.get(entityId).invoke());
            assertThat(response).isNotNull();
            SoftAssertions assertions = new SoftAssertions();
            assertions.assertThat(response.getId()).isEqualTo(entityId);
            assertions.assertThat(response.getProperty()).isEqualTo(entityRequest.getProperty());
            assertions.assertThat(response.getParts()).containsExactly("part1", "part2");
            assertions.assertAll();
        }

        @Test
        @DisplayName("should throw a bad request exception for incorrect id")
        void shouldThrowBadRequestForIncorrectId() {
            assertThatThrownBy(() -> eventually(service.get("XXXX-YYYY").invoke()))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BadRequest.class)
                    .hasMessageContaining("ID is incorrect. The format of ID should be UUID.");
        }

        @Test
        @DisplayName("should throw a not found exception for not exist a entity")
        void shouldThrowNotFoundForNotExistEntity() {
            assertThatThrownBy(() -> eventually(service.get("00000000-1111-2222-3333-444444444444").invoke()))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(NotFound.class)
                    .hasMessageContaining("not found");
        }

    }

    @Nested
    @DisplayName("update entities")
    class UpdateEntityTest extends EntityTest {

        @Test
        @SuppressWarnings("unchecked")
        @DisplayName("should be successfully update entity")
        void shouldUpdateEntity() throws InterruptedException, ExecutionException, TimeoutException {
            UpdateEntityRequest request = UpdateEntityRequest.builder()
                    .property("new property")
                    .parts(singleton("new part1"))
                    .build();
            eventually(service.update(entityId).invoke(request));
            EntityResource response = eventually(service.get(entityId).invoke());
            assertThat(response).isNotNull();
            SoftAssertions assertions = new SoftAssertions();
            assertions.assertThat(response.getId()).isNotNull();
            assertions.assertThat(response.getProperty()).isEqualTo(request.getProperty());
            assertions.assertThat(response.getParts()).containsExactly("new part1");
            assertions.assertAll();
        }

        @Test
        @SuppressWarnings("unchecked")
        @DisplayName("should be successfully update entity without parts")
        void shouldUpdateEntityWithoutTextsAndParts() throws InterruptedException, ExecutionException, TimeoutException {
            UpdateEntityRequest request = UpdateEntityRequest.builder()
                    .property("new property")
                    .build();
            eventually(service.update(entityId).invoke(request));
            EntityResource response = eventually(service.get(entityId).invoke());
            assertThat(response).isNotNull();
            SoftAssertions assertions = new SoftAssertions();
            assertions.assertThat(response.getId()).isNotNull();
            assertions.assertThat(response.getProperty()).isEqualTo(request.getProperty());
            assertions.assertThat(response.getParts()).isEmpty();
            assertions.assertAll();
        }

        @Test
        @DisplayName("should throw a bad request exception for incorrect id")
        void shouldThrowBadRequestForIncorrectId() {
            assertThatThrownBy(() -> eventually(service.update("XXXX-YYYY").invoke(null)))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BadRequest.class)
                    .hasMessageContaining("ID is incorrect. The format of ID should be UUID.");
        }

        @Test
        @DisplayName("should throw a bad request for entity with blank property")
        void shouldThrowBadRequestForBlankName() {
            UpdateEntityRequest request = UpdateEntityRequest.builder().build();
            assertThatThrownBy(() -> eventually(service.update(entityId).invoke(request)))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BadRequest.class)
                    .hasMessageContaining("Property of entity can't be blank");

        }

        @Test
        @DisplayName("should throw a not found exception for not exist a entity")
        void shouldThrowNotFoundForNotExistEntity() {
            UpdateEntityRequest request = UpdateEntityRequest.builder()
                    .property(entityRequest.getProperty())
                    .build();
            assertThatThrownBy(() -> eventually(service.update("00000000-1111-2222-3333-444444444444").invoke(request)))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(NotFound.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("should throw an exception if length of property incorrect")
        void shouldThrowExceptionIfIncorrectLengthName() {
            UpdateEntityRequest request = UpdateEntityRequest.builder()
                    .property(RandomStringUtils.random(110))
                    .build();
            assertThatThrownBy(() -> eventually(service.update(entityId).invoke(request)))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BadRequest.class)
                    .hasMessageContaining("Length of property can't be more than 100 character");
        }

        @Test
        @DisplayName("should throw an exception if part is blank")
        void shouldThrowExceptionIfBlankPart() {
            UpdateEntityRequest request = UpdateEntityRequest.builder()
                    .property(entityRequest.getProperty())
                    .parts(singleton(""))
                    .build();
            assertThatThrownBy(() -> eventually(service.update(entityId).invoke(request)))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BadRequest.class)
                    .hasMessageContaining("Name of part can't be blank");
        }

        @Test
        @DisplayName("should throw an exception if length of part incorrect")
        void shouldThrowExceptionIfIncorrectLengthPart() {
            UpdateEntityRequest request = UpdateEntityRequest.builder()
                    .property(entityRequest.getProperty())
                    .parts(singleton(RandomStringUtils.random(110)))
                    .build();
            assertThatThrownBy(() -> eventually(service.update(entityId).invoke(request)))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BadRequest.class)
                    .hasMessageContaining("Length of part can't be more than 100 character");
        }

    }

    @Nested
    @DisplayName("deleting entities")
    class DeleteEntityTest {

        @Test
        @DisplayName("should delete exist entity")
        void shouldDeleteExistEntity() throws InterruptedException, ExecutionException, TimeoutException {
            EntityResource createEntity = eventually(service.create().invoke(entityRequest));
            String id = createEntity.getId();
            EntityResource getResponse = eventually(service.get(id).invoke());
            assertThat(getResponse).isNotNull();
            NotUsed deleteResponse = eventually(service.delete(id).invoke());
            assertThat(deleteResponse).isEqualTo(notUsed());
            assertThatThrownBy(() -> eventually(service.get(id).invoke()))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(NotFound.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("should throw a bad request exception for incorrect id")
        void shouldThrowBadRequestForIncorrectId() {
            assertThatThrownBy(() -> eventually(service.delete("XXXX-YYYY").invoke()))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(BadRequest.class)
                    .hasMessageContaining("ID is incorrect. The format of ID should be UUID.");
        }

        @Test
        @DisplayName("should delete not exist entity")
        void shouldThrowNotFoundForNotExistEntity() throws InterruptedException, ExecutionException, TimeoutException {
            NotUsed deleteResponse = eventually(service.delete("00000000-1111-2222-3333-444444444444").invoke());
            assertThat(deleteResponse).isEqualTo(notUsed());
        }

    }

    abstract class EntityTest {

        String entityId;

        @BeforeEach
        void createEntity() throws InterruptedException, ExecutionException, TimeoutException {
            entityId = eventually(service.create().invoke(entityRequest)).getId();
        }

        @AfterEach
        void deleteEntity() throws InterruptedException, ExecutionException, TimeoutException {
            eventually(service.delete(entityId).invoke());
        }

    }

}

