package com.y3technologies.masters.service.impl;

import com.y3technologies.masters.service.OperationLogService;
import com.y3technologies.masters.service.PartnerTypesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.y3technologies.masters.model.PartnerTypes;
import com.y3technologies.masters.repository.PartnerTypesRepository;

@Service
public class PartnerTypesServiceImpl implements PartnerTypesService {

    @Autowired
    private PartnerTypesRepository partnerTypesRepository;

    @Autowired
    private OperationLogService operationLogService;

    @Override
    @Transactional
    public void save(PartnerTypes model) {
        Long id = model.getId();
        Boolean updateFlag = Boolean.TRUE;
        if(model.getId() != null) {
            PartnerTypes oldModel = partnerTypesRepository.findById(id).get();
            updateFlag = oldModel.getHashcode() != model.hashCode();
        }
        model.setHashcode(model.hashCode());
        partnerTypesRepository.save(model);
        if(updateFlag) {
            operationLogService.log(id == null, model, model.getClass().getSimpleName(), model.getId().toString());
        }
    }
}