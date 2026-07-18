package de.itestra.dashboard.events.scheduler;

import de.itestra.dashboard.common.constants.DateTimeFormatterConstants;
import de.itestra.dashboard.common.util.NotificationSender;
import de.itestra.dashboard.events.dto.DashboardEventResponse;
import de.itestra.dashboard.events.dto.EventNotificationMessage;
import de.itestra.dashboard.events.service.DashboardEventService;
import de.itestra.dashboard.events.service.EventNotificationFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler responsible for sending daily event reminder notifications.
 * <p>
 * Runs daily at a configurable time (default: 12:00 noon) to send notifications
 * about upcoming events. Retrieves events from {@link DashboardEventService},
 * formats them using {@link EventNotificationFormatter}, and sends them via
 * {@link NotificationSender}.
 * </p>
 * <p>
 * This scheduler can be enabled/disabled via the property
 * {@code notifications.events.enabled} and the schedule can be customized
 * via {@code notifications.events.schedule.cron}.
 * </p>
 */
@Component
@ConditionalOnProperty(
        name = "notifications.events.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class EventNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventNotificationScheduler.class);

    private final DashboardEventService dashboardEventService;
    private final EventNotificationFormatter notificationFormatter;
    private final NotificationSender notificationSender;

    /**
     * Constructs the scheduler with required dependencies.
     *
     * @param dashboardEventService service for retrieving cached events
     * @param notificationFormatter formatter for creating notification messages
     * @param notificationSender    sender for dispatching notifications
     */
    public EventNotificationScheduler(
            DashboardEventService dashboardEventService,
            EventNotificationFormatter notificationFormatter,
            NotificationSender notificationSender
    ) {
        this.dashboardEventService = dashboardEventService;
        this.notificationFormatter = notificationFormatter;
        this.notificationSender = notificationSender;
    }

    /**
     * Scheduled task that sends daily reminder notifications.
     * <p>
     * Default schedule: Daily at 12:00 (noon).
     * Can be customized via {@code notifications.events.schedule.cron} property.
     * </p>
     * <p>
     * Process:
     * <ol>
     *   <li>Checks if notification sender is available</li>
     *   <li>Retrieves events scheduled for tomorrow</li>
     *   <li>Formats each event into notification messages</li>
     *   <li>Sends channel messages for all events</li>
     *   <li>Sends direct messages for applicable events (e.g., birthdays)</li>
     * </ol>
     * </p>
     */
    @Scheduled(cron = "${notifications.events.schedule.cron:0 0 12 * * *}")
    public void sendDailyEventReminders() {
        log.info("Starting daily event reminder notifications");

        if (!notificationSender.isAvailable()) {
            log.warn("NotificationSender is not available, skipping notifications");
            return;
        }

        List<DashboardEventResponse> tomorrowsEvents = getEventsForTomorrow();

        if (tomorrowsEvents.isEmpty()) {
            log.info("No events scheduled for tomorrow, skipping notifications");
            return;
        }

        log.info("Found {} event(s) for tomorrow, sending notifications", tomorrowsEvents.size());

        int successCount = 0;
        for (DashboardEventResponse event : tomorrowsEvents) {
            if (sendEventNotification(event)) {
                successCount++;
            }
        }

        log.info("Completed sending notifications: {}/{} successful",
                successCount, tomorrowsEvents.size());
    }

    /**
     * Retrieves all events scheduled for tomorrow.
     * <p>
     * Filters cached events by comparing their date with tomorrow's date
     * formatted according to the application's standard date format.
     * </p>
     *
     * @return list of events happening tomorrow
     */
    private List<DashboardEventResponse> getEventsForTomorrow() {
        List<DashboardEventResponse> cachedEvents = dashboardEventService.getCachedEvents();
        String tomorrowDate = getTomorrowDateFormatted();

        return cachedEvents.stream()
                .filter(event -> event.dashboardEventDate().equals(tomorrowDate))
                .toList();
    }

    /**
     * Formats tomorrow's date in the format used by DashboardEventResponse.
     * <p>
     * Uses {@link DateTimeFormatterConstants#DATE_FORMATTER} to ensure
     * consistency with event date formatting.
     * </p>
     *
     * @return formatted date string (dd.MM.yyyy)
     */
    private String getTomorrowDateFormatted() {
        return LocalDate.now()
                .plusDays(1)
                .format(DateTimeFormatterConstants.DATE_FORMATTER);
    }

    /**
     * Sends notification for a single event.
     * <p>
     * Formats the event into messages using {@link EventNotificationFormatter}
     * and dispatches them via {@link NotificationSender}. Channel messages are
     * sent for all events, and direct messages are sent when applicable
     * (e.g., birthday reminders).
     * </p>
     *
     * @param event the event to send notification for
     * @return true if notification was sent successfully, false if an error occurred
     */
    private boolean sendEventNotification(DashboardEventResponse event) {
        try {
            EventNotificationMessage message = notificationFormatter.formatEventNotification(event);

            // Send channel message
            notificationSender.sendToChannel(message.channelMessage());
            log.debug("Sent channel notification for event: {}", event.dashboardEventDescription());

            // Send direct message if applicable (e.g., birthday reminders)
            if (message.hasDirectMessage()) {
                notificationSender.sendDirectMessage(
                        message.directMessage(),
                        message.recipientEmail()
                );
                log.debug("Sent direct message to: {}", message.recipientEmail());
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to send notification for event: {}. Error: {}",
                    event.dashboardEventDescription(), e.getMessage(), e);
            return false;
        }
    }
}
