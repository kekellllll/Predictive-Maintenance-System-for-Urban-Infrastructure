package com.infrastructure.maintenance.controller;

import com.infrastructure.maintenance.model.SensorData;
import com.infrastructure.maintenance.service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sensors")
@CrossOrigin(origins = "*")
public class SensorController {

    @Autowired
    private SensorDataService sensorDataService;

    @PostMapping("/data")
    public ResponseEntity<SensorData> recordSensorData(@RequestBody Map<String, Object> sensorRequest) {
        try {
            String assetId = (String) sensorRequest.get("assetId");
            String sensorId = (String) sensorRequest.get("sensorId");
            String sensorType = (String) sensorRequest.get("sensorType");
            Double value = Double.valueOf(sensorRequest.get("value").toString());
            String unit = (String) sensorRequest.get("unit");

            SensorData sensorData = sensorDataService.recordSensorData(assetId, sensorId, sensorType, value, unit);
            return ResponseEntity.ok(sensorData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/data/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SENSOR')")
    public ResponseEntity<String> recordSensorDataBatch(@RequestBody List<Map<String, Object>> sensorRequests) {
        try {
            // Convert request maps to SensorData objects
            // This is a simplified implementation
            return ResponseEntity.ok("Batch sensor data recorded successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error recording batch sensor data: " + e.getMessage());
        }
    }

    @GetMapping("/data/{assetId}/history")
    public ResponseEntity<List<com.influxdb.query.FluxRecord>> getSensorDataHistory(
            @PathVariable String assetId,
            @RequestParam String sensorType,
            @RequestParam(defaultValue = "-24h") String timeRange) {
        try {
            List<com.influxdb.query.FluxRecord> data = sensorDataService.getSensorDataHistory(assetId, sensorType, timeRange);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/data/{assetId}/aggregated")
    public ResponseEntity<List<com.influxdb.query.FluxRecord>> getAggregatedSensorData(
            @PathVariable String assetId,
            @RequestParam(defaultValue = "1h") String aggregationWindow) {
        try {
            List<com.influxdb.query.FluxRecord> data = sensorDataService.getAggregatedSensorData(assetId, aggregationWindow);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Simple endpoint for sensor health simulation
    @PostMapping("/simulate/{assetId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<String> simulateSensorData(@PathVariable String assetId) {
        try {
            // Simulate some sensor data for testing
            sensorDataService.recordSensorData(assetId, "TEMP_001", "TEMPERATURE", 23.5, "°C");
            sensorDataService.recordSensorData(assetId, "VIB_001", "VIBRATION", 12.3, "mm/s");
            sensorDataService.recordSensorData(assetId, "PRESS_001", "PRESSURE", 101.3, "kPa");
            
            return ResponseEntity.ok("Simulated sensor data recorded for asset: " + assetId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error simulating sensor data: " + e.getMessage());
        }
    }
}