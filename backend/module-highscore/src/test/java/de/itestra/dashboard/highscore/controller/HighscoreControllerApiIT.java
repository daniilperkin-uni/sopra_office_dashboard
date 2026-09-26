package de.itestra.dashboard.highscore.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import de.itestra.dashboard.highscore.MatchResult;
import de.itestra.dashboard.highscore.dto.OverviewResponse;
import de.itestra.dashboard.highscore.dto.darts.request.DartsEntryRequest;
import de.itestra.dashboard.highscore.dto.darts.response.DartsEntryResponse;
import de.itestra.dashboard.highscore.dto.kicker.request.KickerEntryRequest;
import de.itestra.dashboard.highscore.dto.kicker.response.KickerEntryResponse;
import de.itestra.dashboard.highscore.service.HighscoreService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class HighscoreControllerApiIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    HighscoreService highscoreService;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private TestRestTemplate adminRest() {
        return rest.withBasicAuth("admin", "REDACTED-SECRET");
    }

    @Test
    void api_getOverview_returns200_andJsonShape() throws Exception {
        OverviewResponse response = new OverviewResponse(List.of(), List.of(), List.of());
        Mockito.when(highscoreService.getOverview()).thenReturn(response);

        ResponseEntity<String> res = rest.getForEntity(url("/api/highscore-overview"), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getHeaders().getContentType()).isNotNull();
        assertThat(res.getHeaders().getContentType().toString()).contains("application/json");

        JsonNode json = objectMapper.readTree(res.getBody());
        assertThat(json.has("dartsTop5")).isTrue();
        assertThat(json.has("kickerTop5")).isTrue();
        assertThat(json.has("matchHistoryEntries")).isTrue();

        Mockito.verify(highscoreService).getOverview();
        Mockito.verifyNoMoreInteractions(highscoreService);
    }

    @Test
    void api_postDarts_valid_returns201_andBody() throws Exception {
        DartsEntryRequest req = new DartsEntryRequest("Max Mustermann", 18);

        DartsEntryResponse resp = new DartsEntryResponse(1L, "Max Mustermann", 18, "12:30 24.12.2025");
        Mockito.when(highscoreService.createDartsEntry(any(DartsEntryRequest.class))).thenReturn(resp);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(req), headers);

        ResponseEntity<String> res = adminRest().exchange(url("/api/matches/darts"), HttpMethod.POST, entity, String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode json = objectMapper.readTree(res.getBody());
        assertThat(json.get("id").asLong()).isEqualTo(1L);
        assertThat(json.get("player").asText()).isEqualTo("Max Mustermann");
        assertThat(json.get("points").asInt()).isEqualTo(18);
        assertThat(json.get("date").asText()).isEqualTo("12:30 24.12.2025");

        Mockito.verify(highscoreService).createDartsEntry(any(DartsEntryRequest.class));
        Mockito.verifyNoMoreInteractions(highscoreService);
    }

    @Test
    void api_postDarts_invalid_returns400_andDoesNotCallService() {
        String invalidJson = """
                {
                  "playerName": "   ",
                  "dartsToFinish": 18
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> res = adminRest().exchange(
                url("/api/matches/darts"),
                HttpMethod.POST,
                new HttpEntity<>(invalidJson, headers),
                String.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Mockito.verifyNoInteractions(highscoreService);
    }

    @Test
    void api_postKicker_valid_returns201_andBody() throws Exception {
        KickerEntryRequest req = new KickerEntryRequest(
                List.of("Max Mustermann", "Dora Musterfrau"),
                List.of("Ida Oberstein", "Hans Dampf"),
                MatchResult.TEAM_A_WIN
        );

        KickerEntryResponse resp = new KickerEntryResponse(
                2L,
                req.teamAPlayers(),
                req.teamBPlayers(),
                "Team A won",
                "12:30 24.12.2025"
        );

        Mockito.when(highscoreService.createKickerEntry(any(KickerEntryRequest.class))).thenReturn(resp);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(req), headers);

        ResponseEntity<String> res = adminRest().exchange(url("/api/matches/kicker"), HttpMethod.POST, entity, String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode json = objectMapper.readTree(res.getBody());

        assertThat(json.get("id").asLong()).isEqualTo(2L);

        assertThat(json.get("teamAPlayers").isArray()).isTrue();
        assertThat(json.get("teamAPlayers").get(0).asText()).isEqualTo("Max Mustermann");
        assertThat(json.get("teamAPlayers").get(1).asText()).isEqualTo("Dora Musterfrau");

        assertThat(json.get("teamBPlayers").isArray()).isTrue();
        assertThat(json.get("teamBPlayers").get(0).asText()).isEqualTo("Ida Oberstein");
        assertThat(json.get("teamBPlayers").get(1).asText()).isEqualTo("Hans Dampf");

        assertThat(json.get("matchResult").asText()).isEqualTo("Team A won");
        assertThat(json.get("dateText").asText()).isEqualTo("12:30 24.12.2025");

        Mockito.verify(highscoreService).createKickerEntry(any(KickerEntryRequest.class));
        Mockito.verifyNoMoreInteractions(highscoreService);
    }
}
