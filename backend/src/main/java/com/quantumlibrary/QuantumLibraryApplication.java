package com.quantumlibrary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * QuantumLibrary Backend — Main Application Entry Point
 * ======================================================
 * Features:
 *   ✅ JWT Authentication (Admin + Member roles)
 *   ✅ Book CRUD API
 *   ✅ Borrow & Return System (3-book limit, 14-day period)
 *   ✅ Fine Management (₹5/day after due date)
 *   ✅ 7 Email Notification types via Gmail SMTP
 *   ✅ Scheduled Jobs (daily reminders + overdue alerts)
 *   ✅ Admin Dashboard Statistics API
 *   ✅ H2 In-Memory Database (auto-creates tables)
 */
@SpringBootApplication
@EnableScheduling   // enables @Scheduled jobs (reminders, overdue checks)
@EnableAsync        // enables @Async on email sending (non-blocking)
public class QuantumLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuantumLibraryApplication.class, args);

        System.out.println("""

╔══════════════════════════════════════════════════╗
║        📚  QuantumLibrary Backend Started        ║
║  ──────────────────────────────────────────────  ║
║  🌐  API Base :  http://localhost:8080           ║
║  🗄️   H2 Console:  http://localhost:8080         ║
║                    /h2-console                   ║
║  ──────────────────────────────────────────────  ║
║  👤  Admin  : admin@quantumlibrary.com           ║
║  🔑  Password: admin123                          ║
║  ──────────────────────────────────────────────  ║
║  👤  Member : hussain0706w@gmail.com             ║
║  🔑  Password: member123                         ║
╚══════════════════════════════════════════════════╝
""");
    }
}
