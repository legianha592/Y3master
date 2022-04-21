package com.y3technologies.masters.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.y3technologies.masters.dto.PartnerConfigDto;
import com.y3technologies.masters.model.PartnerConfig;

@Repository
public interface PartnerConfigRepository extends BaseRepository<PartnerConfig> {

	@Modifying
	@Query("update PartnerConfig pc set pc.activeInd = false WHERE pc.id = :id")
	void inactiveById(@Param("id") Long id);

	@Query("select new com.y3technologies.masters.dto.PartnerConfigDto(pc.id,pc.activeInd,pc.value,pc.description,pc.configCode.id as configCodeId,"
			+ "pc.configCode.code as configCode,pc.configCode.description as configDescription,pc.partners.id as partnerId,pc.partners.partnerCode,pc.partners.description as partnerDescription,pc.version as version)" +
			" from PartnerConfig pc where pc.partners.id = :partnerId")
	List<PartnerConfigDto> findByPartnerId(Long partnerId);

	@Modifying
	@Query("update PartnerConfig p set p.activeInd = :status WHERE p.id = :id")
	void updateStatus(@Param("id") Long id, @Param("status") Boolean status);
	
	@Query("select pc from PartnerConfig pc where pc.activeInd=true and pc.partners.id = :partnerId")
	List<PartnerConfig> getByPartnerId(Long partnerId);
}
