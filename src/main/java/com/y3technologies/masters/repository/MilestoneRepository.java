package com.y3technologies.masters.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.y3technologies.masters.model.Milestone;

import java.util.List;

@Repository
public interface MilestoneRepository extends BaseRepository<Milestone> {

	@Modifying
	@Query("update Milestone p set p.activeInd = false WHERE p.id = :id")
	void inactiveById(@Param("id") Long id);
	
	@Modifying
	@Query("update Milestone p set p.activeInd = :status WHERE p.id = :id")
	void updateStatus(@Param("id") Long id, @Param("status") Boolean status);

	@Query("select m from Milestone m where m.activeInd = 1 and m.tenantId = :tenantId and m.milestoneCode = :milestoneCode and milestoneStatus = :milestoneStatus")
	Milestone findByMilestoneCodeAndMilestoneStatusAndTenantId(@Param("milestoneCode") String milestoneCode, @Param("milestoneStatus") String milestoneStatus, @Param("tenantId") Long tenantId);

	int countByMilestoneCodeAndTenantId(@Param("milestoneCode") String milestoneCode, @Param("tenantId") Long tenantId);

	List<Milestone> findByTenantId(@Param("tenantId") Long tenantId);
}
