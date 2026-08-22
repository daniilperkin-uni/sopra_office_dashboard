package de.itestra.dashboard.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MattermostClient}.
 *
 * <p>
 * The WebClient is mocked at the fluent-chain boundary, which lets us verify
 * request targets and payload routing without standing up a real Mattermost
 * server. Covers the two externally visible behaviors most worth guarding:
 * the health check interpretation and channel vs. direct-message posting.
 * </p>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
class MattermostClientTest {

    private MattermostClient client;
    private WebClient webClient;
    private WebClient.RequestHeadersUriSpec headersUriSpec;
    private WebClient.RequestHeadersSpec headersSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() throws Exception {
        client = new MattermostClient();
        webClient = mock(WebClient.class);
        headersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        headersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        setField("baseUrl", "http://mattermost.test");
        setField("token", "REDACTED-SECRET");
        setField("channelId", "town-square");
        setField("webClient", webClient);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = MattermostClient.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(client, value);
    }

    private void stubGet() {
        when(webClient.get()).thenReturn(headersUriSpec);
        when(headersUriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("isActive() reports true when the server answers status OK")
    void isActiveTrueOnOkStatus() {
        stubGet();
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(Map.of("status", "OK")));

        assertTrue(client.isActive());
    }

    @Test
    @DisplayName("isActive() reports false when the server answers something else")
    void isActiveFalseOnUnexpectedStatus() {
        stubGet();
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(Map.of("status", "FAIL")));

        assertFalse(client.isActive());
    }

    @Test
    @DisplayName("isActive() reports false instead of throwing when the server is unreachable")
    void isActiveFalseOnConnectionError() {
        stubGet();
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new IllegalStateException("server unreachable")));

        assertFalse(client.isActive());
    }

    @Test
    @DisplayName("postMessageToChannel() posts to the configured channel id")
    void postMessageToChannelUsesConfiguredChannel() {
        WebClient.RequestBodyUriSpec bodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec postHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec postResponseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(bodyUriSpec);
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        when(bodyUriSpec.uri(uriCaptor.capture())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenReturn(postResponseSpec);
        when(postResponseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        client.postMessageToChannel("hello");

        assertEquals("/posts", uriCaptor.getValue());

        ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(bodySpec).bodyValue(bodyCaptor.capture());
        Map<String, String> sentBody = (Map<String, String>) bodyCaptor.getValue();
        assertEquals("town-square", sentBody.get("channel_id"));
        assertEquals("hello", sentBody.get("message"));
    }

    @Test
    @DisplayName("postDirectMessage() resolves the user and posts to their direct channel")
    void postDirectMessageResolvesUserAndPosts() throws Exception {
        // The client caches its own bot id at startup; provide it directly.
        Field botField = MattermostClient.class.getDeclaredField("botUserId");
        botField.setAccessible(true);
        botField.set(client, "bot-1");

        // First GET resolves the user by email. The uri() stub must accept the
        // path-variable overload used by getUserIdByEmail ("/users/email/{email}").
        when(webClient.get()).thenReturn(headersUriSpec);
        when(headersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(headersSpec);
        when(headersUriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(Map.of("id", "user-42")));

        // Second call is POST /channels/direct -> returns the channel id as body.
        WebClient.RequestBodyUriSpec bodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec postHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec postResponseSpec = mock(WebClient.ResponseSpec.class);

        // postDirectMessage makes TWO posts: channel creation, then the message.
        // The final one hits POST /posts and returns an empty entity.
        when(webClient.post()).thenReturn(bodyUriSpec);
        when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        when(bodyUriSpec.uri(uriCaptor.capture())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenReturn(postResponseSpec);
        when(postResponseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(Map.of("id", "dm-channel")));
        when(postResponseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        client.postDirectMessage("ping", "someone@example.com");

        // The client posts twice (channel creation + the message itself); the
        // message must end up addressed to the direct channel id.
        ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(bodySpec, Mockito.times(2)).bodyValue(bodyCaptor.capture());
        Map<String, String> sentBody = (Map<String, String>) bodyCaptor.getAllValues().get(1);
        assertEquals("dm-channel", sentBody.get("channel_id"));
        assertEquals("ping", sentBody.get("message"));
    }
}
