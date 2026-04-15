package com.quantumlibrary.service;

import com.quantumlibrary.entity.Book;
import com.quantumlibrary.entity.BorrowRecord;
import com.quantumlibrary.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════
 *  EmailService — All 7 automated email notification types
 * ═══════════════════════════════════════════════════════════
 *
 *  1. sendWelcomeEmail        — Member registration
 *  2. sendBorrowConfirmation  — Book borrowed
 *  3. sendReturnConfirmation  — Book returned (with fine if any)
 *  4. sendDueSoonReminder     — 3 days before due date
 *  5. sendOverdueAlert        — Book is overdue + current fine
 *  6. sendFineReceipt         — Fine paid at counter
 *  7. sendNewBookAlert        — New book added (to all members)
 *  8. sendWeeklyAdminDigest   — Sunday stats digest to admin
 *
 *  All methods are @Async — they run in a background thread
 *  so the REST response is returned instantly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${library.email.from}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ──────────────────────────────────────────────────────────
    //  1. WELCOME EMAIL (fired on member registration)
    // ──────────────────────────────────────────────────────────
    @Async
    public void sendWelcomeEmail(User user) {
        String subject = "📚 Welcome to QuantumLibrary!";
        String body = buildTemplate(
            "Welcome to QuantumLibrary! 🎉",
            "Your account has been created successfully.",
            "<p>Hi <strong>" + user.getName() + "</strong>, you are now a registered member of QuantumLibrary.</p>" +
            "<p><strong>Library Rules:</strong></p>" +
            "<ul style='line-height:1.9;'>" +
            "<li>📖 Borrow up to <strong>3 books</strong> at a time</li>" +
            "<li>📅 Return within <strong>14 days</strong></li>" +
            "<li>💸 Fine: <strong>₹5 per day</strong> after the due date</li>" +
            "<li>🔄 Contact the librarian for renewals</li>" +
            "</ul>",
            "Visit Library Portal", "http://localhost:5500"
        );
        send(user.getEmail(), subject, body);
    }

    // ──────────────────────────────────────────────────────────
    //  2. BORROW CONFIRMATION EMAIL
    // ──────────────────────────────────────────────────────────
    @Async
    public void sendBorrowConfirmation(User user, BorrowRecord borrow) {
        String subject = "📖 Book Borrowed: " + borrow.getBook().getTitle();
        String body = buildTemplate(
            "Book Borrowed Successfully! 📖",
            "Here are the details of your borrowed book.",
            "<table style='width:100%;border-collapse:collapse;margin-bottom:16px;'>" +
            row("Book",        borrow.getBook().getTitle()) +
            row("Author",      borrow.getBook().getAuthor()) +
            row("Borrow Date", borrow.getBorrowDate().format(DATE_FMT)) +
            rowHighlight("Due Date", borrow.getDueDate().format(DATE_FMT)) +
            "</table>" +
            "<p style='background:#fff3cd;padding:14px;border-radius:8px;border-left:4px solid #ffc107;'>" +
            "⚠️ A fine of <strong>₹5 per day</strong> will be charged after the due date." +
            "</p>",
            "View My Books", "http://localhost:5500/dashboard.html"
        );
        send(user.getEmail(), subject, body);
    }

    // ──────────────────────────────────────────────────────────
    //  3. RETURN CONFIRMATION EMAIL (includes fine if applicable)
    // ──────────────────────────────────────────────────────────
    @Async
    public void sendReturnConfirmation(User user, BorrowRecord borrow, Double fine) {
        String subject = "✅ Book Returned: " + borrow.getBook().getTitle();
        String fineSection = (fine != null && fine > 0)
            ? "<p style='background:#f8d7da;padding:14px;border-radius:8px;border-left:4px solid #e74c3c;'>" +
              "💸 <strong>Fine Amount: ₹" + String.format("%.0f", fine) +
              "</strong> — Please pay at the library counter. A receipt will be sent upon payment.</p>"
            : "<p style='background:#d4edda;padding:14px;border-radius:8px;border-left:4px solid #27ae60;'>" +
              "✅ <strong>No fine!</strong> Returned on time. Thank you!</p>";

        String body = buildTemplate(
            "Book Returned Successfully! ✅",
            "Thank you for returning the book on time.",
            "<table style='width:100%;border-collapse:collapse;margin-bottom:16px;'>" +
            row("Book",          borrow.getBook().getTitle()) +
            row("Returned On",   borrow.getReturnDate().format(DATE_FMT)) +
            "</table>" + fineSection,
            "Visit Library", "http://localhost:5500/dashboard.html"
        );
        send(user.getEmail(), subject, body);
    }

    // ──────────────────────────────────────────────────────────
    //  4. DUE-SOON REMINDER (3 days before due date)
    // ──────────────────────────────────────────────────────────
    @Async
    public void sendDueSoonReminder(User user, BorrowRecord borrow) {
        String subject = "⏰ Reminder: Book Due Soon — " + borrow.getBook().getTitle();
        String body = buildTemplate(
            "Your Book is Due Soon! ⏰",
            "Hi " + user.getName() + ", this is a friendly reminder.",
            "<table style='width:100%;border-collapse:collapse;margin-bottom:16px;'>" +
            row("Book",     borrow.getBook().getTitle()) +
            rowHighlight("Due Date", borrow.getDueDate().format(DATE_FMT)) +
            "</table>" +
            "<p style='background:#fff3cd;padding:14px;border-radius:8px;border-left:4px solid #ffc107;'>" +
            "⚠️ Please return the book before the due date to avoid a <strong>₹5/day fine</strong>." +
            "</p>",
            "Visit Library", "http://localhost:5500/dashboard.html"
        );
        send(user.getEmail(), subject, body);
    }

    // ──────────────────────────────────────────────────────────
    //  5. OVERDUE ALERT (book past due date, shows current fine)
    // ──────────────────────────────────────────────────────────
    @Async
    public void sendOverdueAlert(User user, BorrowRecord borrow, double fine) {
        String subject = "🚨 OVERDUE: " + borrow.getBook().getTitle();
        String body = buildTemplate(
            "Your Book is Overdue! 🚨",
            "Hi " + user.getName() + ", your book is past the due date.",
            "<table style='width:100%;border-collapse:collapse;margin-bottom:16px;'>" +
            row("Book",        borrow.getBook().getTitle()) +
            rowHighlight("Was Due On", borrow.getDueDate().format(DATE_FMT)) +
            "</table>" +
            "<p style='background:#f8d7da;padding:14px;border-radius:8px;border-left:4px solid #e74c3c;'>" +
            "🚨 <strong>Current Fine: ₹" + String.format("%.0f", fine) +
            "</strong> — This fine increases by ₹5 every day. Please return the book immediately.</p>",
            "Return Now", "http://localhost:5500/dashboard.html"
        );
        send(user.getEmail(), subject, body);
    }

    // ──────────────────────────────────────────────────────────
    //  6. FINE PAYMENT RECEIPT
    // ──────────────────────────────────────────────────────────
    @Async
    public void sendFineReceipt(User user, Double amount) {
        String subject = "🧾 Fine Payment Receipt — QuantumLibrary";
        String body = buildTemplate(
            "Fine Payment Received 🧾",
            "Your fine has been successfully cleared.",
            "<table style='width:100%;border-collapse:collapse;margin-bottom:16px;'>" +
            row("Member",     user.getName()) +
            rowHighlight("Amount Paid", "₹" + String.format("%.0f", amount)) +
            "</table>" +
            "<p style='background:#d4edda;padding:14px;border-radius:8px;border-left:4px solid #27ae60;'>" +
            "✅ Your account is now <strong>clear</strong>. You can borrow books again!</p>",
            "Borrow Books", "http://localhost:5500/dashboard.html"
        );
        send(user.getEmail(), subject, body);
    }

    // ──────────────────────────────────────────────────────────
    //  7. NEW BOOK ALERT (sent to all members when admin adds a book)
    // ──────────────────────────────────────────────────────────
    @Async
    public void sendNewBookAlert(List<User> members, Book book) {
        String subject = "📚 New Book Added: " + book.getTitle();
        String body = buildTemplate(
            "New Book in QuantumLibrary! 📚",
            "A new book is now available for borrowing.",
            "<table style='width:100%;border-collapse:collapse;margin-bottom:16px;'>" +
            row("Title",      book.getTitle()) +
            row("Author",     book.getAuthor()) +
            row("Genre",      book.getGenre()) +
            row("Available Copies", String.valueOf(book.getStock())) +
            "</table>",
            "Borrow Now", "http://localhost:5500/dashboard.html"
        );
        for (User member : members) {
            send(member.getEmail(), subject, body);
        }
    }

    // ──────────────────────────────────────────────────────────
    //  8. WEEKLY ADMIN DIGEST (every Sunday 10 AM)
    // ──────────────────────────────────────────────────────────
    @Async
    public void sendWeeklyAdminDigest(String adminEmail, long totalBooks,
                                      long totalMembers, long activeBorrows,
                                      long overdueCount, Double finesCollected) {
        String subject = "📊 QuantumLibrary — Weekly Digest";
        String body = buildTemplate(
            "Weekly Library Statistics 📊",
            "Here is your weekly summary for QuantumLibrary.",
            "<table style='width:100%;border-collapse:collapse;'>" +
            row("📚 Total Books",      String.valueOf(totalBooks)) +
            row("👥 Total Members",    String.valueOf(totalMembers)) +
            row("📖 Active Borrows",   String.valueOf(activeBorrows)) +
            rowHighlight("🚨 Overdue Books", String.valueOf(overdueCount)) +
            row("💸 Fines Collected",  "₹" + String.format("%.0f", finesCollected)) +
            "</table>",
            "Open Admin Panel", "http://localhost:5500/admin.html"
        );
        send(adminEmail, subject, body);
    }

    // ──────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ──────────────────────────────────────────────────────────

    /** Normal table row */
    private String row(String label, String value) {
        return "<tr style='border-bottom:1px solid #ecf0f1;'>" +
               "<td style='padding:10px 12px;color:#7f8c8d;width:40%;'>" + label + "</td>" +
               "<td style='padding:10px 12px;font-weight:600;color:#2c3e50;'>" + value + "</td>" +
               "</tr>";
    }

    /** Highlighted (red) table row — for due dates, overdue counts */
    private String rowHighlight(String label, String value) {
        return "<tr style='border-bottom:1px solid #ecf0f1;background:#fff5f5;'>" +
               "<td style='padding:10px 12px;color:#7f8c8d;width:40%;'>" + label + "</td>" +
               "<td style='padding:10px 12px;font-weight:700;color:#e74c3c;'>" + value + "</td>" +
               "</tr>";
    }

    /**
     * Builds the full branded HTML email template.
     * All 8 email types use this template.
     */
    private String buildTemplate(String title, String subtitle,
                                  String content, String btnText, String btnUrl) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>" +
               "<body style='margin:0;padding:0;background:#f0f4f8;font-family:Arial,sans-serif;'>" +
               "<table width='100%' cellpadding='0' cellspacing='0'>" +
               "<tr><td align='center' style='padding:40px 16px;'>" +
               "<table width='580' cellpadding='0' cellspacing='0' " +
               "style='background:#fff;border-radius:16px;overflow:hidden;" +
               "box-shadow:0 8px 32px rgba(0,0,0,0.12);'>" +

               // ── Header ──
               "<tr><td style='background:linear-gradient(135deg,#1a252f 0%,#2980b9 100%);" +
               "padding:36px 32px;text-align:center;'>" +
               "<div style='font-size:48px;margin-bottom:8px;'>📚</div>" +
               "<h1 style='color:#fff;margin:0 0 4px;font-size:26px;font-weight:800;" +
               "letter-spacing:-0.5px;'>QuantumLibrary</h1>" +
               "<p style='color:rgba(255,255,255,0.7);margin:0;font-size:13px;'>Library Management System</p>" +
               "</td></tr>" +

               // ── Title band ──
               "<tr><td style='background:#f8fafc;padding:20px 32px;border-bottom:1px solid #e8ecf0;'>" +
               "<h2 style='margin:0;font-size:20px;color:#1a252f;font-weight:700;'>" + title + "</h2>" +
               "<p style='margin:6px 0 0;font-size:14px;color:#7f8c8d;'>" + subtitle + "</p>" +
               "</td></tr>" +

               // ── Body ──
               "<tr><td style='padding:28px 32px;color:#2c3e50;font-size:15px;line-height:1.7;'>" +
               content +
               "</td></tr>" +

               // ── Button ──
               "<tr><td style='padding:4px 32px 32px;text-align:center;'>" +
               "<a href='" + btnUrl + "' style='display:inline-block;" +
               "background:linear-gradient(135deg,#3498db,#2980b9);" +
               "color:#fff;text-decoration:none;padding:14px 36px;" +
               "border-radius:10px;font-weight:700;font-size:15px;" +
               "box-shadow:0 4px 16px rgba(52,152,219,0.4);'>" + btnText + "</a>" +
               "</td></tr>" +

               // ── Footer ──
               "<tr><td style='background:#f8fafc;padding:20px 32px;text-align:center;" +
               "border-top:1px solid #e8ecf0;'>" +
               "<p style='color:#adb5bd;font-size:12px;margin:0;'>" +
               "© 2026 QuantumLibrary · 123 Knowledge Street, Chennai – 600001</p>" +
               "<p style='color:#adb5bd;font-size:12px;margin:6px 0 0;'>" +
               "This is an automated notification. Please do not reply to this email.</p>" +
               "</td></tr></table></td></tr></table></body></html>";
    }

    /**
     * Sends an HTML email. Errors are logged but never crash the caller
     * because email sending is non-critical to the REST response.
     */
    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("✅ Email sent ➜ {} | {}", to, subject);
        } catch (MessagingException e) {
            log.error("❌ Email failed ➜ {} | {} | Error: {}", to, subject, e.getMessage());
        }
    }
}
