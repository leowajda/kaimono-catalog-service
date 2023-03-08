package com.kaimono.catalog.service.web;

import com.kaimono.catalog.service.config.SecurityConfig;
import com.kaimono.catalog.service.domain.Book;
import com.kaimono.catalog.service.domain.BookNotFoundException;
import com.kaimono.catalog.service.domain.BookService;
import junit.aggregator.book.CsvToBook;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.BDDMockito.given;

@Import(SecurityConfig.class)
@WebFluxTest(BookController.class)
class BookControllerTests {

    private static final SimpleGrantedAuthority CUSTOMER_ROLE =
            new SimpleGrantedAuthority("ROLE_customer");

    private static final SimpleGrantedAuthority EMPLOYEE_ROLE =
            new SimpleGrantedAuthority("ROLE_employee");

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private BookService bookService;

    @MockBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    public void whenGetBookExistingAndAuthenticatedThenShouldReturn200(@CsvToBook Book book) {
        given(bookService.viewBookDetails(book.isbn()))
                .willReturn(Mono.just(book));

        var mockedJwt = SecurityMockServerConfigurers.mockJwt()
                .authorities(CUSTOMER_ROLE);

        webClient
                .mutateWith(mockedJwt)
                    .get()
                        .uri("/books/" + book.isbn())
                            .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                    .isOk()
                .expectBody(Book.class)
                    .isEqualTo(book);
    }

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    public void whenGetBookExistingAndNotAuthenticatedThenShouldReturn200(@CsvToBook Book book) {
        given(bookService.viewBookDetails(book.isbn()))
                .willReturn(Mono.just(book));

        webClient
                .get()
                    .uri("/books/" + book.isbn())
                        .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                    .isOk()
                .expectBody(Book.class)
                    .isEqualTo(book);
    }

    @ParameterizedTest
    @ValueSource(strings = { "1234567890", "1234567891", "1234567892" })
    public void whenGetBookNotExistingAndAuthenticatedThenShouldReturn404(String isbn) {
        given(bookService.viewBookDetails(isbn))
                .willReturn(Mono.error(new BookNotFoundException(isbn)));

        var mockedJwt = SecurityMockServerConfigurers.mockJwt()
                .authorities(CUSTOMER_ROLE);

        webClient
                .mutateWith(mockedJwt)
                    .get()
                        .uri("/books/" + isbn)
                            .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                    .isNotFound()
                .expectBody(String.class)
                    .isEqualTo("The book with ISBN " + isbn + " was not found.");
    }

    @ParameterizedTest
    @ValueSource(strings = { "1234567890", "1234567891", "1234567892" })
    public void whenGetBookNotExistingAndNotAuthenticatedThenShouldReturn404(String isbn) {
        given(bookService.viewBookDetails(isbn))
                .willReturn(Mono.error(new BookNotFoundException(isbn)));

        webClient
                .get()
                    .uri("/books/" + isbn)
                        .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                    .isNotFound()
                .expectBody(String.class)
                    .isEqualTo("The book with ISBN " + isbn + " was not found.");
    }

    @ParameterizedTest
    @ValueSource(strings = { "1234567890", "1234567891", "1234567892" })
    public void whenDeleteBookWithEmployeeRoleThenShouldReturn204(String isbn) {
        var mockedJwt = SecurityMockServerConfigurers.mockJwt()
                .authorities(EMPLOYEE_ROLE);
        webClient
                .mutateWith(mockedJwt)
                    .delete()
                        .uri("/books/" + isbn)
                            .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                    .isNoContent()
                .expectBody()
                    .isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1234567890", "1234567891", "1234567892" })
    public void whenDeleteBookWithCustomerRoleThenShouldReturn403(String isbn) {
        var mockedJwt = SecurityMockServerConfigurers.mockJwt()
                .authorities(CUSTOMER_ROLE);

        webClient
                .mutateWith(mockedJwt)
                    .delete()
                        .uri("/books/" + isbn)
                            .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                    .isForbidden()
                .expectBody()
                    .isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1234567890", "1234567891", "1234567892" })
    public void whenDeleteBookNotAuthenticatedThenShouldReturn401(String isbn) {
        webClient
                .delete()
                    .uri("/books/" + isbn)
                        .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                    .isUnauthorized()
                .expectBody()
                    .isEmpty();
    }

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    public void whenPostBookWithEmployeeRoleThenShouldReturn201(@CsvToBook Book book) {
        given(bookService.addBookToCatalog(book))
                .willReturn(Mono.just(book));

        var mockedJwt = SecurityMockServerConfigurers.mockJwt()
                .authorities(EMPLOYEE_ROLE);

        webClient
                .mutateWith(mockedJwt)
                    .post()
                        .uri("/books")
                            .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(book)
                .exchange()
                .expectStatus()
                    .isCreated()
                .expectBody(Book.class)
                    .isEqualTo(book);
    }

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    public void whenPostBookWithCustomerRoleThenShouldReturn403(@CsvToBook Book book) {
        var mockedJwt = SecurityMockServerConfigurers.mockJwt()
                .authorities(CUSTOMER_ROLE);

        webClient
                .mutateWith(mockedJwt)
                    .post()
                        .uri("/books")
                            .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(book)
                .exchange()
                .expectStatus()
                    .isForbidden()
                .expectBody()
                    .isEmpty();
    }


    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    public void whenPostBookAndNotAuthenticatedThenShouldReturn403(@CsvToBook Book book) {
        webClient
                .post()
                    .uri("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(book)
                .exchange()
                .expectStatus()
                    .isUnauthorized()
                .expectBody()
                    .isEmpty();
    }

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    public void whenPutBookWithEmployeeRoleThenShouldReturn200(@CsvToBook Book book) {
        given(bookService.editBookDetails(book.isbn(), book))
                .willReturn(Mono.just(book));

        var mockedJwt = SecurityMockServerConfigurers.mockJwt()
                .authorities(EMPLOYEE_ROLE);

        webClient
                .mutateWith(mockedJwt)
                    .put()
                        .uri("/books/" + book.isbn())
                            .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(book)
                .exchange()
                .expectStatus()
                    .isOk()
                .expectBody(Book.class)
                    .isEqualTo(book);
    }

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    public void whenPutBookWithCustomerRoleThenShouldReturn403(@CsvToBook Book book) {
            var mockedJwt = SecurityMockServerConfigurers.mockJwt()
                    .authorities(CUSTOMER_ROLE);

            webClient
                    .mutateWith(mockedJwt)
                        .put()
                            .uri("/books" + book.isbn())
                                .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(book)
                    .exchange()
                    .expectStatus()
                        .isForbidden()
                    .expectBody()
                        .isEmpty();
    }

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    public void whenPutBookAndNotAuthenticatedThenShouldReturn401(@CsvToBook Book book) {
        webClient
                .put()
                    .uri("/books/" + book.isbn())
                        .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(book)
                .exchange()
                .expectStatus()
                    .isUnauthorized()
                .expectBody()
                    .isEmpty();
    }

}
