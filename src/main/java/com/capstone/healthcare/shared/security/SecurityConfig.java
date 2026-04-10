package com.capstone.healthcare.shared.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtFilter jwtFilter;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints
                                                .requestMatchers(
                                                                "/",
                                                                "/index.html",
                                                                "/api/auth/signup",
                                                                "/api/auth/login",
                                                                "/api/auth/token/refresh",
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs",
                                                                "/v3/api-docs/**")
                                                .permitAll()
                                                // Read-only test/center lookup available to authenticated users
                                                .requestMatchers(HttpMethod.GET, "/api/tests/**").authenticated()
                                                .requestMatchers(HttpMethod.GET, "/api/centers/**").authenticated()
                                                // Admin-only endpoints
                                                // Appointment listing/approval: CENTER_ADMIN also allowed (guarded by
                                                // @PreAuthorize)
                                                .requestMatchers("/api/admin/appointments/**")
                                                .hasAnyRole(RoleConstants.ADMIN, RoleConstants.CENTER_ADMIN)
                                                .requestMatchers("/api/admin/**").hasRole(RoleConstants.ADMIN)
                                                .requestMatchers(HttpMethod.POST, "/api/tests/**")
                                                .hasRole(RoleConstants.ADMIN)
                                                .requestMatchers(HttpMethod.PUT, "/api/tests/**")
                                                .hasRole(RoleConstants.ADMIN)
                                                .requestMatchers(HttpMethod.DELETE, "/api/tests/**")
                                                .hasRole(RoleConstants.ADMIN)
                                                // Test-association endpoints on centers are guarded by @PreAuthorize
                                                // (CENTER_ADMIN allowed)
                                                .requestMatchers(HttpMethod.POST, "/api/centers/*/tests/*")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.DELETE, "/api/centers/*/tests/*")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.POST, "/api/centers/**")
                                                .hasRole(RoleConstants.ADMIN)
                                                .requestMatchers(HttpMethod.PUT, "/api/centers/**")
                                                .hasRole(RoleConstants.ADMIN)
                                                .requestMatchers(HttpMethod.DELETE, "/api/centers/**")
                                                .hasRole(RoleConstants.ADMIN)
                                                // Everything else requires authentication
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
