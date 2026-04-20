package com.capstone.healthcare.auth.service.impl;

import com.capstone.healthcare.auth.model.RefreshToken;
import com.capstone.healthcare.auth.model.UserAccount;
import com.capstone.healthcare.auth.repository.IRefreshTokenRepository;
import com.capstone.healthcare.auth.repository.IUserRepository;
import com.capstone.healthcare.auth.service.IAuthTokenService;
import com.capstone.healthcare.shared.exception.ResourceNotFoundException;
import com.capstone.healthcare.shared.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthTokenServiceImpl implements IAuthTokenService {

        private final SecretKey signingKey;
        private final long expirationMs;
        private final long refreshExpirationMs;
        private final IUserRepository userRepository;
        private final IRefreshTokenRepository refreshTokenRepository;

        public AuthTokenServiceImpl(
                        @Value("${security.jwt.secret}") String secret,
                        @Value("${security.jwt.expiration-ms}") long expirationMs,
                        @Value("${security.jwt.refresh-expiration-ms}") long refreshExpirationMs,
                        IUserRepository userRepository,
                        IRefreshTokenRepository refreshTokenRepository) {
                this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                this.expirationMs = expirationMs;
                this.refreshExpirationMs = refreshExpirationMs;
                this.userRepository = userRepository;
                this.refreshTokenRepository = refreshTokenRepository;
        }

        @Override
        @Transactional
        public String generateToken(UserAccount user) {
                List<String> roles = user.getRoles().stream()
                                .map(r -> "ROLE_" + r.getRoleName())
                                .collect(Collectors.toList());
                return Jwts.builder()
                                .subject(user.getEmail())
                                .claim("userId", user.getUserId())
                                .claim("roles", roles)
                                .issuedAt(new Date())
                                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                                .signWith(signingKey)
                                .compact();
        }

        @Override

        public UserPrincipal validateToken(String token) {
                Claims claims = Jwts.parser()
                                .verifyWith(signingKey)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();
                String email = claims.getSubject();
                UserAccount user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("UserAccount", "email", email));
                return UserPrincipal.from(user);
        }

        @Override
        @Transactional
        public String refreshToken(String refreshTokenValue) {
                RefreshToken stored = refreshTokenRepository.findByTokenValue(refreshTokenValue)
                                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));
                if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
                        throw new IllegalStateException("Refresh token is expired or revoked");
                }
                UserAccount user = userRepository.findById(stored.getUserId())
                                .orElseThrow(() -> new ResourceNotFoundException("UserAccount", "id",
                                                stored.getUserId()));
                return generateToken(user);
        }

        @Override
        @Transactional
        public void revokeToken(String refreshTokenValue) {
                RefreshToken stored = refreshTokenRepository.findByTokenValue(refreshTokenValue)
                                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));
                stored.setRevoked(true);
                refreshTokenRepository.save(stored);
        }

        @Transactional
        public RefreshToken createRefreshToken(int userId) {
                RefreshToken token = RefreshToken.builder()
                                .userId(userId)
                                .tokenValue(UUID.randomUUID().toString())
                                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                                .revoked(false)
                                .build();
                return refreshTokenRepository.save(token);
        }
}
