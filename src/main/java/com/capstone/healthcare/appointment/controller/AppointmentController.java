package com.capstone.healthcare.appointment.controller;

import com.capstone.healthcare.appointment.dto.*;
import com.capstone.healthcare.appointment.model.Appointment;
import com.capstone.healthcare.appointment.model.AppointmentStatusHistory;
import com.capstone.healthcare.appointment.service.IAppointmentService;
import com.capstone.healthcare.appointment.service.IAppointmentStatusService;
import com.capstone.healthcare.appointment.service.IAppointmentWorkflowService;
import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;
import com.capstone.healthcare.patient.model.Patient;
import com.capstone.healthcare.patient.service.IPatientService;
import com.capstone.healthcare.shared.response.ApiResponse;
import com.capstone.healthcare.shared.security.RoleConstants;
import com.capstone.healthcare.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Booking, status tracking, and appointment history")
public class AppointmentController {

        private final IAppointmentService appointmentService;
        private final IAppointmentStatusService appointmentStatusService;
        private final IPatientService patientService;
        private final IAppointmentWorkflowService workflowService;

        // ── POST /api/appointments ────────────────────────────────────────────────
        @PostMapping
        @Operation(summary = "Book a new appointment")
        public ResponseEntity<ApiResponse<AppointmentResponse>> book(
                        @Valid @RequestBody CreateAppointmentRequest request,
                        @AuthenticationPrincipal UserPrincipal principal) {

                Patient patient = patientService.viewPatient(principal.getUsername());

                Set<DiagnosticTest> tests = request.getTestIds().stream()
                                .map(id -> DiagnosticTest.builder().id(id).build())
                                .collect(Collectors.toSet());

                Appointment appointment = Appointment.builder()
                                .patient(patient)
                                .diagnosticCenter(DiagnosticCenter.builder().id(request.getCenterId()).build())
                                .diagnosticTests(tests)
                                .appointmentDate(request.getAppointmentDate())
                                .build();

                Appointment saved = appointmentService.addAppointment(appointment);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.ok("Appointment booked", toResponse(saved)));
        }

        // ── GET /api/appointments ─────────────────────────────────────────────────
        @GetMapping
        @Operation(summary = "Get appointments for a patient (by name) or all for admin")
        public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointments(
                        @RequestParam(required = false) String patient,
                        @AuthenticationPrincipal UserPrincipal principal) {

                boolean isAdmin = principal.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals(RoleConstants.ROLE_ADMIN));

                String lookupName = patient;
                if (lookupName == null) {
                        if (!isAdmin) {
                                // customers default to their own appointments
                                lookupName = patientService.viewPatient(principal.getUsername()).getName();
                        } else {
                                // admins with no filter get all — delegate to filtered list with wildcard via
                                // service
                                List<Appointment> all = appointmentService.getAppointmentList(0, "", 0);
                                return ResponseEntity.ok(ApiResponse.ok(
                                                all.stream().map(this::toResponse).toList()));
                        }
                }

                String finalName = lookupName;
                if (!isAdmin && !patientService.viewPatient(principal.getUsername()).getName().equals(finalName)) {
                        throw new AccessDeniedException("Access denied");
                }

                List<AppointmentResponse> responses = appointmentService.viewAppointments(finalName)
                                .stream().map(this::toResponse).toList();
                return ResponseEntity.ok(ApiResponse.ok(responses));
        }

        // ── GET /api/appointments/{id} ────────────────────────────────────────────
        @GetMapping("/{id}")
        @Operation(summary = "Get appointment details by ID")
        public ResponseEntity<ApiResponse<AppointmentDetailResponse>> getAppointment(
                        @PathVariable int id,
                        @AuthenticationPrincipal UserPrincipal principal) {

                Appointment appointment = appointmentService.viewAppointment(id);
                enforceOwnerOrAdmin(appointment, principal);

                List<AppointmentStatusHistory> history = appointmentStatusService.getStatusHistory(id);
                return ResponseEntity.ok(ApiResponse.ok(toDetailResponse(appointment, history)));
        }

        // ── GET /api/appointments/{id}/status ─────────────────────────────────────
        @GetMapping("/{id}/status")
        @Operation(summary = "Get approval status and history for an appointment")
        public ResponseEntity<ApiResponse<AppointmentStatusResponse>> getStatus(
                        @PathVariable int id,
                        @AuthenticationPrincipal UserPrincipal principal) {

                Appointment appointment = appointmentService.viewAppointment(id);
                enforceOwnerOrAdmin(appointment, principal);

                List<AppointmentStatusHistory> history = appointmentStatusService.getStatusHistory(id);
                AppointmentStatusResponse response = AppointmentStatusResponse.builder()
                                .appointmentId(id)
                                .currentStatus(appointment.getApprovalStatus())
                                .history(history.stream().map(h -> AppointmentStatusResponse.StatusEntry.builder()
                                                .previousStatus(h.getPreviousStatus())
                                                .newStatus(h.getNewStatus())
                                                .changedBy(h.getChangedBy())
                                                .changedAt(h.getChangedAt())
                                                .comments(h.getComments())
                                                .build()).toList())
                                .build();
                return ResponseEntity.ok(ApiResponse.ok(response));
        }

        // ── DELETE /api/appointments/{id} ─────────────────────────────────────────
        @DeleteMapping("/{id}")
        @Operation(summary = "Cancel an appointment (soft delete — sets status to CANCELLED)")
        public ResponseEntity<ApiResponse<AppointmentResponse>> deleteAppointment(
                        @PathVariable int id,
                        @AuthenticationPrincipal UserPrincipal principal) {

                Appointment appointment = appointmentService.viewAppointment(id);
                enforceOwnerOrAdmin(appointment, principal);

                Appointment cancelled = workflowService.cancelAppointment(id, principal.getUsername());
                return ResponseEntity.ok(ApiResponse.ok("Appointment cancelled", toResponse(cancelled)));
        }

        // ── helpers ───────────────────────────────────────────────────────────────
        private void enforceOwnerOrAdmin(Appointment appointment, UserPrincipal principal) {
                boolean isAdmin = principal.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals(RoleConstants.ROLE_ADMIN));
                if (!isAdmin
                                && !appointment.getPatient().getUsername().equals(principal.getUsername())) {
                        throw new AccessDeniedException("Access denied");
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

        private AppointmentDetailResponse toDetailResponse(
                        Appointment a, List<AppointmentStatusHistory> history) {

                com.capstone.healthcare.patient.dto.PatientProfileResponse patientDto = com.capstone.healthcare.patient.dto.PatientProfileResponse
                                .builder()
                                .patientId(a.getPatient().getPatientId())
                                .name(a.getPatient().getName())
                                .phoneNo(a.getPatient().getPhoneNo())
                                .age(a.getPatient().getAge())
                                .gender(a.getPatient().getGender())
                                .username(a.getPatient().getUsername())
                                .build();

                AppointmentDetailResponse.CenterSummary centerDto = AppointmentDetailResponse.CenterSummary.builder()
                                .id(a.getDiagnosticCenter().getId())
                                .name(a.getDiagnosticCenter().getName())
                                .address(a.getDiagnosticCenter().getAddress())
                                .build();

                Set<com.capstone.healthcare.diagnostictest.dto.TestSearchResponse> tests = a.getDiagnosticTests()
                                .stream()
                                .map(t -> com.capstone.healthcare.diagnostictest.dto.TestSearchResponse.builder()
                                                .id(t.getId())
                                                .testName(t.getTestName())
                                                .testPrice(t.getTestPrice())
                                                .build())
                                .collect(Collectors.toSet());

                List<AppointmentStatusResponse.StatusEntry> statusEntries = history.stream()
                                .map(h -> AppointmentStatusResponse.StatusEntry.builder()
                                                .previousStatus(h.getPreviousStatus())
                                                .newStatus(h.getNewStatus())
                                                .changedBy(h.getChangedBy())
                                                .changedAt(h.getChangedAt())
                                                .comments(h.getComments())
                                                .build())
                                .toList();

                return AppointmentDetailResponse.builder()
                                .id(a.getId())
                                .appointmentDate(a.getAppointmentDate())
                                .approvalStatus(a.getApprovalStatus())
                                .remarks(a.getRemarks())
                                .patient(patientDto)
                                .center(centerDto)
                                .diagnosticTests(tests)
                                .statusHistory(statusEntries)
                                .build();
        }
}
