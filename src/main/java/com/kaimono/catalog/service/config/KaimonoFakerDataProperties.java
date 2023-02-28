package com.kaimono.catalog.service.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@Validated
@ConfigurationProperties(prefix = "kaimono.faker.data")
public record KaimonoFakerDataProperties(

        @NotNull
        @Positive(message = "amount cannot be negative.")
        Integer amount,

        @NotNull
        @Positive(message = "min-price cannot be negative.")
        Integer minPrice,

        @NotNull
        @Positive(message = "max-price cannot be negative.")
        Integer maxPrice

) { }
