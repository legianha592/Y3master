package com.y3technologies.masters.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.y3technologies.masters.model.Partners;

@Repository
public interface PartnersRepository extends BaseRepository<Partners>, PartnerRepositoryCustom {

	@Modifying
	@Query("update Partners p set p.activeInd = :status WHERE p.id = :id")
	void updateStatus(@Param("id") Long id, @Param("status") Boolean status);

    @Query("Select distinct p from Partners p join p.partnerTypes pt where p.activeInd = 1 and p.tenantId = :tenantId and pt.lookup.lookupCode = :partnerType")
	List<Partners> getByTenantIdAndPartnerTypes(@Param("tenantId") Long tenantId, @Param("partnerType") String partnerType);

	Partners findByPartnerCodeAndTenantId(String partnerCode, Long tenantId);

    List<Partners> findByTenantId(@Param("tenantId") Long tenantId);

	@Query("Select distinct p from Partners p where p.id = :customerId")
    Partners getPartnersByCustomerId(@Param("customerId") Long customerId);

	@Query("select count(*) from Partners p join p.partnerTypes pt on p.id = pt.partners.id join pt.lookup l on pt.lookup.id = l.id " +
			"where l.lookupCode = :lookupCode and p.partnerName = :partnerName")
	int countByCondition(@Param("partnerName") String partnerName, @Param("lookupCode") String lookupCode);

	@Query("select count(*) from Partners p join p.partnerTypes pt on p.id = pt.partners.id join pt.lookup l on pt.lookup.id = l.id " +
			"where l.lookupCode = :lookupCode and p.partnerName = :partnerName and p.tenantId = :tenantId")
	int countByPartnerNameAndTenantId(@Param("partnerName") String partnerName, @Param("lookupCode") String lookupCode, @Param("tenantId") Long tenantId);

	@Query("select count(*) from Partners p join p.partnerTypes pt on p.id = pt.partners.id join pt.lookup l on pt.lookup.id = l.id " +
			"where l.lookupCode = :lookupCode and p.partnerName = :partnerName and p.tenantId = :tenantId and p.customerId = :customerId")
	int countByPartnerNameAndTenantIdAnAndCustomerId(@Param("partnerName") String partnerName,
													 @Param("lookupCode") String lookupCode,
													 @Param("tenantId") Long tenantId,
													 @Param("customerId") Long customerId);

	@Query("select p from Partners p join p.partnerTypes pt on p.id = pt.partners.id join pt.lookup l on pt.lookup.id = l.id " +
			"where l.lookupCode = :lookupCode and p.partnerName = :partnerName and p.tenantId = :tenantId and p.customerId = :customerId")
	Partners getByPartnerNameAndTenantIdAndCustomerId(@Param("partnerName") String partnerName,
													 @Param("lookupCode") String lookupCode,
													 @Param("tenantId") Long tenantId,
													 @Param("customerId") Long customerId);

	@Query("select p from Partners p join p.partnerTypes pt on p.id = pt.partners.id join pt.lookup l on pt.lookup.id = l.id " +
			"where l.lookupCode = :lookupCode and p.partnerName = :partnerName")
	List<Partners> findByPartnerNameAndLookupCode(@Param("partnerName") String partnerName, @Param("lookupCode") String lookupCode);

	@Query("select p from Partners p join p.partnerTypes pt on p.id = pt.partners.id join pt.lookup l on pt.lookup.id = l.id " +
			"where p.tenantId =:tenantId and l.lookupCode = :lookupCode and p.partnerName = :partnerName")
	List<Partners> findByTenantIdAndPartnerNameAndLookupCode (Long tenantId, String partnerName, String lookupCode);

	Partners findByPartnerNameAndTenantId(String partnerName, Long tenantId);

	@Query("select p from Partners p join p.partnerTypes pt on p.id = pt.partners.id join pt.lookup l on pt.lookup.id = l.id " +
			"where lower(p.partnerName) in ?1 and l.lookupCode = ?2 and p.tenantId = ?3")
	List<Partners> findByListPartnerNameAndLookupCode(Collection<String> listName, String lookupCode, Long tenantId);
}
