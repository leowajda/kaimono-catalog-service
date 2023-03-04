package com.kaimono.catalog.service.domain;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Flux<Book> viewBookList() {
        return bookRepository.findAll();
    }

    public Mono<Book> viewBookDetails(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .switchIfEmpty(Mono.error(() ->
                        new BookNotFoundException(isbn)));
    }

    public Mono<Book> addBookToCatalog(Book book) {
        return bookRepository.findByIsbn(book.isbn())
                .flatMap(prevBook ->
                        Mono.<Book>error(() ->
                                new BookAlreadyExistsException(prevBook.isbn())))
                .switchIfEmpty(Mono.fromSupplier(() -> book)
                        .flatMap(bookRepository::save));
    }

    public Mono<Void> removeBookFromCatalog(String isbn) {
        return bookRepository.deleteByIsbn(isbn);
    }

    public Mono<Book> editBookDetails(String isbn, Book newBook) {
        return bookRepository.findByIsbn(isbn)
                .flatMap(prevBook ->
                        Mono.fromSupplier(() ->
                                editBook(prevBook, newBook)).flatMap(bookRepository::save))
                .switchIfEmpty(Mono.fromSupplier(() -> newBook)
                        .flatMap(bookRepository::save));
    }

    private static Book editBook(Book prevBook, Book newBook) {
        return new Book(
                prevBook.id(),
                prevBook.isbn(),
                newBook.title(),
                newBook.author(),
                newBook.publisher(),
                newBook.price(),
                prevBook.createdDate(),
                prevBook.lastModifiedDate(),
                prevBook.version()
        );
    }

}
