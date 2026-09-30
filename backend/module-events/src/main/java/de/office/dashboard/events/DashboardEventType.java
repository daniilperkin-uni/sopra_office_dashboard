package de.office.dashboard.events;

/**
 * Enumeration of dashboard event types.
 * <p>
 * Represents the different types of employee-related events that can be
 * displayed on the dashboard.
 * </p>
 */
public enum DashboardEventType {

    /**
     * Employee birthday event
     */
    BIRTHDAY,

    /**
     * Employee work anniversary event
     */
    WORK_ANNIVERSARY,

    /**
     * Employee probation period ending event
     */
    PROBATION_END;

    /**
     * Returns a human-readable label for this event type.
     *
     * @return the display label for this event type
     */
    public String getLabel() {
        return switch (this) {
            case BIRTHDAY -> "Birthday";
            case WORK_ANNIVERSARY -> "Work Anniversary";
            case PROBATION_END -> "Probation End";
        };
    }
}
