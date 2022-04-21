package com.y3technologies.masters.repository;


import com.y3technologies.masters.model.Partners;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.y3technologies.masters.model.Driver;

import java.util.Collection;
import java.util.List;

@Repository
public interface DriverRepository  extends BaseRepository<Driver>, DriverRepositoryCustom{

	@Modifying
	@Query("update Driver p set p.activeInd = :status WHERE p.id = :id")
	void updateStatus(@Param("id") Long id, @Param("status") Boolean status);

	@Query("select d from Driver d where d.activeInd = true and d.email = :email and d.tenantId = :tenantId")
	Driver findByUserEmailAndTenantId(@Param("email") String email, @Param("tenantId") Long tenantId);

	List<Driver> findByName(String name);

	@Query("select d from Driver d where d.activeInd = true and lower(d.name) in ?1 and d.tenantId = ?2")
	List<Driver> findByListName(Collection<String> listName, Long tenantId);

	@Query("select count (d) from Driver d where d.tenantId = :tenantId and d.activeInd = 1")
	Long countAllByTenantId(@Param("tenantId") Long tenantId);
}
