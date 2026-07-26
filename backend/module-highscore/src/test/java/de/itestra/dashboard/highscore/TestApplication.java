package de.itestra.dashboard.highscore;

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
 * Test-only Spring Boot application for module-highscore integration tests.
 * <p>
 * Library modules don't have their own @SpringBootApplication class, so
 * Spring Boot's test scanner can't find one. This class provides the bootstrap
 * configuration needed by {@code @SpringBootTest} and {@code @WebMvcTest}.
 * <p>
 * Security mirrors the production SecurityConfig: GET endpoints are public,
 * mutations require ADMIN role, credentials come from test application.properties.
 */
@SpringBootApplication(scanBasePackages = "de.itestra.dashboard.highscore")
class TestApplication {

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
