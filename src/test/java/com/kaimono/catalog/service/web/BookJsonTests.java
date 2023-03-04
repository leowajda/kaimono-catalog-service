package com.kaimono.catalog.service.web;

import com.kaimono.catalog.service.domain.Book;
import junit.aggregator.book.CsvToBook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookJsonTests {

    @Autowired
    private JacksonTester<Book> json;

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, Adelphi, 9.90")
    public void testSerialize(@CsvToBook Book book) throws Exception {
        var jsonContent = json.write(book);

        assertThat(jsonContent)
                .extractingJsonPathStringValue("@.isbn")
                .isEqualTo(book.isbn());

        assertThat(jsonContent)
                .extractingJsonPathStringValue("@.title")
                .isEqualTo(book.title());

        assertThat(jsonContent)
                .extractingJsonPathStringValue("@.author")
                .isEqualTo(book.author());

        assertThat(jsonContent)
                .extractingJsonPathStringValue("@.publisher")
                .isEqualTo(book.publisher());

        assertThat(jsonContent)
                .extractingJsonPathNumberValue("@.price")
                .isEqualTo(book.price());
    }

    @Test
    public void testDeserialize() throws Exception {

        var content = """
            {
                "id": 1,
                "isbn": "1234567890",
                "title": "Thus Spoke Zarathustra",
                "author": "Friedrich Nietzsche",
                "publisher": "Adelphi",
                "price": 9.90,
                "createdDate": "2023-02-28T15:13:38.312934931Z",
                "lastModifiedDate": "2023-02-28T15:13:38.312934931Z",
                "version": 0
            }
        """;

        var expectedBook = new Book(
                1L,
                "1234567890",
                "Thus Spoke Zarathustra",
                "Friedrich Nietzsche",
                "Adelphi",
                9.90,
                Instant.parse("2023-02-28T15:13:38.312934931Z"),
                Instant.parse("2023-02-28T15:13:38.312934931Z"),
                0);

        assertThat(json.parse(content))
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);
    }

}
