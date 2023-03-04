package com.kaimono.catalog.service.domain;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

public interface BookRepository extends ReactiveCrudRepository<Book, Long> {

    @Modifying
    @Transactional
    @Query("delete from book where isbn = :isbn")
    Mono<Void> deleteByIsbn(String isbn);

    Mono<Book> findByIsbn(String isbn);
}
