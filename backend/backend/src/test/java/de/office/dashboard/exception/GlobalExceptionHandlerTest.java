package de.office.dashboard.exception;

import de.office.dashboard.common.exception.DuplicateResourceException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for the status code of a duplicate resource.
 * <p>
 * The controllers document 409 for "already exists" and the frontend checks
 * that status, but a plain IllegalArgumentException used to fall into the
 * 400 handler. The test pins the mapping without booting a Spring context:
 * the finish line for the HTTP layer is the handler, not the controller.
 * </p>
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void duplicateResource_isAnsweredWith409AndTheReasonInDetail() {
        ResponseEntity<ProblemDetail> response =
                handler.handleDuplicate(new DuplicateResourceException("This item is already in the catalog."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getBody().getDetail()).isEqualTo("This item is already in the catalog.");
    }
}
