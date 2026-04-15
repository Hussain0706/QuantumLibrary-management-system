package com.quantumlibrary.config;

import com.quantumlibrary.entity.Book;
import com.quantumlibrary.entity.User;
import com.quantumlibrary.repository.BookRepository;
import com.quantumlibrary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DataLoader — seeds the database on first startup.
 *
 *  Seeds:
 *   👤 1 Admin    → admin@quantumlibrary.com / admin123
 *   👤 1 Member   → hussain0706w@gmail.com   / member123
 *   📚 20 books   ← same catalog as the frontend localStorage data
 *
 *  Checks before inserting:
 *   - Users: only seeds if users table is empty
 *   - Books:  only seeds if books table is empty
 *
 *  This means re-running the app never duplicates data.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedBooks();
        log.info("🚀 QuantumLibrary backend is ready!");
    }

    // ─────────────────────────────────────────────────
    private void seedUsers() {
        if (userRepository.count() > 0) return;
        log.info("🌱 Seeding users...");

        userRepository.saveAll(List.of(
            User.builder()
                .name("Library Admin")
                .email("admin@quantumlibrary.com")
                .password(passwordEncoder.encode("admin123"))
                .role(User.Role.ROLE_ADMIN)
                .phone("+91 93922 46590")
                .active(true)
                .build(),
            User.builder()
                .name("Hussain")
                .email("hussain0706w@gmail.com")
                .password(passwordEncoder.encode("member123"))
                .role(User.Role.ROLE_MEMBER)
                .phone("+91 93922 46590")
                .active(true)
                .build()
        ));
        log.info("✅ Default admin and member created");
    }

    // ─────────────────────────────────────────────────
    private void seedBooks() {
        if (bookRepository.count() > 0) return;
        log.info("🌱 Seeding 20 books...");

        bookRepository.saveAll(List.of(
            b("1984",                         "George Orwell",        "Fiction",     1949,
              "978-0-45-128423-4", 3,
              "A dystopian novel about totalitarianism, surveillance, and repression.",
              "https://covers.openlibrary.org/b/id/8575708-M.jpg", 5),

            b("To Kill a Mockingbird",         "Harper Lee",           "Fiction",     1960,
              "978-0-06-112008-4", 2,
              "A Pulitzer Prize-winning masterwork exploring racial injustice and moral growth.",
              "https://covers.openlibrary.org/b/id/8228691-M.jpg", 5),

            b("A Brief History of Time",       "Stephen Hawking",      "Science",     1988,
              "978-0-55-317521-9", 2,
              "Hawking explores the laws that govern our universe in an accessible masterpiece.",
              "https://covers.openlibrary.org/b/id/8739161-M.jpg", 5),

            b("Sapiens",                       "Yuval Noah Harari",    "History",     2011,
              "978-0-06-231609-7", 4,
              "A brief history of humankind from the Stone Age to the modern era.",
              "https://covers.openlibrary.org/b/id/8908901-M.jpg", 5),

            b("The Great Gatsby",              "F. Scott Fitzgerald",  "Fiction",     1925,
              "978-0-74-323356-5", 3,
              "A novel about the Jazz Age, the American Dream, and obsession.",
              "https://covers.openlibrary.org/b/id/11430066-M.jpg", 4),

            b("The Alchemist",                 "Paulo Coelho",         "Fiction",     1988,
              "978-0-06-231500-7", 5,
              "A philosophical novel about following your dreams and listening to your heart.",
              "https://covers.openlibrary.org/b/id/8229155-M.jpg", 5),

            b("Thinking Fast and Slow",        "Daniel Kahneman",      "Science",     2011,
              "978-0-37-453355-7", 2,
              "A groundbreaking tour of the mind explaining the two systems of thought.",
              "https://covers.openlibrary.org/b/id/9264855-M.jpg", 4),

            b("Dune",                          "Frank Herbert",        "Fiction",     1965,
              "978-0-44-101359-7", 3,
              "An epic science fiction saga set in a distant future amid a feudal empire.",
              "https://covers.openlibrary.org/b/id/11301554-M.jpg", 5),

            b("Meditations",                   "Marcus Aurelius",      "Philosophy",  180,
              "978-0-14-044140-6", 4,
              "Personal writings of Marcus Aurelius on Stoic philosophy and self-discipline.",
              "https://covers.openlibrary.org/b/id/8231856-M.jpg", 5),

            b("The Hobbit",                    "J.R.R. Tolkien",       "Fiction",     1937,
              "978-0-54-792822-7", 3,
              "A fantasy adventure about Bilbo Baggins recruited for a quest to reclaim a treasure.",
              "https://covers.openlibrary.org/b/id/8406786-M.jpg", 5),

            b("Atomic Habits",                 "James Clear",          "Science",     2018,
              "978-0-73-521129-2", 5,
              "A guide to building good habits and breaking bad ones through small incremental changes.",
              "https://covers.openlibrary.org/b/id/10527107-M.jpg", 5),

            b("Clean Code",                    "Robert C. Martin",     "Programming", 2008,
              "978-0-13-235088-4", 3,
              "A handbook of agile software craftsmanship. Learn to write clean, readable code.",
              "https://covers.openlibrary.org/b/id/8732158-M.jpg", 5),

            b("Effective Java",                "Joshua Bloch",         "Programming", 2018,
              "978-0-13-468599-1", 2,
              "Best-practice guide for the Java platform — the definitive Java programming resource.",
              "https://covers.openlibrary.org/b/isbn/9780134685991-M.jpg", 5),

            b("Python Crash Course",           "Eric Matthes",         "Programming", 2019,
              "978-1-59-327603-4", 4,
              "A hands-on, project-based introduction to Python programming for beginners.",
              "https://covers.openlibrary.org/b/isbn/9781593276034-M.jpg", 5),

            b("Introduction to Algorithms",    "Cormen et al.",        "Programming", 2009,
              "978-0-26-203384-8", 2,
              "The definitive textbook on algorithms — data structures, analysis, and design.",
              "https://covers.openlibrary.org/b/isbn/9780262033848-M.jpg", 5),

            b("Cracking the Coding Interview", "Gayle L. McDowell",    "Programming", 2015,
              "978-0-98-478280-8", 5,
              "189 programming questions and solutions — the premier interview prep book.",
              "https://covers.openlibrary.org/b/isbn/9780984782857-M.jpg", 5),

            b("The C Programming Language",    "Kernighan & Ritchie",  "Programming", 1988,
              "978-0-13-110362-7", 3,
              "The original C reference by the creators of the language.",
              "https://covers.openlibrary.org/b/isbn/9780131103627-M.jpg", 5),

            b("Homo Deus",                     "Yuval Noah Harari",    "History",     2015,
              "978-0-06-246401-6", 3,
              "A brief history of tomorrow — the next steps of humanity.",
              "https://covers.openlibrary.org/b/id/10527394-M.jpg", 4),

            b("The Art of War",                "Sun Tzu",              "Philosophy",  500,
              "978-1-59-030760-0", 6,
              "Ancient Chinese text on military strategy with timeless life wisdom.",
              "https://covers.openlibrary.org/b/id/8227470-M.jpg", 5),

            b("JavaScript: The Good Parts",    "Douglas Crockford",    "Programming", 2008,
              "978-0-59-651774-8", 3,
              "Highlights the reliable and elegant JavaScript features that are actually good.",
              "https://covers.openlibrary.org/b/isbn/9780596517748-M.jpg", 4)
        ));
        log.info("✅ 20 books seeded into the catalog");
    }

    /** Helper to build a Book entity */
    private Book b(String title, String author, String genre, int year, String isbn,
                   int stock, String desc, String cover, int rating) {
        return Book.builder()
                .title(title).author(author).genre(genre).year(year).isbn(isbn)
                .stock(stock).description(desc).coverUrl(cover).rating(rating)
                .build();
    }
}
