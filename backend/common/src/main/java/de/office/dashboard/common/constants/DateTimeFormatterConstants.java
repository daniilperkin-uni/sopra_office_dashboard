package de.office.dashboard.common.constants;

import java.time.format.DateTimeFormatter;

public final class DateTimeFormatterConstants {

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");

    public static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd.MM.yyyy");
        
    private DateTimeFormatterConstants() {
        throw new UnsupportedOperationException("This is a utility class and not ment for instantiation.");
    }
}
