package com.kaimono.catalog.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaimono.catalog.service.domain.Book;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import junit.aggregator.book.CsvToBook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KaimonoCatalogServiceApplicationTests {

    private static KeycloakToken bjornTokens;
    private static KeycloakToken isabelleTokens;

    @Autowired
    private WebTestClient webClient;

    @Container
    private static final PostgreSQLContainer<?> postgresql =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.4"));

    @Container
    private static final KeycloakContainer keycloakContainer =
            new KeycloakContainer("quay.io/keycloak/keycloak:19.0")
                    .withRealmImportFile("test-realm-config.json");

    @BeforeAll
    public static void generateAccessTokens() {
        WebClient webClient = WebClient.builder()
                .baseUrl(keycloakContainer.getAuthServerUrl() + "realms/kaimono/protocol/openid-connect/token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();

        isabelleTokens = authenticateWith("isabelle", "password", webClient);
        bjornTokens = authenticateWith("bjorn", "password", webClient);
    }

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    public void whenGetRequestWithIdThenBookReturned(@CsvToBook Book book) {
        var expectedBook = webClient
                .post()
                    .uri("/books")
                        .headers(header -> header.setBearerAuth(isabelleTokens.accessToken()))
                            .bodyValue(book)
                .exchange()
                .expectStatus()
                    .isCreated()
                .expectBody(Book.class)
                    .value(body ->
                            assertThat(body.isbn())
                                    .isNotNull()
                                    .isEqualTo(book.isbn()))
                .returnResult()
                    .getResponseBody();

        webClient
                .get()
                    .uri("/books/" + book.isbn())
                .exchange()
                .expectStatus()
                    .isOk()
                .expectBody(Book.class)
                    .value(actualBook ->
                        assertThat(actualBook.isbn())
                                .isNotNull()
                                .isEqualTo(expectedBook.isbn())
                );
    }

    @ParameterizedTest
    @CsvSource("1234567893, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    public void whenPostRequestThenBookCreated(@CsvToBook Book book) {
        webClient
                .post()
                    .uri("/books")
                        .headers(header -> header.setBearerAuth(isabelleTokens.accessToken()))
                            .bodyValue(book)
                .exchange()
                .expectStatus()
                    .isCreated()
                .expectBody(Book.class)
                    .value(actualBook ->
                        assertThat(actualBook.isbn())
                                .isNotNull()
                                .isEqualTo(book.isbn())
                );
    }

    @ParameterizedTest
    @CsvSource("1234567899, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    public void whenPutRequestThenBookUpdated(@CsvToBook Book book) {
        var createdBook = webClient
                .post()
                    .uri("/books")
                        .headers(header -> header.setBearerAuth(isabelleTokens.accessToken()))
                            .bodyValue(book)
                .exchange()
                .expectStatus()
                    .isCreated()
                .expectBody(Book.class)
                    .value(body ->
                            assertThat(body.isbn())
                                    .isNotNull()
                                    .isEqualTo(book.isbn()))
                .returnResult()
                    .getResponseBody();

        var updatedBook = new Book (
                createdBook.id(),
                createdBook.isbn(),
                createdBook.title(),
                createdBook.author(),
                createdBook.publisher(),
                7.9,
                createdBook.createdDate(),
                createdBook.lastModifiedDate(),
                createdBook.version(),
                createdBook.createdBy(),
                createdBook.lastModifiedBy()
        );

        webClient
                .put()
                    .uri("/books/" + book.isbn())
                        .headers(header -> header.setBearerAuth(isabelleTokens.accessToken()))
                            .bodyValue(updatedBook)
                .exchange()
                .expectStatus()
                    .isOk()
                .expectBody(Book.class)
                    .value(actualBook ->
                        assertThat(actualBook.price())
                                .isNotNull()
                                .isEqualTo(updatedBook.price())
                    );
    }

    @ParameterizedTest
    @CsvSource("8977557964, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    void whenDeleteRequestThenBookDeleted(@CsvToBook Book book) {
        webClient
                .post()
                    .uri("/books")
                        .headers(header -> header.setBearerAuth(isabelleTokens.accessToken()))
                            .bodyValue(book)
                .exchange()
                .expectStatus()
                    .isCreated();

        webClient
                .delete()
                    .uri("/books/" + book.isbn())
                        .headers(header -> header.setBearerAuth(isabelleTokens.accessToken()))
                .exchange()
                .expectStatus()
                    .isNoContent();

        webClient
                .get()
                    .uri("/books/" + book.isbn())
                .exchange()
                .expectStatus()
                    .isNotFound()
                .expectBody(String.class)
                    .isEqualTo("The book with ISBN " + book.isbn() + " was not found.");
    }

    @DynamicPropertySource
    private static void keycloakProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "realms/kaimono");
    }

    @DynamicPropertySource
    private static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.username", postgresql::getUsername);
        registry.add("spring.r2dbc.password", postgresql::getPassword);
        registry.add("spring.flyway.url", postgresql::getJdbcUrl);
        registry.add("spring.r2dbc.url", () ->
                String.format("r2dbc:postgresql://%s:%s/%s",
                        postgresql.getHost(),
                        postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                        postgresql.getDatabaseName())
        );
    }

    private record KeycloakToken(String accessToken) {
        @JsonCreator private KeycloakToken(@JsonProperty("access_token") String accessToken) {
            this.accessToken = accessToken;
        }
    }

    private static KeycloakToken authenticateWith(String username, String password, WebClient webClient) {
        return webClient
                .post()
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", "kaimono-test")
                        .with("username", username)
                        .with("password", password))
                .retrieve()
                .bodyToMono(KeycloakToken.class)
                .block();
    }

}
