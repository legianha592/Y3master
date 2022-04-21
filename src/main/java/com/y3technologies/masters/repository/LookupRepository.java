package com.y3technologies.masters.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.y3technologies.masters.model.Lookup;

@Repository
public interface LookupRepository extends BaseRepository<Lookup> {

    @Query("select l from Lookup l where l.activeInd = 1 and l.tenantId in (:tenantId, 0) order by seq asc")
    List<Lookup> findAllByTenantId(Long tenantId);

    Lookup findByLookupTypeAndLookupCode(String lookupType, String lookupCode);

    @Query("select l from Lookup l where l.tenantId = :tenantId and lookupCode = :lookupCode and lookupType = :lookupType")
    Lookup findByLookupTypeAndLookupCodeAndTenantId(@Param("lookupType") String lookupType, @Param("lookupCode") String lookupCode, @Param("tenantId") Long tenantId);

    List<Lookup> findByLookupType(String lookupType);

    @Query("select l from Lookup l where tenantId = :tenantId and lookupDescription = :lookupDescription and lookupType = :lookupType")
    Lookup findByLookupTypeAndLookupDescriptionAndTenantId(@Param("lookupType") String lookupType, @Param("lookupDescription") String lookupDescription, @Param("tenantId") Long tenantId);

    @Query("select l from Lookup l where lookupDescription = :lookupDescription and l.tenantId = :tenantId")
    List<Lookup> findByLookupDescriptionAndTenantId(@Param("lookupDescription") String lookupDescription, @Param("tenantId") Long tenantId);

    @Query("select l from Lookup l where (l.tenantId = :tenantId or l.tenantId = 0) and lookupType = :lookupType")
    List<Lookup> findAllByLookupTypeAndTenantId(@Param("lookupType") String lookupType, @Param("tenantId") Long tenantId);

    @Query("select l from Lookup l where l.tenantId = ?1 and l.lookupType = ?2 and lower(l.lookupDescription) in ?3")
    List<Lookup> findByLookupTypeAndListDescription(Long tenantId, String lookupType, Collection<String> listLookupDescription);

    @Query("select l from Lookup l where lower(l.lookupDescription) in ?1 and (l.tenantId = ?2 or l.tenantId = 0) and l.lookupType = ?3")
    List<Lookup> findByListDescriptionAndTenantId(Collection<String> listDescription, Long tenantId, String lookupType);

    int countByTenantIdAndLookupCodeAndLookupType(Long tenantId, String lookUpCode, String lookUpType);
}
