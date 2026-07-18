package de.itestra.dashboard.highscore.mapper;

import de.itestra.dashboard.common.constants.DateTimeFormatterConstants;
import de.itestra.dashboard.highscore.dto.matchhistory.response.MatchHistoryResponse;
import de.itestra.dashboard.highscore.entity.DartsEntry;
import de.itestra.dashboard.highscore.entity.KickerEntry;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for converting game entries to unified {@link MatchHistoryResponse} DTOs.
 * <p>
 * Provides overloaded methods to transform both {@link DartsEntry} and {@link KickerEntry}
 * entities into a common match history format for display purposes.
 * </p>
 */
@Component
public class MatchHistoryEntryMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatterConstants.DATE_TIME_FORMATTER;

    /**
     * Converts a darts entry to a unified match history response.
     * <p>
     * Sets game type to "Darts" and formats player name and dart count.
     * </p>
     *
     * @param match the darts entry to convert
     * @return unified match history response
     */
    public MatchHistoryResponse toResponse(DartsEntry match) {
        String dateText = "-";
        if (match.getCreatedAt() != null) {
            dateText = match.getCreatedAt().format(FORMATTER);
        }
        
        return new MatchHistoryResponse(
                "Darts",
                match.getPlayerName() != null ? match.getPlayerName() : "Unknown",
                match.getDartsToFinish(),
                "-",
                match.getCreatedAt(),
                dateText
        );
    }

    /**
     * Converts a kicker entry to a unified match history response.
     * <p>
     * Sets game type to "Kicker", formats team compositions as "Team A vs Team B",
     * and includes the match outcome.
     * </p>
     *
     * @param match the kicker entry to convert
     * @return unified match history response
     */
    public MatchHistoryResponse toResponse(KickerEntry match) {
        List<String> teamA = match.getTeamAPlayerNames() != null ? match.getTeamAPlayerNames() : new ArrayList<>();
        List<String> teamB = match.getTeamBPlayerNames() != null ? match.getTeamBPlayerNames() : new ArrayList<>();
        
        String teamAText = String.join(" & ", teamA);
        String teamBText = String.join(" & ", teamB);

        String players = teamAText + " vs " + teamBText;
        
        String dateText = "-";
        if (match.getCreatedAt() != null) {
            dateText = match.getCreatedAt().format(FORMATTER);
        }

        String matchResultText = "Draw";
        if (match.getMatchResult() != null) {
             matchResultText = switch (match.getMatchResult()) {
                case TEAM_A_WIN -> teamAText + " won";
                case TEAM_B_WIN -> teamBText + " won";
            };
        }

        return new MatchHistoryResponse(
                "Kicker",
                players,
                null,
                matchResultText,
                match.getCreatedAt(),
                dateText
        );
    }
}
