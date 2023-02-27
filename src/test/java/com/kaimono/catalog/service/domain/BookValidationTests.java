package com.kaimono.catalog.service.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class BookValidationTests {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        var factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, 9.90")
    public void whenAllFieldsCorrectThenValidationSucceeds(@CsvToBook Book book) {
        var violations = validator.validate(book);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @CsvSource("'', Thus Spoke Zarathustra, Friedrich Nietzsche, 9.90")
    void whenIsbnNotDefinedThenValidationFails(@CsvToBook Book book) {
        var violations = validator.validate(book);
        assertThat(violations).hasSize(2);

        var constraintViolationMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        assertThat(constraintViolationMessages)
                .contains("The book ISBN must be defined.")
                .contains("The ISBN format must be valid.");
    }

    @ParameterizedTest
    @CsvSource("AV34567890, Thus Spoke Zarathustra, Friedrich Nietzsche, 9.90")
    void whenIsbnDefinedButIncorrectThenValidationFails(@CsvToBook Book book) {
        var violations = validator.validate(book);
        assertThat(violations).hasSize(1);

        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("The ISBN format must be valid.");
    }

    @ParameterizedTest
    @CsvSource("1234567890, '', Friedrich Nietzsche, 9.90")
    void whenTitleIsNotDefinedThenValidationFails(@CsvToBook Book book) {
        var violations = validator.validate(book);
        assertThat(violations).hasSize(1);

        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("The book title must be defined.");
    }

    @ParameterizedTest
    @CsvSource(value = "1234567890, Thus Spoke Zarathustra, N/A, 9.90", nullValues = "N/A")
    void whenAuthorIsNotDefinedThenValidationFails(@CsvToBook Book book) {
        var violations = validator.validate(book);
        assertThat(violations).hasSize(1);

        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("The book author must be defined.");
    }

    @ParameterizedTest
    @CsvSource(value = "1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, N/A", nullValues = "N/A")
    void whenPriceIsNotDefinedThenValidationFails(@CsvToBook Book book) {
        var violations = validator.validate(book);
        assertThat(violations).hasSize(1);

        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("The book price must be defined.");
    }

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, 0.0")
    void whenPriceDefinedButZeroThenValidationFails(@CsvToBook Book book) {
        var violations = validator.validate(book);
        assertThat(violations).hasSize(1);

        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("The book price must be greater than zero.");
    }

    @ParameterizedTest
    @CsvSource("1234567890, Thus Spoke Zarathustra, Friedrich Nietzsche, -9.90")
    void whenPriceDefinedButNegativeThenValidationFails(@CsvToBook Book book) {
        var violations = validator.validate(book);
        assertThat(violations).hasSize(1);

        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("The book price must be greater than zero.");
    }

}
