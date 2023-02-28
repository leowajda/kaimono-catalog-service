package com.kaimono.catalog.service.faker;

import com.kaimono.catalog.service.config.KaimonoFakerDataProperties;
import com.kaimono.catalog.service.domain.Book;
import com.kaimono.catalog.service.domain.BookRepository;
import net.datafaker.Faker;
import net.datafaker.providers.base.ProviderRegistration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@ConditionalOnProperty(value = "kaimono.faker.enabled", havingValue = "true")
public class KaimonoFakerLoader {

    private final KaimonoFakerDataProperties kaimonoFakerDataProperties;
    private final BookRepository bookRepository;

    private final ProviderRegistration isbnFaker;
    private final Faker bookFaker;

    public KaimonoFakerLoader(BookRepository bookRepository, KaimonoFakerDataProperties kaimonoFakerDataProperties) {
        this.kaimonoFakerDataProperties = kaimonoFakerDataProperties;
        this.bookRepository = bookRepository;
        this.isbnFaker = new Faker().unique().getFaker();
        this.bookFaker = new Faker();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadFakeBooks() {
        bookRepository.deleteAll();

        var fakeBooks = Stream.generate(this::getFakeBook)
                .peek(System.out::println)
                .limit(kaimonoFakerDataProperties.amount())
                .toList();

        bookRepository.saveAll(fakeBooks);
    }

    private Book getFakeBook() {
        return Book.of(
                isbnFaker.regexify("([0-9]{10}|[0-9]{13})"),
                bookFaker.book().title(),
                bookFaker.book().author(),
                bookFaker.book().publisher(),
                (double) bookFaker.random().nextInt(
                        kaimonoFakerDataProperties.minPrice(),
                        kaimonoFakerDataProperties.maxPrice())
        );
    }

}
