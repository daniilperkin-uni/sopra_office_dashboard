package de.itestra.dashboard.parking;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

/**
 * Test-only Spring Boot application for module-parking integration tests.
 * <p>
 * Library modules don't have their own @SpringBootApplication class, so
 * Spring Boot's test scanner can't find one. This class provides the bootstrap
 * configuration needed by {@code @SpringBootTest}, including a security
 * configuration that mirrors the production SecurityConfig: GET endpoints are
 * public, mutations require ADMIN role.
 * </p>
 * <p>
 * The shared {@code GlobalExceptionHandler} is scanned as well: the production
 * application scans the whole {@code de.itestra.dashboard} root and therefore
 * gets the RFC-7807 advice, while ParkingController no longer has a local
 * IllegalArgumentException handler. Without the exception package a violated
 * business rule (duplicate booking) surfaced as an unhandled exception instead
 * of the documented 400.
 * </p>
 */
@SpringBootApplication(scanBasePackages = {"de.itestra.dashboard.parking", "de.itestra.dashboard.exception"})
public class TestApplication {

    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/**").permitAll()
                    .requestMatchers("/api/**").hasRole("ADMIN")
                    .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> { });
            return http.build();
        }

        @Bean
        UserDetailsService userDetailsService(
                @Value("${admin.username:admin}") String username,
                @Value("${admin.password}") String password,
                PasswordEncoder passwordEncoder) {
            UserDetails admin = User.withUsername(username)
                    .password(passwordEncoder.encode(password))
                    .roles("ADMIN")
                    .build();
            return new InMemoryUserDetailsManager(admin);
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
}
