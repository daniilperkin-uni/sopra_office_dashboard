package de.itestra.dashboard.parking.controller;

import de.itestra.dashboard.parking.entity.ParkingEntry;
import de.itestra.dashboard.parking.repository.ParkingEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = de.itestra.dashboard.BackendApplication.class)
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
@Transactional
public class ParkingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParkingEntryRepository repository;

    @Test
    public void createEntry_ValidRequest_ReturnsCreatedEntry() throws Exception {
        // Given
        String requestJson = """
                {
                    "employeeName": "John Doe",
                    "date": "25-02-2026"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.date").value("2026-02-25"));

        // Verify database persistence
        assertThat(repository.count()).isEqualTo(1);
        ParkingEntry saved = repository.findAll().get(0);
        assertThat(saved.getEmployeeName()).isEqualTo("John Doe");
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2026, 02, 25));
    }

    @Test
    public void getEntries_WithMultipleEntries_ReturnsAllEntries() throws Exception {
        // Given - create 3 entries directly in repository
        createAndSaveEntry("Alice Johnson", LocalDate.of(2026, 02, 10));
        createAndSaveEntry("Bob Smith", LocalDate.of(2026, 02, 11));
        createAndSaveEntry("Charlie Brown", LocalDate.of(2026, 02, 12));

        // When & Then
        mockMvc.perform(get("/api/entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].employeeName").exists())
                .andExpect(jsonPath("$[1].employeeName").exists())
                .andExpect(jsonPath("$[2].employeeName").exists());
    }

    @Test
    public void createEntry_BlankEmployeeName_ReturnsBadRequest() throws Exception {
        // Given
        String requestJson = """
                {
                    "employeeName": "   ",
                    "date": "25-02-2026"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createEntry_DuplicateEmployeeAndDate_VerifyPrevented() throws Exception {
        // Given - create first entry
        String firstRequest = """
                {
                    "employeeName": "Doppeltes Lottchen",
                    "date": "01-04-2026"
                }
                """;

        mockMvc.perform(post("/api/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequest))
                .andExpect(status().isCreated());

        // When - try to create duplicate
        String duplicateRequest = """
                {
                    "employeeName": "Doppeltes Lottchen",
                    "date": "01-04-2026"
                }
                """;

        // Then - verify exception is thrown (database constraint or service validation)
        try {
            mockMvc.perform(post("/api/entries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(duplicateRequest))
                    .andReturn();
        } catch (Exception e) {
            // Exception expected - verify it's about duplication
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).contains("already registered");
        }

        // Verify only one entry exists
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    public void getEntries_EmptyDatabase_ReturnsEmptyArray() throws Exception {
        // Given - database is empty (due to @Transactional)

        // When & Then
        mockMvc.perform(get("/api/entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0))
                .andExpect(content().json("[]"));
    }

    // Helper method to create and save parking entries
    private void createAndSaveEntry(String employeeName, LocalDate date) {
        ParkingEntry entry = new ParkingEntry();
        entry.setEmployeeName(employeeName);
        entry.setDate(date);
        repository.save(entry);
    }
}
