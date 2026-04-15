package com.quantumlibrary.service;

import com.quantumlibrary.entity.Book;
import com.quantumlibrary.entity.User;
import com.quantumlibrary.repository.BookRepository;
import com.quantumlibrary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * BookService — full CRUD for the book catalog.
 *
 *  GET    /api/books            → list / search / filter
 *  GET    /api/books/{id}       → single book
 *  POST   /api/books            → add book (Admin only)
 *  PUT    /api/books/{id}       → update book (Admin only)
 *  DELETE /api/books/{id}       → delete book (Admin only)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + id));
    }

    /** Search by title / author / genre (full-text LIKE query) */
    public List<Book> search(String query) {
        if (!StringUtils.hasText(query)) return bookRepository.findAll();
        return bookRepository.search(query.trim());
    }

    /** Filter books by exact genre name (case-insensitive) */
    public List<Book> filterByGenre(String genre) {
        return bookRepository.findByGenreIgnoreCase(genre);
    }

    /**
     * Admin: add a new book.
     * If notifyMembers=true, sends a "New Arrival!" email to all active members.
     */
    public Book addBook(Book book, boolean notifyMembers) {
        Book saved = bookRepository.save(book);
        log.info("📚 New book added: '{}' by {} (stock: {})",
                saved.getTitle(), saved.getAuthor(), saved.getStock());

        if (notifyMembers) {
            List<User> members = userRepository.findByRole(User.Role.ROLE_MEMBER);
            emailService.sendNewBookAlert(members, saved);
            log.info("📧 New book email sent to {} member(s)", members.size());
        }
        return saved;
    }

    /** Admin: update book details or adjust stock */
    public Book updateBook(Long id, Book updated) {
        Book existing = getById(id);
        existing.setTitle(updated.getTitle());
        existing.setAuthor(updated.getAuthor());
        existing.setGenre(updated.getGenre());
        existing.setYear(updated.getYear());
        existing.setIsbn(updated.getIsbn());
        existing.setStock(updated.getStock());
        existing.setDescription(updated.getDescription());
        existing.setCoverUrl(updated.getCoverUrl());
        existing.setRating(updated.getRating());
        return bookRepository.save(existing);
    }

    /** Admin: remove a book from the catalog */
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
        log.info("🗑️  Book deleted: id={}", id);
    }

    // ── Stats helpers used by AdminController ──

    public long totalBooks()     { return bookRepository.count(); }
    public long availableBooks() { return bookRepository.countByStockGreaterThan(0); }
}
