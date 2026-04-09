package com.capstone.healthcare.admin.controller;

import com.capstone.healthcare.admin.dto.AdminDashboardResponse;
import com.capstone.healthcare.admin.service.IAdminDashboardService;
import com.capstone.healthcare.shared.response.ApiResponse;
import com.capstone.healthcare.shared.security.RoleConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
@RequiredArgsConstructor
@Tag(name = "Admin — Dashboard", description = "Admin dashboard and high-level metrics")
public class AdminController {

    private final IAdminDashboardService dashboardService;

    // ── GET /api/admin/dashboard ─────────────────────────────────────────────
    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard summary (totals + pending count)")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getDashboardSummary()));
    }
}
