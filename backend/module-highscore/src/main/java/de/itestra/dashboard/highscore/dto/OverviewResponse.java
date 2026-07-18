package de.itestra.dashboard.highscore.dto;

import de.itestra.dashboard.highscore.dto.matchhistory.response.MatchHistoryResponse;
import de.itestra.dashboard.highscore.projection.DartsPlayerProjection;
import de.itestra.dashboard.highscore.projection.PlayerPointsProjection;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * Data Transfer Object providing a highscore overview.
 * <p>
 * Aggregates leaderboard data and recent match history for both Darts and Kicker games
 * into a single response, giving a complete view of current standings and recent activity.
 * </p>
 *
 * @param dartsTop5 top 5 darts players ranked by fewest darts to finish
 * @param kickerTop5 top 5 kicker teams ranked by total points
 * @param matchHistoryEntries recent matches from both games in chronological order
 */
@Schema(description = "Comprehensive overview of highscore data combining top performers and recent match history for both Darts and Kicker games")
public record OverviewResponse(
        @Schema(
                description = "Top 5 players in Darts ranked by their total throws in ascending order. Each entry contains the player's name and total throws.",
                example = "[{\"playerName\": \"Max\", \"totalThrows\": 15}, {\"playerName\": \"Anna\", \"totalThrows\": 18}]"
        )
        List<DartsPlayerProjection> dartsTop5,

        @Schema(
                description = "Top 5 players in Kicker (table football) ranked by their total points in descending order. Each entry contains the player's name and accumulated points.",
                example = "[{\"playerName\": \"John\", \"totalPoints\": 10}, {\"playerName\": \"Jane\", \"totalPoints\": 5}]"
        )
        List<PlayerPointsProjection> kickerTop5,

        @Schema(
                description = "Chronological list of recent match results across both game types. Provides context about recent gaming activity.",
                implementation = MatchHistoryResponse.class
        )
        List<MatchHistoryResponse> matchHistoryEntries
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
