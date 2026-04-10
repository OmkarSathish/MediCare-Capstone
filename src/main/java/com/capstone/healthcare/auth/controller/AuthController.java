package com.capstone.healthcare.auth.controller;

import com.capstone.healthcare.auth.dto.*;
import com.capstone.healthcare.auth.model.UserAccount;
import com.capstone.healthcare.auth.service.IAdminService;
import com.capstone.healthcare.auth.service.IAuthTokenService;
import com.capstone.healthcare.auth.service.IUserService;
import com.capstone.healthcare.auth.service.impl.AuthTokenServiceImpl;
import com.capstone.healthcare.shared.response.ApiResponse;
import com.capstone.healthcare.shared.security.RoleConstants;
import com.capstone.healthcare.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Signup, login, token refresh, and admin registration")
public class AuthController {

        private final IUserService userService;
        private final IAdminService adminService;
        private final IAuthTokenService authTokenService;
        private final AuthTokenServiceImpl authTokenServiceImpl;

        @Value("${security.jwt.expiration-ms}")
        private long expirationMs;

        // ── POST /api/auth/signup ────────────────────────────────────────────────
        @PostMapping("/signup")
        @Operation(summary = "Register a new customer account")
        public ResponseEntity<ApiResponse<UserProfileResponse>> signup(
                        @Valid @RequestBody SignupRequest request) {

                UserAccount user = UserAccount.builder()
                                .fullName(request.getFullName())
                                .email(request.getEmail())
                                .phone(request.getPhone())
                                .passwordHash(request.getPassword()) // service will hash it
                                .build();

                UserAccount saved = userService.addUser(user);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.ok("Account created", toProfile(saved)));
        }

        // ── POST /api/auth/login ─────────────────────────────────────────────────
        @PostMapping("/login")
        @Operation(summary = "Authenticate and receive JWT tokens")
        public ResponseEntity<ApiResponse<AuthResponse>> login(
                        @Valid @RequestBody LoginRequest request) {

                UserAccount user = userService.validateUser(request.getUsername(), request.getPassword());
                String accessToken = authTokenService.generateToken(user);
                String refreshToken = authTokenServiceImpl.createRefreshToken(user.getUserId()).getTokenValue();

                AuthResponse body = AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .expiresIn(expirationMs / 1000)
                                .build();

                return ResponseEntity.ok(ApiResponse.ok(body));
        }

        // ── POST /api/auth/token/refresh ─────────────────────────────────────────
        @PostMapping("/token/refresh")
        @Operation(summary = "Exchange a refresh token for a new access token")
        public ResponseEntity<ApiResponse<AuthResponse>> refresh(
                        @Valid @RequestBody TokenRefreshRequest request) {

                String newAccessToken = authTokenService.refreshToken(request.getRefreshToken());
                AuthResponse body = AuthResponse.builder()
                                .accessToken(newAccessToken)
                                .tokenType("Bearer")
                                .expiresIn(expirationMs / 1000)
                                .build();

                return ResponseEntity.ok(ApiResponse.ok(body));
        }

        // ── POST /api/auth/logout ────────────────────────────────────────────────
        @PostMapping("/logout")
        @Operation(summary = "Revoke the provided refresh token")
        public ResponseEntity<ApiResponse<Void>> logout(
                        @Valid @RequestBody TokenRefreshRequest request) {

                authTokenService.revokeToken(request.getRefreshToken());
                return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
        }

        // ── GET /api/auth/me ─────────────────────────────────────────────────────
        @GetMapping("/me")
        @Operation(summary = "Get the currently authenticated user's profile")
        public ResponseEntity<ApiResponse<UserProfileResponse>> me(
                        @AuthenticationPrincipal UserPrincipal principal) {

                UserAccount user = userService.findByEmail(principal.getUsername());
                return ResponseEntity.ok(ApiResponse.ok(toProfile(user)));
        }

        // ── POST /api/auth/admin/register ────────────────────────────────────────
        @PostMapping("/admin/register")
        @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
        @Operation(summary = "Register a new admin account (primary admin only)")
        public ResponseEntity<ApiResponse<Void>> registerAdmin(
                        @Valid @RequestBody SignupRequest request) {

                adminService.registerAdmin(request.getEmail(), request.getPassword());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.ok("Admin account created", null));
        }

        // ── POST /api/auth/admin/center-admins ───────────────────────────────────
        @PostMapping("/admin/center-admins")
        @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
        @Operation(summary = "Create a center admin account and assign to a diagnostic center (primary admin only)")
        public ResponseEntity<ApiResponse<Void>> registerCenterAdmin(
                        @Valid @RequestBody CreateCenterAdminRequest request) {

                adminService.registerCenterAdmin(
                                request.getEmail(), request.getPassword(), request.getFullName(),
                                request.getCenterId());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.ok("Center admin account created", null));
        }

        // ── GET /api/auth/admin/center-admins ────────────────────────────────────
        @GetMapping("/admin/center-admins")
        @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
        @Operation(summary = "List all center admin accounts (primary admin only)")
        public ResponseEntity<ApiResponse<List<UserProfileResponse>>> listCenterAdmins() {
                List<UserProfileResponse> list = adminService.listCenterAdmins()
                                .stream().map(this::toProfile).toList();
                return ResponseEntity.ok(ApiResponse.ok(list));
        }

        // ── DELETE /api/auth/admin/center-admins/{id} ────────────────────────────
        @DeleteMapping("/admin/center-admins/{id}")
        @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
        @Operation(summary = "Remove a center admin account (revokes role and center assignment)")
        public ResponseEntity<ApiResponse<Void>> removeCenterAdmin(@PathVariable int id) {
                adminService.removeCenterAdmin(id);
                return ResponseEntity.ok(ApiResponse.ok("Center admin removed", null));
        }

        // ── helpers ──────────────────────────────────────────────────────────────
        private UserProfileResponse toProfile(UserAccount user) {
                return UserProfileResponse.builder()
                                .userId(user.getUserId())
                                .fullName(user.getFullName())
                                .email(user.getEmail())
                                .phone(user.getPhone())
                                .centerId(user.getCenterId())
                                .roles(user.getRoles().stream()
                                                .map(r -> r.getRoleName())
                                                .collect(Collectors.toSet()))
                                .build();
        }
}
