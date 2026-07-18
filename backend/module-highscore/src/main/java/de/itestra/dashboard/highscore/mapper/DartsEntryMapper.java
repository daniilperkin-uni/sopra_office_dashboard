package de.itestra.dashboard.highscore.mapper;

import de.itestra.dashboard.common.constants.DateTimeFormatterConstants;
import de.itestra.dashboard.highscore.dto.darts.response.DartsEntryResponse;
import de.itestra.dashboard.highscore.entity.DartsEntry;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;

/**
 * Mapper for converting {@link DartsEntry} entities to {@link DartsEntryResponse} DTOs.
 * <p>
 * Handles data transformation including date formatting for API responses.
 * </p>
 */
@Component
public class DartsEntryMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatterConstants.DATE_TIME_FORMATTER;

    /**
     * Converts a darts entry entity to a response DTO.
     * <p>
     * Formats the creation timestamp using the standard date-time formatter.
     * </p>
     *
     * @param match the darts entry entity to convert
     * @return the darts entry response DTO
     */
    public DartsEntryResponse toResponse(DartsEntry match) {
        String dateText = match.getCreatedAt().format(FORMATTER);
        return new DartsEntryResponse(
                match.getId(),
                match.getPlayerName(),
                match.getDartsToFinish(),
                dateText
        );
    }
}
