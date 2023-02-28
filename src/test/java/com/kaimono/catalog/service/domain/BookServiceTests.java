package com.kaimono.catalog.service.domain;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        var expectedIsbn = book.isbn();

        when(bookRepository.existsByIsbn(expectedIsbn))
                .thenReturn(true);

        assertThatThrownBy(() -> bookService.addBookToCatalog(book))
                .isInstanceOf(BookAlreadyExistsException.class)
                .hasMessage("A book with ISBN " + expectedIsbn + " already exists.");
    }

    @ParameterizedTest
    @ValueSource(strings = "1234561232")
    void whenBookToReadDoesNotExistThenThrows(String isbn) {
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.viewBookDetails(isbn))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("The book with ISBN " + isbn + " was not found.");
    }

}
