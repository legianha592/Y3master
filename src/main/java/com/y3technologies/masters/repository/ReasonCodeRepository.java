package com.y3technologies.masters.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.y3technologies.masters.model.ReasonCode;

import java.util.List;

@Repository
public interface ReasonCodeRepository extends BaseRepository<ReasonCode> {
	
	@Modifying
	@Query("update ReasonCode p set p.activeInd = false WHERE p.id = :id")
	void inactiveById(@Param("id") Long id);
	
	@Modifying
	@Query("update ReasonCode p set p.activeInd = true WHERE p.id = :id")
	void activeById(@Param("id") Long id);
	
	@Modifying
	@Query("update ReasonCode p set p.activeInd = :status WHERE p.id = :id")
	void updateStatus(@Param("id") Long id, @Param("status") Boolean status);

	int countByReasonCodeAndReasonDescriptionAndTenantId(@Param("reasonCode") String reasonCode,
														 @Param("reasonDescription") String reasonDescription, @Param("tenantId") Long tenantId);

	List<ReasonCode> findByTenantId(@Param("tenantId") Long tenantId);
}
