package de.itestra.dashboard.highscore.mapper;

import de.itestra.dashboard.common.constants.DateTimeFormatterConstants;
import de.itestra.dashboard.highscore.dto.kicker.response.KickerEntryResponse;
import de.itestra.dashboard.highscore.entity.KickerEntry;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;

/**
 * Mapper for converting {@link KickerEntry} entities to {@link KickerEntryResponse} DTOs.
 * <p>
 * Handles data transformation including match result translation and date formatting.
 * </p>
 */
@Component
public class KickerEntryMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatterConstants.DATE_TIME_FORMATTER;

    /**
     * Converts a kicker entry entity to a response DTO.
     * <p>
     * Translates the match result enum to the text ("Team A won" / "Team B won")
     * and formats the creation timestamp.
     * </p>
     *
     * @param match the kicker entry entity to convert
     * @return the kicker entry response DTO
     */
    public KickerEntryResponse toResponse(KickerEntry match) {
        String dateText = match.getCreatedAt().format(FORMATTER);
        String matchResultText = switch (match.getMatchResult()) {
            case TEAM_A_WIN -> "Team A won";
            case TEAM_B_WIN -> "Team B won";
        };

        return new KickerEntryResponse(
                match.getId(),
                match.getTeamAPlayerNames(),
                match.getTeamBPlayerNames(),
                matchResultText,
                dateText
        );
    }
}
