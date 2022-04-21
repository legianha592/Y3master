package com.y3technologies.masters.repository;

import com.y3technologies.masters.dto.PartnerTypesDto;
import com.y3technologies.masters.model.PartnerLocation;
import com.y3technologies.masters.model.PartnerTypes;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerLocationRepository extends BaseRepository<PartnerLocation> {

    @Query("select pl from PartnerLocation pl where pl.activeInd=true and pl.partners.id = :partnerId")
    List<PartnerLocation> findByPartnerId(Long partnerId);

    @Query("select pl from PartnerLocation pl where  pl.partners.id in :ids")
    List<PartnerLocation> findByPartnersIds(@Param("ids") List<Long> ids);

}
