package de.itestra.dashboard.common.util;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.lang.NonNull;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Client for communicating with the Mattermost messaging platform via REST API.
 * <p>
 * This client provides methods to post messages to channels and send direct
 * messages to users. It uses Spring WebClient for communication and
 * supports both channel broadcasts and direct user messaging.
 * </p>
 */
@Service
public class MattermostClient {

    private static final Log LOG = LogFactory.getLog(MattermostClient.class);

    @Value("${mattermost.api.token}")
    private String token;

    @Value("${mattermost.api.url}")
    private @NonNull String baseUrl;

    @Value("${mattermost.api.channelId}")
    private String channelId;

    private String botUserId;
    private WebClient webClient;

    /**
     * Initializes the Mattermost client after bean construction.
     * <p>
     * Sets up the WebClient with base URL, authentication token, and request
     * logging.
     * Also retrieves the bot user ID for direct messaging functionality.
     * </p>
     */
    @PostConstruct
    private void init() {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .filter(logRequest())
                .build();

        try {
            this.botUserId = getBotUserId();
        } catch (Exception e) {
            LOG.error("Failed to initialize Mattermost client:" + e.getMessage());
            return;
        }
    }

    /**
     * Creates a request logging filter for debugging HTTP requests.
     *
     * @return exchange filter function that logs request details
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            LOG.info("Request: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> LOG.debug("Header: " + name + "=" + value)));
            return Mono.just(clientRequest);
        });
    }

    /**
     * Retrieves the user ID of the bot account.
     * <p>
     * Makes an authenticated request to the Mattermost API to get the current
     * user's (bot's) information.
     * </p>
     *
     * @return the bot user's ID
     */
    public String getBotUserId() {
        Map<String, Object> response = webClient.get()
                .uri("/users/me")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block(Duration.ofSeconds(10));

        LOG.info(response);
        return (String) response.get("id");
    }

    /**
     * Creates a direct message channel between the bot and a specific user.
     *
     * @param receiver the user ID of the recipient
     * @return the channel ID of the created direct channel
     */
    public String createDirectChannelToUser(String receiver) {
        LOG.info("Creating channel for " + receiver + " and bot: " + botUserId);
        List<String> userIds = List.of(botUserId, receiver);

        LOG.info("userIds: " + userIds);

        Map<String, Object> response = webClient.post()
                .uri("/channels/direct")
                .bodyValue(userIds)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
        return (String) response.get("id");
    }

    /**
     * Posts a message to the configured default channel.
     *
     * @param message the message text to post
     */
    public void postMessageToChannel(String message) {
        postMessage(message, this.channelId);
    }

    /**
     * Posts a message to a specific channel.
     *
     * @param message  the message text
     * @param receiver the channel ID to post to
     */
    private void postMessage(String message, String receiver) {
        Map<String, String> messageBody = Map.of(
                "channel_id", receiver,
                "message", message);

        if (messageBody == null) {
            return;
        }

        webClient.post()
                .uri("/posts")
                .bodyValue((Object) messageBody)
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(10));
    }

    /**
     * Sends a direct message to a user identified by email.
     * <p>
     * Looks up the user ID by email, creates a direct channel, and sends the message.
     * </p>
     *
     * @param message the message text to send
     * @param email   the recipient's email address
     */
    public void postDirectMessage(String message, String email) {
        String userId = this.getUserIdByEmail(email);
        String channel = createDirectChannelToUser(userId);
        postMessage(message, channel);
    }

    /**
     * Retrieves a user's ID by their email address.
     *
     * @param email the user's email address
     * @return the user ID
     */
    private String getUserIdByEmail(String email) {
        Map<String, Object> response = webClient.get()
                .uri("/users/email/{email}", email)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block(Duration.ofSeconds(10));

        return (String) response.get("id");
    }

    /**
     * Checks if the Mattermost server is active and responding.
     *
     * @return true if the server responds with status "OK", false
     *         otherwise
     */
    public boolean isActive() {
        try {
            Map<String, Object> response = webClient.get()
                    .uri("/system/ping")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block(Duration.ofSeconds(10));

            return "OK".equals(response.get("status"));
        } catch (Exception e) {
            LOG.error("Failed to check Mattermost server status", e);
            return false;
        }
    }
}
