package com.quantumlibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response body for successful login — contains JWT token + user info */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** JWT bearer token — store in sessionStorage on the frontend */
    private String token;

    /** Role: ROLE_ADMIN or ROLE_MEMBER */
    private String role;

    private String name;
    private String email;
    private Long   id;
}
