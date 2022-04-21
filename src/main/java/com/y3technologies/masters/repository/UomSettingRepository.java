package com.y3technologies.masters.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.y3technologies.masters.model.UomSetting;

@Repository
public interface UomSettingRepository extends BaseRepository<UomSetting>, UomSettingRepositoryCustom {
	
	@Modifying
	@Query("update UomSetting p set p.activeInd = false WHERE p.id = :id")
	void inactiveById(@Param("id") Long id);
	
	@Modifying
	@Query("update UomSetting p set p.activeInd = true WHERE p.id = :id")
	void activeById(@Param("id") Long id);
	
	@Modifying
	@Query("update UomSetting p set p.activeInd = :status WHERE p.id = :id")
	void updateStatus(@Param("id") Long id, @Param("status") Boolean status);

	@Query("select u from UomSetting u where u.activeInd = 1")
	public List<UomSetting> findAllActiveUOM();

	@Query("select u from UomSetting u where u.activeInd = 1 and u.uomGroup = :uomGroup")
	public List<UomSetting> findAllByUomGroup(String uomGroup);

	public List<UomSetting> findByUomAndUomGroup (String uom, String uomGroup);
}
