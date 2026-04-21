package com.capstone.healthcare;

import com.capstone.healthcare.auth.model.Role;
import com.capstone.healthcare.auth.model.UserAccount;
import com.capstone.healthcare.auth.repository.IAdminRepository;
import com.capstone.healthcare.auth.repository.IRoleRepository;
import com.capstone.healthcare.auth.repository.IUserRepository;
import com.capstone.healthcare.auth.service.IAuthTokenService;
import com.capstone.healthcare.auth.service.IPasswordEncoderService;
import com.capstone.healthcare.auth.service.impl.AuthTokenServiceImpl;
import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import com.capstone.healthcare.diagnosticcenter.repository.IDiagnosticCenterRepository;
import com.capstone.healthcare.shared.security.RoleConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Auth integration tests — 14 cases:
 *
 * A1  — GET  /api/auth/me                              → 200 OK with profile
 * A2  — POST /api/auth/token/refresh (valid token)     → 200 OK, new access token
 * A3  — POST /api/auth/token/refresh (bogus token)     → 404 Not Found
 * A4  — POST /api/auth/logout                          → 200 OK, token revoked
 * A5  — ADMIN creates CENTER_ADMIN                     → 201 Created
 * A6  — ADMIN lists center admins                      → 200 OK, non-empty list
 * A7  — ADMIN removes CENTER_ADMIN                     → 200 OK
 * A8  — CENTER_ADMIN creates staff member              → 201 Created
 * A9  — CENTER_ADMIN lists staff for their center      → 200 OK
 * A10 — CENTER_ADMIN removes their own staff           → 200 OK
 * A11 — CENTER_ADMIN removes staff from different center → 403 Forbidden
 * A12 — POST /api/auth/forgot-password, unknown email  → 404 Not Found
 * A13 — POST /api/auth/reset-password, no OTP requested → 400 Bad Request
 * A14 — POST /api/auth/reset-password, wrong OTP      → 400 Bad Request
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired IRoleRepository roleRepository;
    @Autowired IUserRepository userRepository;
    @Autowired IAdminRepository adminRepository;
    @Autowired IDiagnosticCenterRepository centerRepository;
    @Autowired IAuthTokenService authTokenService;
    @Autowired AuthTokenServiceImpl authTokenServiceImpl;
    @Autowired IPasswordEncoderService passwordEncoderService;

    // Mock mail sender so forgot-password tests don't need a real SMTP server
    @MockBean JavaMailSender javaMailSender;

    private static String adminToken;
    private static String customerToken;
    private static String centerAdminToken;
    private static String centerAdminOtherToken;  // belongs to a different center
    private static String refreshTokenValue;
    private static int centerAdminUserId;         // used by A7
    private static int staffUserId;              // used by A10/A11
    private static int otherCenterStaffId;       // staff at a different center
    private static int centerId;
    private static int otherCenterId;

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
        if (roleRepository.findByRoleName(RoleConstants.CENTER_STAFF).isEmpty())
            roleRepository.save(Role.builder().roleName(RoleConstants.CENTER_STAFF).build());

        Role adminRole       = roleRepository.findByRoleName(RoleConstants.ADMIN).get();
        Role customerRole    = roleRepository.findByRoleName(RoleConstants.CUSTOMER).get();
        Role centerAdminRole = roleRepository.findByRoleName(RoleConstants.CENTER_ADMIN).get();
        Role staffRole       = roleRepository.findByRoleName(RoleConstants.CENTER_STAFF).get();

        // ── Diagnostic centers ─────────────────────────────────────────────────
        DiagnosticCenter center = centerRepository.findAll().stream()
                .filter(c -> c.getName().equals("Auth Test Center"))
                .findFirst()
                .orElseGet(() -> centerRepository.save(DiagnosticCenter.builder()
                        .name("Auth Test Center")
                        .address("1 Auth Ave")
                        .contactEmail("auth@center.com")
                        .status("ACTIVE")
                        .build()));
        centerId = center.getId();

        DiagnosticCenter otherCenter = centerRepository.findAll().stream()
                .filter(c -> c.getName().equals("Other Auth Center"))
                .findFirst()
                .orElseGet(() -> centerRepository.save(DiagnosticCenter.builder()
                        .name("Other Auth Center")
                        .address("2 Auth Ave")
                        .contactEmail("other@center.com")
                        .status("ACTIVE")
                        .build()));
        otherCenterId = otherCenter.getId();

        // ── Users ──────────────────────────────────────────────────────────────
        UserAccount admin = userRepository.findByEmail("auth.admin@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Auth Admin")
                        .email("auth.admin@test.com")
                        .phone("09700000001")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .roles(Set.of(adminRole))
                        .build()));
        adminToken = authTokenService.generateToken(admin);

        UserAccount customer = userRepository.findByEmail("auth.customer@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Auth Customer")
                        .email("auth.customer@test.com")
                        .phone("09700000002")
                        .passwordHash(passwordEncoderService.encode("Secure@123"))
                        .status("ACTIVE")
                        .roles(Set.of(customerRole))
                        .build()));
        customerToken = authTokenService.generateToken(customer);
        // Create a real refresh token for A2/A4 tests
        if (refreshTokenValue == null) {
            refreshTokenValue = authTokenServiceImpl.createRefreshToken(customer.getUserId()).getTokenValue();
        }

        UserAccount centerAdmin = userRepository.findByEmail("auth.centeradmin@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Auth Center Admin")
                        .email("auth.centeradmin@test.com")
                        .phone("09700000003")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .centerId(centerId)
                        .roles(Set.of(centerAdminRole))
                        .build()));
        centerAdminToken = authTokenService.generateToken(centerAdmin);

        UserAccount centerAdminOther = userRepository.findByEmail("auth.centeradmin.other@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Auth Center Admin Other")
                        .email("auth.centeradmin.other@test.com")
                        .phone("09700000004")
                        .passwordHash(passwordEncoderService.encode("Admin@1234"))
                        .status("ACTIVE")
                        .centerId(otherCenterId)
                        .roles(Set.of(centerAdminRole))
                        .build()));
        centerAdminOtherToken = authTokenService.generateToken(centerAdminOther);

        // Pre-seed a staff member belonging to otherCenter (used in A11)
        UserAccount otherStaff = userRepository.findByEmail("auth.staff.other@test.com")
                .orElseGet(() -> userRepository.save(UserAccount.builder()
                        .fullName("Other Center Staff")
                        .email("auth.staff.other@test.com")
                        .phone("09700000005")
                        .passwordHash(passwordEncoderService.encode("Staff@1234"))
                        .status("ACTIVE")
                        .centerId(otherCenterId)
                        .roles(Set.of(staffRole))
                        .build()));
        otherCenterStaffId = otherStaff.getUserId();
    }

    // =========================================================================
    // A1 — Authenticated user fetches their own profile → 200 OK
    // =========================================================================
    @Test
    @Order(1)
    @DisplayName("A1: GET /api/auth/me → 200 OK with caller's profile")
    void a1_getMe_returnsProfile() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("auth.customer@test.com"));
    }

    // =========================================================================
    // A2 — Refresh with valid refresh token → 200 OK, new access token
    // =========================================================================
    @Test
    @Order(2)
    @DisplayName("A2: POST /api/auth/token/refresh (valid) → 200 OK, new accessToken returned")
    void a2_refreshToken_valid_returnsNewAccessToken() throws Exception {
        Map<String, String> body = Map.of("refreshToken", refreshTokenValue);

        mockMvc.perform(post("/api/auth/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    // =========================================================================
    // A3 — Refresh with a bogus/non-existent token → 404 Not Found
    // =========================================================================
    @Test
    @Order(3)
    @DisplayName("A3: POST /api/auth/token/refresh (invalid token) → 404 Not Found")
    void a3_refreshToken_invalid_returnsNotFound() throws Exception {
        Map<String, String> body = Map.of("refreshToken", "completely-fake-token-value");

        mockMvc.perform(post("/api/auth/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // A4 — Logout revokes the refresh token → 200 OK
    // =========================================================================
    @Test
    @Order(4)
    @DisplayName("A4: POST /api/auth/logout → 200 OK, token revoked")
    void a4_logout_revokesRefreshToken() throws Exception {
        // Create a fresh refresh token to revoke (avoid invalidating A2's token)
        UserAccount customer = userRepository.findByEmail("auth.customer@test.com").get();
        String tokenToRevoke = authTokenServiceImpl.createRefreshToken(customer.getUserId()).getTokenValue();

        Map<String, String> body = Map.of("refreshToken", tokenToRevoke);

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Using the revoked token for refresh should now fail
        mockMvc.perform(post("/api/auth/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isInternalServerError());
    }

    // =========================================================================
    // A5 — ADMIN creates a CENTER_ADMIN account → 201 Created
    // =========================================================================
    @Test
    @Order(5)
    @DisplayName("A5: ADMIN creates a CENTER_ADMIN → 201 Created")
    void a5_adminCreatesCenterAdmin_returnsCreated() throws Exception {
        Map<String, Object> body = Map.of(
                "fullName", "New Center Admin",
                "email", "new.centeradmin@test.com",
                "password", "Admin@1234",
                "centerId", centerId);

        MvcResult result = mockMvc.perform(post("/api/auth/admin/center-admins")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        // Capture the created user's ID for A7
        userRepository.findByEmail("new.centeradmin@test.com")
                .ifPresent(u -> centerAdminUserId = u.getUserId());
    }

    // =========================================================================
    // A6 — ADMIN lists all center admins → 200 OK, list contains the new entry
    // =========================================================================
    @Test
    @Order(6)
    @DisplayName("A6: ADMIN lists center admins → 200 OK with at least one entry")
    void a6_adminListsCenterAdmins_returnsList() throws Exception {
        mockMvc.perform(get("/api/auth/admin/center-admins")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    // =========================================================================
    // A7 — ADMIN removes the CENTER_ADMIN created in A5 → 200 OK
    // =========================================================================
    @Test
    @Order(7)
    @DisplayName("A7: ADMIN removes CENTER_ADMIN → 200 OK")
    void a7_adminRemovesCenterAdmin_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/auth/admin/center-admins/{id}", centerAdminUserId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // A8 — CENTER_ADMIN creates a staff member → 201 Created
    // =========================================================================
    @Test
    @Order(8)
    @DisplayName("A8: CENTER_ADMIN creates a staff member → 201 Created")
    void a8_centerAdminCreatesStaff_returnsCreated() throws Exception {
        Map<String, String> body = Map.of(
                "fullName", "New Staff Member",
                "email", "new.staff@test.com",
                "password", "Staff@1234");

        mockMvc.perform(post("/api/auth/center-admin/staff")
                .header("Authorization", "Bearer " + centerAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // Capture created staff's ID for A10
        userRepository.findByEmail("new.staff@test.com")
                .ifPresent(u -> staffUserId = u.getUserId());
    }

    // =========================================================================
    // A9 — CENTER_ADMIN lists staff for their center → 200 OK
    // =========================================================================
    @Test
    @Order(9)
    @DisplayName("A9: CENTER_ADMIN lists staff for their center → 200 OK with at least one entry")
    void a9_centerAdminListsStaff_returnsList() throws Exception {
        mockMvc.perform(get("/api/auth/center-admin/staff")
                .header("Authorization", "Bearer " + centerAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    // =========================================================================
    // A10 — CENTER_ADMIN removes their own staff member → 200 OK
    // =========================================================================
    @Test
    @Order(10)
    @DisplayName("A10: CENTER_ADMIN removes their own staff → 200 OK")
    void a10_centerAdminRemovesOwnStaff_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/auth/center-admin/staff/{id}", staffUserId)
                .header("Authorization", "Bearer " + centerAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // A11 — CENTER_ADMIN removes staff belonging to a different center → 403
    // =========================================================================
    @Test
    @Order(11)
    @DisplayName("A11: CENTER_ADMIN tries to remove staff from a different center → 403 Forbidden")
    void a11_centerAdminRemovesOtherCenterStaff_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/auth/center-admin/staff/{id}", otherCenterStaffId)
                .header("Authorization", "Bearer " + centerAdminToken))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // A12 — Forgot password with unknown email → 404 Not Found
    // =========================================================================
    @Test
    @Order(12)
    @DisplayName("A12: POST /api/auth/forgot-password with unknown email → 404 Not Found")
    void a12_forgotPassword_unknownEmail_returnsNotFound() throws Exception {
        Map<String, String> body = Map.of("email", "nobody@unknown.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // A13 — Reset password with no OTP ever requested → 400 Bad Request
    // =========================================================================
    @Test
    @Order(13)
    @DisplayName("A13: POST /api/auth/reset-password, no OTP requested → 400 Bad Request")
    void a13_resetPassword_noOtpRequested_returnsBadRequest() throws Exception {
        Map<String, String> body = Map.of(
                "email", "auth.customer@test.com",
                "otp", "000000",
                "newPassword", "NewPass@1234");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("No OTP")));
    }

    // =========================================================================
    // A14 — Reset password with wrong OTP → 400 Bad Request
    // =========================================================================
    @Test
    @Order(14)
    @DisplayName("A14: POST /api/auth/reset-password, wrong OTP after valid request → 400 Bad Request")
    void a14_resetPassword_wrongOtp_returnsBadRequest() throws Exception {
        // First trigger a valid OTP (mail sender is mocked — no actual email sent)
        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("email", "auth.customer@test.com"))))
                .andExpect(status().isOk());

        // Now submit a deliberately wrong OTP
        Map<String, String> body = Map.of(
                "email", "auth.customer@test.com",
                "otp", "000000",
                "newPassword", "NewPass@1234");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid OTP")));
    }
}
