package com.infrastructure.maintenance.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String sensorId;

    @NotBlank
    private String sensorType; // TEMPERATURE, VIBRATION, PRESSURE, HUMIDITY, STRAIN, etc.

    @NotNull
    private Double value;

    private String unit;

    @NotNull
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private InfrastructureAsset asset;

    @Column(name = "quality_score")
    private Double qualityScore = 1.0; // Data quality indicator (0.0 - 1.0)

    private String metadata; // JSON string for additional sensor metadata

    // Constructors
    public SensorData() {}

    public SensorData(String sensorId, String sensorType, Double value, 
                     String unit, InfrastructureAsset asset) {
        this.sensorId = sensorId;
        this.sensorType = sensorType;
        this.value = value;
        this.unit = unit;
        this.asset = asset;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public String getSensorType() { return sensorType; }
    public void setSensorType(String sensorType) { this.sensorType = sensorType; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public InfrastructureAsset getAsset() { return asset; }
    public void setAsset(InfrastructureAsset asset) { this.asset = asset; }

    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}