package com.y3technologies.masters.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.y3technologies.masters.dto.PartnerTypesDto;
import com.y3technologies.masters.model.PartnerTypes;

@Repository
public interface PartnerTypesRepository extends BaseRepository<PartnerTypes> {

    @Query("select new com.y3technologies.masters.dto.PartnerTypesDto(pt.activeInd,"
            + "pt.lookup.id as partnerTypeId,pt.lookup.lookupCode as partnerType,pt.version as version)" +
            " from PartnerTypes pt where pt.partners.id = :partnerId")
    List<PartnerTypesDto> findByPartnerId(Long partnerId);

    @Query("select pt from PartnerTypes pt where pt.activeInd=true and pt.partners.id = :partnerId")
    List<PartnerTypes> getByPartnerId(Long partnerId);
}
