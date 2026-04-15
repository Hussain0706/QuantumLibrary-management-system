package com.quantumlibrary.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * BorrowRecord — tracks every book borrow/return transaction.
 * When a book is borrowed: stock—, returned=false.
 * When returned: stock++, returned=true, returnDate set.
 */
@Entity
@Table(name = "borrow_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The member who borrowed the book */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "joinDate"})
    private User user;

    /** The book that was borrowed */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /** When the book was borrowed */
    @Column(nullable = false)
    private LocalDateTime borrowDate;

    /** Due date = borrowDate + 14 days */
    @Column(nullable = false)
    private LocalDateTime dueDate;

    /** Set when the book is physically returned */
    private LocalDateTime returnDate;

    /** false = still borrowed, true = returned */
    @Builder.Default
    @Column(nullable = false)
    private boolean returned = false;

    @PrePersist
    public void prePersist() {
        if (borrowDate == null) {
            borrowDate = LocalDateTime.now();
        }
    }
}
