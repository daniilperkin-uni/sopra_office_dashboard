package de.office.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests fuer {@link CorsConfig}.
 * <p>
 * Beide CORS-Registrierungen (Spring Security ueber die
 * {@link CorsConfigurationSource}-Bean und MVC ueber
 * {@link CorsConfig#addCorsMappings(CorsRegistry)}) muessen exakt dieselbe
 * Origin-Liste sehen. Der Test verwendet bewusst eine Konfiguration mit
 * Leerzeichen nach den Kommas, weil genau dieser Fall vorher zu
 * unterschiedlichen Listen gefuehrt hat.
 * </p>
 */
public class CorsConfigTest {

    private static final String ORIGINS_WITH_SPACES =
            "http://localhost:3000, http://localhost:5173 ,http://localhost:8098,";

    private static final List<String> EXPECTED_ORIGINS = List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:8098");

    @Test
    public void securityAndMvcRegistrationUseTheSameTrimmedOrigins() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", ORIGINS_WITH_SPACES);

        CorsConfiguration securityConfig = corsConfig.corsConfigurationSource()
                .getCorsConfiguration(new MockHttpServletRequest("GET", "/api/entries"));

        CorsRegistry registry = new CorsRegistry();
        corsConfig.addCorsMappings(registry);
        Map<?, ?> mvcMappings = (Map<?, ?>) ReflectionTestUtils.invokeMethod(registry, "getCorsConfigurations");
        CorsConfiguration mvcConfig = (CorsConfiguration) mvcMappings.get("/**");

        assertThat(securityConfig).isNotNull();
        assertThat(mvcConfig).isNotNull();
        assertThat(securityConfig.getAllowedOrigins()).containsExactlyElementsOf(EXPECTED_ORIGINS);
        assertThat(mvcConfig.getAllowedOrigins()).containsExactlyElementsOf(EXPECTED_ORIGINS);
        assertThat(securityConfig.getAllowedOrigins()).isEqualTo(mvcConfig.getAllowedOrigins());
    }

    @Test
    public void originWithSurroundingWhitespaceIsAccepted() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", ORIGINS_WITH_SPACES);

        CorsConfiguration config = corsConfig.corsConfigurationSource()
                .getCorsConfiguration(new MockHttpServletRequest("GET", "/api/entries"));

        assertThat(config.checkOrigin("http://localhost:5173")).isEqualTo("http://localhost:5173");
        assertThat(config.checkOrigin("http://evil.example")).isNull();
    }
}
