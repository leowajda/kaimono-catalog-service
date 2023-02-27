package com.kaimono.catalog.service;

import com.kaimono.catalog.service.domain.Book;
import com.kaimono.catalog.service.domain.CsvToBook;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KaimonoCatalogServiceApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, 9.90")
    void whenGetRequestWithIdThenBookReturned(@CsvToBook Book book) {
        var isbn = book.isbn();
        var expectedBook = webTestClient
                .post()
                .uri("/books")
                .bodyValue(book)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Book.class)
                .value(repoBook -> assertThat(repoBook).isNotNull())
                .returnResult()
                .getResponseBody();

        webTestClient.get()
                .uri("/books/" + isbn)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Book.class).value(actualBook -> {
                    assertThat(actualBook).isNotNull();
                    assertThat(actualBook.isbn()).isEqualTo(expectedBook.isbn());
                });
    }

    @ParameterizedTest
    @CsvSource("1234567893, Thus Spoke Zarathustra, Friedrich Nietzsche, 9.90")
    void whenPostRequestThenBookCreated(@CsvToBook Book book) {
        webTestClient
                .post()
                .uri("/books")
                .bodyValue(book)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Book.class).value(actualBook -> {
                    assertThat(actualBook).isNotNull();
                    assertThat(actualBook.isbn()).isEqualTo(book.isbn());
                });
    }

    @ParameterizedTest
    @CsvSource("1234567895, Thus Spoke Zarathustra, Friedrich Nietzsche, 9.90")
    void whenPutRequestThenBookUpdated(@CsvToBook Book book) {
        var createdBook = webTestClient
                .post()
                .uri("/books")
                .bodyValue(book)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Book.class).value(repoBook -> assertThat(repoBook).isNotNull())
                .returnResult()
                .getResponseBody();

        var updatedBook = new Book(createdBook.isbn(), createdBook.title(), createdBook.author(), 7.9);
        webTestClient
                .put()
                .uri("/books/" + book.isbn())
                .bodyValue(updatedBook)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Book.class).value(actualBook -> {
                    assertThat(actualBook).isNotNull();
                    assertThat(actualBook.price()).isEqualTo(updatedBook.price());
                });
    }

    @ParameterizedTest
    @CsvSource("1234567897, Thus Spoke Zarathustra, Friedrich Nietzsche, 9.90")
    void whenDeleteRequestThenBookDeleted(@CsvToBook Book book) {
        webTestClient
                .post()
                .uri("/books")
                .bodyValue(book)
                .exchange()
                .expectStatus().isCreated();

        webTestClient
                .delete()
                .uri("/books/" + book.isbn())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient
                .get()
                .uri("/books/" + book.isbn())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(errorMessage ->
                        assertThat(errorMessage).isEqualTo("The book with ISBN " + book.isbn() + " was not found.")
                );
    }


}
