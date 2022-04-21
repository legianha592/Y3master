package com.y3technologies.masters.service;

import com.y3technologies.masters.dto.LocationIdAndPartnerIdDto;
import com.y3technologies.masters.model.PartnerLocation;

import java.util.List;

public interface PartnerLocationService {

    void save(PartnerLocation model);

    List<PartnerLocation> createOrUpdateMultiple(List<PartnerLocation> partnerLocationList);

    List<PartnerLocation> findByLocationIdAndPartnerId(List<LocationIdAndPartnerIdDto> locationIdAndPartnerIdDtoList);

    List<PartnerLocation> findByPartnerIds(List<Long> partnerIds);
}
