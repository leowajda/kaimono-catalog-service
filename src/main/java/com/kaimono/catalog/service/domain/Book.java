package com.kaimono.catalog.service.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.*;

import java.time.Instant;

public record Book(

        @Id
        Long id,

        @NotBlank(message = "The book ISBN must be defined.")
        @Pattern(regexp = "^([0-9]{10}|[0-9]{13})$", message = "The ISBN format must be valid.")
        String isbn,

        @NotBlank(message = "The book title must be defined.")
        String title,

        @NotBlank(message = "The book author must be defined.")
        String author,

        @NotBlank(message = "The publisher must be defined.")
        String publisher,

        @NotNull(message = "The book price must be defined.")
        @Positive(message = "The book price must be greater than zero.")
        Double price,

        @CreatedDate
        Instant createdDate,

        @LastModifiedDate
        Instant lastModifiedDate,

        @Version
        int version,

        @CreatedBy
        String createdBy,

        @LastModifiedBy
        String lastModifiedBy

) {

        public static Book of(String isbn, String title, String author, String publisher, Double price) {
                return new Book(null, isbn, title, author, publisher, price, null, null,0, null, null);
        }

}
