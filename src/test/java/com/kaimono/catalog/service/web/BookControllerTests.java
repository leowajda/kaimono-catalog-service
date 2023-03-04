package com.kaimono.catalog.service.web;

import com.kaimono.catalog.service.domain.BookNotFoundException;
import com.kaimono.catalog.service.domain.BookService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebFluxTest(BookController.class)
class BookControllerTests {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private BookService bookService;

    @ParameterizedTest
    @ValueSource(strings = { "1234567890", "1234567891", "1234567892" })
    void whenGetBookNotExistingThenShouldReturn404(String isbn) throws Exception {
        given(bookService.viewBookDetails(isbn))
                .willReturn(Mono.error(() ->
                        new BookNotFoundException(isbn)));

        webClient.get()
                .uri("/books/" + isbn)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class).value(body ->
                        assertThat(body).isEqualTo("The book with ISBN " + isbn + " was not found.")
                );
    }

}
