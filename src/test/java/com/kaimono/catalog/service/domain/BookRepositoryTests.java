package com.kaimono.catalog.service.domain;

import com.kaimono.catalog.service.config.DataConfig;
import junit.aggregator.book.CsvToBook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@Import(DataConfig.class)
@ActiveProfiles("integration")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BookRepositoryJdbcTests {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @BeforeEach
    public void beforeEach() {
        bookRepository.deleteAll();
    }

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    public void findBookByIsbnWhenExisting(@CsvToBook Book book) {
        jdbcAggregateTemplate.insert(book);

        var retrievedBook = bookRepository.findByIsbn(book.isbn());

        assertThat(retrievedBook).isPresent();
        assertThat(retrievedBook.get().isbn()).isEqualTo(book.isbn());
    }

    @ParameterizedTest
    @ValueSource(strings = "1234561238")
    void findBookByIsbnWhenNotExisting(String isbn) {
        Optional<Book> actualBook = bookRepository.findByIsbn(isbn);
        assertThat(actualBook).isEmpty();
    }

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    void existsByIsbnWhenExisting(@CsvToBook Book book) {
        jdbcAggregateTemplate.insert(book);

        boolean exists = bookRepository.existsByIsbn(book.isbn());
        assertThat(exists).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = "1234561238")
    void existsByIsbnWhenNotExisting(String isbn) {
        boolean existing = bookRepository.existsByIsbn(isbn);
        assertThat(existing).isFalse();
    }

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    void deleteByIsbn(@CsvToBook Book book) {

        var persistedBook = jdbcAggregateTemplate.insert(book);
        bookRepository.deleteByIsbn(book.isbn());
        assertThat(jdbcAggregateTemplate.findById(persistedBook.id(), Book.class)).isNull();
    }

}
