package com.infrastructure.maintenance.controller;

import com.infrastructure.maintenance.model.InfrastructureAsset;
import com.infrastructure.maintenance.service.InfrastructureAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/infrastructure")
@CrossOrigin(origins = "*")
public class InfrastructureController {

    @Autowired
    private InfrastructureAssetService assetService;

    @PostMapping("/assets")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<InfrastructureAsset> createAsset(@Valid @RequestBody InfrastructureAsset asset) {
        InfrastructureAsset createdAsset = assetService.createAsset(asset);
        return ResponseEntity.ok(createdAsset);
    }

    @GetMapping("/assets")
    public ResponseEntity<Page<InfrastructureAsset>> getAllAssets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InfrastructureAsset> assets = assetService.getAllAssets(pageable);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/assets/{id}")
    public ResponseEntity<InfrastructureAsset> getAssetById(@PathVariable Long id) {
        return assetService.getAssetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/assets/asset-id/{assetId}")
    public ResponseEntity<InfrastructureAsset> getAssetByAssetId(@PathVariable String assetId) {
        return assetService.getAssetByAssetId(assetId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/assets/type/{type}")
    public ResponseEntity<List<InfrastructureAsset>> getAssetsByType(@PathVariable String type) {
        List<InfrastructureAsset> assets = assetService.getAssetsByType(type);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/assets/status/{status}")
    public ResponseEntity<List<InfrastructureAsset>> getAssetsByStatus(@PathVariable String status) {
        try {
            InfrastructureAsset.AssetStatus assetStatus = InfrastructureAsset.AssetStatus.valueOf(status.toUpperCase());
            List<InfrastructureAsset> assets = assetService.getAssetsByStatus(assetStatus);
            return ResponseEntity.ok(assets);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/assets/high-priority")
    public ResponseEntity<List<InfrastructureAsset>> getHighPriorityAssets(
            @RequestParam(defaultValue = "5") Integer priority) {
        List<InfrastructureAsset> assets = assetService.getHighPriorityAssets(priority);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/assets/nearby")
    public ResponseEntity<List<InfrastructureAsset>> getAssetsNearLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radiusKm) {
        List<InfrastructureAsset> assets = assetService.getAssetsNearLocation(latitude, longitude, radiusKm);
        return ResponseEntity.ok(assets);
    }

    @PutMapping("/assets/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<InfrastructureAsset> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody InfrastructureAsset assetDetails) {
        InfrastructureAsset updatedAsset = assetService.updateAsset(id, assetDetails);
        if (updatedAsset != null) {
            return ResponseEntity.ok(updatedAsset);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/assets/{assetId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<InfrastructureAsset> updateAssetStatus(
            @PathVariable String assetId,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String statusStr = statusUpdate.get("status");
            InfrastructureAsset.AssetStatus newStatus = InfrastructureAsset.AssetStatus.valueOf(statusStr.toUpperCase());
            
            InfrastructureAsset updatedAsset = assetService.updateAssetStatus(assetId, newStatus);
            if (updatedAsset != null) {
                return ResponseEntity.ok(updatedAsset);
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/assets/{assetId}/schedule-maintenance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<InfrastructureAsset> scheduleMaintenance(
            @PathVariable String assetId,
            @RequestBody Map<String, String> maintenanceRequest) {
        try {
            LocalDateTime scheduledDate = LocalDateTime.parse(maintenanceRequest.get("scheduledDate"));
            InfrastructureAsset updatedAsset = assetService.scheduleMaintenance(assetId, scheduledDate);
            
            if (updatedAsset != null) {
                return ResponseEntity.ok(updatedAsset);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/assets/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = Map.of(
                "operational", assetService.getAssetCountByStatus(InfrastructureAsset.AssetStatus.OPERATIONAL),
                "maintenance_required", assetService.getAssetCountByStatus(InfrastructureAsset.AssetStatus.MAINTENANCE_REQUIRED),
                "under_maintenance", assetService.getAssetCountByStatus(InfrastructureAsset.AssetStatus.UNDER_MAINTENANCE),
                "critical", assetService.getAssetCountByStatus(InfrastructureAsset.AssetStatus.CRITICAL),
                "out_of_service", assetService.getAssetCountByStatus(InfrastructureAsset.AssetStatus.OUT_OF_SERVICE)
        );
        return ResponseEntity.ok(stats);
    }
}