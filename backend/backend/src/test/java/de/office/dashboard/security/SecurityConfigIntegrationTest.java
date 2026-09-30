package de.office.dashboard.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Prueft CSRF-Schutz und die Deny-by-default-Autorisierung.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequest_setsReadableXsrfCookie() throws Exception {
        mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));
    }

    @Test
    void postWithoutCsrfToken_isForbidden() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postWithCsrfToken_isAccepted() throws Exception {
        mockMvc.perform(post("/api/auth/logout").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void csrfEndpoint_returnsToken() throws Exception {
        mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"));
    }

    @Test
    void loginWithoutCsrfToken_isForbidden() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"x\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginWithCsrfToken_succeeds() throws Exception {
        // Echter SPA-Ablauf: Token holen, dann Cookie + Header mitsenden
        MvcResult primed = mockMvc.perform(get("/api/auth/csrf")).andReturn();
        Cookie xsrf = primed.getResponse().getCookie("XSRF-TOKEN");
        mockMvc.perform(post("/api/auth/login")
                        .cookie(xsrf)
                        .header("X-XSRF-TOKEN", xsrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"REDACTED-SECRET\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void loginWithWrongPassword_isUnauthorized() throws Exception {
        // Real cookie flow, not csrf(): that post-processor swaps the shared
        // CsrfFilter repository and breaks the XSRF-cookie tests run after it
        MvcResult primed = mockMvc.perform(get("/api/auth/csrf")).andReturn();
        Cookie xsrf = primed.getResponse().getCookie("XSRF-TOKEN");
        mockMvc.perform(post("/api/auth/login")
                        .cookie(xsrf)
                        .header("X-XSRF-TOKEN", xsrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid username or password"));
    }

    @Test
    void apiMutation_requiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/entries").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownPath_isDeniedByDefault() throws Exception {
        mockMvc.perform(get("/internal/secret").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void swaggerDocs_arePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}
