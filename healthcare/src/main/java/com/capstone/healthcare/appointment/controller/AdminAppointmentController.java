package com.capstone.healthcare.appointment.controller;

import com.capstone.healthcare.appointment.dto.*;
import com.capstone.healthcare.appointment.model.Appointment;
import com.capstone.healthcare.appointment.service.IAppointmentService;
import com.capstone.healthcare.appointment.service.IAppointmentWorkflowService;
import com.capstone.healthcare.shared.response.ApiResponse;
import com.capstone.healthcare.shared.security.RoleConstants;
import com.capstone.healthcare.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/appointments")
@PreAuthorize("hasAnyRole('" + RoleConstants.CENTER_ADMIN + "', '" + RoleConstants.CENTER_STAFF + "')")
@RequiredArgsConstructor
@Tag(name = "Admin — Appointments", description = "Admin approval, rejection, and filtered appointment listing")
public class AdminAppointmentController {

        private final IAppointmentService appointmentService;
        private final IAppointmentWorkflowService workflowService;

        // ── GET /api/admin/appointments ───────────────────────────────────────────
        @GetMapping
        @Operation(summary = "List appointments filtered by center, test, and status")
        public ResponseEntity<ApiResponse<List<AppointmentResponse>>> listAppointments(
                        @RequestParam(defaultValue = "0") int centerId,
                        @RequestParam(defaultValue = "") String test,
                        @RequestParam(defaultValue = "-1") int status,
                        @AuthenticationPrincipal UserPrincipal principal) {

                // CENTER_ADMIN and CENTER_STAFF are both scoped to their assigned center
                boolean isScopedAdmin = principal.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals(RoleConstants.ROLE_CENTER_ADMIN)
                                                || a.getAuthority().equals(RoleConstants.ROLE_CENTER_STAFF));
                int effectiveCenterId = isScopedAdmin
                                ? (principal.getCenterId() != null ? principal.getCenterId() : 0)
                                : centerId;

                List<Appointment> appointments = appointmentService.getAppointmentList(effectiveCenterId, test, status);
                return ResponseEntity.ok(ApiResponse.ok(
                                appointments.stream().map(this::toResponse).toList()));
        }

        // ── PUT /api/admin/appointments/{id}/approve ───────────────────────────────
        @PutMapping("/{id}/approve")
        @Operation(summary = "Approve a pending appointment")
        public ResponseEntity<ApiResponse<AppointmentResponse>> approve(
                        @PathVariable int id,
                        @Valid @RequestBody ApproveAppointmentRequest request,
                        @AuthenticationPrincipal UserPrincipal principal) {

                enforceAppointmentOwnership(id, principal);
                Appointment updated = workflowService.approveAppointment(
                                id, principal.getUsername(), request.getRemarks());
                return ResponseEntity.ok(ApiResponse.ok("Appointment approved", toResponse(updated)));
        }

        // ── PUT /api/admin/appointments/{id}/reject ────────────────────────────────
        @PutMapping("/{id}/reject")
        @Operation(summary = "Reject a pending appointment")
        public ResponseEntity<ApiResponse<AppointmentResponse>> reject(
                        @PathVariable int id,
                        @Valid @RequestBody RejectAppointmentRequest request,
                        @AuthenticationPrincipal UserPrincipal principal) {

                enforceAppointmentOwnership(id, principal);
                Appointment updated = workflowService.rejectAppointment(
                                id, principal.getUsername(), request.getRemarks());
                return ResponseEntity.ok(ApiResponse.ok("Appointment rejected", toResponse(updated)));
        }

        // ── helpers ───────────────────────────────────────────────────────────────
        private void enforceAppointmentOwnership(int appointmentId, UserPrincipal principal) {
                boolean isScopedAdmin = principal.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals(RoleConstants.ROLE_CENTER_ADMIN)
                                                || a.getAuthority().equals(RoleConstants.ROLE_CENTER_STAFF));
                if (!isScopedAdmin)
                        return;
                Appointment appointment = appointmentService.viewAppointment(appointmentId);
                if (principal.getCenterId() == null ||
                                appointment.getDiagnosticCenter().getId() != principal.getCenterId()) {
                        throw new AccessDeniedException(
                                        "You can only manage appointments at your assigned center");
                }
        }

        private AppointmentResponse toResponse(Appointment a) {
                return AppointmentResponse.builder()
                                .id(a.getId())
                                .appointmentDate(a.getAppointmentDate())
                                .approvalStatus(a.getApprovalStatus())
                                .patientName(a.getPatient().getName())
                                .centerName(a.getDiagnosticCenter().getName())
                                .remarks(a.getRemarks())
                                .build();
        }
}
