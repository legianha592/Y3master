package com.y3technologies.masters.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.y3technologies.masters.dto.LocationIdAndPartnerIdDto;
import com.y3technologies.masters.model.PartnerLocation;
import com.y3technologies.masters.model.QPartnerLocation;
import com.y3technologies.masters.repository.PartnerLocationRepository;
import com.y3technologies.masters.service.OperationLogService;
import com.y3technologies.masters.service.PartnerLocationService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class PartnerLocationServiceImpl implements PartnerLocationService {

    @Autowired
    private PartnerLocationRepository partnerLocationRepository;

    @Autowired
    private OperationLogService operationLogService;

    @Override
    @Transactional
    public void save(PartnerLocation model) {
        Long id = model.getId();
        Boolean updateFlag = Boolean.TRUE;
        if(model.getId() != null) {
            PartnerLocation oldModel = partnerLocationRepository.findById(id).get();
            updateFlag = oldModel.getHashcode() != model.hashCode();
        }
        model.setHashcode(model.hashCode());
        partnerLocationRepository.save(model);
        if(updateFlag) {
            operationLogService.log(id == null, model, model.getClass().getSimpleName(), model.getId().toString());
        }
    }

    @Override
    public List<PartnerLocation> createOrUpdateMultiple(List<PartnerLocation> partnerLocationList) {
        return partnerLocationRepository.saveAll(partnerLocationList);
    }

    @Override
    public List<PartnerLocation> findByLocationIdAndPartnerId(List<LocationIdAndPartnerIdDto> locationIdAndPartnerIdDtoList) {
        if (locationIdAndPartnerIdDtoList.isEmpty()) {
            return Collections.emptyList();
        }

        QPartnerLocation partnerLocation = QPartnerLocation.partnerLocation;
        BooleanExpression query = partnerLocation.isNotNull();

        var firstElement = locationIdAndPartnerIdDtoList.get(0);
        query = query.and(partnerLocation.locationId.eq(firstElement.getLocationId())).and(partnerLocation.partners.id.eq(firstElement.getPartnerId()));

        for (var i = 1; i < locationIdAndPartnerIdDtoList.size(); i++) {
            var locationIdAndConsigneeId = locationIdAndPartnerIdDtoList.get(i);
            query = query.or(partnerLocation.isNotNull().and(partnerLocation.locationId.eq(locationIdAndConsigneeId.getLocationId())).and(partnerLocation.partners.id.eq(locationIdAndConsigneeId.getPartnerId())));
        }

        return partnerLocationRepository.findAll(query);
    }

    @Override
    public List<PartnerLocation> findByPartnerIds(List<Long> partnerIds) {
        if (CollectionUtils.isEmpty(partnerIds)) {
            return Collections.emptyList();
        }
        QPartnerLocation partnerLocation = QPartnerLocation.partnerLocation;
        BooleanExpression query = partnerLocation.isNotNull();
        query = query.and(partnerLocation.partners.id.in(partnerIds));
        return partnerLocationRepository.findAll(query);
    }
}
