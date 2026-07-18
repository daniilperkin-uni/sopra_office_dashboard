package de.itestra.dashboard.config;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "display_config")
public class DisplayConfigEntity {

    @Id
    private Long id = 1L; // Singleton pattern in DB

    private boolean rotationEnabled = true;

    private boolean skipOneDisplayInRotation = false;

    private int rotationIntervalSeconds = 15;

    private String defaultSingleViewId = "calendar";

    public DisplayConfigEntity() {
    }

    public DisplayConfigEntity(boolean rotationEnabled, int rotationIntervalSeconds, String defaultSingleViewId) {
        this.rotationEnabled = rotationEnabled;
        this.rotationIntervalSeconds = rotationIntervalSeconds;
        this.defaultSingleViewId = defaultSingleViewId;
    }

    public Long getId() {
        return id;
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
