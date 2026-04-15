package com.quantumlibrary.service;

import com.quantumlibrary.entity.BorrowRecord;
import com.quantumlibrary.entity.Fine;
import com.quantumlibrary.entity.User;
import com.quantumlibrary.repository.FineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * FineService — fine calculation and payment management.
 *
 *  Rule: ₹5 per day after the due date (configurable via library.fine.per.day)
 *
 *  GET  /api/fines/my        → member's own fines
 *  GET  /api/fines/all       → all fines (Admin)
 *  POST /api/fines/pay/{id}  → mark fine paid + send receipt email
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FineService {

    private final FineRepository fineRepository;
    private final EmailService emailService;

    @Value("${library.fine.per.day:5}")
    private double finePerDay;

    /**
     * Calculates fine amount for a given borrow record.
     * Works for both returned books (uses returnDate) and still-active ones (uses now).
     */
    public double calculateFineAmount(BorrowRecord record) {
        LocalDateTime compareDate = (record.isReturned() && record.getReturnDate() != null)
                ? record.getReturnDate()
                : LocalDateTime.now();

        if (!compareDate.isAfter(record.getDueDate())) {
            return 0.0; // returned on time
        }

        long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), compareDate);
        return Math.max(0, overdueDays * finePerDay);
    }

    /**
     * Calculates the fine and persists it to the DB.
     * Idempotent: if a Fine row already exists for this borrow, it updates the amount.
     * Called by BorrowService on book return and by the nightly scheduler.
     *
     * @return fine amount in ₹ (0 if no fine)
     */
    @Transactional
    public double calculateAndSaveFine(BorrowRecord record) {
        double amount = calculateFineAmount(record);
        if (amount <= 0) return 0.0;

        Optional<Fine> existing = fineRepository.findByBorrowRecord_Id(record.getId());
        if (existing.isPresent()) {
            existing.get().setAmount(amount);
            fineRepository.save(existing.get());
            return amount;
        }

        Fine fine = Fine.builder()
                .borrowRecord(record)
                .user(record.getUser())
                .amount(amount)
                .paid(false)
                .build();

        fineRepository.save(fine);
        log.info("💸 Fine created: ₹{} for member {} | book '{}'",
                String.format("%.0f", amount),
                record.getUser().getEmail(),
                record.getBook().getTitle());
        return amount;
    }

    /** Member's own fines (paid + unpaid) */
    public List<Fine> getMemberFines(User user) {
        return fineRepository.findByUser(user);
    }

    /** All fines in the system (Admin view) */
    public List<Fine> getAllFines() {
        return fineRepository.findAll();
    }

    /** All unpaid fines (used by scheduler to update amounts daily) */
    public List<Fine> getUnpaidFines() {
        return fineRepository.findByPaidFalse();
    }

    /**
     * Admin marks a fine as paid (cash at counter).
     * Sends a receipt email to the member asynchronously.
     */
    @Transactional
    public Fine payFine(Long fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new IllegalArgumentException("Fine not found: " + fineId));

        if (fine.isPaid()) {
            throw new IllegalStateException("Fine is already marked as paid");
        }

        fine.setPaid(true);
        fine.setPaidDate(LocalDateTime.now());
        Fine saved = fineRepository.save(fine);

        // Send receipt email (async)
        emailService.sendFineReceipt(fine.getUser(), fine.getAmount());
        log.info("✅ Fine paid: ₹{} by {}", String.format("%.0f", fine.getAmount()),
                fine.getUser().getEmail());

        return saved;
    }

    // ── Stats helpers ──

    public Double totalCollected() {
        Double v = fineRepository.totalCollected();
        return v == null ? 0.0 : v;
    }

    public Double totalOutstanding() {
        Double v = fineRepository.totalOutstanding();
        return v == null ? 0.0 : v;
    }
}
