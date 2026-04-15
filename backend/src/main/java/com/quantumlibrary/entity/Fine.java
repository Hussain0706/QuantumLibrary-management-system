package com.quantumlibrary.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Fine entity — tracks overdue fines.
 * A fine is created when a book is returned late OR by the nightly scheduler.
 * Rate: ₹5 per day after the due date.
 */
@Entity
@Table(name = "fines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The borrow record that caused this fine */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "borrow_record_id", nullable = false)
    @JsonIgnoreProperties({"user"})
    private BorrowRecord borrowRecord;

    /** The member who owes this fine */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "joinDate"})
    private User user;

    /** Total fine amount in ₹ */
    @Column(nullable = false)
    private Double amount;

    /** false = unpaid, true = paid */
    @Builder.Default
    @Column(nullable = false)
    private boolean paid = false;

    /** Timestamp when the fine was paid at the counter */
    private LocalDateTime paidDate;
}
