package de.itestra.dashboard.common.util;

/**
 * Interface for sending notifications to various channels.
 * <p>
 * Implementations should handle the actual communication with
 * notification platforms (Mattermost, Email, etc.).
 * This interface works with pre-formatted message strings,
 * ensuring a separation between message creation and message delivery.
 * </p>
 */
public interface NotificationSender {

    /**
     * Sends a notification message to a configured default channel.
     *
     * @param message the fully formatted message text to send
     * @throws RuntimeException if sending fails
     */
    void sendToChannel(String message);

    /**
     * Sends a direct message to a specific user.
     *
     * @param message the fully formatted message text to send
     * @param recipientEmail the email address of the recipient
     * @throws RuntimeException if sending fails
     */
    void sendDirectMessage(String message, String recipientEmail);

    /**
     * Checks if the notification sender is available.
     *
     * @return true if ready to send notifications, false otherwise
     */
    boolean isAvailable();
}
