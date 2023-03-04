package com.kaimono.catalog.service.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;


@Validated
@ConfigurationProperties(prefix = "kaimono.faker.data")
public record KaimonoFakerDataProperties(

        @NotNull
        @Positive(message = "min-price cannot be negative.")
        Integer minPrice,

        @NotNull
        @Positive(message = "max-price cannot be negative.")
        Integer maxPrice,

        @NotNull
        @DurationMin(message = "frequency cannot be negative.")
        Duration frequency

) { }
