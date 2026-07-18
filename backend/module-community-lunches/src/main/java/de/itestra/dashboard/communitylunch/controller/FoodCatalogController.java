package de.itestra.dashboard.communitylunch.controller;

import de.itestra.dashboard.communitylunch.dto.catalog.request.CreateFoodCatalogItemRequest;
import de.itestra.dashboard.communitylunch.dto.catalog.request.UpdateFoodCatalogItemRequest;
import de.itestra.dashboard.communitylunch.dto.catalog.response.FoodCatalogItemResponse;
import de.itestra.dashboard.communitylunch.service.FoodCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

/**
 * REST controller for managing the food catalog of meal options.
 * <p>
 * Provides endpoints for creating, updating, and retrieving reusable meal options
 * that can be added to community lunch events. Supports soft delete pattern using
 * an active flag, and prevents duplicate labels through case-insensitive matching.
 * </p>
 */
@Validated
@RestController
@RequestMapping("/api/food-catalog")
@Tag(name = "Food Catalog", description = "Reusable meal options catalog management")
public class FoodCatalogController {

    private final FoodCatalogService service;

    /**
     * Constructs a new FoodCatalogController.
     *
     * @param service service for managing food catalog items
     */
    public FoodCatalogController(FoodCatalogService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get all catalog items", description = "Retrieves all food catalog items, optionally filtered to show only active items")
    @ApiResponse(responseCode = "200", description = "List of catalog items", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = FoodCatalogItemResponse.class))))
    public ResponseEntity<List<FoodCatalogItemResponse>> getCatalogItems(
            @Parameter(description = "Filter to show only active items (true) or all items including inactive (false)", example = "true")
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        return ResponseEntity.ok(service.getCatalogItems(activeOnly));
    }

    @PostMapping
    @Operation(summary = "Create catalog item", description = "Creates a new food catalog item. Uses case-insensitive duplicate checking. If an inactive item with the same label exists, it will be reactivated instead")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catalog item created or reactivated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FoodCatalogItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Active item with this label already exists")
    })
    public ResponseEntity<FoodCatalogItemResponse> createCatalogItem(
            @Valid @RequestBody CreateFoodCatalogItemRequest request
    ) {
        try {
            return ResponseEntity.ok(service.createCatalogItem(request));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update catalog item", description = "Updates an existing food catalog item's label. Uses case-insensitive duplicate checking to prevent conflicts with other active items")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catalog item updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FoodCatalogItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Catalog item not found"),
            @ApiResponse(responseCode = "409", description = "Another active item with this label already exists")
    })
    public ResponseEntity<FoodCatalogItemResponse> updateCatalogItem(
            @Parameter(description = "Unique identifier of the catalog item", example = "1", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateFoodCatalogItemRequest request
    ) {
        try {
            return ResponseEntity.ok(service.updateCatalogItem(id, request));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Toggle catalog item active status", description = "Activates or deactivates a catalog item (soft delete). Inactive items are hidden from catalog listings by default")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Active status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Catalog item not found")
    })
    public ResponseEntity<Void> updateCatalogItemActive(
            @Parameter(description = "Unique identifier of the catalog item", example = "1", required = true)
            @PathVariable @Positive Long id,
            @Parameter(description = "Whether the item should be active", example = "true", required = true)
            @RequestParam boolean active
    ) {
        service.updateCatalogItemActive(id, active);
        return ResponseEntity.noContent().build();
    }
}

