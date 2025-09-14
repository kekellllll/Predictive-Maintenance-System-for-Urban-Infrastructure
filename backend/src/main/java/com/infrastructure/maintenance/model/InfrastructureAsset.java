package com.infrastructure.maintenance.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "infrastructure_assets")
public class InfrastructureAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String assetId;

    @NotBlank
    private String name;

    @NotBlank
    private String type; // BRIDGE, ROAD, BUILDING, TUNNEL, etc.

    private String description;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private AssetStatus status = AssetStatus.OPERATIONAL;

    @Column(name = "installation_date")
    private LocalDateTime installationDate;

    @Column(name = "last_maintenance")
    private LocalDateTime lastMaintenance;

    @Column(name = "next_scheduled_maintenance")
    private LocalDateTime nextScheduledMaintenance;

    @Column(name = "maintenance_priority")
    private Integer maintenancePriority = 0; // 0 = low, 10 = critical

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SensorData> sensorData;

    // Constructors
    public InfrastructureAsset() {}

    public InfrastructureAsset(String assetId, String name, String type, 
                             Double latitude, Double longitude) {
        this.assetId = assetId;
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAssetId() { return assetId; }
    public void setAssetId(String assetId) { this.assetId = assetId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public AssetStatus getStatus() { return status; }
    public void setStatus(AssetStatus status) { this.status = status; }

    public LocalDateTime getInstallationDate() { return installationDate; }
    public void setInstallationDate(LocalDateTime installationDate) { this.installationDate = installationDate; }

    public LocalDateTime getLastMaintenance() { return lastMaintenance; }
    public void setLastMaintenance(LocalDateTime lastMaintenance) { this.lastMaintenance = lastMaintenance; }

    public LocalDateTime getNextScheduledMaintenance() { return nextScheduledMaintenance; }
    public void setNextScheduledMaintenance(LocalDateTime nextScheduledMaintenance) { this.nextScheduledMaintenance = nextScheduledMaintenance; }

    public Integer getMaintenancePriority() { return maintenancePriority; }
    public void setMaintenancePriority(Integer maintenancePriority) { this.maintenancePriority = maintenancePriority; }

    public List<SensorData> getSensorData() { return sensorData; }
    public void setSensorData(List<SensorData> sensorData) { this.sensorData = sensorData; }

    public enum AssetStatus {
        OPERATIONAL, MAINTENANCE_REQUIRED, UNDER_MAINTENANCE, OUT_OF_SERVICE, CRITICAL
    }
}