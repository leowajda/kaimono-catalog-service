package com.kaimono.catalog.service.domain;

import junit.aggregator.book.CsvToBook;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceTests {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    void whenBookToCreateAlreadyExistsThenThrows(@CsvToBook Book book) {
        when(bookRepository.findByIsbn(book.isbn()))
                .thenReturn(Mono.error(new BookAlreadyExistsException(book.isbn())));

        StepVerifier.create(bookService.addBookToCatalog(book))
                .verifyErrorMessage("A book with ISBN " + book.isbn() + " already exists.");
    }

    @ParameterizedTest
    @ValueSource(strings = "1234561232")
    void whenBookToReadDoesNotExistThenThrows(String isbn) {
        when(bookRepository.findByIsbn(isbn))
                .thenReturn(Mono.error(new BookNotFoundException(isbn)));

        StepVerifier.create(bookService.viewBookDetails(isbn))
                .verifyErrorMessage("The book with ISBN " + isbn + " was not found.");
    }

}
