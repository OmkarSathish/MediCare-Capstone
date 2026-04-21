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
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Diagnostic Center integration tests — 12 cases:
 *
 * DC1  — GET /api/centers (public, no auth) → 200 OK, list returned
 * DC2  — GET /api/centers?search= (keyword match) → 200 OK, filtered
 * DC3  — GET /api/centers/{id} (public) → 200 OK, correct center
 * DC4  — GET /api/centers/{id}/tests → 200 OK, offerings list
 * DC5  — GET /api/centers/offering/{testId} → 200 OK, centers list
 * DC6  — POST /api/centers/{id}/tests/{testId} (CENTER_ADMIN, own center) → 201 Created
 * DC7  — GET /api/centers/{id}/tests/{testId}/suggested-price → 200 OK, suggestedPrice
 * DC8  — PUT /api/centers/{id}/tests/{testId}/price (owner) → 200 OK
 * DC9  — PUT /api/centers/{id}/tests/{testId}/price (non-existent offering) → 404 Not Found
 * DC10 — DELETE /api/centers/{id}/tests/{testId} (CENTER_ADMIN, own center) → 200 OK
 * DC11 — DELETE /api/centers/{id} (ADMIN soft-delete) → 200 OK, status INACTIVE
 * DC12 — CENTER_ADMIN manages tests at a different center → 403 Forbidden
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DiagnosticCenterIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired IRoleRepository roleRepository;
    @Autowired IUserRepository userRepository;
    @Autowired IDiagnosticCenterRepository centerRepository;
    @Autowired IDiagnosticTestRepository testRepository;
    @Autowired ICenterTestOfferingRepository offeringRepository;
    @Autowired IAuthTokenService authTokenService;
    @Autowired IPasswordEncoderService passwordEncoderService;

    private static String adminToken;
    private static String centerAdminToken;
    private static String otherCenterAdminToken;
    private static int centerId;
    private static int otherCenterId;
    private static int testId;       // test pre-added to centerId
    private static int addTestId;    // test used in DC6 add/remove flow

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void seed() {
        // ── Roles ──────────────────────────────────────────────────────────────
        if (roleRepository.findByRoleName(RoleConstants.ADMIN).isEmpty())
            roleRepository.save(Role.builder().roleName(RoleConstants.ADMIN).build());
        if (roleRepository.findByRoleName(RoleConstants.CENTER_ADMIN).isEmpty())
            roleRepository.save(Role.builder().roleName(RoleConstants.CENTER_ADMIN).build());

        Role adminRole       = roleRepository.findByRoleName(RoleConstants.ADMIN).get();
        Role centerAdminRole = roleRepository.findByRoleName(RoleConstants.CENTER_ADMIN).get();

        // ── Centers ────────────────────────────────────────────────────────────
        DiagnosticCenter center = centerRepository.findAll().stream()
                .filter(c -> c.getName().equals("DC Test Center"))
                .findFirst()
                .orElseGet(() -> centerRepository.save(DiagnosticCenter.builder()
                        .name("DC Test Center")
                        .address("1 DC Ave")
                        .contactEmail("dc@center.com")
                        .status("ACTIVE")
                        .build()));
        centerId = center.getId();

        DiagnosticCenter otherCenter = centerRepository.findAll().stream()
                .filter(c -> c.getName().equals("DC Other Center"))
                .findFirst()
                .orElseGet(() -> centerRepository.save(DiagnosticCenter.builder()
                        .name("DC Other Center")
                        .address("2 DC Ave")
                        .contactEmail("dc.other@center.com")
                        .status("ACTIVE")
                        .build()));
        otherCenterId = otherCenter.getId();

        // ── Tests ──────────────────────────────────────────────────────────────
        DiagnosticTest test = testRepository.findAll().stream()
                .filter(t -> t.getTestName().equals("DC Test CBC"))
                .findFirst()
                .orElseGet(() -> testRepository.save(DiagnosticTest.builder()
                        .testName("DC Test CBC")
                        .testPrice(500.0)
                        .status("ACTIVE")
                        .build()));
        testId = test.getId();

        // Ensure test is pre-offered at centerId (needed for DC4, DC5, DC7, DC8)
        if (offeringRepository.findById(new CenterTestOfferingKey(centerId, testId)).isEmpty()) {
            offeringRepository.save(CenterTestOffering.builder()
                    .id(new CenterTestOfferingKey(centerId, testId))
                    .center(center)
                    .test(test)
                    .price(500.0)
                    .build());
        }

        DiagnosticTest addTest = testRepository.findAll().stream()
                .filter(t -> t.getTestName().equals("DC Test Urine"))
                .findFirst()
                .orElseGet(() -> testRepository.save(DiagnosticTest.builder()
                        .testName("DC Test Urine")
                        .testPrice(300.0)
                        .status("ACTIVE")
                        .build()));
        addTestId = addTest.getId();
        // Remove any stale offering so DC6 can always re-add it fresh
        offeringRepository.findById(new CenterTestOfferingKey(centerId, addTestId))
                .ifPresent(offeringRepository::delete);

        // ── Users ──────────────────────────────────────────────────────────────
        UserAccount admin = userRepository.findByEmail("dc.admin@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("DC Admin")
                        .email("dc.admin@test.com")
                        .phone("09700000001")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .roles(Set.of(adminRole))
                        .build()));
        adminToken = authTokenService.generateToken(admin);

        UserAccount centerAdmin = userRepository.findByEmail("dc.centeradmin@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("DC Center Admin")
                        .email("dc.centeradmin@test.com")
                        .phone("09700000002")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .centerId(centerId)
                        .roles(Set.of(centerAdminRole))
                        .build()));
        centerAdminToken = authTokenService.generateToken(centerAdmin);

        UserAccount otherCenterAdmin = userRepository.findByEmail("dc.centeradmin.other@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("DC Other Center Admin")
                        .email("dc.centeradmin.other@test.com")
                        .phone("09700000003")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .centerId(otherCenterId)
                        .roles(Set.of(centerAdminRole))
                        .build()));
        otherCenterAdminToken = authTokenService.generateToken(otherCenterAdmin);
    }

    // =========================================================================
    // DC1 — List all centers (no auth) → 200 OK
    // =========================================================================
    @Test
    @Order(1)
    @DisplayName("DC1: Public list all centers → 200 OK")
    void dc1_publicListAllCenters() throws Exception {
        mockMvc.perform(get("/api/centers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    // =========================================================================
    // DC2 — Search centers by name keyword → 200 OK, filtered
    // =========================================================================
    @Test
    @Order(2)
    @DisplayName("DC2: Search centers by name keyword → 200 OK, matching results")
    void dc2_searchCentersByName() throws Exception {
        mockMvc.perform(get("/api/centers").param("search", "DC Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[*].name", hasItem(containsString("DC Test"))));
    }

    // =========================================================================
    // DC3 — Get center by ID (public) → 200 OK
    // =========================================================================
    @Test
    @Order(3)
    @DisplayName("DC3: Public get center by ID → 200 OK")
    void dc3_publicGetCenterById() throws Exception {
        mockMvc.perform(get("/api/centers/{id}", centerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(centerId))
                .andExpect(jsonPath("$.data.name").value("DC Test Center"));
    }

    // =========================================================================
    // DC4 — Get tests offered at a center → 200 OK, list with seeded test
    // =========================================================================
    @Test
    @Order(4)
    @DisplayName("DC4: Get tests offered at a center → 200 OK, non-empty")
    void dc4_getTestsAtCenter() throws Exception {
        mockMvc.perform(get("/api/centers/{id}/tests", centerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    // =========================================================================
    // DC5 — Get centers offering a specific test → 200 OK
    // =========================================================================
    @Test
    @Order(5)
    @DisplayName("DC5: Get centers offering a specific test → 200 OK, list includes seeded center")
    void dc5_getCentersOfferingTest() throws Exception {
        mockMvc.perform(get("/api/centers/offering/{testId}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[*].id", hasItem(centerId)));
    }

    // =========================================================================
    // DC6 — CENTER_ADMIN adds a test to own center → 201 Created
    // =========================================================================
    @Test
    @Order(6)
    @DisplayName("DC6: CENTER_ADMIN adds test to own center → 201 Created")
    void dc6_centerAdminAddsTest() throws Exception {
        mockMvc.perform(post("/api/centers/{id}/tests/{testId}", centerId, addTestId)
                .header("Authorization", "Bearer " + centerAdminToken)
                .param("price", "350"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.testId").value(addTestId));
    }

    // =========================================================================
    // DC7 — Get suggested price for a test at a center → 200 OK
    // =========================================================================
    @Test
    @Order(7)
    @DisplayName("DC7: Get suggested price for test at center → 200 OK, suggestedPrice present")
    void dc7_getSuggestedPrice() throws Exception {
        mockMvc.perform(get("/api/centers/{id}/tests/{testId}/suggested-price", centerId, testId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.suggestedPrice").isNumber());
    }

    // =========================================================================
    // DC8 — Update price for a test at a center → 200 OK
    // =========================================================================
    @Test
    @Order(8)
    @DisplayName("DC8: CENTER_ADMIN updates price for test at own center → 200 OK")
    void dc8_updateTestPrice() throws Exception {
        mockMvc.perform(put("/api/centers/{id}/tests/{testId}/price", centerId, testId)
                .header("Authorization", "Bearer " + centerAdminToken)
                .param("price", "550"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // DC9 — Update price for a non-existent offering → 404 Not Found
    // =========================================================================
    @Test
    @Order(9)
    @DisplayName("DC9: Update price for non-existent offering → 404 Not Found")
    void dc9_updatePriceNonExistentOffering() throws Exception {
        mockMvc.perform(put("/api/centers/{id}/tests/{testId}/price", centerId, 99999)
                .header("Authorization", "Bearer " + adminToken)
                .param("price", "100"))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // DC10 — CENTER_ADMIN removes test from own center → 200 OK
    // =========================================================================
    @Test
    @Order(10)
    @DisplayName("DC10: CENTER_ADMIN removes test from own center → 200 OK")
    void dc10_centerAdminRemovesTest() throws Exception {
        mockMvc.perform(delete("/api/centers/{id}/tests/{testId}", centerId, addTestId)
                .header("Authorization", "Bearer " + centerAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // DC11 — ADMIN soft-deletes a center → 200 OK, status INACTIVE
    // =========================================================================
    @Test
    @Order(11)
    @DisplayName("DC11: ADMIN soft-deletes center → 200 OK, status INACTIVE")
    void dc11_adminSoftDeletesCenter() throws Exception {
        // Create a throwaway center to delete
        String body = mapper.writeValueAsString(
                java.util.Map.of("name", "DC Throwaway", "address", "99 DC Ave", "contactEmail", "throwaway@dc.com"));

        String response = mockMvc.perform(post("/api/centers")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int newId = mapper.readTree(response).path("data").path("id").asInt();

        mockMvc.perform(delete("/api/centers/{id}", newId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    // =========================================================================
    // DC12 — CENTER_ADMIN manages tests at a different center → 403 Forbidden
    // =========================================================================
    @Test
    @Order(12)
    @DisplayName("DC12: CENTER_ADMIN manages tests at different center → 403 Forbidden")
    void dc12_centerAdminCrossCenter() throws Exception {
        // otherCenterAdmin tries to add a test to centerId (not their center)
        mockMvc.perform(post("/api/centers/{id}/tests/{testId}", centerId, testId)
                .header("Authorization", "Bearer " + otherCenterAdminToken)
                .param("price", "500"))
                .andExpect(status().isForbidden());
    }
}
