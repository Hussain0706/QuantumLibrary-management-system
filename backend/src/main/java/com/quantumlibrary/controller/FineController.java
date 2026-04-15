package com.quantumlibrary.controller;

import com.quantumlibrary.dto.ApiResponse;
import com.quantumlibrary.entity.Fine;
import com.quantumlibrary.entity.User;
import com.quantumlibrary.service.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FineController — fine management endpoints.
 *
 *  Member:
 *    GET /api/fines/my           → view own fines (paid + unpaid)
 *
 *  Admin only:
 *    GET  /api/fines/all         → all fines in the system
 *    POST /api/fines/pay/{id}    → mark fine as paid + send receipt email
 */
@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;

    /** Member: view own outstanding and historical fines */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Fine>>> myFines(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Your fines", fineService.getMemberFines(user)));
    }

    /** Admin: view all fines system-wide */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<Fine>>> allFines() {
        return ResponseEntity.ok(ApiResponse.success("All fines", fineService.getAllFines()));
    }

    /**
     * Admin: mark a fine as paid (cash at counter).
     * Automatically sends a payment receipt email to the member.
     */
    @PostMapping("/pay/{fineId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Fine>> payFine(@PathVariable Long fineId) {
        Fine fine = fineService.payFine(fineId);
        return ResponseEntity.ok(ApiResponse.success(
            "Fine marked as paid! Receipt email sent to " + fine.getUser().getEmail(), fine));
    }
}
