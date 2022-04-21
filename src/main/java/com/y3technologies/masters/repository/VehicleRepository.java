package com.y3technologies.masters.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.y3technologies.masters.model.Vehicle;

@Repository
public interface VehicleRepository  extends BaseRepository<Vehicle>, VehicleRepositoryCustom {

	@Modifying
	@Query("update Vehicle p set p.activeInd = :status WHERE p.id = :id")
	void updateStatus(@Param("id") Long id, @Param("status") Boolean status);

	@Query("select p from Vehicle p where p.activeInd = true and p.defaultDriver.id = :defaultDriverId and p.tenantId = :tenantId order by greatest(ifnull(createdDate,0), ifnull(updatedDate,0)) desc")
	List<Vehicle> findByDefaultDriverIdAndTenantId(@Param("defaultDriverId") Long defaultDriverId, @Param("tenantId") Long tenantId);

	int countByTenantIdAndVehicleRegNumber (Long tenantId, String vehicleRegNumber);

	@Query("select v from Vehicle v where v.activeInd = true and v.vehicleRegNumber = :vehicleNo and v.tenantId = :tenantId")
	Vehicle findByVehicleNo(Long tenantId, String vehicleNo);
}
