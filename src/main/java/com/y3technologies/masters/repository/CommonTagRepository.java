package com.y3technologies.masters.repository;

import com.y3technologies.masters.model.CommonTag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommonTagRepository extends BaseRepository<CommonTag> {
    List<CommonTag> findByTagAndTagType(String tag, String tagType);

    @Query("select c from CommonTag c where c.tenantId = :tenantId and c.tag = :tag and c.tagType = :tagType")
    CommonTag findByTagTypeTagTenantId(@Param ("tag") String tag, @Param("tagType") String tagType, @Param("tenantId") Long tenantId);

    @Query("select c from CommonTag c where c.activeInd = 1 and c.tenantId in (:tenantId, 0) order by tag asc")
    List<CommonTag> findAllByTenantId(Long tenantId);

    @Query("select count(c) from CommonTag c where c.tag = :tag and c.tagType = :tagType and c.tenantId = :tenantId")
    int countByTagAndTypeAndTenantId (@Param ("tag") String tag, @Param("tagType") String tagType, @Param("tenantId") Long tenantId);
}
