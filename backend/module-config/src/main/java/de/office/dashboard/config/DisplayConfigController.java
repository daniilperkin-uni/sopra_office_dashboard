package de.office.dashboard.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config/display")
@Tag(name = "Display Configuration", description = "Manage dashboard display settings including view rotation and default view selection")
public class DisplayConfigController {

    private final DisplayConfigService service;

    public DisplayConfigController(DisplayConfigService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Get current display configuration",
            description = "Retrieves the current display configuration settings including rotation status, interval, and default view."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Display configuration successfully retrieved",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DisplayConfigDto.class)
            )
    )
    public DisplayConfigDto getConfig() {
        return service.getConfig();
    }

    @PostMapping
    @Operation(
            summary = "Update display configuration",
            description = "Updates the display configuration with new settings. All fields in the request body will replace the existing configuration."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Display configuration successfully updated and returned",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DisplayConfigDto.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid configuration data provided (e.g., negative interval or invalid view ID)",
            content = @Content(mediaType = "application/json")
    )
    public DisplayConfigDto updateConfig(@Valid @RequestBody DisplayConfigDto dto) {
        return service.updateConfig(dto);
    }
}
