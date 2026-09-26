package de.itestra.dashboard.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Configuration for dashboard display behavior including automatic view rotation settings")
public class DisplayConfigDto {

    @Schema(description = "Whether automatic rotation between different dashboard views is enabled", example = "true")
    private boolean rotationEnabled;

    @Schema(description = "Whether to skip the OneDisplay (dashboard) view during automatic rotation", example = "false")
    private boolean skipOneDisplayInRotation;

    @Schema(description = "Time in seconds before rotating to the next view; -1 selects the game mode view", example = "15")
    private int rotationIntervalSeconds;

    @Schema(description = "Identifier of the view to display when rotation is disabled. Valid values include 'Kalender', 'Geburtstage', 'Parkplätze', etc.", example = "Kalender")
    @NotNull(message = "Default view ID must be specified")
    private String defaultSingleViewId;

    public DisplayConfigDto() {
    }

    public DisplayConfigDto(boolean rotationEnabled, boolean skipOneDisplayInRotation, int rotationIntervalSeconds,
            String defaultSingleViewId) {
        this.rotationEnabled = rotationEnabled;
        this.skipOneDisplayInRotation = skipOneDisplayInRotation;
        this.rotationIntervalSeconds = rotationIntervalSeconds;
        this.defaultSingleViewId = defaultSingleViewId;
    }

    @AssertTrue(message = "Rotation interval must be -1 (game mode) or at least 5 seconds")
    public boolean isValidInterval() {
        return rotationIntervalSeconds == -1 || rotationIntervalSeconds >= 5;
    }

    public boolean isRotationEnabled() {
        return rotationEnabled;
    }

    public void setRotationEnabled(boolean rotationEnabled) {
        this.rotationEnabled = rotationEnabled;
    }

    public boolean isSkipOneDisplayInRotation() {
        return skipOneDisplayInRotation;
    }

    public void setSkipOneDisplayInRotation(boolean skipOneDisplayInRotation) {
        this.skipOneDisplayInRotation = skipOneDisplayInRotation;
    }

    public int getRotationIntervalSeconds() {
        return rotationIntervalSeconds;
    }

    public void setRotationIntervalSeconds(int rotationIntervalSeconds) {
        this.rotationIntervalSeconds = rotationIntervalSeconds;
    }

    public String getDefaultSingleViewId() {
        return defaultSingleViewId;
    }

    public void setDefaultSingleViewId(String defaultSingleViewId) {
        this.defaultSingleViewId = defaultSingleViewId;
    }
}
