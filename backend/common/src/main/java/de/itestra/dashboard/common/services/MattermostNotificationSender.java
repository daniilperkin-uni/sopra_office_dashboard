package de.itestra.dashboard.common.services;

import de.itestra.dashboard.common.util.MattermostClient;
import de.itestra.dashboard.common.util.NotificationSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Mattermost-based implementation of {@link NotificationSender}.
 * <p>
 * Delegates to {@link MattermostClient} for actual communication,
 * acting as an adapter between the notification interface and
 * the Mattermost client. This separation allows for easy replacement
 * of the notification provider (e.g., Email, MS Teams) without
 * affecting the scheduling and formatting logic.
 * </p>
 */
@Service
public class MattermostNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(MattermostNotificationSender.class);

    private final MattermostClient mattermostClient;

    /**
     * Constructs the sender with the required Mattermost client.
     *
     * @param mattermostClient the client for communicating with Mattermost API
     */
    public MattermostNotificationSender(MattermostClient mattermostClient) {
        this.mattermostClient = mattermostClient;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends the message to the configured Mattermost channel.
     * </p>
     *
     * @throws RuntimeException if sending to Mattermost fails
     */
    @Override
    public void sendToChannel(String message) {
        try {
            mattermostClient.postMessageToChannel(message);
            log.debug("Successfully sent channel notification");
        } catch (Exception e) {
            log.error("Failed to send channel notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send channel notification", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends a direct message via Mattermost to the user identified by email.
     * </p>
     *
     * @throws RuntimeException if sending the direct message fails
     */
    @Override
    public void sendDirectMessage(String message, String recipientEmail) {
        try {
            mattermostClient.postDirectMessage(message, recipientEmail);
            log.debug("Successfully sent direct message to: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send direct message to {}: {}", recipientEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send direct message to " + recipientEmail, e);
        }
    }

    /**
     * <p>
     * Checks if the Mattermost service is available by performing a health check
     * against the Mattermost server.
     * </p>
     *
     * @return true if the Mattermost service is available and responding
     * with status "OK", false otherwise
     */
    @Override
    public boolean isAvailable() {
        return mattermostClient.isActive();
    }
}
