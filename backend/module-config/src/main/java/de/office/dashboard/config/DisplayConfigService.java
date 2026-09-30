package de.office.dashboard.config;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisplayConfigService {

    private final DisplayConfigRepository repository;

    public DisplayConfigService(DisplayConfigRepository repository) {
        this.repository = repository;
    }

    public DisplayConfigDto getConfig() {
        DisplayConfigEntity entity = repository.findById(1L).orElseGet(() -> {
            DisplayConfigEntity defaultEntity = new DisplayConfigEntity();
            return repository.save(defaultEntity);
        });
        return mapToDto(entity);
    }

    @Transactional
    public DisplayConfigDto updateConfig(DisplayConfigDto dto) {
        DisplayConfigEntity entity = repository.findById(1L).orElseGet(DisplayConfigEntity::new);
        entity.setRotationEnabled(dto.isRotationEnabled());
        entity.setSkipOneDisplayInRotation(dto.isSkipOneDisplayInRotation());
        entity.setRotationIntervalSeconds(dto.getRotationIntervalSeconds());
        entity.setDefaultSingleViewId(dto.getDefaultSingleViewId());

        entity = repository.save(entity);
        return mapToDto(entity);
    }

    private DisplayConfigDto mapToDto(DisplayConfigEntity entity) {
        return new DisplayConfigDto(
                entity.isRotationEnabled(),
                entity.isSkipOneDisplayInRotation(),
                entity.getRotationIntervalSeconds(),
                entity.getDefaultSingleViewId()
        );
    }
}
