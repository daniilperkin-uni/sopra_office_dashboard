package de.itestra.dashboard.events.dto;

/**
 * Represents a formatted notification message for an event.
 * <p>
 * Contains all information needed to send a notification,
 * with messages already formatted and ready to send.
 * This DTO separates message creation from message delivery.
 * </p>
 *
 * @param channelMessage the message to post in public channel
 * @param directMessage  optional direct message to send to employee (null if none)
 * @param recipientEmail email of recipient for direct message (null if no direct message)
 */
public record EventNotificationMessage(
        String channelMessage,
        String directMessage,
        String recipientEmail
) {

    /**
     * Creates a channel-only notification without a direct message.
     *
     * @param message the message to send to the channel
     * @return notification message configured for channel-only delivery
     */
    public static EventNotificationMessage channelOnly(String message) {
        return new EventNotificationMessage(message, null, null);
    }

    /**
     * Creates a notification with both channel and direct message.
     *
     * @param channelMessage the message to post in the public channel
     * @param directMessage  the message to send directly to the employee
     * @param recipientEmail the email address of the direct message recipient
     * @return notification message configured for both channel and direct delivery
     */
    public static EventNotificationMessage withDirectMessage(
            String channelMessage,
            String directMessage,
            String recipientEmail
    ) {
        return new EventNotificationMessage(channelMessage, directMessage, recipientEmail);
    }

    /**
     * Checks if this notification includes a direct message.
     *
     * @return true if a direct message should be sent, false otherwise
     */
    public boolean hasDirectMessage() {
        return directMessage != null && recipientEmail != null;
    }
}
