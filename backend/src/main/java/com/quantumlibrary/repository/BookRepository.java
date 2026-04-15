package com.quantumlibrary.repository;

import com.quantumlibrary.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /** Filter by genre (case-insensitive) */
    List<Book> findByGenreIgnoreCase(String genre);

    /** Full-text search across title, author, and genre */
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title)  LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(b.genre)  LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Book> search(@Param("q") String query);

    /** Books with at least 1 copy available */
    List<Book> findByStockGreaterThan(int stock);

    /** Count available books */
    long countByStockGreaterThan(int stock);
}
