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
import reactor.core.publisher.Flux;

import java.util.stream.Stream;

@Component
@ConditionalOnProperty(value = "kaimono.faker.enabled", havingValue = "true")
public class KaimonoFakerLoader {

    private final KaimonoFakerDataProperties kaimonoFakerDataProperties;
    private final BookRepository bookRepository;
    private final ProviderRegistration isbnFaker;
    private final Faker bookFaker;

    public KaimonoFakerLoader(KaimonoFakerDataProperties kaimonoFakerDataProperties, BookRepository bookRepository) {
        this.kaimonoFakerDataProperties = kaimonoFakerDataProperties;
        this.bookRepository = bookRepository;
        this.isbnFaker = new Faker().unique().getFaker();
        this.bookFaker = new Faker();
    }

    @EventListener(ApplicationReadyEvent.class)
    public Flux<Book> loadFakeBooks() {
        return Flux.fromStream(Stream.generate(this::getFakeBook))
                .flatMap(bookRepository::save)
                .doOnEach(System.out::println) // TODO - replace with logs
                .delayElements(kaimonoFakerDataProperties.frequency());
    }

    private Book getFakeBook() {

        var fakeBook = bookFaker.book();
        var fakeIsbn = isbnFaker.regexify("([0-9]{10}|[0-9]{13})");
        var fakePrice = (double) bookFaker.random().nextInt(
                kaimonoFakerDataProperties.minPrice(),
                kaimonoFakerDataProperties.maxPrice()
        );

        return Book.of(fakeIsbn, fakeBook.title(), fakeBook.author(), fakeBook.publisher(), fakePrice);
    }

}
