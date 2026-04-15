package com.quantumlibrary.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Book entity — the library catalog.
 * Stock tracks how many copies are currently available.
 */
@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String genre;

    @Column(name = "pub_year")
    private Integer year;

    @Column(unique = true)
    private String isbn;

    /** Number of copies currently available for borrowing */
    @Column(nullable = false)
    private Integer stock;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String coverUrl;

    /** Star rating 1–5 */
    private Integer rating;
}
