package com.quantumlibrary.controller;

import com.quantumlibrary.dto.ApiResponse;
import com.quantumlibrary.entity.User;
import com.quantumlibrary.repository.BorrowRepository;
import com.quantumlibrary.repository.FineRepository;
import com.quantumlibrary.repository.UserRepository;
import com.quantumlibrary.service.BookService;
import com.quantumlibrary.service.BorrowService;
import com.quantumlibrary.service.FineService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminController — admin-only dashboard and member management.
 * All endpoints require ROLE_ADMIN (enforced by class-level @PreAuthorize).
 *
 *  GET    /api/admin/stats              → dashboard statistics
 *  GET    /api/admin/members            → all members list
 *  GET    /api/admin/members/{id}       → member detail
 *  DELETE /api/admin/members/{id}       → remove member (cascade-safe)
 *  PUT    /api/admin/members/{id}/toggle → activate/deactivate member
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final BookService      bookService;
    private final BorrowService    borrowService;
    private final FineService      fineService;
    private final UserRepository   userRepository;
    private final BorrowRepository borrowRepository;
    private final FineRepository   fineRepository;

    /**
     * Dashboard statistics — real-time data for the admin dashboard cards.
     *
     * Returns:
     *   totalBooks, availableBooks, totalMembers,
     *   activeBorrows, issuedToday, overdueCount,
     *   finesCollected, finesOutstanding
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBooks",       bookService.totalBooks());
        stats.put("availableBooks",   bookService.availableBooks());
        stats.put("totalMembers",     userRepository.findByRole(User.Role.ROLE_MEMBER).size());
        stats.put("activeBorrows",    borrowService.countActiveBorrows());
        stats.put("issuedToday",      borrowService.countIssuedToday());
        stats.put("overdueCount",     borrowService.getOverdueBorrows().size());
        stats.put("finesCollected",   fineService.totalCollected());
        stats.put("finesOutstanding", fineService.totalOutstanding());
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats", stats));
    }

    /** Get all registered members */
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<List<User>>> getAllMembers() {
        List<User> members = userRepository.findByRole(User.Role.ROLE_MEMBER);
        return ResponseEntity.ok(ApiResponse.success("Members: " + members.size(), members));
    }

    /** Get a single member's profile */
    @GetMapping("/members/{id}")
    public ResponseEntity<ApiResponse<User>> getMember(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + id));
        return ResponseEntity.ok(ApiResponse.success("Member found", user));
    }

    /**
     * Permanently remove a member account.
     * First deletes their fines and borrow records (FK cascade-safe).
     */
    @DeleteMapping("/members/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + id));

        // Delete fines first (FK: fines → borrow_records, users)
        fineRepository.deleteAll(fineRepository.findByUser(user));

        // Delete borrow records (FK: borrow_records → users)
        borrowRepository.deleteAll(borrowRepository.findByUser(user));

        // Now safe to delete the user
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }

    /** Toggle member account active/inactive (soft ban) */
    @PutMapping("/members/{id}/toggle")
    public ResponseEntity<ApiResponse<User>> toggleMemberStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + id));
        user.setActive(!user.isActive());
        User saved = userRepository.save(user);
        String status = saved.isActive() ? "activated ✅" : "deactivated 🚫";
        return ResponseEntity.ok(ApiResponse.success("Member account " + status, saved));
    }
}
