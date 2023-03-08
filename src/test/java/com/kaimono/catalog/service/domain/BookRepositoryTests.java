package com.kaimono.catalog.service.domain;

import com.kaimono.catalog.service.config.DataConfig;
import junit.aggregator.book.CsvToBook;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@Testcontainers
@Import(DataConfig.class)
public class BookRepositoryTests {

    @Autowired
    private BookRepository bookRepository;

    @Container
    private static final PostgreSQLContainer<?> postgresql =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.4"));

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    void whenCreateBookNotAuthenticatedThenNoAuditMetadata(@CsvToBook Book book) {
        StepVerifier.create(bookRepository.save(book))
                .assertNext(incomingBook -> {
                    assertThat(incomingBook.createdBy()).isNull();
                    assertThat(incomingBook.lastModifiedBy()).isNull();
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @WithMockUser("mock-user")
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    void whenCreateBookAuthenticatedThenAuditMetadata(@CsvToBook Book book) {
        StepVerifier.create(bookRepository.save(book))
                .assertNext(incomingBook -> {
                    assertThat(incomingBook.createdBy()).isEqualTo("mock-user");
                    assertThat(incomingBook.lastModifiedBy()).isEqualTo("mock-user");
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource("1234563890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    public void findBookByIsbnWhenExisting(@CsvToBook Book book) {
        var savedBook = bookRepository.save(book)
                .map(Book::isbn)
                .flatMap(bookRepository::findByIsbn);

        StepVerifier.create(savedBook)
                .expectNextMatches(incomingBook ->
                        incomingBook.isbn().equals(book.isbn()))
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = "1234561238")
    void findBookByIsbnWhenNotExisting(String isbn) {
        var expectedBook = bookRepository.findByIsbn(isbn);
        StepVerifier.create(expectedBook).verifyComplete();
    }

    @ParameterizedTest
    @CsvSource("1234567891, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    void deleteByIsbn(@CsvToBook Book book) {
        var savedBook = bookRepository.save(book)
                .map(Book::isbn)
                .flatMap(bookRepository::deleteByIsbn);

        StepVerifier.create(savedBook).verifyComplete();
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

}
