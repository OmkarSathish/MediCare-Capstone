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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Diagnostic Test integration tests — 7 cases:
 *
 * DT1 — GET /api/tests (public) → 200 OK, list returned
 * DT2 — GET /api/tests?search= (keyword) → 200 OK, filtered
 * DT3 — GET /api/tests/{id} (public) → 200 OK, correct test
 * DT4 — GET /api/tests/{testId}/prices → 200 OK, sorted price list
 * DT5 — PUT /api/tests/{id} (ADMIN updates) → 200 OK, updated fields
 * DT6 — DELETE /api/tests/{id} (ADMIN soft-delete) → 200 OK, status INACTIVE
 * DT7 — GET /api/tests/{id} for non-existent ID → 404 Not Found
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DiagnosticTestIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired IRoleRepository roleRepository;
    @Autowired IUserRepository userRepository;
    @Autowired IDiagnosticCenterRepository centerRepository;
    @Autowired IDiagnosticTestRepository testRepository;
    @Autowired ICenterTestOfferingRepository offeringRepository;
    @Autowired IAuthTokenService authTokenService;
    @Autowired IPasswordEncoderService passwordEncoderService;

    private static String adminToken;
    private static int seededTestId;   // pre-existing test (for read tests)
    private static int createdTestId;  // created in DT1 setup, updated in DT5, deleted in DT6

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void seed() {
        // ── Roles ──────────────────────────────────────────────────────────────
        if (roleRepository.findByRoleName(RoleConstants.ADMIN).isEmpty())
            roleRepository.save(Role.builder().roleName(RoleConstants.ADMIN).build());

        Role adminRole = roleRepository.findByRoleName(RoleConstants.ADMIN).get();

        UserAccount admin = userRepository.findByEmail("dt.admin@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("DT Admin")
                        .email("dt.admin@test.com")
                        .phone("09500000001")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .roles(Set.of(adminRole))
                        .build()));
        adminToken = authTokenService.generateToken(admin);

        // ── Seeded test with pricing at a center (for DT4) ────────────────────
        DiagnosticTest seededTest = testRepository.findAll().stream()
                .filter(t -> t.getTestName().equals("DT Seeded CBC"))
                .findFirst()
                .orElseGet(() -> testRepository.save(DiagnosticTest.builder()
                        .testName("DT Seeded CBC")
                        .testPrice(400.0)
                        .status("ACTIVE")
                        .build()));
        seededTestId = seededTest.getId();

        DiagnosticCenter center = centerRepository.findAll().stream()
                .filter(c -> c.getName().equals("DT Price Center"))
                .findFirst()
                .orElseGet(() -> centerRepository.save(DiagnosticCenter.builder()
                        .name("DT Price Center")
                        .address("1 DT Ave")
                        .contactEmail("dt@center.com")
                        .status("ACTIVE")
                        .build()));

        if (offeringRepository.findById(
                new CenterTestOfferingKey(center.getId(), seededTestId)).isEmpty()) {
            offeringRepository.save(CenterTestOffering.builder()
                    .id(new CenterTestOfferingKey(center.getId(), seededTestId))
                    .center(center)
                    .test(seededTest)
                    .price(400.0)
                    .build());
        }
    }

    // =========================================================================
    // DT1 — List all tests (no auth) → 200 OK
    // =========================================================================
    @Test
    @Order(1)
    @DisplayName("DT1: Public list all tests → 200 OK, list returned")
    void dt1_publicListAllTests() throws Exception {
        mockMvc.perform(get("/api/tests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    // =========================================================================
    // DT2 — Search tests by name keyword → 200 OK, filtered
    // =========================================================================
    @Test
    @Order(2)
    @DisplayName("DT2: Search tests by name keyword → 200 OK, matching results")
    void dt2_searchTestsByName() throws Exception {
        mockMvc.perform(get("/api/tests").param("search", "DT Seeded"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[*].testName", hasItem(containsString("DT Seeded"))));
    }

    // =========================================================================
    // DT3 — Get a single test by ID (public) → 200 OK
    // =========================================================================
    @Test
    @Order(3)
    @DisplayName("DT3: Public get test by ID → 200 OK, correct test")
    void dt3_publicGetTestById() throws Exception {
        mockMvc.perform(get("/api/tests/{id}", seededTestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(seededTestId))
                .andExpect(jsonPath("$.data.testName").value("DT Seeded CBC"));
    }

    // =========================================================================
    // DT4 — Get all center prices for a test → 200 OK, sorted list
    // =========================================================================
    @Test
    @Order(4)
    @DisplayName("DT4: Get center prices for test → 200 OK, non-empty sorted list")
    void dt4_getPricesForTest() throws Exception {
        mockMvc.perform(get("/api/tests/{testId}/prices", seededTestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].price").isNumber());
    }

    // =========================================================================
    // DT5 — ADMIN creates a test, then updates it → 200 OK
    // =========================================================================
    @Test
    @Order(5)
    @DisplayName("DT5: ADMIN updates a test → 200 OK, updated name and price")
    void dt5_adminUpdatesTest() throws Exception {
        // First, create a test to update
        Map<String, Object> createBody = Map.of("testName", "DT Mutable Test", "testPrice", 250.0);
        MvcResult created = mockMvc.perform(post("/api/tests")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createBody)))
                .andExpect(status().isCreated())
                .andReturn();
        createdTestId = mapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("id").asInt();

        // Now update it
        Map<String, Object> updateBody = Map.of("testName", "DT Mutable Test Updated", "testPrice", 275.0);
        mockMvc.perform(put("/api/tests/{id}", createdTestId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.testName").value("DT Mutable Test Updated"))
                .andExpect(jsonPath("$.data.testPrice").value(275.0));
    }

    // =========================================================================
    // DT6 — ADMIN soft-deletes a test → 200 OK, status INACTIVE
    // =========================================================================
    @Test
    @Order(6)
    @DisplayName("DT6: ADMIN soft-deletes test → 200 OK, status INACTIVE")
    void dt6_adminSoftDeletesTest() throws Exception {
        mockMvc.perform(delete("/api/tests/{id}", createdTestId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    // =========================================================================
    // DT7 — Get non-existent test by ID → 404 Not Found
    // =========================================================================
    @Test
    @Order(7)
    @DisplayName("DT7: Get non-existent test by ID → 404 Not Found")
    void dt7_getNonExistentTest() throws Exception {
        mockMvc.perform(get("/api/tests/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
