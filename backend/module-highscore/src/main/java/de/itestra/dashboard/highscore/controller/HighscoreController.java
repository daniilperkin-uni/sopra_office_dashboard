package de.itestra.dashboard.highscore.controller;

import de.itestra.dashboard.highscore.dto.OverviewResponse;
import de.itestra.dashboard.highscore.dto.darts.request.DartsEntryRequest;
import de.itestra.dashboard.highscore.dto.darts.response.DartsEntryResponse;
import de.itestra.dashboard.highscore.dto.kicker.request.KickerEntryRequest;
import de.itestra.dashboard.highscore.dto.kicker.response.KickerEntryResponse;
import de.itestra.dashboard.highscore.entity.DartsEntry;
import de.itestra.dashboard.highscore.entity.KickerEntry;
import de.itestra.dashboard.highscore.service.HighscoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing highscores and match statistics for Darts and Kicker games.
 * <p>
 * Provides endpoints to:
 * <ul>
 *   <li>Retrieve leaderboards and match history overview</li>
 *   <li>Create, read, update, and delete Darts match results</li>
 *   <li>Create, read, update, and delete Kicker match results</li>
 * </ul>
 * The highscore system tracks player performance and maintains rankings for both games.
 * </p>
 */
@Validated
@RestController
@RequestMapping("/api")
@Tag(name = "Highscore", description = "Darts and Kicker statistic module")
public class HighscoreController {
    private final HighscoreService highscoreService;

    /**
     * Constructs a new HighscoreController.
     *
     * @param highscoreService service for managing highscores and match data
     */
    public HighscoreController(HighscoreService highscoreService) {
        this.highscoreService = highscoreService;
    }

    @GetMapping("/highscore-overview")
    @Operation(summary = "Retrieve leaderboard", description = "Returns the leaderBoard")
    @ApiResponse(responseCode = "200", description = "Darts leaderboard, Kicker leaderboard and Match history",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OverviewResponse.class)))
    public OverviewResponse getOverview() {
        return highscoreService.getOverview();
    }

    @GetMapping("/matches/darts")
    @Operation(summary = "Retrieve all darts matches", description = "Returns all darts matches")
    @ApiResponse(responseCode = "200", description = "Darts leaderboard, Kicker leaderboard and Match history",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = DartsEntry.class))))
    public List<DartsEntry> getAllDartsMatches() {
        return highscoreService.getAllDartsMatches();
    }

    @GetMapping("/matches/kicker")
    @Operation(summary = "Retrieve all kicker matches", description = "Returns all kicker matches")
    @ApiResponse(responseCode = "200", description = "List of all kicker matches",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = KickerEntry.class))))
    public List<KickerEntry> getAllKickerMatches() {
        return highscoreService.getAllKickerMatches();
    }

    @PostMapping("/matches/darts")
    @Operation(summary = "Create a new darts match", description = "Records a new darts match result and updates the highscore standings accordingly")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Match successfully created",
                    content = @Content(schema = @Schema(implementation = DartsEntryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            )
    })
    public DartsEntryResponse createDartsEntry(
            @Valid @RequestBody DartsEntryRequest request
    ) {
        return highscoreService.createDartsEntry(request);
    }

    @PutMapping("/matches/darts/{id}")
    @Operation(summary = "Update a darts match", description = "Updates a new darts match result and updates the highscore standings accordingly")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Match successfully updated",
                    content = @Content(schema = @Schema(implementation = DartsEntryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            )
    })
    public DartsEntryResponse updateDartsEntry(
            @Parameter(description = "Unique identifier of the darts match to update", example = "1", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody DartsEntryRequest request
    ) {
        return highscoreService.updateDartsEntry(id, request);
    }

    @DeleteMapping("/matches/darts/{id}")
    @Operation(summary = "Delete a specific darts match", description = "Deletes a darts match by its ID")
    public void deleteDartsMatch(
            @Parameter(description = "Unique identifier of the darts match to delete", example = "1", required = true)
            @PathVariable Long id) {
        highscoreService.deleteDartsEntry(id);
    }

    @PostMapping("/matches/kicker")
    @Operation(summary = "Create a new kicker match", description = "Records a new kicker match result and updates the highscore standings accordingly")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Match successfully created",
                    content = @Content(schema = @Schema(implementation = KickerEntryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            )
    })
    public KickerEntryResponse createKickerEntry(
            @Valid @RequestBody KickerEntryRequest request
    ) {
        return highscoreService.createKickerEntry(request);
    }

    @PutMapping("/matches/kicker/{id}")
    @Operation(summary = "Update a kicker match", description = "Updates a kicker match result and updates the highscore standings accordingly")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Match successfully updated",
                    content = @Content(schema = @Schema(implementation = KickerEntryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            )
    })
    public KickerEntryResponse updateKickerEntry(
            @Parameter(description = "Unique identifier of the kicker match to update", example = "1", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody KickerEntryRequest request
    ) {
        return highscoreService.updateKickerEntry(id, request);
    }

    @DeleteMapping("/matches/kicker/{id}")
    @Operation(summary = "Delete a specific kicker matches", description = "Deletes a kicker match by its ID")
    public void deleteKickerMatch(
            @Parameter(description = "Unique identifier of the kicker match to delete", example = "1", required = true)
            @PathVariable Long id) {
        highscoreService.deleteKickerEntry(id);
    }
}
