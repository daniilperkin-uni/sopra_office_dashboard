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
 * testing the packaged application: GET /actuator/health was allowed by
 * SecurityConfig but no actuator dependency existed, so it hit exactly this
 * path.
 * </p>
 * <p>
 * The actuator starter is a real dependency now, so /actuator/health answers
 * 200 and this test asserts the live endpoint instead of the former 404. The
 * catch-all guard is still covered by {@link #unmappedApiPath_returns404_not500()}.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whitelistedActuatorHealth_isReachableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void unmappedApiPath_returns404_not500() throws Exception {
        mockMvc.perform(get("/api/this-endpoint-does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
