package de.office.dashboard.highscore.controller;

import de.office.dashboard.highscore.service.EloRankingService;
import de.office.dashboard.highscore.service.EloRankingService.PlayerRanking;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST-Controller für die ELO-Rangliste der Darts-Spiele.
 * <p>
 * Stellt die pro Spieler aggregierte Statistik (ELO-Wert, Siege,
 * Niederlagen, aktuelle und beste Siegesserie) bereit. Die Berechnung
 * erfolgt on-demand aus dem gespeicherten Spielverlauf - es gibt keine
 * eigene Tabelle und keinen Schema-Change.
 * </p>
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Elo Ranking", description = "ELO-Rangliste fuer Darts")
public class EloRankingController {

    private final EloRankingService eloRankingService;

    /**
     * Konstruktor für die Abhängigkeitsinjektion.
     *
     * @param eloRankingService reine Berechnungsklasse für die Rangliste
     */
    public EloRankingController(EloRankingService eloRankingService) {
        this.eloRankingService = eloRankingService;
    }

    /**
     * Liefert die vollständige ELO-Rangliste, sortiert nach ELO absteigend.
     *
     * @return ein Eintrag pro Spieler im gespeicherten Spielverlauf
     */
    @GetMapping("/elo-rankings")
    @Operation(summary = "Retrieve the darts ELO rankings",
            description = "Returns per-player ELO rating, wins, losses and streaks, sorted by ELO descending")
    @ApiResponse(responseCode = "200", description = "ELO-Rangliste",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PlayerRanking.class))))
    public List<PlayerRanking> getEloRankings() {
        return eloRankingService.getRankings();
    }
}
