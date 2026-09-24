package de.itestra.dashboard.parking.controller;

import de.itestra.dashboard.parking.TestApplication;
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
import java.time.format.DateTimeFormatter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
@Transactional
public class ParkingControllerIntegrationTest {

    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParkingEntryRepository repository;

    @Test
    public void createEntry_ValidRequest_ReturnsCreatedEntry() throws Exception {
        LocalDate futureDate = futureWeekday(30);
        String apiDate = futureDate.format(API_DATE_FORMAT);
        String isoDate = futureDate.format(ISO_FORMAT);

        String requestJson = """
                {
                    "employeeName": "John Doe",
                    "date": "%s"
                }
                """.formatted(apiDate);

        mockMvc.perform(post("/api/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.date").value(isoDate));

        assertThat(repository.count()).isEqualTo(1);
        ParkingEntry saved = repository.findAll().get(0);
        assertThat(saved.getEmployeeName()).isEqualTo("John Doe");
        assertThat(saved.getDate()).isEqualTo(futureDate);
    }

    @Test
    public void getEntries_WithMultipleEntries_ReturnsAllEntries() throws Exception {
        createAndSaveEntry("Alice Johnson", LocalDate.now().plusDays(10));
        createAndSaveEntry("Bob Smith", LocalDate.now().plusDays(11));
        createAndSaveEntry("Charlie Brown", LocalDate.now().plusDays(12));

        mockMvc.perform(get("/api/entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].employeeName").exists())
                .andExpect(jsonPath("$[1].employeeName").exists())
                .andExpect(jsonPath("$[2].employeeName").exists());
    }

    @Test
    public void createEntry_BlankEmployeeName_ReturnsBadRequest() throws Exception {
        String apiDate = futureWeekday(30).format(API_DATE_FORMAT);
        String requestJson = """
                {
                    "employeeName": "   ",
                    "date": "%s"
                }
                """.formatted(apiDate);

        mockMvc.perform(post("/api/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createEntry_DuplicateEmployeeAndDate_VerifyPrevented() throws Exception {
        LocalDate futureDate = futureWeekday(45);
        String apiDate = futureDate.format(API_DATE_FORMAT);

        String firstRequest = """
                {
                    "employeeName": "Doppeltes Lottchen",
                    "date": "%s"
                }
                """.formatted(apiDate);

        mockMvc.perform(post("/api/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequest))
                .andExpect(status().isCreated());

        String duplicateRequest = """
                {
                    "employeeName": "Doppeltes Lottchen",
                    "date": "%s"
                }
                """.formatted(apiDate);

        mockMvc.perform(post("/api/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateRequest))
                .andExpect(status().isBadRequest());

        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    public void getEntries_EmptyDatabase_ReturnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0))
                .andExpect(content().json("[]"));
    }

    private void createAndSaveEntry(String employeeName, LocalDate date) {
        ParkingEntry entry = new ParkingEntry();
        entry.setEmployeeName(employeeName);
        entry.setDate(date);
        repository.save(entry);
    }

    /**
     * Liefert den ersten Werktag ab heute + {@code days}, da Buchungen am
     * Wochenende abgelehnt werden (sonst schlaegt der Test datumsabhaengig fehl).
     */
    private static LocalDate futureWeekday(int days) {
        LocalDate date = LocalDate.now().plusDays(days);
        while (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
