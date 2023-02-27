package com.kaimono.catalog.service.web;

import com.kaimono.catalog.service.domain.Book;
import com.kaimono.catalog.service.domain.CsvToBook;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookJsonTests {
    @Autowired
    private JacksonTester<Book> json;
    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, 9.90")
    void testSerialize(@CsvToBook Book book) throws Exception {
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
                .extractingJsonPathNumberValue("@.price")
                .isEqualTo(book.price());
    }

    @ParameterizedTest
    @ValueSource(strings = """
           {
                "isbn": "1234567890",
                "title": "Thus Spoke Zarathustra",
                "author": "Friedrich Nietzsche",
                "price": 9.90
           }
            """)
    void testDeserialize(String content) throws Exception {
        var expectedBook = new Book("1234567890", "Thus Spoke Zarathustra", "Friedrich Nietzsche", 9.90);

        assertThat(json.parse(content))
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);
    }

}
