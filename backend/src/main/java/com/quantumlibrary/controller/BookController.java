package com.quantumlibrary.controller;

import com.quantumlibrary.dto.ApiResponse;
import com.quantumlibrary.entity.Book;
import com.quantumlibrary.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BookController — REST endpoints for the book catalog.
 *
 *  Public (no auth):
 *    GET  /api/books               → list all books (supports ?search= and ?genre=)
 *    GET  /api/books/{id}          → single book detail
 *
 *  Admin only:
 *    POST   /api/books             → add new book (?notifyMembers=true to email all)
 *    PUT    /api/books/{id}        → update book
 *    DELETE /api/books/{id}        → remove book
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /** List all books with optional search/filter */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Book>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre) {

        List<Book> books;
        if (search != null && !search.isBlank()) {
            books = bookService.search(search);
        } else if (genre != null && !genre.isBlank()) {
            books = bookService.filterByGenre(genre);
        } else {
            books = bookService.getAllBooks();
        }
        return ResponseEntity.ok(ApiResponse.success("Books fetched: " + books.size(), books));
    }

    /** Get a single book by ID */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Book>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Book found", bookService.getById(id)));
    }

    /** Admin: add a new book to the catalog */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Book>> addBook(
            @RequestBody Book book,
            @RequestParam(defaultValue = "false") boolean notifyMembers) {
        Book saved = bookService.addBook(book, notifyMembers);
        return ResponseEntity.ok(ApiResponse.success("Book added successfully!", saved));
    }

    /** Admin: update an existing book */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Book>> updateBook(
            @PathVariable Long id, @RequestBody Book book) {
        return ResponseEntity.ok(ApiResponse.success("Book updated", bookService.updateBook(id, book)));
    }

    /** Admin: delete a book */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Book deleted", null));
    }
}
