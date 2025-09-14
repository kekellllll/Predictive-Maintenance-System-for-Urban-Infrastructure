package com.infrastructure.maintenance.service;

import com.infrastructure.maintenance.model.InfrastructureAsset;
import com.infrastructure.maintenance.repository.InfrastructureAssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InfrastructureAssetService {

    @Autowired
    private InfrastructureAssetRepository assetRepository;

    @Autowired
    private EventPublisher eventPublisher;

    public InfrastructureAsset createAsset(InfrastructureAsset asset) {
        asset.setStatus(InfrastructureAsset.AssetStatus.OPERATIONAL);
        InfrastructureAsset savedAsset = assetRepository.save(asset);
        
        eventPublisher.publishAlert(
                savedAsset.getAssetId(),
                "ASSET_CREATED",
                "New infrastructure asset registered: " + savedAsset.getName(),
                "INFO"
        );
        
        return savedAsset;
    }

    public Optional<InfrastructureAsset> getAssetById(Long id) {
        return assetRepository.findById(id);
    }

    public Optional<InfrastructureAsset> getAssetByAssetId(String assetId) {
        return assetRepository.findByAssetId(assetId);
    }

    public Page<InfrastructureAsset> getAllAssets(Pageable pageable) {
        return assetRepository.findAll(pageable);
    }

    public List<InfrastructureAsset> getAssetsByType(String type) {
        return assetRepository.findByType(type);
    }

    public List<InfrastructureAsset> getAssetsByStatus(InfrastructureAsset.AssetStatus status) {
        return assetRepository.findByStatus(status);
    }

    public List<InfrastructureAsset> getHighPriorityAssets(Integer priority) {
        return assetRepository.findByMaintenancePriorityGreaterThanEqual(priority);
    }

    public List<InfrastructureAsset> getAssetsNearLocation(Double latitude, Double longitude, Double radiusKm) {
        // Simple radius calculation (not accurate for large distances)
        Double radiusSquared = Math.pow(radiusKm / 111.0, 2); // Approximate conversion
        return assetRepository.findAssetsWithinRadius(latitude, longitude, radiusSquared);
    }

    public InfrastructureAsset updateAsset(Long id, InfrastructureAsset assetDetails) {
        return assetRepository.findById(id)
                .map(asset -> {
                    asset.setName(assetDetails.getName());
                    asset.setDescription(assetDetails.getDescription());
                    asset.setType(assetDetails.getType());
                    asset.setLatitude(assetDetails.getLatitude());
                    asset.setLongitude(assetDetails.getLongitude());
                    asset.setStatus(assetDetails.getStatus());
                    asset.setMaintenancePriority(assetDetails.getMaintenancePriority());
                    
                    if (assetDetails.getLastMaintenance() != null) {
                        asset.setLastMaintenance(assetDetails.getLastMaintenance());
                    }
                    
                    if (assetDetails.getNextScheduledMaintenance() != null) {
                        asset.setNextScheduledMaintenance(assetDetails.getNextScheduledMaintenance());
                    }
                    
                    return assetRepository.save(asset);
                })
                .orElse(null);
    }

    public InfrastructureAsset updateAssetStatus(String assetId, InfrastructureAsset.AssetStatus newStatus) {
        return assetRepository.findByAssetId(assetId)
                .map(asset -> {
                    InfrastructureAsset.AssetStatus oldStatus = asset.getStatus();
                    asset.setStatus(newStatus);
                    
                    InfrastructureAsset updatedAsset = assetRepository.save(asset);
                    
                    // Publish status change event
                    eventPublisher.publishAlert(
                            assetId,
                            "STATUS_CHANGE",
                            String.format("Asset status changed from %s to %s", oldStatus, newStatus),
                            newStatus == InfrastructureAsset.AssetStatus.CRITICAL ? "CRITICAL" : "INFO"
                    );
                    
                    return updatedAsset;
                })
                .orElse(null);
    }

    public InfrastructureAsset scheduleMaintenance(String assetId, LocalDateTime scheduledDate) {
        return assetRepository.findByAssetId(assetId)
                .map(asset -> {
                    asset.setNextScheduledMaintenance(scheduledDate);
                    asset.setStatus(InfrastructureAsset.AssetStatus.MAINTENANCE_REQUIRED);
                    
                    InfrastructureAsset updatedAsset = assetRepository.save(asset);
                    
                    eventPublisher.publishAlert(
                            assetId,
                            "MAINTENANCE_SCHEDULED",
                            "Maintenance scheduled for " + scheduledDate,
                            "INFO"
                    );
                    
                    return updatedAsset;
                })
                .orElse(null);
    }

    public void deleteAsset(Long id) {
        assetRepository.deleteById(id);
    }

    public Long getAssetCountByStatus(InfrastructureAsset.AssetStatus status) {
        return assetRepository.countByStatus(status);
    }
}