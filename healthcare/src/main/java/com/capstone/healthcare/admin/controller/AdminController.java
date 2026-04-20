package com.capstone.healthcare.admin.controller;

import com.capstone.healthcare.admin.dto.AdminDashboardResponse;
import com.capstone.healthcare.admin.dto.CenterAdminDashboardResponse;
import com.capstone.healthcare.admin.service.IAdminDashboardService;
import com.capstone.healthcare.admin.service.ICenterAdminDashboardService;
import com.capstone.healthcare.shared.exception.ResourceNotFoundException;
import com.capstone.healthcare.shared.response.ApiResponse;
import com.capstone.healthcare.shared.security.RoleConstants;
import com.capstone.healthcare.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin — Dashboard", description = "Admin dashboard and high-level metrics")
public class AdminController {

    private final IAdminDashboardService dashboardService;
    private final ICenterAdminDashboardService centerDashboardService;

    // ── GET /api/admin/dashboard ─────────────────────────────────────────────
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    @Operation(summary = "Get admin dashboard summary (totals + pending count)")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getDashboardSummary()));
    }

    // ── GET /api/admin/dashboard/center ──────────────────────────────────────
    @GetMapping("/dashboard/center")
    @PreAuthorize("hasAnyRole('" + RoleConstants.CENTER_ADMIN + "', '" + RoleConstants.CENTER_STAFF + "')")
    @Operation(summary = "Get center-scoped dashboard analytics for the calling center admin")
    public ResponseEntity<ApiResponse<CenterAdminDashboardResponse>> getCenterDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        Integer centerId = principal.getCenterId();
        if (centerId == null) {
            throw new ResourceNotFoundException("DiagnosticCenter", "centerId", "null — user has no assigned center");
        }
        return ResponseEntity.ok(ApiResponse.ok(
                centerDashboardService.getDashboardForCenter(centerId)));
    }
}
