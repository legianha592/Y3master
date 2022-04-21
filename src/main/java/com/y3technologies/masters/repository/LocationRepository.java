package com.y3technologies.masters.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.y3technologies.masters.model.Location;

@Repository
public interface LocationRepository  extends BaseRepository<Location>{

	@Modifying
	@Query("update Location l set l.activeInd = false WHERE l.id = :id")
	void inactiveById(@Param("id") Long id);

	@Modifying
	@Query("update Location p set p.activeInd = :status WHERE p.id = :id")
	void updateStatus(@Param("id") Long id, @Param("status") Boolean status);

	@Query("select l from Location l where l.tenantId = :tenantId")
	public List<Location> findAllByTenantId(Long tenantId);

	@Query("select l from Location l where l.activeInd = 1 and l.tenantId = :tenantId and locName = :locName")
	public Location findByTenantIdLocName(Long tenantId, String locName);

	@Query("select l from Location l where l.activeInd = 1 and l.tenantId = :tenantId and locCode = :locCode")
	public Location findByTenantIdAndLocCode(Long tenantId, String locCode);

	@Query("select l from Location l where l.activeInd = 1 and locCode = :locCode")
	public List<Location> findByAllLocCode(String locCode);

	int countByTenantIdAndLocName(Long tenantId, String locName);

	@Query("select l from Location l where l.activeInd = 1 and l.id in :idList")
	public List<Location> findByIds(List<Long> idList);

	@Query("select l from Location l where l.id in :idList")
	public List<Location> findAllByIdList(Collection<Long> idList);

	@Query("select l from Location l where l.activeInd = 1 and l.tenantId = ?1 and lower(l.locName) in ?2")
	public List<Location> findByListLocName(Long tenantId, Collection<String> listLocName);
}
