package de.itestra.dashboard.communitylunch.service;

import de.itestra.dashboard.communitylunch.entity.CommunityLunchEvent;
import de.itestra.dashboard.communitylunch.entity.LunchEventOption;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Formats community lunch events into notification messages.
 * <p>
 * Responsible for creating messages for different lunch event types
 * and formatting lunch options in a consistent way.
 * </p>
 */
@Component
public class LunchNotificationFormatter {

    /**
     * Formats a lunch event creation notification.
     * <p>
     * Creates a channel message announcing a new lunch event with
     * its date and available options.
     * </p>
     *
     * @param event the lunch event to format
     * @return formatted notification message ready to send
     */
    public String formatLunchCreationNotification(CommunityLunchEvent event) {
        String lunchOptions = formatLunchOptions(event.getOptions());
        return String.format(
                """
                        Hallo zusammen,
                        es wurde ein neues **Lunch Event** erstellt 🍴
                          - **Wann:** %s
                          - **Optionen:**
                        %s
                        """, event.getDate().toString(), lunchOptions);
    }

    /**
     * Formats a list of lunch options into a readable string.
     * <p>
     * Each option is formatted as a bullet point, using either
     * the catalog item label or custom label.
     * </p>
     *
     * @param options the lunch options to format
     * @return formatted options string with line breaks
     */
    private String formatLunchOptions(List<LunchEventOption> options) {
        return options.stream()
                .map(this::formatOption)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Formats a single lunch option.
     * <p>
     * Uses the catalog item label if available, otherwise
     * uses the custom label provided by the user.
     * </p>
     *
     * @param option the option to format
     * @return formatted option string with bullet point
     */
    private String formatOption(LunchEventOption option) {
        String label = option.getCatalogItem() == null
                ? option.getCustomLabel()
                : option.getCatalogItem().getLabel();
        return "    - " + label;
    }
}
