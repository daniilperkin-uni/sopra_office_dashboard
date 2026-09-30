package de.office.dashboard.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Regression guard for the catch-all handler in {@link GlobalExceptionHandler}.
 * <p>
 * An unmapped public URL was answered with 500 "Internal server error" plus a
 * logged stack trace, because the {@code Exception} handler is resolved before
 * Spring's own 404 handling of a missing static resource. Found while smoke
 * testing the packaged application: GET /actuator/health is allowed by
 * SecurityConfig but no actuator dependency exists, so it hit exactly this
 * path.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whitelistedButMissingActuatorPath_returns404_not500() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void unmappedApiPath_returns404_not500() throws Exception {
        mockMvc.perform(get("/api/this-endpoint-does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
