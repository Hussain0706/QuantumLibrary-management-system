package com.quantumlibrary.service;

import com.quantumlibrary.entity.Book;
import com.quantumlibrary.entity.BorrowRecord;
import com.quantumlibrary.entity.User;
import com.quantumlibrary.exception.BorrowLimitException;
import com.quantumlibrary.repository.BookRepository;
import com.quantumlibrary.repository.BorrowRepository;
import com.quantumlibrary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * BorrowService — core borrowing and returning logic.
 *
 *  Business Rules:
 *  ✅ Max 3 active borrows per member (configurable)
 *  ✅ Book stock must be > 0 to borrow
 *  ✅ Due date = borrowDate + 14 days (configurable)
 *  ✅ Fine calculated automatically on return
 *  ✅ Email confirmation sent on borrow and return
 *  ✅ Ownership check: members can only return their own books
 *
 *  POST /api/borrow               → borrow a book
 *  POST /api/borrow/return/{id}   → return a book (with ownership check)
 *  GET  /api/borrow/my            → member's active borrows
 *  GET  /api/borrow/my/history    → member's ALL borrows (history)
 *  GET  /api/borrow/all           → all records (Admin)
 *  GET  /api/borrow/overdue       → overdue records (Admin)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowService {

    private final BorrowRepository borrowRepository;
    private final BookRepository   bookRepository;
    private final UserRepository   userRepository;
    private final FineService      fineService;
    private final EmailService     emailService;

    @Value("${library.borrow.days:14}")
    private int borrowDays;

    @Value("${library.max.borrows:3}")
    private int maxBorrows;

    /**
     * Borrow a book for a member.
     *
     * Checks:
     *  1. User exists
     *  2. Book exists
     *  3. Book has stock > 0
     *  4. Member hasn't hit their 3-book limit
     *
     * On success:
     *  - Decrements book stock
     *  - Creates BorrowRecord with 14-day due date
     *  - Sends borrow confirmation email
     */
    @Transactional
    public BorrowRecord borrowBook(Long bookId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        if (book.getStock() <= 0) {
            throw new IllegalStateException(
                "'" + book.getTitle() + "' is currently out of stock. Please check back later.");
        }

        List<BorrowRecord> active = borrowRepository.findByUserAndReturnedFalse(user);
        if (active.size() >= maxBorrows) {
            throw new BorrowLimitException(
                "You have reached the maximum borrow limit (" + maxBorrows +
                " books). Please return a book to continue borrowing.");
        }

        // Deduct stock
        book.setStock(book.getStock() - 1);
        bookRepository.save(book);

        // Create borrow record
        BorrowRecord record = BorrowRecord.builder()
                .user(user)
                .book(book)
                .borrowDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(borrowDays))
                .returned(false)
                .build();

        BorrowRecord saved = borrowRepository.save(record);
        log.info("📖 Book borrowed: '{}' by {} | Due: {}",
                book.getTitle(), user.getEmail(), saved.getDueDate().toLocalDate());

        // Email confirmation (async)
        emailService.sendBorrowConfirmation(user, saved);

        return saved;
    }

    /**
     * Return a borrowed book.
     *
     * Security:
     *  - Validates the borrow record belongs to the requesting user.
     *  - Admins can return any book (isAdmin = true bypasses ownership check).
     *
     * On success:
     *  - Marks record as returned
     *  - Increments book stock back
     *  - Calculates and saves fine (if overdue)
     *  - Sends return confirmation email (with fine if applicable)
     */
    @Transactional
    public BorrowRecord returnBook(Long borrowId, Long requestingUserId, boolean isAdmin) {
        BorrowRecord record = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Borrow record not found: " + borrowId));

        if (record.isReturned()) {
            throw new IllegalStateException("This book has already been returned.");
        }

        // Ownership check — members can only return their own books
        if (!isAdmin && !record.getUser().getId().equals(requestingUserId)) {
            throw new IllegalStateException("You can only return books you have borrowed.");
        }

        record.setReturned(true);
        record.setReturnDate(LocalDateTime.now());
        borrowRepository.save(record);

        // Restore stock
        Book book = record.getBook();
        book.setStock(book.getStock() + 1);
        bookRepository.save(book);

        // Calculate + persist fine
        double fine = fineService.calculateAndSaveFine(record);

        // Email confirmation (async)
        emailService.sendReturnConfirmation(record.getUser(), record, fine);

        log.info("✅ Book returned: '{}' by {} | Fine: ₹{}",
                book.getTitle(), record.getUser().getEmail(),
                String.format("%.0f", fine));

        return record;
    }

    // ── Query methods ──

    /** Active (unreturned) borrows for a specific member */
    public List<BorrowRecord> getMemberBorrows(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return borrowRepository.findByUserAndReturnedFalse(user);
    }

    /** ALL borrows for a specific member (active + returned) — for history view */
    public List<BorrowRecord> getMemberAllBorrows(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return borrowRepository.findByUser(user);
    }

    /** All borrow records (Admin dashboard) */
    public List<BorrowRecord> getAllBorrows() {
        return borrowRepository.findAll();
    }

    /** All overdue borrow records (past due date, not returned) */
    public List<BorrowRecord> getOverdueBorrows() {
        return borrowRepository.findOverdue(LocalDateTime.now());
    }

    // ── Stats helpers used by AdminController ──

    public long countActiveBorrows() {
        return borrowRepository.countByReturnedFalse();
    }

    public long countIssuedToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end   = start.plusDays(1);
        return borrowRepository.countBorrowedToday(start, end);
    }
}
