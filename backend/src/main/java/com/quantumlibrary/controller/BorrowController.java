package com.quantumlibrary.controller;

import com.quantumlibrary.dto.ApiResponse;
import com.quantumlibrary.dto.BorrowRequestDto;
import com.quantumlibrary.entity.BorrowRecord;
import com.quantumlibrary.entity.User;
import com.quantumlibrary.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BorrowController — handles borrowing and returning books.
 *
 *  Member:
 *    POST /api/borrow              → borrow a book
 *    POST /api/borrow/return/{id}  → return a book
 *    GET  /api/borrow/my           → my active borrows
 *    GET  /api/borrow/my/history   → my full borrow history (all)
 *
 *  Admin only:
 *    GET  /api/borrow/all          → all borrow records
 *    GET  /api/borrow/overdue      → all overdue borrows
 */
@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    /** Borrow a book — email confirmation is sent automatically */
    @PostMapping
    public ResponseEntity<ApiResponse<BorrowRecord>> borrow(
            @RequestBody BorrowRequestDto req,
            Authentication auth) {
        User user = (User) auth.getPrincipal();
        BorrowRecord record = borrowService.borrowBook(req.getBookId(), user.getId());
        return ResponseEntity.ok(ApiResponse.success(
            "Book borrowed! Confirmation email sent to " + user.getEmail(), record));
    }

    /**
     * Return a borrowed book.
     * Validates that the borrow record belongs to the authenticated user
     * (unless the caller is an Admin, who can process any return).
     */
    @PostMapping("/return/{borrowId}")
    public ResponseEntity<ApiResponse<BorrowRecord>> returnBook(
            @PathVariable Long borrowId,
            Authentication auth) {
        User user = (User) auth.getPrincipal();
        boolean isAdmin = user.getRole() == User.Role.ROLE_ADMIN;
        BorrowRecord record = borrowService.returnBook(borrowId, user.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(
            "Book returned successfully! Check your email for details.", record));
    }

    /** Get current member's active (not returned) borrows */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BorrowRecord>>> myBorrows(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(
            "Your active borrows", borrowService.getMemberBorrows(user.getId())));
    }

    /** Get current member's full borrow history (active + returned) */
    @GetMapping("/my/history")
    public ResponseEntity<ApiResponse<List<BorrowRecord>>> myBorrowHistory(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(
            "Your borrow history", borrowService.getMemberAllBorrows(user.getId())));
    }

    /** Admin: get all borrow records in the system */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<BorrowRecord>>> allBorrows() {
        return ResponseEntity.ok(ApiResponse.success("All borrows", borrowService.getAllBorrows()));
    }

    /** Admin: get all overdue borrow records */
    @GetMapping("/overdue")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<BorrowRecord>>> overdueBorrows() {
        List<BorrowRecord> overdue = borrowService.getOverdueBorrows();
        return ResponseEntity.ok(ApiResponse.success(
            "Overdue books: " + overdue.size(), overdue));
    }
}
