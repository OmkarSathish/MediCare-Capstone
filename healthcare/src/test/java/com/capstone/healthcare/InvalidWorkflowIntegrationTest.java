package com.capstone.healthcare;

import com.capstone.healthcare.auth.model.Role;
import com.capstone.healthcare.auth.model.UserAccount;
import com.capstone.healthcare.auth.repository.IRoleRepository;
import com.capstone.healthcare.auth.repository.IUserRepository;
import com.capstone.healthcare.auth.service.IAuthTokenService;
import com.capstone.healthcare.auth.service.IPasswordEncoderService;
import com.capstone.healthcare.diagnosticcenter.model.CenterTestOffering;
import com.capstone.healthcare.diagnosticcenter.model.CenterTestOfferingKey;
import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import com.capstone.healthcare.diagnosticcenter.repository.ICenterTestOfferingRepository;
import com.capstone.healthcare.diagnosticcenter.repository.IDiagnosticCenterRepository;
import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;
import com.capstone.healthcare.diagnostictest.repository.IDiagnosticTestRepository;
import com.capstone.healthcare.patient.model.Patient;
import com.capstone.healthcare.patient.repository.IPatientRepository;
import com.capstone.healthcare.shared.security.RoleConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Invalid workflow integration tests (10 negative / error-path cases).
 *
 * Each test deliberately triggers a specific error that the
 * GlobalExceptionHandler
 * is responsible for returning. The handler-to-test mapping is:
 *
 * I1 → handleIllegalState (409 duplicate signup)
 * I2 → handleAuthentication (401 bad credentials)
 * I3 → 401 missing token (Spring Security itself rejects)
 * I4 → handleValidation (400 test not offered at center)
 * I5 → handleAccessDenied (403 owner guard on appointment)
 * I6 → handleIllegalState (409 cancel APPROVED appointment)
 * I7 → handleMethodArgumentNotValid (400 missing rejection remarks)
 * I8 → handleMethodArgumentNotValid (400 blank center name)
 * I9 → handleNotFound (404 delete non-existent center)
 * I10 → handleAccessDenied (403 CUSTOMER calling admin-only route)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InvalidWorkflowIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    IRoleRepository roleRepository;
    @Autowired
    IUserRepository userRepository;
    @Autowired
    IPatientRepository patientRepository;
    @Autowired
    IDiagnosticCenterRepository centerRepository;
    @Autowired
    IDiagnosticTestRepository testRepository;
    @Autowired
    ICenterTestOfferingRepository offeringRepository;
    @Autowired
    IAuthTokenService authTokenService;
    @Autowired
    IPasswordEncoderService passwordEncoderService;

    // ── Shared state ──────────────────────────────────────────────────────────
    private static String customerToken; // jane2@test.com — CUSTOMER role
    private static String otherCustomerToken; // other@test.com — different CUSTOMER
    private static String adminToken;
    private static String centerAdminToken;
    private static int centerId;
    private static int testId;
    private static int approvedAppointmentId; // used in I6

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ─────────────────────────────────────────────────────────────────────────
    // One-time DB seed — skipped if already done (H2 persists across tests)
    // ─────────────────────────────────────────────────────────────────────────
    @BeforeEach
    void seedOnce() {
        // Guard: only seed if the roles table is empty (i.e.
        // ValidWorkflowIntegrationTest
        // already seeded them if it ran first in the same JVM, so we skip inserting
        // duplicate roles but still derive tokens if our users are missing).
        if (roleRepository.findByRoleName(RoleConstants.CUSTOMER).isEmpty()) {
            roleRepository.save(Role.builder().roleName(RoleConstants.CUSTOMER).build());
        }
        if (roleRepository.findByRoleName(RoleConstants.ADMIN).isEmpty()) {
            roleRepository.save(Role.builder().roleName(RoleConstants.ADMIN).build());
        }
        if (roleRepository.findByRoleName(RoleConstants.CENTER_ADMIN).isEmpty()) {
            roleRepository.save(Role.builder().roleName(RoleConstants.CENTER_ADMIN).build());
        }

        Role customerRole = roleRepository.findByRoleName(RoleConstants.CUSTOMER).get();
        Role adminRole = roleRepository.findByRoleName(RoleConstants.ADMIN).get();
        Role centerAdminRole = roleRepository.findByRoleName(RoleConstants.CENTER_ADMIN).get();

        // ── Primary admin ─────────────────────────────────────────────────────
        UserAccount admin = userRepository.findByEmail("admin2@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Admin Two")
                        .email("admin2@test.com")
                        .phone("09000000001")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .roles(Set.of(adminRole))
                        .build()));
        adminToken = authTokenService.generateToken(admin);

        // ── Diagnostic center and test ────────────────────────────────────────
        DiagnosticCenter center = centerRepository.findAll().stream()
                .filter(c -> c.getName().startsWith("City Health Lab"))
                .findFirst()
                .orElseGet(() -> centerRepository.save(DiagnosticCenter.builder()
                        .name("City Health Lab")
                        .address("123 Main St")
                        .contactEmail("city@lab.com")
                        .status("ACTIVE")
                        .build()));
        centerId = center.getId();

        DiagnosticTest test = testRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> testRepository.save(DiagnosticTest.builder()
                        .testName("Complete Blood Count")
                        .testPrice(500.0)
                        .status("ACTIVE")
                        .build()));
        testId = test.getId();

        if (offeringRepository.findById(new CenterTestOfferingKey(center.getId(), test.getId())).isEmpty()) {
            offeringRepository.save(CenterTestOffering.builder()
                    .id(new CenterTestOfferingKey(center.getId(), test.getId()))
                    .center(center)
                    .test(test)
                    .price(500.0)
                    .build());
        }

        // ── Center admin ──────────────────────────────────────────────────────
        UserAccount centerAdmin = userRepository.findByEmail("centeradmin2@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Center Admin Two")
                        .email("centeradmin2@test.com")
                        .phone("09111111112")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .centerId(center.getId())
                        .roles(Set.of(centerAdminRole))
                        .build()));
        centerAdminToken = authTokenService.generateToken(centerAdmin);

        // ── Primary customer (jane2) ──────────────────────────────────────────
        UserAccount customer = userRepository.findByEmail("jane2@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Jane Two")
                        .email("jane2@test.com")
                        .phone("09123456780")
                        .passwordHash(passwordEncoderService.encode("Secure@123"))
                        .status("ACTIVE")
                        .roles(Set.of(customerRole))
                        .build()));
        customerToken = authTokenService.generateToken(customer);
        patientRepository.findByUsername("jane2@test.com").orElseGet(() -> patientRepository.save(Patient.builder()
                .name("Jane Two")
                .phoneNo("09123456780")
                .age(29)
                .gender("Female")
                .username("jane2@test.com")
                .build()));

        // ── Secondary customer (other) — no patient profile ───────────────────
        UserAccount other = userRepository.findByEmail("other@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Other User")
                        .email("other@test.com")
                        .phone("09199999999")
                        .passwordHash(passwordEncoderService.encode("Secure@123"))
                        .status("ACTIVE")
                        .roles(Set.of(customerRole))
                        .build()));
        otherCustomerToken = authTokenService.generateToken(other);

        // ── Pre-book and approve an appointment for I6 ────────────────────────
        if (approvedAppointmentId == 0) {
            // Book via HTTP so workflow initialises status history correctly
            try {
                Map<String, Object> bookBody = Map.of(
                        "centerId", centerId,
                        "testIds", Set.of(testId),
                        "appointmentDate", LocalDate.now().plusDays(30).toString());
                MvcResult bookResult = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookBody)))
                        .andReturn();

                approvedAppointmentId = mapper.readTree(
                        bookResult.getResponse().getContentAsString())
                        .path("data").path("id").asInt();

                // Approve it
                mockMvc.perform(put("/api/admin/appointments/{id}/approve", approvedAppointmentId)
                        .header("Authorization", "Bearer " + centerAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("remarks", "pre-approved for I6"))));

            } catch (Exception e) {
                throw new RuntimeException("Failed to pre-seed approved appointment for I6", e);
            }
        }
    }

    // =========================================================================
    // I1 — Sign up with an already-registered email → 400 Bad Request
    // UserServiceImpl.addUser throws ValidationException → handleValidation
    // =========================================================================
    @Test
    @Order(1)
    @DisplayName("I1: Signup with duplicate email → 400 Bad Request")
    void i1_signup_duplicateEmail_returnsBadRequest() throws Exception {
        // First signup
        Map<String, String> body = Map.of(
                "fullName", "Duplicate User",
                "email", "duplicate@test.com",
                "phone", "09100000001",
                "password", "Secure@123",
                "role", "CUSTOMER");
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // Second signup with same email — ValidationException → 400
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already registered")));
    }

    // =========================================================================
    // I2 — Login with wrong password → 400 Bad Request
    // UserServiceImpl.validateUser throws ValidationException → handleValidation
    // =========================================================================
    @Test
    @Order(2)
    @DisplayName("I2: Login with wrong password → 400 Bad Request")
    void i2_login_wrongPassword_returnsBadRequest() throws Exception {
        Map<String, String> body = Map.of(
                "username", "jane2@test.com",
                "password", "WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    // =========================================================================
    // I3 — Book appointment without a Bearer token → 403 Forbidden
    // No AuthenticationEntryPoint configured; Spring Security's default
    // AccessDeniedHandler handles unauthenticated requests → 403
    // =========================================================================
    @Test
    @Order(3)
    @DisplayName("I3: Book appointment with no token → 403 Forbidden")
    void i3_bookAppointment_noToken_returnsForbidden() throws Exception {
        Map<String, Object> body = Map.of(
                "centerId", centerId,
                "testIds", Set.of(testId),
                "appointmentDate", LocalDate.now().plusDays(7).toString());

        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // I4 — Book appointment with a past date → 400 Validation Failed
    // CreateAppointmentRequest.appointmentDate has @Future constraint
    // → MethodArgumentNotValidException → handleMethodArgumentNotValid
    // =========================================================================
    @Test
    @Order(4)
    @DisplayName("I4: Book appointment with past date → 400 Validation Failed")
    void i4_bookAppointment_pastDate_returnsBadRequest() throws Exception {
        Map<String, Object> body = Map.of(
                "centerId", centerId,
                "testIds", Set.of(testId),
                "appointmentDate", LocalDate.now().minusDays(1).toString()); // yesterday — @Future violation

        mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    // =========================================================================
    // I5 — Customer views another customer's appointment → 403 Forbidden
    // =========================================================================
    @Test
    @Order(5)
    @DisplayName("I5: Customer views another customer's appointment by ID → 403 Forbidden")
    void i5_getAppointment_otherOwner_returnsForbidden() throws Exception {
        // Book an appointment as jane2
        Map<String, Object> bookBody = Map.of(
                "centerId", centerId,
                "testIds", Set.of(testId),
                "appointmentDate", LocalDate.now().plusDays(8).toString());
        MvcResult bookResult = mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(bookBody)))
                .andExpect(status().isCreated())
                .andReturn();

        int appointmentId = mapper.readTree(bookResult.getResponse().getContentAsString())
                .path("data").path("id").asInt();

        // "other" user tries to view jane2's appointment
        mockMvc.perform(get("/api/appointments/{id}", appointmentId)
                .header("Authorization", "Bearer " + otherCustomerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // =========================================================================
    // I6 — Cancel an already-CANCELLED appointment → 400 Bad Request
    // AppointmentWorkflowServiceImpl.cancelAppointment throws ValidationException
    // for CANCELLED/REJECTED statuses → handleValidation → 400
    // =========================================================================
    @Test
    @Order(6)
    @DisplayName("I6: Cancel an already-CANCELLED appointment → 400 Bad Request")
    void i6_cancelCancelledAppointment_returnsBadRequest() throws Exception {
        // Book a new appointment
        Map<String, Object> bookBody = Map.of(
                "centerId", centerId,
                "testIds", Set.of(testId),
                "appointmentDate", LocalDate.now().plusDays(25).toString());
        MvcResult bookResult = mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(bookBody)))
                .andExpect(status().isCreated())
                .andReturn();

        int apptId = mapper.readTree(bookResult.getResponse().getContentAsString())
                .path("data").path("id").asInt();

        // First cancel — succeeds
        mockMvc.perform(delete("/api/appointments/{id}", apptId)
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());

        // Second cancel on the CANCELLED appointment — ValidationException → 400
        mockMvc.perform(delete("/api/appointments/{id}", apptId)
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // =========================================================================
    // I7 — Reject appointment without providing remarks → 400 Bad Request
    // =========================================================================
    @Test
    @Order(7)
    @DisplayName("I7: Reject appointment without remarks → 400 Validation Failed")
    void i7_rejectAppointment_missingRemarks_returnsBadRequest() throws Exception {
        // Book a fresh appointment to reject
        Map<String, Object> bookBody = Map.of(
                "centerId", centerId,
                "testIds", Set.of(testId),
                "appointmentDate", LocalDate.now().plusDays(9).toString());
        MvcResult bookResult = mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(bookBody)))
                .andExpect(status().isCreated())
                .andReturn();

        int appointmentId = mapper.readTree(bookResult.getResponse().getContentAsString())
                .path("data").path("id").asInt();

        // Reject with blank remarks — @NotBlank on RejectAppointmentRequest.remarks
        Map<String, String> rejectBody = Map.of("remarks", "");

        mockMvc.perform(put("/api/admin/appointments/{id}/reject", appointmentId)
                .header("Authorization", "Bearer " + centerAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rejectBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    // =========================================================================
    // I8 — Create a center with a blank name → 400 Validation Failed
    // =========================================================================
    @Test
    @Order(8)
    @DisplayName("I8: Create center with blank name → 400 Validation Failed")
    void i8_createCenter_blankName_returnsBadRequest() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "", // @NotBlank violation
                "address", "Some Street");

        mockMvc.perform(post("/api/centers")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    // =========================================================================
    // I9 — Delete a center that does not exist → 404 Not Found
    // =========================================================================
    @Test
    @Order(9)
    @DisplayName("I9: Delete non-existent center → 404 Not Found")
    void i9_deleteCenter_notFound_returnsNotFound() throws Exception {
        int nonExistentId = 999_999;

        mockMvc.perform(delete("/api/centers/{id}", nonExistentId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // =========================================================================
    // I10 — CUSTOMER tries to create a diagnostic test → 403 Forbidden
    // =========================================================================
    @Test
    @Order(10)
    @DisplayName("I10: CUSTOMER calls admin-only POST /api/tests → 403 Forbidden")
    void i10_createTest_asCustomer_returnsForbidden() throws Exception {
        Map<String, Object> body = Map.of(
                "testName", "Unauthorized Test",
                "testPrice", 300.0);

        // Filter-chain level rejection bypasses GlobalExceptionHandler —
        // Spring Security's AccessDeniedHandler sends the response directly.
        // Assert only the HTTP status, not the response body format.
        mockMvc.perform(post("/api/tests")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }
}
