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
 * Appointment integration tests — 11 cases:
 *
 * AP1 — ADMIN lists all appointments → 200 OK, list returned
 * AP2 — CENTER_ADMIN lists appointments scoped to their center → 200 OK
 * AP3 — CENTER_ADMIN lists filtered by status → 200 OK, correct status
 * AP4 — CENTER_ADMIN rejects a PENDING appointment → 200 OK, REJECTED
 * AP5 — Approve an already-APPROVED appointment → 400 Bad Request
 * AP6 — Reject an already-REJECTED appointment → 400 Bad Request
 * AP7 — Cancel an APPROVED appointment (allowed) → 200 OK, CANCELLED
 * AP8 — Cancel an already-REJECTED appointment → 400 Bad Request
 * AP9 — Book exceeding 4-tests-per-day limit → 400 Bad Request
 * AP10 — Book exceeding 3-centers-per-day limit → 400 Bad Request
 * AP11 — CENTER_ADMIN approves appointment at different center → 403 Forbidden
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppointmentIntegrationTest {

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

    private static String adminToken;
    private static String customerToken;
    private static String centerAdminToken;
    private static String centerAdminOtherToken;
    private static int centerId;
    private static int otherCenterId;
    private static int testId;
    private static int test2Id;
    private static int test3Id;
    private static int test4Id;
    private static int test5Id;
    private static int otherCenterTestId;

    // IDs set during tests for reuse
    private static int pendingApptId;
    private static int approvedApptId;
    private static int rejectedApptId;

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
        if (roleRepository.findByRoleName(RoleConstants.CENTER_ADMIN).isEmpty())
            roleRepository.save(Role.builder().roleName(RoleConstants.CENTER_ADMIN).build());

        Role adminRole = roleRepository.findByRoleName(RoleConstants.ADMIN).get();
        Role customerRole = roleRepository.findByRoleName(RoleConstants.CUSTOMER).get();
        Role centerAdminRole = roleRepository.findByRoleName(RoleConstants.CENTER_ADMIN).get();

        // ── Centers ────────────────────────────────────────────────────────────
        DiagnosticCenter center = centerRepository.findAll().stream()
                .filter(c -> c.getName().equals("Appt Test Center"))
                .findFirst()
                .orElseGet(() -> centerRepository.save(DiagnosticCenter.builder()
                        .name("Appt Test Center")
                        .address("1 Appt Ave")
                        .contactEmail("appt@center.com")
                        .status("ACTIVE")
                        .build()));
        centerId = center.getId();

        DiagnosticCenter otherCenter = centerRepository.findAll().stream()
                .filter(c -> c.getName().equals("Appt Other Center"))
                .findFirst()
                .orElseGet(() -> centerRepository.save(DiagnosticCenter.builder()
                        .name("Appt Other Center")
                        .address("2 Appt Ave")
                        .contactEmail("other.appt@center.com")
                        .status("ACTIVE")
                        .build()));
        otherCenterId = otherCenter.getId();

        // ── Tests (5 at primary center for limit test + 1 at other center) ─────
        testId = seedTest("Appt Test CBC", 500.0, center);
        test2Id = seedTest("Appt Test Urine", 400.0, center);
        test3Id = seedTest("Appt Test Xray", 600.0, center);
        test4Id = seedTest("Appt Test MRI", 800.0, center);
        test5Id = seedTest("Appt Test CT", 900.0, center);
        otherCenterTestId = seedTest("Appt Other Test", 300.0, otherCenter);

        // ── Users ──────────────────────────────────────────────────────────────
        UserAccount admin = userRepository.findByEmail("appt.admin@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Appt Admin")
                        .email("appt.admin@test.com")
                        .phone("09600000001")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .roles(Set.of(adminRole))
                        .build()));
        adminToken = authTokenService.generateToken(admin);

        UserAccount customer = userRepository.findByEmail("appt.customer@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Appt Customer")
                        .email("appt.customer@test.com")
                        .phone("09600000002")
                        .passwordHash(passwordEncoderService.encode("Secure@123"))
                        .status("ACTIVE")
                        .roles(Set.of(customerRole))
                        .build()));
        customerToken = authTokenService.generateToken(customer);
        patientRepository.findByUsername("appt.customer@test.com")
                .orElseGet(() -> patientRepository.save(Patient.builder()
                        .name("Appt Customer")
                        .phoneNo("09600000002")
                        .age(30)
                        .gender("Male")
                        .username("appt.customer@test.com")
                        .build()));

        UserAccount centerAdmin = userRepository.findByEmail("appt.centeradmin@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Appt Center Admin")
                        .email("appt.centeradmin@test.com")
                        .phone("09600000003")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .centerId(centerId)
                        .roles(Set.of(centerAdminRole))
                        .build()));
        centerAdminToken = authTokenService.generateToken(centerAdmin);

        UserAccount centerAdminOther = userRepository.findByEmail("appt.centeradmin.other@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Appt Other Center Admin")
                        .email("appt.centeradmin.other@test.com")
                        .phone("09600000004")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .centerId(otherCenterId)
                        .roles(Set.of(centerAdminRole))
                        .build()));
        centerAdminOtherToken = authTokenService.generateToken(centerAdminOther);
    }

    /**
     * Idempotently seeds a diagnostic test and its offering at the given center.
     */
    private int seedTest(String name, double price, DiagnosticCenter center) {
        DiagnosticTest test = testRepository.findAll().stream()
                .filter(t -> t.getTestName().equals(name))
                .findFirst()
                .orElseGet(() -> testRepository.save(DiagnosticTest.builder()
                        .testName(name)
                        .testPrice(price)
                        .status("ACTIVE")
                        .build()));
        if (offeringRepository.findById(
                new CenterTestOfferingKey(center.getId(), test.getId())).isEmpty()) {
            offeringRepository.save(CenterTestOffering.builder()
                    .id(new CenterTestOfferingKey(center.getId(), test.getId()))
                    .center(center)
                    .test(test)
                    .price(price)
                    .build());
        }
        return test.getId();
    }

    /** Helper: book an appointment and return its ID. */
    private int book(int cId, Set<Integer> tests, LocalDate date) throws Exception {
        Map<String, Object> body = Map.of(
                "centerId", cId,
                "testIds", tests,
                "appointmentDate", date.toString());
        MvcResult result = mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asInt();
    }

    // =========================================================================
    // AP1 — ADMIN lists all appointments → 200 OK
    // =========================================================================
    @Test
    @Order(1)
    @DisplayName("AP1: ADMIN lists all appointments → 200 OK, list returned")
    void ap1_adminListsAllAppointments() throws Exception {
        // Ensure at least one appointment exists
        pendingApptId = book(centerId, Set.of(testId), LocalDate.now().plusDays(10));

        mockMvc.perform(get("/api/appointments")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    // =========================================================================
    // AP2 — CENTER_ADMIN lists appointments scoped to their own center → 200 OK
    // =========================================================================
    @Test
    @Order(2)
    @DisplayName("AP2: CENTER_ADMIN lists appointments for their center → 200 OK")
    void ap2_centerAdminListsOwnCenterAppointments() throws Exception {
        mockMvc.perform(get("/api/admin/appointments")
                .header("Authorization", "Bearer " + centerAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    // =========================================================================
    // AP3 — CENTER_ADMIN lists appointments filtered by status=0 (PENDING) → 200 OK
    // =========================================================================
    @Test
    @Order(3)
    @DisplayName("AP3: CENTER_ADMIN filters appointments by PENDING status → 200 OK, all PENDING")
    void ap3_centerAdminFiltersAppointmentsByStatus() throws Exception {
        mockMvc.perform(get("/api/admin/appointments")
                .param("status", "0")
                .header("Authorization", "Bearer " + centerAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // Every returned appointment must have PENDING status
                .andExpect(jsonPath("$.data[*].approvalStatus",
                        everyItem(is("PENDING"))));
    }

    // =========================================================================
    // AP4 — CENTER_ADMIN rejects a PENDING appointment → 200 OK, REJECTED
    // =========================================================================
    @Test
    @Order(4)
    @DisplayName("AP4: CENTER_ADMIN rejects a PENDING appointment → 200 OK, status REJECTED")
    void ap4_centerAdminRejectsPendingAppointment() throws Exception {
        // Book a fresh appointment to reject
        int apptId = book(centerId, Set.of(test2Id), LocalDate.now().plusDays(20));

        MvcResult result = mockMvc.perform(put("/api/admin/appointments/{id}/reject", apptId)
                .header("Authorization", "Bearer " + centerAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("remarks", "Slot unavailable"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.approvalStatus").value("REJECTED"))
                .andReturn();

        rejectedApptId = mapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asInt();
    }

    // =========================================================================
    // AP5 — Approve an already-APPROVED appointment → 400 Bad Request
    // =========================================================================
    @Test
    @Order(5)
    @DisplayName("AP5: Approve an already-APPROVED appointment → 400 Bad Request")
    void ap5_approveAlreadyApprovedAppointment() throws Exception {
        // Approve the pending appointment from AP1
        mockMvc.perform(put("/api/admin/appointments/{id}/approve", pendingApptId)
                .header("Authorization", "Bearer " + centerAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("remarks", "All good"))))
                .andExpect(status().isOk());
        approvedApptId = pendingApptId;

        // Try to approve it again
        mockMvc.perform(put("/api/admin/appointments/{id}/approve", approvedApptId)
                .header("Authorization", "Bearer " + centerAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("remarks", "Again"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // =========================================================================
    // AP6 — Reject an already-REJECTED appointment → 400 Bad Request
    // =========================================================================
    @Test
    @Order(6)
    @DisplayName("AP6: Reject an already-REJECTED appointment → 400 Bad Request")
    void ap6_rejectAlreadyRejectedAppointment() throws Exception {
        mockMvc.perform(put("/api/admin/appointments/{id}/reject", rejectedApptId)
                .header("Authorization", "Bearer " + centerAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("remarks", "Still unavailable"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // =========================================================================
    // AP7 — Cancel an APPROVED appointment → 200 OK, CANCELLED
    // =========================================================================
    @Test
    @Order(7)
    @DisplayName("AP7: Cancel an APPROVED appointment → 200 OK, status CANCELLED")
    void ap7_cancelApprovedAppointment() throws Exception {
        mockMvc.perform(delete("/api/appointments/{id}", approvedApptId)
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.approvalStatus").value("CANCELLED"));
    }

    // =========================================================================
    // AP8 — Cancel an already-REJECTED appointment → 400 Bad Request
    // =========================================================================
    @Test
    @Order(8)
    @DisplayName("AP8: Cancel an already-REJECTED appointment → 400 Bad Request")
    void ap8_cancelRejectedAppointment() throws Exception {
        mockMvc.perform(delete("/api/appointments/{id}", rejectedApptId)
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // =========================================================================
    // AP9 — Book exceeding 4-tests-per-day limit → 400 Bad Request
    // =========================================================================
    @Test
    @Order(9)
    @DisplayName("AP9: Book appointment exceeding 4-tests/day limit → 400 Bad Request")
    void ap9_exceedDailyTestLimit() throws Exception {
        LocalDate limitDate = LocalDate.now().plusDays(40);

        // Book 4 tests in one appointment (hits the limit exactly)
        book(centerId, Set.of(testId, test2Id, test3Id, test4Id), limitDate);

        // A 5th test on the same day must be rejected
        Map<String, Object> body = Map.of(
                "centerId", centerId,
                "testIds", Set.of(test5Id),
                "appointmentDate", limitDate.toString());

        mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Daily limit exceeded")));
    }

    // =========================================================================
    // AP10 — Book exceeding 3-centers-per-day limit → 400 Bad Request
    // =========================================================================
    @Test
    @Order(10)
    @DisplayName("AP10: Book appointment exceeding 3-centers/day limit → 400 Bad Request")
    void ap10_exceedDailyCenterLimit() throws Exception {
        LocalDate limitDate = LocalDate.now().plusDays(50);

        // Need 3 distinct centers. Seed a 3rd center with a test.
        DiagnosticCenter center3 = centerRepository.findAll().stream()
                .filter(c -> c.getName().equals("Appt Center Three"))
                .findFirst()
                .orElseGet(() -> centerRepository.save(DiagnosticCenter.builder()
                        .name("Appt Center Three")
                        .address("3 Appt Ave")
                        .contactEmail("three.appt@center.com")
                        .status("ACTIVE")
                        .build()));
        int center3TestId = seedTest("Appt C3 Test", 200.0, center3);

        // Book at center 1, center 2 (other), center 3 — 3 distinct centers (limit)
        book(centerId, Set.of(testId), limitDate);
        book(otherCenterId, Set.of(otherCenterTestId), limitDate);
        book(center3.getId(), Set.of(center3TestId), limitDate);

        // 4th distinct center must be rejected
        DiagnosticCenter center4 = centerRepository.findAll().stream()
                .filter(c -> c.getName().equals("Appt Center Four"))
                .findFirst()
                .orElseGet(() -> centerRepository.save(DiagnosticCenter.builder()
                        .name("Appt Center Four")
                        .address("4 Appt Ave")
                        .contactEmail("four.appt@center.com")
                        .status("ACTIVE")
                        .build()));
        int center4TestId = seedTest("Appt C4 Test", 200.0, center4);

        Map<String, Object> body = Map.of(
                "centerId", center4.getId(),
                "testIds", Set.of(center4TestId),
                "appointmentDate", limitDate.toString());

        mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Daily limit exceeded")));
    }

    // =========================================================================
    // AP11 — CENTER_ADMIN approves appointment at a different center → 403
    // =========================================================================
    @Test
    @Order(11)
    @DisplayName("AP11: CENTER_ADMIN approves appointment at a different center → 403 Forbidden")
    void ap11_centerAdminApprovesOtherCenterAppointment() throws Exception {
        // Book appointment at the primary center
        int apptId = book(centerId, Set.of(test3Id), LocalDate.now().plusDays(60));

        // The admin of the OTHER center tries to approve it
        mockMvc.perform(put("/api/admin/appointments/{id}/approve", apptId)
                .header("Authorization", "Bearer " + centerAdminOtherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("remarks", "Trying to approve"))))
                .andExpect(status().isForbidden());
    }
}
