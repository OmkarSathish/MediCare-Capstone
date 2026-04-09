package com.capstone.healthcare.patient.controller;

import com.capstone.healthcare.patient.dto.*;
import com.capstone.healthcare.patient.model.Patient;
import com.capstone.healthcare.patient.model.TestResult;
import com.capstone.healthcare.patient.service.IPatientService;
import com.capstone.healthcare.patient.service.ITestResultService;
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

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient profile management and test result lookup")
public class PatientController {

    private final IPatientService patientService;
    private final ITestResultService testResultService;

    // ── POST /api/patients ────────────────────────────────────────────────────
    @PostMapping
    @Operation(summary = "Register a patient profile for the authenticated user")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> register(
            @Valid @RequestBody PatientProfileRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        Patient patient = Patient.builder()
                .name(request.getName())
                .phoneNo(request.getPhoneNo())
                .age(request.getAge())
                .gender(request.getGender())
                .username(principal.getUsername())
                .build();

        Patient saved = patientService.registerPatient(patient);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Patient profile created", toResponse(saved)));
    }

    // ── GET /api/patients/{username} ──────────────────────────────────────────
    @GetMapping("/{username}")
    @Operation(summary = "Get a patient profile by username")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> getPatient(
            @PathVariable String username,
            @AuthenticationPrincipal UserPrincipal principal) {

        enforceOwnerOrAdmin(username, principal);
        Patient patient = patientService.viewPatient(username);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(patient)));
    }

    // ── PUT /api/patients/{username} ──────────────────────────────────────────
    @PutMapping("/{username}")
    @Operation(summary = "Update a patient profile")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> updatePatient(
            @PathVariable String username,
            @Valid @RequestBody PatientProfileRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        enforceOwnerOrAdmin(username, principal);
        Patient existing = patientService.viewPatient(username);
        existing.setName(request.getName());
        existing.setPhoneNo(request.getPhoneNo());
        existing.setAge(request.getAge());
        existing.setGender(request.getGender());

        Patient updated = patientService.updatePatientDetails(existing);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", toResponse(updated)));
    }

    // ── GET /api/patients/{username}/results ──────────────────────────────────
    @GetMapping("/{username}/results")
    @Operation(summary = "Get all test results for a patient")
    public ResponseEntity<ApiResponse<List<TestResultResponse>>> getAllResults(
            @PathVariable String username,
            @AuthenticationPrincipal UserPrincipal principal) {

        enforceOwnerOrAdmin(username, principal);
        List<TestResultResponse> results = patientService.getAllTestResult(username)
                .stream().map(this::toResultResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    // ── GET /api/patients/results/{id} ────────────────────────────────────────
    @GetMapping("/results/{id}")
    @Operation(summary = "Get a specific test result by ID")
    public ResponseEntity<ApiResponse<TestResultResponse>> getResult(
            @PathVariable int id,
            @AuthenticationPrincipal UserPrincipal principal) {

        TestResult result = patientService.viewTestResult(id);
        return ResponseEntity.ok(ApiResponse.ok(toResultResponse(result)));
    }

    // ── POST /api/patients/results (admin) ────────────────────────────────────
    @PostMapping("/results")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    @Operation(summary = "Add a test result to an appointment (admin only)")
    public ResponseEntity<ApiResponse<TestResultResponse>> addResult(
            @RequestBody TestResultRequest request) {

        TestResult tr = TestResult.builder()
                .appointmentId(request.getAppointmentId())
                .testReading(request.getTestReading())
                .medicalCondition(request.getMedicalCondition())
                .build();
        TestResult saved = testResultService.addTestResult(tr);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Test result recorded", toResultResponse(saved)));
    }

    // ── PUT /api/patients/results/{id} (admin) ────────────────────────────────
    @PutMapping("/results/{id}")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    @Operation(summary = "Update a test result (admin only)")
    public ResponseEntity<ApiResponse<TestResultResponse>> updateResult(
            @PathVariable int id,
            @RequestBody TestResultRequest request) {

        TestResult tr = TestResult.builder()
                .id(id)
                .appointmentId(request.getAppointmentId())
                .testReading(request.getTestReading())
                .medicalCondition(request.getMedicalCondition())
                .build();
        TestResult updated = testResultService.updateResult(tr);
        return ResponseEntity.ok(ApiResponse.ok("Test result updated", toResultResponse(updated)));
    }

    // ── DELETE /api/patients/results/{id} (admin) ─────────────────────────────
    @DeleteMapping("/results/{id}")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    @Operation(summary = "Delete a test result (admin only)")
    public ResponseEntity<ApiResponse<TestResultResponse>> deleteResult(@PathVariable int id) {
        TestResult removed = testResultService.removeTestResult(id);
        return ResponseEntity.ok(ApiResponse.ok("Test result removed", toResultResponse(removed)));
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private void enforceOwnerOrAdmin(String username, UserPrincipal principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleConstants.ROLE_ADMIN));
        if (!isAdmin && !principal.getUsername().equals(username)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private PatientProfileResponse toResponse(Patient p) {
        return PatientProfileResponse.builder()
                .patientId(p.getPatientId())
                .name(p.getName())
                .phoneNo(p.getPhoneNo())
                .age(p.getAge())
                .gender(p.getGender())
                .username(p.getUsername())
                .build();
    }

    private TestResultResponse toResultResponse(TestResult tr) {
        return TestResultResponse.builder()
                .id(tr.getId())
                .testReading(tr.getTestReading())
                .medicalCondition(tr.getMedicalCondition())
                .appointmentId(tr.getAppointmentId())
                .build();
    }
}
