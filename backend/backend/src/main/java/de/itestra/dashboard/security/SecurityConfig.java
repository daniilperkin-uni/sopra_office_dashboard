package de.itestra.dashboard.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration.
 * <p>
 * Session-based auth with a single in-memory admin user whose credentials are
 * sourced from environment variables (admin.username / admin.password).
 * GET endpoints are public so the kiosk display works unauthenticated;
 * all mutations require the ADMIN role.
 * </p>
 * <p>
 * CSRF ist aktiv: das Token liegt im Cookie {@code XSRF-TOKEN} (fuer JS lesbar)
 * und muss als Header {@code X-XSRF-TOKEN} mitgesendet werden (Axios macht das
 * automatisch). Nicht explizit freigegebene Pfade werden abgelehnt.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Pfade ausserhalb von /api, die ohne Anmeldung erreichbar sind. */
    static final String[] PUBLIC_PATHS = {
        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
        "/actuator/health", "/error",
        "/", "/index.html", "/favicon.ico", "/assets/**",
    };

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // spa(): XSRF-TOKEN-Cookie (fuer JS lesbar) + SPA-Token-Handler aus Security 7
            .csrf(csrf -> csrf
                .spa()
                // Login ohne vorheriges Token erlauben (erster Request der SPA)
                .ignoringRequestMatchers("/api/auth/login"))
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/**").permitAll()
                .requestMatchers("/api/**").hasRole("ADMIN")
                // Oeffentliche Infrastruktur: Swagger, Health, Fehlerseite, SPA-Assets
                .requestMatchers(PUBLIC_PATHS).permitAll()
                // Alles andere standardmaessig verbieten
                .anyRequest().denyAll()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> { });
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(
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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
