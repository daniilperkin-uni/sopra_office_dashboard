package de.itestra.dashboard.communitylunch.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumeration representing the status of a community lunch event.
 */
@Schema(description = "Community lunch event status")
public enum LunchStatus {

    @Schema(description = "Event is in draft mode, voting not yet open")
    DRAFT,

    @Schema(description = "Event is open for voting")
    OPEN,

    @Schema(description = "Event is closed, voting has ended")
    CLOSED
}
