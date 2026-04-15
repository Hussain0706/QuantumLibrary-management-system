package com.quantumlibrary.scheduler;

import com.quantumlibrary.entity.BorrowRecord;
import com.quantumlibrary.entity.User;
import com.quantumlibrary.repository.BorrowRepository;
import com.quantumlibrary.repository.UserRepository;
import com.quantumlibrary.service.BookService;
import com.quantumlibrary.service.BorrowService;
import com.quantumlibrary.service.EmailService;
import com.quantumlibrary.service.FineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * LibraryScheduler — automated background jobs that run on schedule.
 *
 *  ┌─────────────────────────────────────────────────────────────────┐
 *  │  Job 1: Daily at 08:00 AM — Due-soon reminder emails (3 days)  │
 *  │  Job 2: Daily at 09:00 AM — Overdue detection + fine emails    │
 *  │  Job 3: Every Sunday at 10:00 AM — Admin weekly digest email   │
 *  └─────────────────────────────────────────────────────────────────┘
 *
 *  These jobs run automatically as long as the Spring Boot server is running.
 *  No manual action required.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LibraryScheduler {

    private final BorrowRepository borrowRepository;
    private final UserRepository   userRepository;
    private final EmailService     emailService;
    private final FineService      fineService;
    private final BorrowService    borrowService;
    private final BookService      bookService;

    // ══════════════════════════════════════════════════════════════
    //  JOB 1: Due-Soon Reminder — every day at 08:00 AM
    //  Sends a friendly reminder to members whose book is due in ≤3 days
    // ══════════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDueSoonReminders() {
        log.info("⏰ [SCHEDULER] Job 1 — Due-soon reminder check started");

        LocalDateTime now         = LocalDateTime.now();
        LocalDateTime threeDays   = now.plusDays(3);

        List<BorrowRecord> borrows = borrowRepository.findDueSoon(now, threeDays);

        if (borrows.isEmpty()) {
            log.info("⏰ [SCHEDULER] No due-soon books today. Skipping emails.");
            return;
        }

        for (BorrowRecord borrow : borrows) {
            emailService.sendDueSoonReminder(borrow.getUser(), borrow);
            log.info("  📧 Reminder → {} | '{}' due on {}",
                    borrow.getUser().getEmail(),
                    borrow.getBook().getTitle(),
                    borrow.getDueDate().toLocalDate());
        }

        log.info("⏰ [SCHEDULER] Job 1 complete — {} reminder(s) sent", borrows.size());
    }

    // ══════════════════════════════════════════════════════════════
    //  JOB 2: Overdue Detection — every day at 09:00 AM
    //  Recalculates fines and emails members with overdue books
    // ══════════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 9 * * *")
    public void processOverdueBooks() {
        log.info("🚨 [SCHEDULER] Job 2 — Overdue check started");

        List<BorrowRecord> overdue = borrowRepository.findOverdue(LocalDateTime.now());

        if (overdue.isEmpty()) {
            log.info("🚨 [SCHEDULER] No overdue books today.");
            return;
        }

        for (BorrowRecord borrow : overdue) {
            // Update fine in DB (amount grows each day)
            double fine = fineService.calculateAndSaveFine(borrow);

            // Send alert email
            emailService.sendOverdueAlert(borrow.getUser(), borrow, fine);

            log.info("  ⚠️  Overdue → {} | '{}' | Fine: ₹{}",
                    borrow.getUser().getEmail(),
                    borrow.getBook().getTitle(),
                    String.format("%.0f", fine));
        }

        log.info("🚨 [SCHEDULER] Job 2 complete — {} overdue book(s) processed", overdue.size());
    }

    // ══════════════════════════════════════════════════════════════
    //  JOB 3: Weekly Admin Digest — every Sunday at 10:00 AM
    //  Emails all admins a summary of the week's library activity
    // ══════════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 10 * * SUN")
    public void sendWeeklyAdminDigest() {
        log.info("📊 [SCHEDULER] Job 3 — Weekly admin digest started");

        List<User> admins = userRepository.findByRole(User.Role.ROLE_ADMIN);
        long totalBooks    = bookService.totalBooks();
        long totalMembers  = userRepository.findByRole(User.Role.ROLE_MEMBER).size();
        long activeBorrows = borrowService.countActiveBorrows();
        long overdueCount  = borrowService.getOverdueBorrows().size();
        Double collected   = fineService.totalCollected();

        for (User admin : admins) {
            emailService.sendWeeklyAdminDigest(
                admin.getEmail(), totalBooks, totalMembers,
                activeBorrows, overdueCount, collected
            );
            log.info("  📧 Weekly digest → {}", admin.getEmail());
        }

        log.info("📊 [SCHEDULER] Job 3 complete — digest sent to {} admin(s)", admins.size());
    }
}
