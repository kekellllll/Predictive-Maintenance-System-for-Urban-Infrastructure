package com.infrastructure.maintenance.controller;

import com.infrastructure.maintenance.model.MaintenancePrediction;
import com.infrastructure.maintenance.service.PredictionEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@CrossOrigin(origins = "*")
public class PredictionController {

    @Autowired
    private PredictionEngineService predictionEngineService;

    @PostMapping("/trigger/{assetId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<String> triggerPrediction(@PathVariable String assetId) {
        try {
            predictionEngineService.triggerPredictionForAsset(assetId);
            return ResponseEntity.ok("Prediction triggered for asset: " + assetId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error triggering prediction: " + e.getMessage());
        }
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<MaintenancePrediction>> getPredictionsForAsset(@PathVariable String assetId) {
        List<MaintenancePrediction> predictions = predictionEngineService.getPredictionsForAsset(assetId);
        if (predictions != null) {
            return ResponseEntity.ok(predictions);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/high-risk")
    public ResponseEntity<List<MaintenancePrediction>> getHighRiskPredictions() {
        List<MaintenancePrediction> predictions = predictionEngineService.getHighRiskPredictions();
        return ResponseEntity.ok(predictions);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<MaintenancePrediction>> getPredictionsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<MaintenancePrediction> predictions = predictionEngineService.getPredictionsInDateRange(startDate, endDate);
        return ResponseEntity.ok(predictions);
    }
}