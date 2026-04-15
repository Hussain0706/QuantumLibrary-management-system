package com.quantumlibrary.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * User entity — represents both Admins and Members.
 * Role is stored as a string enum in the DB.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt-encoded password — never returned in JSON responses */
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String phone;

    @Column(nullable = false)
    private LocalDateTime joinDate;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    public void prePersist() {
        if (joinDate == null) {
            joinDate = LocalDateTime.now();
        }
    }

    /** User roles used by Spring Security */
    public enum Role {
        ROLE_ADMIN,
        ROLE_MEMBER
    }
}
