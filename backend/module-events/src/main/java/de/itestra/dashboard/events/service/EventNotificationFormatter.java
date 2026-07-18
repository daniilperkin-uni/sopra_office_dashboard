package de.itestra.dashboard.events.service;

import de.itestra.dashboard.events.dto.DashboardEventResponse;
import de.itestra.dashboard.events.dto.EventNotificationMessage;
import org.springframework.stereotype.Component;

/**
 * Formats dashboard events into notification messages.
 * <p>
 * Responsible for creating messages for different event types.
 * This service implements the rules for which events require
 * direct messages and how messages should be formatted.
 * </p>
 */
@Component
public class EventNotificationFormatter {

    private static final String BIRTHDAY_TYPE = "BIRTHDAY";

    /**
     * Formats an event into a notification message.
     * <p>
     * Creates a channel message for all events. Birthday events
     * additionally receive a direct reminder message to the employee.
     * </p>
     *
     * @param event the event to format
     * @return formatted notification message ready to send
     */
    public EventNotificationMessage formatEventNotification(DashboardEventResponse event) {
        String channelMessage = createChannelMessage(event);

        if (isBirthdayEvent(event)) {
            String directMessage = createBirthdayReminderMessage(event);
            return EventNotificationMessage.withDirectMessage(
                    channelMessage,
                    directMessage,
                    event.employeeEmail()
            );
        }

        return EventNotificationMessage.channelOnly(channelMessage);
    }

    /**
     * Creates a channel message for an event.
     * <p>
     * Uses the description already created by DashboardEventMapper,
     * which provides consistent, localized event descriptions.
     * </p>
     *
     * @param event the event to create a message for
     * @return formatted channel message
     */
    private String createChannelMessage(DashboardEventResponse event) {
        return event.dashboardEventDescription();
    }

    /**
     * Creates a direct birthday reminder message for an employee.
     * <p>
     * Sends a friendly reminder to bring cake for their birthday.
     * </p>
     *
     * @param event the birthday event
     * @return formatted direct message
     */
    private String createBirthdayReminderMessage(DashboardEventResponse event) {
        return String.format(
                "Hallo %s, \nmorgen ist dein Geburtstag, vergiss bitte nicht den Kuchen mitzubringen ;-)",
                event.employeeName()
        );
    }

    /**
     * Checks if an event is a birthday event.
     *
     * @param event the event to check
     * @return true if the event is a birthday, false otherwise
     */
    private boolean isBirthdayEvent(DashboardEventResponse event) {
        return BIRTHDAY_TYPE.equals(event.dashboardEventType());
    }
}
