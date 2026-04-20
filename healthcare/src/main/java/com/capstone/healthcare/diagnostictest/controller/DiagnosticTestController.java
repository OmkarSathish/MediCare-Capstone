package com.capstone.healthcare.diagnostictest.controller;

import com.capstone.healthcare.diagnostictest.dto.*;
import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;
import com.capstone.healthcare.diagnostictest.model.TestCategory;
import com.capstone.healthcare.diagnostictest.service.IDiagnosticTestService;
import com.capstone.healthcare.diagnostictest.service.ITestCatalogService;
import com.capstone.healthcare.diagnostictest.service.ITestService;
import com.capstone.healthcare.diagnosticcenter.dto.TestPriceEntry;
import com.capstone.healthcare.diagnosticcenter.service.ICenterPricingService;
import com.capstone.healthcare.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@Tag(name = "Diagnostic Tests", description = "Test catalog CRUD and search")
public class DiagnosticTestController {

    private final IDiagnosticTestService diagnosticTestService;
    private final ITestService testService;
    private final ITestCatalogService testCatalogService;
    private final ICenterPricingService pricingService;

    // ── GET /api/tests/{testId}/prices ─────────────────────────────────────
    @GetMapping("/{testId}/prices")
    @Operation(summary = "Get all center prices for a specific test, sorted ascending")
    public ResponseEntity<ApiResponse<List<TestPriceEntry>>> getPricesForTest(
            @PathVariable int testId) {
        return ResponseEntity.ok(ApiResponse.ok(pricingService.getPricesForTest(testId)));
    }

    // ── GET /api/tests ────────────────────────────────────────────────────────
    @GetMapping
    @Operation(summary = "List all diagnostic tests or search by name")
    public ResponseEntity<ApiResponse<List<TestResponse>>> getAllTests(
            @RequestParam(required = false, defaultValue = "") String search) {

        List<DiagnosticTest> tests = search.isBlank()
                ? diagnosticTestService.getAllTest()
                : testCatalogService.searchTests(search);
        return ResponseEntity.ok(ApiResponse.ok(tests.stream().map(this::toResponse).toList()));
    }

    // ── GET /api/tests/{id} ───────────────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(summary = "Get a diagnostic test by ID")
    public ResponseEntity<ApiResponse<TestResponse>> getTestById(@PathVariable int id) {
        DiagnosticTest test = testService.viewAllTest("").stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElseThrow(() -> new com.capstone.healthcare.shared.exception.ResourceNotFoundException(
                        "DiagnosticTest", "id", id));
        return ResponseEntity.ok(ApiResponse.ok(toResponse(test)));
    }

    // ── POST /api/tests ───────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new diagnostic test (admin only)")
    public ResponseEntity<ApiResponse<TestResponse>> createTest(
            @Valid @RequestBody CreateTestRequest request) {

        DiagnosticTest test = fromCreateRequest(request);
        DiagnosticTest saved = testService.addTest(test);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Test created", toResponse(saved)));
    }

    // ── PUT /api/tests/{id} ───────────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing diagnostic test (admin only)")
    public ResponseEntity<ApiResponse<TestResponse>> updateTest(
            @PathVariable int id,
            @Valid @RequestBody UpdateTestRequest request) {

        request.setId(id);
        DiagnosticTest test = fromUpdateRequest(request);
        DiagnosticTest updated = diagnosticTestService.updateTestDetail(test);
        return ResponseEntity.ok(ApiResponse.ok("Test updated", toResponse(updated)));
    }

    // ── DELETE /api/tests/{id} ────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a diagnostic test (admin only)")
    public ResponseEntity<ApiResponse<TestResponse>> deleteTest(@PathVariable int id) {
        DiagnosticTest test = DiagnosticTest.builder().id(id).build();
        DiagnosticTest removed = testService.removeTest(test);
        return ResponseEntity.ok(ApiResponse.ok("Test deactivated", toResponse(removed)));
    }

    // ── mappers ───────────────────────────────────────────────────────────────
    private TestResponse toResponse(DiagnosticTest t) {
        return TestResponse.builder()
                .id(t.getId())
                .testName(t.getTestName())
                .testPrice(t.getTestPrice())
                .status(t.getStatus())
                .categoryName(t.getCategory() != null ? t.getCategory().getCategoryName() : null)
                .build();
    }

    private DiagnosticTest fromCreateRequest(CreateTestRequest req) {
        DiagnosticTest.DiagnosticTestBuilder builder = DiagnosticTest.builder()
                .testName(req.getTestName())
                .testPrice(req.getTestPrice());
        if (req.getCategoryId() != null) {
            builder.category(TestCategory.builder().categoryId(req.getCategoryId()).build());
        }
        return builder.build();
    }

    private DiagnosticTest fromUpdateRequest(UpdateTestRequest req) {
        return DiagnosticTest.builder()
                .id(req.getId())
                .testName(req.getTestName())
                .testPrice(req.getTestPrice())
                .build();
    }
}
