package com.quantumlibrary.repository;

import com.quantumlibrary.entity.Fine;
import com.quantumlibrary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {

    /** All fines (paid + unpaid) for a member */
    List<Fine> findByUser(User user);

    /** Only unpaid fines for a member */
    List<Fine> findByUserAndPaidFalse(User user);

    /** All unpaid fines system-wide */
    List<Fine> findByPaidFalse();

    /** Find a fine linked to a specific borrow record (for idempotency) */
    Optional<Fine> findByBorrowRecord_Id(Long borrowRecordId);

    /** Total rupees collected from paid fines */
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.paid = true")
    Double totalCollected();

    /** Total rupees outstanding from unpaid fines */
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.paid = false")
    Double totalOutstanding();
}
