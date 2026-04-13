package com.capstone.healthcare.diagnosticcenter.controller;

import com.capstone.healthcare.appointment.model.Appointment;
import com.capstone.healthcare.diagnosticcenter.dto.*;
import com.capstone.healthcare.diagnosticcenter.model.CenterTestOffering;
import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import com.capstone.healthcare.diagnosticcenter.service.ICenterLookupService;
import com.capstone.healthcare.diagnosticcenter.service.ICenterPricingService;
import com.capstone.healthcare.diagnosticcenter.service.IDiagnosticCenterService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/centers")
@RequiredArgsConstructor
@Tag(name = "Diagnostic Centers", description = "Center CRUD, test offerings, and appointment lookup")
public class DiagnosticCenterController {

        private final IDiagnosticCenterService centerService;
        private final ICenterLookupService centerLookupService;
        private final ICenterPricingService pricingService;

        // ── GET /api/centers ──────────────────────────────────────────────────────
        @GetMapping
        @Operation(summary = "List all centers or search by name keyword")
        public ResponseEntity<ApiResponse<List<CenterSearchResponse>>> getAllCenters(
                        @RequestParam(required = false, defaultValue = "") String search) {

                List<DiagnosticCenter> centers = search.isBlank()
                                ? centerService.getAllDiagnosticCenters()
                                : centerLookupService.searchCenters(search);
                return ResponseEntity.ok(ApiResponse.ok(centers.stream().map(this::toSearchResponse).toList()));
        }

        // ── GET /api/centers/{id} ─────────────────────────────────────────────────
        @GetMapping("/{id}")
        @Operation(summary = "Get a diagnostic center by ID")
        public ResponseEntity<ApiResponse<CenterResponse>> getCenterById(@PathVariable int id) {
                DiagnosticCenter center = centerService.getDiagnosticCenterById(id);
                return ResponseEntity.ok(ApiResponse.ok(toResponse(center)));
        }

        // ── GET /api/centers/{id}/tests ───────────────────────────────────────────
        @GetMapping("/{id}/tests")
        @Operation(summary = "Get all tests offered at a center")
        public ResponseEntity<ApiResponse<Set<CenterTestOfferingResponse>>> getTestsAtCenter(
                        @PathVariable int id) {

                DiagnosticCenter center = centerService.getDiagnosticCenterById(id);
                Set<CenterTestOfferingResponse> offerings = center.getTestOfferings().stream()
                                .map(this::toOfferingResponse)
                                .collect(Collectors.toSet());
                return ResponseEntity.ok(ApiResponse.ok(offerings));
        }

        // ── GET /api/centers/{id}/tests/{testName} ────────────────────────────────
        @GetMapping("/{id}/tests/{testName}")
        @Operation(summary = "Get details of a specific test offered at a center")
        public ResponseEntity<ApiResponse<CenterTestOfferingResponse>> viewTestDetails(
                        @PathVariable int id,
                        @PathVariable String testName) {

                CenterTestOffering offering = centerService.viewTestOffering(id, testName);
                return ResponseEntity.ok(ApiResponse.ok(toOfferingResponse(offering)));
        }

        // ── GET /api/centers/{id}/appointments ────────────────────────────────────
        @GetMapping("/{id}/appointments")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get all appointments at a center (admin only)")
        public ResponseEntity<ApiResponse<List<AppointmentSummary>>> getAppointments(
                        @PathVariable int id) {

                DiagnosticCenter center = centerService.getDiagnosticCenterById(id);
                List<Appointment> appointments = centerService.getListOfAppointments(center.getName());
                return ResponseEntity.ok(ApiResponse.ok(
                                appointments.stream().map(this::toAppointmentSummary).toList()));
        }

        // ── GET /api/centers/offering/{testId} ────────────────────────────────────
        @GetMapping("/offering/{testId}")
        @Operation(summary = "Get all centers offering a specific test")
        public ResponseEntity<ApiResponse<List<CenterSearchResponse>>> getCentersByTest(
                        @PathVariable int testId) {

                List<DiagnosticCenter> centers = centerLookupService.getCentersOfferingTest(testId);
                return ResponseEntity.ok(ApiResponse.ok(centers.stream().map(this::toSearchResponse).toList()));
        }

        // ── POST /api/centers ─────────────────────────────────────────────────────
        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Create a new diagnostic center (admin only)")
        public ResponseEntity<ApiResponse<CenterResponse>> createCenter(
                        @Valid @RequestBody CreateCenterRequest request) {

                DiagnosticCenter center = fromCreateRequest(request);
                DiagnosticCenter saved = centerService.addDiagnosticCenter(center);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.ok("Center created", toResponse(saved)));
        }

        // ── PUT /api/centers/{id} ─────────────────────────────────────────────────
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Update an existing diagnostic center (admin only)")
        public ResponseEntity<ApiResponse<CenterResponse>> updateCenter(
                        @PathVariable int id,
                        @Valid @RequestBody UpdateCenterRequest request) {

                request.setId(id);
                DiagnosticCenter center = fromUpdateRequest(request);
                DiagnosticCenter updated = centerService.updateDiagnosticCenter(center);
                return ResponseEntity.ok(ApiResponse.ok("Center updated", toResponse(updated)));
        }

        // ── POST /api/centers/{id}/tests/{testId} ─────────────────────────────────
        @PostMapping("/{id}/tests/{testId}")
        @PreAuthorize("hasAnyRole('" + RoleConstants.ADMIN + "', '" + RoleConstants.CENTER_ADMIN + "')")
        @Operation(summary = "Add a test to a center's offerings (admin only)")
        public ResponseEntity<ApiResponse<CenterTestOfferingResponse>> addTestToCenter(
                        @PathVariable int id,
                        @PathVariable int testId,
                        @RequestParam(defaultValue = "0") double price,
                        @AuthenticationPrincipal UserPrincipal principal) {

                enforceCenterOwnership(id, principal);
                CenterTestOffering offering = centerService.addTest(id, testId, price);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.ok("Test added to center", toOfferingResponse(offering)));
        }

        // ── GET /api/centers/{id}/tests/{testId}/suggested-price ──────────────────
        @GetMapping("/{id}/tests/{testId}/suggested-price")
        @PreAuthorize("hasAnyRole('" + RoleConstants.ADMIN + "', '" + RoleConstants.CENTER_ADMIN + "')")
        @Operation(summary = "Get a suggested price for a test at a center")
        public ResponseEntity<ApiResponse<SuggestedPriceResponse>> getSuggestedPrice(
                        @PathVariable int id,
                        @PathVariable int testId) {
                return ResponseEntity.ok(ApiResponse.ok(pricingService.getSuggestedPrice(id, testId)));
        }

        // ── PUT /api/centers/{id}/tests/{testId}/price ───────────────────────────
        @PutMapping("/{id}/tests/{testId}/price")
        @PreAuthorize("hasAnyRole('" + RoleConstants.ADMIN + "', '" + RoleConstants.CENTER_ADMIN + "')")
        @Operation(summary = "Update the price of a test at a center")
        public ResponseEntity<ApiResponse<Void>> updateTestPrice(
                        @PathVariable int id,
                        @PathVariable int testId,
                        @RequestParam double price,
                        @AuthenticationPrincipal UserPrincipal principal) {
                enforceCenterOwnership(id, principal);
                pricingService.updatePrice(id, testId, price);
                return ResponseEntity.ok(ApiResponse.ok("Price updated", null));
        }

        // ── DELETE /api/centers/{id}/tests/{testId}
        // ────────────────────────────────────
        @DeleteMapping("/{id}/tests/{testId}")
        @PreAuthorize("hasAnyRole('" + RoleConstants.ADMIN + "', '" + RoleConstants.CENTER_ADMIN + "')")
        @Operation(summary = "Remove a test from a center's offerings (admin only)")
        public ResponseEntity<ApiResponse<Void>> removeTestFromCenter(
                        @PathVariable int id,
                        @PathVariable int testId,
                        @AuthenticationPrincipal UserPrincipal principal) {

                enforceCenterOwnership(id, principal);
                centerService.removeTest(id, testId);
                return ResponseEntity.ok(ApiResponse.ok("Test removed from center", null));
        }

        // ── DELETE /api/centers/{id} ──────────────────────────────────────────────
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Soft-delete a diagnostic center (admin only)")
        public ResponseEntity<ApiResponse<CenterResponse>> deleteCenter(@PathVariable int id) {
                DiagnosticCenter removed = centerService.removeDiagnosticCenter(id);
                return ResponseEntity.ok(ApiResponse.ok("Center deactivated", toResponse(removed)));
        }

        // ── helpers ───────────────────────────────────────────────────────────────
        private void enforceCenterOwnership(int centerId, UserPrincipal principal) {
                boolean isCenterAdmin = principal.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals(RoleConstants.ROLE_CENTER_ADMIN));
                if (!isCenterAdmin)
                        return;
                if (principal.getCenterId() == null || principal.getCenterId() != centerId) {
                        throw new AccessDeniedException(
                                        "You can only manage tests for your assigned center");
                }
        }

        // ── mappers ───────────────────────────────────────────────────────────────
        private CenterResponse toResponse(DiagnosticCenter c) {
                Set<CenterResponse.TestSummary> tests = c.getTestOfferings().stream()
                                .map(o -> CenterResponse.TestSummary.builder()
                                                .id(o.getTest().getId())
                                                .testName(o.getTest().getTestName())
                                                .testPrice(o.getPrice())
                                                .build())
                                .collect(Collectors.toSet());
                return CenterResponse.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .contactNo(c.getContactNo())
                                .address(c.getAddress())
                                .contactEmail(c.getContactEmail())
                                .status(c.getStatus())
                                .servicesOffered(c.getServicesOffered())
                                .tests(tests)
                                .build();
        }

        private CenterSearchResponse toSearchResponse(DiagnosticCenter c) {
                return CenterSearchResponse.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .address(c.getAddress())
                                .build();
        }

        private CenterTestOfferingResponse toOfferingResponse(CenterTestOffering o) {
                return CenterTestOfferingResponse.builder()
                                .centerId(o.getCenter().getId())
                                .testId(o.getTest().getId())
                                .testName(o.getTest().getTestName())
                                .testPrice(o.getPrice())
                                .normalValue(o.getTest().getNormalValue())
                                .units(o.getTest().getUnits())
                                .build();
        }

        private AppointmentSummary toAppointmentSummary(Appointment a) {
                return AppointmentSummary.builder()
                                .appointmentId(a.getId())
                                .patientName(a.getPatient().getName())
                                .appointmentDate(a.getAppointmentDate())
                                .status(a.getApprovalStatus().name())
                                .build();
        }

        private DiagnosticCenter fromCreateRequest(CreateCenterRequest req) {
                return DiagnosticCenter.builder()
                                .name(req.getName())
                                .contactNo(req.getContactNo())
                                .address(req.getAddress())
                                .contactEmail(req.getContactEmail())
                                .servicesOffered(req.getServicesOffered() != null
                                                ? req.getServicesOffered()
                                                : List.of())
                                .build();
        }

        private DiagnosticCenter fromUpdateRequest(UpdateCenterRequest req) {
                return DiagnosticCenter.builder()
                                .id(req.getId())
                                .name(req.getName())
                                .contactNo(req.getContactNo())
                                .address(req.getAddress())
                                .contactEmail(req.getContactEmail())
                                .servicesOffered(req.getServicesOffered() != null
                                                ? req.getServicesOffered()
                                                : List.of())
                                .build();
        }

        // ── inline projection DTO for appointment list ────────────────────────────
        @lombok.Getter
        @lombok.Setter
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        @lombok.Builder
        public static class AppointmentSummary {
                private int appointmentId;
                private String patientName;
                private java.time.LocalDate appointmentDate;
                private String status;
        }
}
