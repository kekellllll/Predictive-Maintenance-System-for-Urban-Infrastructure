package com.infrastructure.maintenance.repository;

import com.infrastructure.maintenance.model.InfrastructureAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InfrastructureAssetRepository extends JpaRepository<InfrastructureAsset, Long> {

    Optional<InfrastructureAsset> findByAssetId(String assetId);

    List<InfrastructureAsset> findByType(String type);

    List<InfrastructureAsset> findByStatus(InfrastructureAsset.AssetStatus status);

    @Query("SELECT a FROM InfrastructureAsset a WHERE a.maintenancePriority >= :priority ORDER BY a.maintenancePriority DESC")
    List<InfrastructureAsset> findByMaintenancePriorityGreaterThanEqual(@Param("priority") Integer priority);

    @Query("SELECT a FROM InfrastructureAsset a WHERE " +
           "(:lat1 - a.latitude) * (:lat1 - a.latitude) + " +
           "(:lon1 - a.longitude) * (:lon1 - a.longitude) <= :radiusSquared")
    List<InfrastructureAsset> findAssetsWithinRadius(@Param("lat1") Double latitude, 
                                                     @Param("lon1") Double longitude, 
                                                     @Param("radiusSquared") Double radiusSquared);

    @Query("SELECT COUNT(a) FROM InfrastructureAsset a WHERE a.status = :status")
    Long countByStatus(@Param("status") InfrastructureAsset.AssetStatus status);
}