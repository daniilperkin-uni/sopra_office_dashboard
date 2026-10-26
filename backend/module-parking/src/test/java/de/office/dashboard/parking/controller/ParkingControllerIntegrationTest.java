package de.office.dashboard.parking.controller;

import de.office.dashboard.parking.TestApplication;
import de.office.dashboard.parking.entity.ParkingEntry;
import de.office.dashboard.parking.entity.RecurringReservation;
import de.office.dashboard.parking.repository.ParkingEntryRepository;
import de.office.dashboard.parking.repository.RecurringReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Autowired
    private RecurringReservationRepository recurringReservationRepository;

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

    @Test
    public void updateRecurringReservation_PreviewWithConflictingDate_ReturnsPerDateStatus() throws Exception {
        LocalDate conflictDate = futureWeekday(7);
        LocalDate freeDate = nextWeekday(conflictDate);
        RecurringReservation reservation = createAndSaveRecurringReservation("Vorschau Konflikt", conflictDate);
        createAndSaveEntry("Vorschau Konflikt", conflictDate);

        String requestJson = recurringRequestJson("Vorschau Konflikt", conflictDate, freeDate,
                Set.of(conflictDate.getDayOfWeek(), freeDate.getDayOfWeek()));

        // Vorschau: kein 400, sondern die Statusmap pro Datum - die UI markiert
        // damit den kollidierenden Tag, statt eine Fehlermeldung zu zeigen.
        mockMvc.perform(put("/api/recurring/{id}", reservation.getId())
                        .param("preview", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservation.getId()))
                .andExpect(jsonPath("$.dates['" + conflictDate.format(ISO_FORMAT) + "']").value("RESERVED_BEFORE"))
                .andExpect(jsonPath("$.dates['" + freeDate.format(ISO_FORMAT) + "']").value("OK"));

        // Eine Vorschau darf nichts schreiben: nur der Konflikt-Eintrag existiert.
        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.existsByEmployeeNameAndDate("Vorschau Konflikt", freeDate)).isFalse();
    }

    @Test
    public void updateRecurringReservation_ApplyWithConflictingDate_ReturnsBadRequest() throws Exception {
        LocalDate conflictDate = futureWeekday(7);
        RecurringReservation reservation = createAndSaveRecurringReservation("Anwenden Konflikt", conflictDate);
        createAndSaveEntry("Anwenden Konflikt", conflictDate);

        String requestJson = recurringRequestJson("Anwenden Konflikt", conflictDate, conflictDate,
                Set.of(conflictDate.getDayOfWeek()));

        // Beim echten Speichern bleibt die Alles-oder-nichts-Regel bestehen.
        mockMvc.perform(put("/api/recurring/{id}", reservation.getId())
                        .param("preview", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        assertThat(repository.count()).isEqualTo(1);
    }

    private void createAndSaveEntry(String employeeName, LocalDate date) {
        ParkingEntry entry = new ParkingEntry();
        entry.setEmployeeName(employeeName);
        entry.setDate(date);
        repository.save(entry);
    }

    /**
     * Legt eine bestehende Serienreservierung an, die per PUT aktualisiert wird.
     * Der Zeitraum deckt bewusst die Testdaten ab, damit die Validierung im
     * Service (endDate nicht in der Vergangenheit) greift.
     */
    private RecurringReservation createAndSaveRecurringReservation(String employeeName, LocalDate startDate) {
        RecurringReservation reservation = new RecurringReservation();
        reservation.setEmployeeName(employeeName);
        reservation.setStartDate(startDate);
        reservation.setEndDate(startDate.plusDays(28));
        reservation.setDaysOfWeek(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        return recurringReservationRepository.save(reservation);
    }

    /**
     * Baut den Request-Body fuer eine Serienreservierung. {@code daysOfWeek} wird
     * explizit gesetzt, damit der Service genau die gewuenschten Tage prueft.
     */
    private static String recurringRequestJson(String employeeName, LocalDate startDate, LocalDate endDate,
            Set<DayOfWeek> daysOfWeek) {
        String days = daysOfWeek.stream()
                .map(day -> "\"" + day.name() + "\"")
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElseThrow();
        return """
                {
                    "employeeName": "%s",
                    "startDate": "%s",
                    "endDate": "%s",
                    "daysOfWeek": [%s]
                }
                """.formatted(employeeName, startDate.format(API_DATE_FORMAT), endDate.format(API_DATE_FORMAT), days);
    }

    /**
     * Liefert den naechsten Werktag nach {@code date} (Ueberspringt Wochenenden).
     */
    private static LocalDate nextWeekday(LocalDate date) {
        LocalDate next = date.plusDays(1);
        while (next.getDayOfWeek() == DayOfWeek.SATURDAY || next.getDayOfWeek() == DayOfWeek.SUNDAY) {
            next = next.plusDays(1);
        }
        return next;
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
