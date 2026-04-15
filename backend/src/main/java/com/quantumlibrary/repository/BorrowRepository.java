package com.quantumlibrary.repository;

import com.quantumlibrary.entity.BorrowRecord;
import com.quantumlibrary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<BorrowRecord, Long> {

    /** All borrows for a specific user */
    List<BorrowRecord> findByUser(User user);

    /** Active (not yet returned) borrows for a member */
    List<BorrowRecord> findByUserAndReturnedFalse(User user);

    /** All currently active borrows system-wide */
    List<BorrowRecord> findByReturnedFalse();

    /** Overdue borrows: not returned AND past due date */
    @Query("SELECT b FROM BorrowRecord b WHERE b.returned = false AND b.dueDate < :now")
    List<BorrowRecord> findOverdue(@Param("now") LocalDateTime now);

    /**
     * Books due within a window — used for 3-day reminder emails.
     * Scheduler calls this with now → now+3days.
     */
    @Query("SELECT b FROM BorrowRecord b WHERE b.returned = false " +
           "AND b.dueDate BETWEEN :now AND :future")
    List<BorrowRecord> findDueSoon(@Param("now") LocalDateTime now,
                                   @Param("future") LocalDateTime future);

    /** Books borrowed today — used in admin stats */
    @Query("SELECT COUNT(b) FROM BorrowRecord b " +
           "WHERE b.borrowDate >= :startOfDay AND b.borrowDate < :endOfDay")
    long countBorrowedToday(@Param("startOfDay") LocalDateTime start,
                            @Param("endOfDay")   LocalDateTime end);

    /** Total active borrow count */
    long countByReturnedFalse();
}
