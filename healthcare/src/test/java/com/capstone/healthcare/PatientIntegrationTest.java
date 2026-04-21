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
 * Patient integration tests — 12 cases:
 *
 * PT1  — POST /api/patients (CUSTOMER) → 201 Created, profile returned
 * PT2  — POST /api/patients again (duplicate) → 400 Bad Request
 * PT3  — GET /api/patients/{username} (owner) → 200 OK
 * PT4  — GET /api/patients/{username} (ADMIN) → 200 OK
 * PT5  — GET /api/patients/{username} (different CUSTOMER) → 403 Forbidden
 * PT6  — PUT /api/patients/{username} (owner updates) → 200 OK
 * PT7  — GET /api/patients/nonexistent (ADMIN) → 404 Not Found
 * PT8  — POST /api/patients/results (ADMIN adds result) → 201 Created
 * PT9  — GET /api/patients/{username}/results (owner) → 200 OK, non-empty
 * PT10 — PUT /api/patients/results/{id} (ADMIN updates result) → 200 OK
 * PT11 — DELETE /api/patients/results/{id} (ADMIN deletes result) → 200 OK
 * PT12 — POST /api/patients/results (CUSTOMER, non-admin) → 403 Forbidden
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PatientIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired IRoleRepository roleRepository;
    @Autowired IUserRepository userRepository;
    @Autowired IDiagnosticCenterRepository centerRepository;
    @Autowired IDiagnosticTestRepository testRepository;
    @Autowired ICenterTestOfferingRepository offeringRepository;
    @Autowired IAuthTokenService authTokenService;
    @Autowired IPasswordEncoderService passwordEncoderService;

    private static String adminToken;
    private static String customerToken;
    private static String otherCustomerToken;
    private static int centerId;
    private static int diagTestId;
    private static int seededApptId;
    private static int resultId;

    private static final String CUSTOMER_EMAIL = "pat.customer@test.com";

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void seed() {
        // ── Roles ──────────────────────────────────────────────────────────────
        if (roleRepository.findByRoleName(RoleConstants.ADMIN).isEmpty())
            roleRepository.save(Role.builder().roleName(RoleConstants.ADMIN).build());
        if (roleRepository.findByRoleName(RoleConstants.CUSTOMER).isEmpty())
            roleRepository.save(Role.builder().roleName(RoleConstants.CUSTOMER).build());

        Role adminRole    = roleRepository.findByRoleName(RoleConstants.ADMIN).get();
        Role customerRole = roleRepository.findByRoleName(RoleConstants.CUSTOMER).get();

        // ── Users ──────────────────────────────────────────────────────────────
        UserAccount admin = userRepository.findByEmail("pat.admin@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Pat Admin")
                        .email("pat.admin@test.com")
                        .phone("09800000001")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .roles(Set.of(adminRole))
                        .build()));
        adminToken = authTokenService.generateToken(admin);

        UserAccount customer = userRepository.findByEmail(CUSTOMER_EMAIL)
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Pat Customer")
                        .email(CUSTOMER_EMAIL)
                        .phone("09800000002")
                        .passwordHash(passwordEncoderService.encode("Secure@123"))
                        .status("ACTIVE")
                        .roles(Set.of(customerRole))
                        .build()));
        customerToken = authTokenService.generateToken(customer);

        UserAccount otherCustomer = userRepository.findByEmail("pat.other@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Pat Other")
                        .email("pat.other@test.com")
                        .phone("09800000003")
                        .passwordHash(passwordEncoderService.encode("Secure@123"))
                        .status("ACTIVE")
                        .roles(Set.of(customerRole))
                        .build()));
        otherCustomerToken = authTokenService.generateToken(otherCustomer);

        // ── Diagnostic center + test offering ─────────────────────────────────
        DiagnosticCenter center = centerRepository.findAll().stream()
                .filter(c -> c.getName().equals("Pat Test Center"))
                .findFirst()
                .orElseGet(() -> centerRepository.save(DiagnosticCenter.builder()
                        .name("Pat Test Center")
                        .address("1 Pat Ave")
                        .contactEmail("pat@center.com")
                        .status("ACTIVE")
                        .build()));
        centerId = center.getId();

        DiagnosticTest diagTest = testRepository.findAll().stream()
                .filter(t -> t.getTestName().equals("Pat Test CBC"))
                .findFirst()
                .orElseGet(() -> testRepository.save(DiagnosticTest.builder()
                        .testName("Pat Test CBC")
                        .testPrice(400.0)
                        .status("ACTIVE")
                        .build()));
        diagTestId = diagTest.getId();

        if (offeringRepository.findById(
                new CenterTestOfferingKey(centerId, diagTestId)).isEmpty()) {
            offeringRepository.save(CenterTestOffering.builder()
                    .id(new CenterTestOfferingKey(centerId, diagTestId))
                    .center(center)
                    .test(diagTest)
                    .price(400.0)
                    .build());
        }
    }

    // =========================================================================
    // PT1 — CUSTOMER registers a patient profile → 201 Created
    // =========================================================================
    @Test
    @Order(1)
    @DisplayName("PT1: CUSTOMER registers patient profile → 201 Created")
    void pt1_customerRegistersPatientProfile() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Pat Customer",
                "phoneNo", "9800000002",
                "age", 30,
                "gender", "Male");

        mockMvc.perform(post("/api/patients")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value(CUSTOMER_EMAIL))
                .andExpect(jsonPath("$.data.name").value("Pat Customer"));
    }

    // =========================================================================
    // PT2 — Duplicate patient registration → 400 Bad Request
    // =========================================================================
    @Test
    @Order(2)
    @DisplayName("PT2: Duplicate patient registration → 400 Bad Request")
    void pt2_duplicateRegistrationRejected() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Pat Customer",
                "phoneNo", "9800000002",
                "age", 30,
                "gender", "Male");

        mockMvc.perform(post("/api/patients")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // PT3 — Owner views own patient profile → 200 OK
    // =========================================================================
    @Test
    @Order(3)
    @DisplayName("PT3: Owner views own patient profile → 200 OK")
    void pt3_ownerViewsOwnProfile() throws Exception {
        mockMvc.perform(get("/api/patients/{username}", CUSTOMER_EMAIL)
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value(CUSTOMER_EMAIL))
                .andExpect(jsonPath("$.data.name").value("Pat Customer"));
    }

    // =========================================================================
    // PT4 — ADMIN views any patient profile → 200 OK
    // =========================================================================
    @Test
    @Order(4)
    @DisplayName("PT4: ADMIN views any patient profile → 200 OK")
    void pt4_adminViewsAnyProfile() throws Exception {
        mockMvc.perform(get("/api/patients/{username}", CUSTOMER_EMAIL)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(CUSTOMER_EMAIL));
    }

    // =========================================================================
    // PT5 — Different CUSTOMER views another user's profile → 403 Forbidden
    // =========================================================================
    @Test
    @Order(5)
    @DisplayName("PT5: Different CUSTOMER views another profile → 403 Forbidden")
    void pt5_otherCustomerForbidden() throws Exception {
        mockMvc.perform(get("/api/patients/{username}", CUSTOMER_EMAIL)
                .header("Authorization", "Bearer " + otherCustomerToken))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // PT6 — Owner updates their profile → 200 OK
    // =========================================================================
    @Test
    @Order(6)
    @DisplayName("PT6: Owner updates patient profile → 200 OK")
    void pt6_ownerUpdatesProfile() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Pat Customer Updated",
                "phoneNo", "9800000002",
                "age", 31,
                "gender", "Male");

        mockMvc.perform(put("/api/patients/{username}", CUSTOMER_EMAIL)
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Pat Customer Updated"))
                .andExpect(jsonPath("$.data.age").value(31));
    }

    // =========================================================================
    // PT7 — ADMIN gets profile for non-existent username → 404 Not Found
    // =========================================================================
    @Test
    @Order(7)
    @DisplayName("PT7: ADMIN gets non-existent patient profile → 404 Not Found")
    void pt7_adminGetsNonExistentProfile() throws Exception {
        mockMvc.perform(get("/api/patients/{username}", "nobody@test.com")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // PT8 — ADMIN adds a test result to a booked appointment → 201 Created
    // =========================================================================
    @Test
    @Order(8)
    @DisplayName("PT8: ADMIN adds test result → 201 Created")
    void pt8_adminAddsTestResult() throws Exception {
        seededApptId = bookAppointment();

        Map<String, Object> body = Map.of(
                "appointmentId", seededApptId,
                "testReading", "Normal range",
                "medicalCondition", "Healthy");

        MvcResult result = mockMvc.perform(post("/api/patients/results")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.testReading").value("Normal range"))
                .andExpect(jsonPath("$.data.medicalCondition").value("Healthy"))
                .andReturn();

        resultId = mapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asInt();
    }

    // =========================================================================
    // PT9 — Owner lists their test results → 200 OK, non-empty
    // =========================================================================
    @Test
    @Order(9)
    @DisplayName("PT9: Owner lists own test results → 200 OK, non-empty")
    void pt9_ownerListsTestResults() throws Exception {
        mockMvc.perform(get("/api/patients/{username}/results", CUSTOMER_EMAIL)
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    // =========================================================================
    // PT10 — ADMIN updates a test result → 200 OK
    // =========================================================================
    @Test
    @Order(10)
    @DisplayName("PT10: ADMIN updates test result → 200 OK")
    void pt10_adminUpdatesTestResult() throws Exception {
        Map<String, Object> body = Map.of(
                "appointmentId", seededApptId,
                "testReading", "Slightly elevated",
                "medicalCondition", "Monitor closely");

        mockMvc.perform(put("/api/patients/results/{id}", resultId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.testReading").value("Slightly elevated"))
                .andExpect(jsonPath("$.data.medicalCondition").value("Monitor closely"));
    }

    // =========================================================================
    // PT11 — ADMIN deletes a test result → 200 OK
    // =========================================================================
    @Test
    @Order(11)
    @DisplayName("PT11: ADMIN deletes test result → 200 OK")
    void pt11_adminDeletesTestResult() throws Exception {
        mockMvc.perform(delete("/api/patients/results/{id}", resultId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // PT12 — CUSTOMER (non-admin) tries to add test result → 403 Forbidden
    // =========================================================================
    @Test
    @Order(12)
    @DisplayName("PT12: CUSTOMER adds test result → 403 Forbidden")
    void pt12_customerCannotAddTestResult() throws Exception {
        Map<String, Object> body = Map.of(
                "appointmentId", 1,
                "testReading", "Unauthorized",
                "medicalCondition", "Not allowed");

        mockMvc.perform(post("/api/patients/results")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    /** Books an appointment for the customer and returns the new appointment ID. */
    private int bookAppointment() throws Exception {
        Map<String, Object> body = Map.of(
                "centerId", centerId,
                "testIds", Set.of(diagTestId),
                "appointmentDate", LocalDate.now().plusDays(5).toString());

        MvcResult result = mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        return mapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asInt();
    }
}
