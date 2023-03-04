package com.kaimono.catalog.service.web;

import com.kaimono.catalog.service.domain.Book;
import com.kaimono.catalog.service.domain.BookAlreadyExistsException;
import com.kaimono.catalog.service.domain.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Flux<Book> get() {
        return bookService.viewBookList();
    }

    @GetMapping("{isbn}")
    public Mono<Book> getByIsbn(@PathVariable String isbn) {
        return bookService.viewBookDetails(isbn);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Book> post(@RequestBody @Valid Mono<Book> book) {
        return book.flatMap(bookService::addBookToCatalog)
                .onErrorResume(BookAlreadyExistsException.class, Mono::error)
                .onErrorResume(WebExchangeBindException.class, Mono::error);
    }

    @DeleteMapping("{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String isbn) {
        return bookService.removeBookFromCatalog(isbn);
    }

    @PutMapping("{isbn}")
    public Mono<Book> put(@PathVariable String isbn, @RequestBody @Valid Mono<Book> book) {
        return book.flatMap(resolvedBook -> bookService.editBookDetails(isbn, resolvedBook))
                .onErrorResume(WebExchangeBindException.class, Mono::error);
    }

}
