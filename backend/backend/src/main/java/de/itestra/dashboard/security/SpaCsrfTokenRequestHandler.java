package de.itestra.dashboard.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

/**
 * CSRF-Token-Handler fuer die Vue-SPA.
 * <p>
 * Axios liest das Cookie {@code XSRF-TOKEN} und sendet den Rohwert im Header
 * {@code X-XSRF-TOKEN}. Header-Werte werden daher unmaskiert geprueft,
 * serverseitig gerenderte Parameter weiterhin BREACH-geschuetzt (XOR).
 * </p>
 */
public final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
    private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       Supplier<CsrfToken> csrfToken) {
        // XOR-Maskierung fuer gerenderte Tokens beibehalten
        this.xor.handle(request, response, csrfToken);
        // Token sofort laden, damit das Cookie bei jeder Antwort gesetzt wird
        csrfToken.get();
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        String headerValue = request.getHeader(csrfToken.getHeaderName());
        // Header (SPA) -> Rohwert; sonst Parameter -> maskierter Wert
        return StringUtils.hasText(headerValue)
                ? this.plain.resolveCsrfTokenValue(request, csrfToken)
                : this.xor.resolveCsrfTokenValue(request, csrfToken);
    }
}
