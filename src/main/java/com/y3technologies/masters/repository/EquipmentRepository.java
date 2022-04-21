package com.y3technologies.masters.repository;

import com.y3technologies.masters.dto.table.EquipmentTableDto;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.y3technologies.masters.model.Equipment;

import java.util.List;

@Repository
public interface EquipmentRepository extends BaseRepository<Equipment> {

	@Modifying
	@Query("update Equipment p set p.activeInd = false WHERE p.id = :id")
	void inactiveById(@Param("id") Long id);
	
	@Modifying
	@Query("update Equipment p set p.activeInd = :status WHERE p.id = :id")
	void updateStatus(@Param("id") Long id, @Param("status") Boolean status);

	List<Equipment> findByTenantId(@Param("tenantId") Long tenantId);

	int countByTenantIdAndUnitAidc1(Long tenantId, String unitAIDC1);
}
