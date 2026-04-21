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
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Dashboard integration tests — 5 cases:
 *
 * D1 — ADMIN fetches platform-wide dashboard → 200 OK with all metric fields
 * D2 — CUSTOMER calls ADMIN dashboard → 403 Forbidden
 * D3 — CENTER_ADMIN fetches center-scoped dashboard → 200 OK with center
 * metrics
 * D4 — CENTER_STAFF fetches center-scoped dashboard → 200 OK
 * D5 — CENTER_ADMIN with no assigned centerId calls center dashboard → 404 Not
 * Found
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DashboardIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    IRoleRepository roleRepository;
    @Autowired
    IUserRepository userRepository;
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
    private static String centerStaffToken;
    private static String centerAdminNoCenterToken;

    @BeforeEach
    void seed() {
        // ── Roles ──────────────────────────────────────────────────────────────
        if (roleRepository.findByRoleName(RoleConstants.ADMIN).isEmpty())
            roleRepository.save(Role.builder().roleName(RoleConstants.ADMIN).build());
        if (roleRepository.findByRoleName(RoleConstants.CUSTOMER).isEmpty())
            roleRepository.save(Role.builder().roleName(RoleConstants.CUSTOMER).build());
        if (roleRepository.findByRoleName(RoleConstants.CENTER_ADMIN).isEmpty())
            roleRepository.save(Role.builder().roleName(RoleConstants.CENTER_ADMIN).build());
        if (roleRepository.findByRoleName(RoleConstants.CENTER_STAFF).isEmpty())
            roleRepository.save(Role.builder().roleName(RoleConstants.CENTER_STAFF).build());

        Role adminRole = roleRepository.findByRoleName(RoleConstants.ADMIN).get();
        Role customerRole = roleRepository.findByRoleName(RoleConstants.CUSTOMER).get();
        Role centerAdminRole = roleRepository.findByRoleName(RoleConstants.CENTER_ADMIN).get();
        Role centerStaffRole = roleRepository.findByRoleName(RoleConstants.CENTER_STAFF).get();

        // ── Diagnostic center with one test offering ───────────────────────────
        DiagnosticCenter center = centerRepository.findAll().stream()
                .filter(c -> c.getName().equals("Dash Test Center"))
                .findFirst()
                .orElseGet(() -> centerRepository.save(DiagnosticCenter.builder()
                        .name("Dash Test Center")
                        .address("1 Dashboard Ave")
                        .contactEmail("dash@center.com")
                        .status("ACTIVE")
                        .build()));

        DiagnosticTest test = testRepository.findAll().stream()
                .filter(t -> t.getTestName().equals("Glucose Test"))
                .findFirst()
                .orElseGet(() -> testRepository.save(DiagnosticTest.builder()
                        .testName("Glucose Test")
                        .testPrice(300.0)
                        .status("ACTIVE")
                        .build()));

        if (offeringRepository.findById(
                new CenterTestOfferingKey(center.getId(), test.getId())).isEmpty()) {
            offeringRepository.save(CenterTestOffering.builder()
                    .id(new CenterTestOfferingKey(center.getId(), test.getId()))
                    .center(center)
                    .test(test)
                    .price(300.0)
                    .build());
        }

        // ── Users (idempotent) ─────────────────────────────────────────────────
        UserAccount admin = userRepository.findByEmail("dash.admin@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Dash Admin")
                        .email("dash.admin@test.com")
                        .phone("09800000001")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .roles(Set.of(adminRole))
                        .build()));
        adminToken = authTokenService.generateToken(admin);

        UserAccount customer = userRepository.findByEmail("dash.customer@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Dash Customer")
                        .email("dash.customer@test.com")
                        .phone("09800000002")
                        .passwordHash(passwordEncoderService.encode("Secure@123"))
                        .status("ACTIVE")
                        .roles(Set.of(customerRole))
                        .build()));
        customerToken = authTokenService.generateToken(customer);

        UserAccount centerAdmin = userRepository.findByEmail("dash.centeradmin@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Dash Center Admin")
                        .email("dash.centeradmin@test.com")
                        .phone("09800000003")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .centerId(center.getId())
                        .roles(Set.of(centerAdminRole))
                        .build()));
        centerAdminToken = authTokenService.generateToken(centerAdmin);

        UserAccount centerStaff = userRepository.findByEmail("dash.centerstaff@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Dash Center Staff")
                        .email("dash.centerstaff@test.com")
                        .phone("09800000004")
                        .passwordHash(passwordEncoderService.encode("Staff@1234"))
                        .status("ACTIVE")
                        .centerId(center.getId())
                        .roles(Set.of(centerStaffRole))
                        .build()));
        centerStaffToken = authTokenService.generateToken(centerStaff);

        // CENTER_ADMIN with no centerId — triggers the 404 path
        UserAccount noCenter = userRepository.findByEmail("dash.nocenter@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Dash No Center")
                        .email("dash.nocenter@test.com")
                        .phone("09800000005")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .centerId(null)
                        .roles(Set.of(centerAdminRole))
                        .build()));
        centerAdminNoCenterToken = authTokenService.generateToken(noCenter);
    }

    // =========================================================================
    // D1 — ADMIN fetches the platform-wide dashboard → 200 OK
    // =========================================================================
    @Test
    @Order(1)
    @DisplayName("D1: ADMIN fetches platform dashboard → 200 OK with all metric fields present")
    void d1_adminDashboard_returnsOk() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // All headline stat fields must be present (values ≥ 0)
                .andExpect(jsonPath("$.data.totalCenters").isNumber())
                .andExpect(jsonPath("$.data.totalTests").isNumber())
                .andExpect(jsonPath("$.data.totalPatients").isNumber())
                .andExpect(jsonPath("$.data.totalAppointments").isNumber())
                .andExpect(jsonPath("$.data.totalCenterAdmins").isNumber())
                // Status breakdown fields
                .andExpect(jsonPath("$.data.pendingAppointments").isNumber())
                .andExpect(jsonPath("$.data.approvedAppointments").isNumber())
                .andExpect(jsonPath("$.data.rejectedAppointments").isNumber())
                .andExpect(jsonPath("$.data.cancelledAppointments").isNumber())
                // Chart maps must be present
                .andExpect(jsonPath("$.data.appointmentsByCenter").exists())
                .andExpect(jsonPath("$.data.appointmentsByMonth").exists())
                .andExpect(jsonPath("$.data.topTests").exists());
    }

    // =========================================================================
    // D2 — CUSTOMER calls ADMIN-only dashboard → 403 Forbidden
    // =========================================================================
    @Test
    @Order(2)
    @DisplayName("D2: CUSTOMER calls admin dashboard → 403 Forbidden")
    void d2_customerCallsAdminDashboard_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // D3 — CENTER_ADMIN fetches center-scoped dashboard → 200 OK
    // =========================================================================
    @Test
    @Order(3)
    @DisplayName("D3: CENTER_ADMIN fetches center dashboard → 200 OK with center metrics")
    void d3_centerAdminDashboard_returnsOk() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/center")
                .header("Authorization", "Bearer " + centerAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.centerName").value("Dash Test Center"))
                .andExpect(jsonPath("$.data.totalAppointments").isNumber())
                .andExpect(jsonPath("$.data.pendingAppointments").isNumber())
                .andExpect(jsonPath("$.data.approvedAppointments").isNumber())
                .andExpect(jsonPath("$.data.rejectedAppointments").isNumber())
                .andExpect(jsonPath("$.data.cancelledAppointments").isNumber())
                // The center was seeded with 1 test offering
                .andExpect(jsonPath("$.data.assignedTests").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.appointmentsByMonth").exists())
                .andExpect(jsonPath("$.data.topTests").exists());
    }

    // =========================================================================
    // D4 — CENTER_STAFF fetches center-scoped dashboard → 200 OK
    // =========================================================================
    @Test
    @Order(4)
    @DisplayName("D4: CENTER_STAFF fetches center dashboard → 200 OK")
    void d4_centerStaffDashboard_returnsOk() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/center")
                .header("Authorization", "Bearer " + centerStaffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.centerName").value("Dash Test Center"));
    }

    // =========================================================================
    // D5 — CENTER_ADMIN with no assigned center calls center dashboard → 404
    // =========================================================================
    @Test
    @Order(5)
    @DisplayName("D5: CENTER_ADMIN with no centerId calls center dashboard → 404 Not Found")
    void d5_centerAdminNoCenterId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/center")
                .header("Authorization", "Bearer " + centerAdminNoCenterToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
