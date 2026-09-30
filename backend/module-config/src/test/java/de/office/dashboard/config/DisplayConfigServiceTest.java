package de.office.dashboard.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DisplayConfigService}.
 *
 * <p>
 * Covers the singleton-config contract: reads always operate on row 1 and
 * create a sensible default when it is missing, while updates write back to
 * that same row. The repository is mocked - no database required.
 * </p>
 */
class DisplayConfigServiceTest {

    private DisplayConfigRepository repository;
    private DisplayConfigService service;

    @BeforeEach
    void setUp() {
        repository = mock(DisplayConfigRepository.class);
        service = new DisplayConfigService(repository);
    }

    @Test
    @DisplayName("getConfig() returns the stored singleton config")
    void getConfigReturnsStoredEntity() {
        DisplayConfigEntity entity = new DisplayConfigEntity();
        entity.setRotationEnabled(true);
        entity.setRotationIntervalSeconds(30);
        entity.setDefaultSingleViewId("calendar");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        DisplayConfigDto dto = service.getConfig();

        assertTrue(dto.isRotationEnabled());
        assertEquals(30, dto.getRotationIntervalSeconds());
        assertEquals("calendar", dto.getDefaultSingleViewId());
    }

    @Test
    @DisplayName("getConfig() creates and persists defaults when no row exists yet")
    void getConfigCreatesDefaultOnFirstRead() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        when(repository.save(any(DisplayConfigEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DisplayConfigDto dto = service.getConfig();

        assertFalse(dto.isSkipOneDisplayInRotation());
        assertEquals(15, dto.getRotationIntervalSeconds());
        Mockito.verify(repository).save(any(DisplayConfigEntity.class));
    }

    @Test
    @DisplayName("updateConfig() writes all fields back to the singleton row")
    void updateConfigWritesAllFields() {
        DisplayConfigEntity existing = new DisplayConfigEntity();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(DisplayConfigEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DisplayConfigDto input = new DisplayConfigDto(false, true, 60, "game");

        DisplayConfigDto result = service.updateConfig(input);

        assertFalse(result.isRotationEnabled());
        assertTrue(result.isSkipOneDisplayInRotation());
        assertEquals(60, result.getRotationIntervalSeconds());
        assertEquals("game", result.getDefaultSingleViewId());
        Mockito.verify(repository).save(existing);
    }
}
