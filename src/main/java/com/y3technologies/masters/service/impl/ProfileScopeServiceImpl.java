package com.y3technologies.masters.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.aas.ProfileScopeDTO;
import com.y3technologies.masters.filter.SecurityConstant;
import com.y3technologies.masters.model.QDriver;
import com.y3technologies.masters.model.QLocation;
import com.y3technologies.masters.model.QPartnerLocation;
import com.y3technologies.masters.model.QPartners;
import com.y3technologies.masters.model.QVehicle;
import com.y3technologies.masters.service.ProfileScopeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProfileScopeServiceImpl implements ProfileScopeService {
    @Autowired
    private AasClient aasClient;

    @Override
    public BooleanExpression getQueryFromProfileScope(Long tenantUserId, BooleanExpression query, String profileScopeSettings, String token) {
        List<ProfileScopeDTO> lstProfileScope = null;
        try {
            if (StringUtils.isEmpty(token)){
                lstProfileScope = aasClient.getProfileScopeByTenantUserId(tenantUserId);
            } else {
                lstProfileScope = aasClient.getProfileScopeByTenantUserId(tenantUserId, SecurityConstant.TOKEN_PREFIX +  token);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        if (CollectionUtils.isEmpty(lstProfileScope)) {
            return query;
        }
        switch (profileScopeSettings) {
            case AppConstants.ProfileScopeSettings.LOCATION:
                return this.getLocationProfileScope(lstProfileScope, query);
            case AppConstants.ProfileScopeSettings.DRIVER:
                return this.getDriverProfileScope(lstProfileScope, query);
            case AppConstants.ProfileScopeSettings.VEHICLE:
                return this.getVehicleProfileScope(lstProfileScope, query);
            case AppConstants.ProfileScopeSettings.CUSTOMER:
                return this.getCustomerProfileScope(lstProfileScope, query);
            case AppConstants.ProfileScopeSettings.TRANSPORTER:
                return this.getTransporterProfileScope(lstProfileScope, query);
            case AppConstants.ProfileScopeSettings.CONSIGNEE:
                return this.getConsigneeProfileScope(lstProfileScope, query);
            case AppConstants.ProfileScopeSettings.CONSIGNEE_PLACE:
                return this.getConsigneePlaceProfileScope(lstProfileScope, query);
            default:
                return query;
        }
    }

    @Override
    public <T> Specification<T> getQueryFromProfileScope(Long tenantUserId, Specification<T> sQuery, String profileScopeSettings) {

        List<ProfileScopeDTO> lstProfileScope = aasClient.getProfileScopeByTenantUserId(tenantUserId);
        if (lstProfileScope.isEmpty()) {
            return sQuery;
        }
        switch (profileScopeSettings) {
            case AppConstants.ProfileScopeSettings.VEHICLE:
                return this.getVehicleProfileScope(lstProfileScope, sQuery);
            case AppConstants.ProfileScopeSettings.DRIVER:
                return this.getDriverProfileScope(lstProfileScope, sQuery);
            default:
                return sQuery;
        }
    }

    private BooleanExpression getLocationProfileScope(List<ProfileScopeDTO> lstProfileScope, BooleanExpression query) {
        List<Long> locationIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.LOCATION);
        if (!locationIds.isEmpty()) {
            QLocation location = QLocation.location;
            query = query.and(location.id.in(locationIds));
        }
        return query;
    }

    private BooleanExpression getDriverProfileScope(List<ProfileScopeDTO> lstProfileScope, BooleanExpression query) {
        List<Long> customerIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.CUSTOMER);
        List<Long> transporterIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.TRANSPORTER);
        QDriver qDriver = QDriver.driver;
        if (!customerIds.isEmpty()) {
            query = query.and(qDriver.partners.customerId.in(customerIds));
        }
        if (!transporterIds.isEmpty()) {
            query = query.and(qDriver.partners.id.in(transporterIds));
        }
        return query;
    }

    private BooleanExpression getVehicleProfileScope(List<ProfileScopeDTO> lstProfileScope, BooleanExpression query) {
        List<Long> customerIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.CUSTOMER);
        List<Long> transporterIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.TRANSPORTER);
        QVehicle qVehicle = QVehicle.vehicle;
        if (!customerIds.isEmpty()) {
            query = query.and(qVehicle.partners.customerId.in(customerIds));
        }
        if (!transporterIds.isEmpty()) {
            query = query.and(qVehicle.partners.id.in(transporterIds));
        }
        return query;
    }

    private <T> Specification<T> getDriverProfileScope(List<ProfileScopeDTO> lstProfileScope, Specification<T> sQuery) {
        final String partnersFieldName = "partners";
        List<Long> customerIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.CUSTOMER);
        List<Long> transporterIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.TRANSPORTER);
        Specification<T> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!customerIds.isEmpty()) {
                predicates.add(root.get(partnersFieldName).get("customerId").in(customerIds));
            }
            if (!transporterIds.isEmpty()) {
                predicates.add(root.get(partnersFieldName).get("id").in(transporterIds));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        return sQuery.and(specification);
    }

    private <T> Specification<T> getVehicleProfileScope(List<ProfileScopeDTO> lstProfileScope, Specification<T> sQuery) {
        final String partnersFieldName = "partners";
        List<Long> customerIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.CUSTOMER);
        List<Long> transporterIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.TRANSPORTER);
        Specification<T> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!customerIds.isEmpty()) {
                predicates.add(root.get(partnersFieldName).get("customerId").in(customerIds));
            }
            if (!transporterIds.isEmpty()) {
                predicates.add(root.get(partnersFieldName).get("id").in(transporterIds));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        return sQuery.and(specification);
    }

    private BooleanExpression getCustomerProfileScope(List<ProfileScopeDTO> lstProfileScope, BooleanExpression query) {
        List<Long> customerIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.CUSTOMER);
        if (!customerIds.isEmpty()) {
            QPartners partners = QPartners.partners;
            query = query.and(partners.id.in(customerIds));
        }
        return query;
    }

    private BooleanExpression getTransporterProfileScope(List<ProfileScopeDTO> lstProfileScope, BooleanExpression query) {
        List<Long> customerIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.CUSTOMER);
        List<Long> transporterIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.TRANSPORTER);
        QPartners partners = QPartners.partners;
        if (!customerIds.isEmpty()) {
            query = query.and(partners.customerId.in(customerIds));
        }
        if (!transporterIds.isEmpty()) {
            query = query.and(partners.id.in(transporterIds));
        }
        return query;
    }

    private BooleanExpression getConsigneeProfileScope(List<ProfileScopeDTO> lstProfileScope, BooleanExpression query) {
        List<Long> customerIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.CUSTOMER);
        List<Long> consigneeIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.CONSIGNEE);
        QPartners partners = QPartners.partners;
        if (!customerIds.isEmpty()) {
            query = query.and(partners.customerId.in(customerIds));
        }
        if (!consigneeIds.isEmpty()) {
            query = query.and(partners.id.in(consigneeIds));
        }
        return query;
    }

    private BooleanExpression getConsigneePlaceProfileScope(List<ProfileScopeDTO> lstProfileScope, BooleanExpression query) {
        List<Long> customerIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.CUSTOMER);
        List<Long> consigneeIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.CONSIGNEE);
        List<Long> locationIds = this.getRefIdsByProfileCode(lstProfileScope, AppConstants.ProfileCode.LOCATION);
        QPartnerLocation qPartnerLocation = QPartnerLocation.partnerLocation;
        if (!customerIds.isEmpty()) {
            query = query.and(qPartnerLocation.partners.customerId.in(customerIds));
        }
        if (!consigneeIds.isEmpty()) {
            query = query.and(qPartnerLocation.partners.id.in(consigneeIds));
        }
        if (!locationIds.isEmpty()) {
            query = query.and(qPartnerLocation.locationId.in(locationIds));
        }
        return query;
    }

    private List<Long> getRefIdsByProfileCode(List<ProfileScopeDTO> lstProfileScope, String profileCode) {
        return lstProfileScope.stream()
                .filter(el -> el.getProfileCode().equalsIgnoreCase(profileCode))
                .map(ProfileScopeDTO::getRefId).collect(Collectors.toList());
    }
}
