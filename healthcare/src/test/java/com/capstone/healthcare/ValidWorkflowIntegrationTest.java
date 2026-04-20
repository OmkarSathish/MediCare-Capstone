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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Valid workflow integration tests (10 happy-path cases).
 *
 * Tests run in declaration order so shared state (tokens, IDs) flows
 * naturally from setup → book → view → cancel → approve → CRUD.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ValidWorkflowIntegrationTest {

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

    // ── Shared state across ordered tests ─────────────────────────────────────
    private static String customerToken;
    private static String adminToken;
    private static String centerAdminToken;
    private static int appointmentId;
    private static int centerId;
    private static int testId;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ─────────────────────────────────────────────────────────────────────────
    // One-time DB seed shared by all tests in this class
    // ─────────────────────────────────────────────────────────────────────────
    @BeforeEach
    void seedOnce() {
        // Idempotent: check for this class's specific users, not just role count,
        // so the seeder works correctly even when InvalidWorkflowIntegrationTest
        // has already populated roles in the same JVM / shared H2 instance.
        if (userRepository.findByEmail("admin@test.com").isPresent()) {
            // Tokens may be null if another test class ran first in the same JVM —
            // re-derive them from the already-persisted users.
            if (adminToken == null) {
                adminToken = authTokenService.generateToken(
                        userRepository.findByEmail("admin@test.com").get());
            }
            if (centerAdminToken == null) {
                userRepository.findByEmail("centeradmin@test.com")
                        .ifPresent(u -> centerAdminToken = authTokenService.generateToken(u));
            }
            if (centerId == 0) {
                centerRepository.findAll().stream()
                        .filter(c -> c.getName().startsWith("City Health Lab"))
                        .findFirst()
                        .ifPresent(c -> centerId = c.getId());
            }
            if (testId == 0) {
                testRepository.findAll().stream()
                        .filter(t -> t.getTestName().equals("Complete Blood Count"))
                        .findFirst()
                        .ifPresent(t -> testId = t.getId());
            }
            return;
        }

        // Ensure roles exist (may already be there from InvalidWorkflowIntegrationTest)
        if (roleRepository.findByRoleName(RoleConstants.CUSTOMER).isEmpty())
            roleRepository.save(Role.builder().roleName(RoleConstants.CUSTOMER).build());
        if (roleRepository.findByRoleName(RoleConstants.ADMIN).isEmpty())
            roleRepository.save(Role.builder().roleName(RoleConstants.ADMIN).build());
        if (roleRepository.findByRoleName(RoleConstants.CENTER_ADMIN).isEmpty())
            roleRepository.save(Role.builder().roleName(RoleConstants.CENTER_ADMIN).build());

        Role adminRole = roleRepository.findByRoleName(RoleConstants.ADMIN).get();
        Role centerAdminRole = roleRepository.findByRoleName(RoleConstants.CENTER_ADMIN).get();

        // Primary admin user
        UserAccount admin = userRepository.save(UserAccount.builder()
                .fullName("System Admin")
                .email("admin@test.com")
                .phone("09000000000")
                .passwordHash(passwordEncoderService.encode("Admin@1234"))
                .status("ACTIVE")
                .roles(Set.of(adminRole))
                .build());
        adminToken = authTokenService.generateToken(admin);

        // Diagnostic center (pre-seeded for appointment booking)
        DiagnosticCenter center = centerRepository.save(DiagnosticCenter.builder()
                .name("City Health Lab")
                .address("123 Main St")
                .contactEmail("city@lab.com")
                .status("ACTIVE")
                .build());
        centerId = center.getId();

        // Diagnostic test (pre-seeded)
        DiagnosticTest test = testRepository.save(DiagnosticTest.builder()
                .testName("Complete Blood Count")
                .testPrice(500.0)
                .status("ACTIVE")
                .build());
        testId = test.getId();

        // Offer the test at the center
        offeringRepository.save(CenterTestOffering.builder()
                .id(new CenterTestOfferingKey(center.getId(), test.getId()))
                .center(center)
                .test(test)
                .price(500.0)
                .build());

        // Center admin user (assigned to the seeded center)
        UserAccount centerAdmin = userRepository.save(UserAccount.builder()
                .fullName("Center Admin")
                .email("centeradmin@test.com")
                .phone("09111111111")
                .passwordHash(passwordEncoderService.encode("Admin@1234"))
                .status("ACTIVE")
                .centerId(center.getId())
                .roles(Set.of(centerAdminRole))
                .build());
        centerAdminToken = authTokenService.generateToken(centerAdmin);
    }

    // =========================================================================
    // V1 — Customer signs up with valid data → 201 Created
    // =========================================================================
    @Test
    @Order(1)
    @DisplayName("V1: Customer signs up with valid data → 201 Created")
    void v1_customerSignup_returnsCreated() throws Exception {
        Map<String, String> body = Map.of(
                "fullName", "Jane Doe",
                "email", "jane@test.com",
                "phone", "09123456789",
                "password", "Secure@123",
                "role", "CUSTOMER");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("jane@test.com"));
    }

    // =========================================================================
    // V2 — Customer logs in with correct credentials → 200 OK + tokens
    // =========================================================================
    @Test
    @Order(2)
    @DisplayName("V2: Customer logs in → 200 OK, accessToken and refreshToken returned")
    void v2_customerLogin_returnsTokens() throws Exception {
        Map<String, String> body = Map.of(
                "username", "jane@test.com",
                "password", "Secure@123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andReturn();

        // Capture token for subsequent tests
        String json = result.getResponse().getContentAsString();
        customerToken = mapper.readTree(json)
                .path("data").path("accessToken").asText();

        // Register patient profile for this user (needed by booking tests)
        patientRepository.save(Patient.builder()
                .name("Jane Doe")
                .phoneNo("09123456789")
                .age(28)
                .gender("Female")
                .username("jane@test.com")
                .build());
    }

    // =========================================================================
    // V3 — Authenticated customer books appointment → 201 Created
    // =========================================================================
    @Test
    @Order(3)
    @DisplayName("V3: Customer books a valid appointment → 201 Created, status PENDING")
    void v3_bookAppointment_returnsCreated() throws Exception {
        Map<String, Object> body = Map.of(
                "centerId", centerId,
                "testIds", Set.of(testId),
                "appointmentDate", LocalDate.now().plusDays(7).toString());

        MvcResult result = mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.approvalStatus").value("PENDING"))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        appointmentId = mapper.readTree(json)
                .path("data").path("id").asInt();
    }

    // =========================================================================
    // V4 — Customer views their appointment list → 200 OK, list returned
    // =========================================================================
    @Test
    @Order(4)
    @DisplayName("V4: Customer views their appointment list → 200 OK, non-empty list")
    void v4_listAppointments_returnsList() throws Exception {
        mockMvc.perform(get("/api/appointments")
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    // =========================================================================
    // V5 — Customer views a specific appointment by ID → 200 OK, full detail
    // =========================================================================
    @Test
    @Order(5)
    @DisplayName("V5: Customer gets appointment by ID → 200 OK with appointment detail")
    void v5_getAppointmentById_returnsDetail() throws Exception {
        mockMvc.perform(get("/api/appointments/{id}", appointmentId)
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(appointmentId))
                .andExpect(jsonPath("$.data.approvalStatus").value("PENDING"));
    }

    // =========================================================================
    // V6 — Customer cancels a PENDING appointment → 200 OK, status CANCELLED
    // =========================================================================
    @Test
    @Order(6)
    @DisplayName("V6: Customer cancels a PENDING appointment → 200 OK, status CANCELLED")
    void v6_cancelAppointment_returnsCancelled() throws Exception {
        // Book a fresh appointment so V7's appointment stays PENDING for approval
        Map<String, Object> bookBody = Map.of(
                "centerId", centerId,
                "testIds", Set.of(testId),
                "appointmentDate", LocalDate.now().plusDays(14).toString());
        MvcResult bookResult = mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(bookBody)))
                .andExpect(status().isCreated())
                .andReturn();

        int cancelId = mapper.readTree(bookResult.getResponse().getContentAsString())
                .path("data").path("id").asInt();

        mockMvc.perform(delete("/api/appointments/{id}", cancelId)
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.approvalStatus").value("CANCELLED"));
    }

    // =========================================================================
    // V7 — Center admin approves a PENDING appointment → 200 OK, status APPROVED
    // =========================================================================
    @Test
    @Order(7)
    @DisplayName("V7: Center admin approves a PENDING appointment → 200 OK, status APPROVED")
    void v7_approveAppointment_returnsApproved() throws Exception {
        Map<String, String> body = Map.of("remarks", "Appointment confirmed");

        mockMvc.perform(put("/api/admin/appointments/{id}/approve", appointmentId)
                .header("Authorization", "Bearer " + centerAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.approvalStatus").value("APPROVED"));
    }

    // =========================================================================
    // V8 — Admin creates a new diagnostic center → 201 Created
    // =========================================================================
    @Test
    @Order(8)
    @DisplayName("V8: Admin creates a diagnostic center → 201 Created")
    void v8_adminCreatesCenter_returnsCreated() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Metro Diagnostics",
                "address", "456 Health Ave",
                "contactEmail", "metro@diag.com",
                "contactNo", "09299999999");

        mockMvc.perform(post("/api/centers")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Metro Diagnostics"));
    }

    // =========================================================================
    // V9 — Admin updates an existing diagnostic center → 200 OK
    // =========================================================================
    @Test
    @Order(9)
    @DisplayName("V9: Admin updates a diagnostic center → 200 OK with updated details")
    void v9_adminUpdatesCenter_returnsUpdated() throws Exception {
        // Pass servicesOffered as an explicit empty list to avoid
        // UnsupportedOperationException from List.of() in the controller mapper
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("name", "City Health Lab Updated");
        body.put("address", "123 Main St, Suite 2");
        body.put("contactEmail", "city-updated@lab.com");
        body.put("contactNo", "09088888888");
        body.put("servicesOffered", new java.util.ArrayList<>());

        mockMvc.perform(put("/api/centers/{id}", centerId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("City Health Lab Updated"));
    }

    // =========================================================================
    // V10 — Admin creates a new diagnostic test → 201 Created
    // =========================================================================
    @Test
    @Order(10)
    @DisplayName("V10: Admin creates a diagnostic test → 201 Created")
    void v10_adminCreatesTest_returnsCreated() throws Exception {
        Map<String, Object> body = Map.of(
                "testName", "Lipid Panel",
                "testPrice", 750.0);

        mockMvc.perform(post("/api/tests")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.testName").value("Lipid Panel"))
                .andExpect(jsonPath("$.data.testPrice").value(750.0));
    }
}
