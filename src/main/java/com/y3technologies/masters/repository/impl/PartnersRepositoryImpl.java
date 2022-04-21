package com.y3technologies.masters.repository.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.DropdownPartnerDto;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.QPartnerTypes;
import com.y3technologies.masters.model.QPartners;
import com.y3technologies.masters.repository.PartnerRepositoryCustom;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Objects;

public class PartnersRepositoryImpl implements PartnerRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Partners> getAllByTenantIdAndPartnerType(Long tenantId, String partnerType) {
        StringBuilder query = new StringBuilder("select partner from Partners as partner ");
        query.append("inner join PartnerTypes as type on partner.id = type.partners.id ");
        query.append("inner join Lookup as lookup on lookup.id = type.lookup.id and lookup.lookupCode = ?1 ");
        query.append("left join Partners as customer on partner.customerId = customer.id ");
        query.append("where partner.tenantId = ?2 ");
        TypedQuery<Partners> qr = entityManager.createQuery(query.toString(), Partners.class);
        return qr.setParameter(1, partnerType).setParameter(2, tenantId).getResultList();
    }

    public List<Long> findConsigneeIdsByConsigneeName(Long tenantId, String consignee) {
        QPartners partners = QPartners.partners;
        QPartnerTypes partnerTypes = QPartnerTypes.partnerTypes;
        BooleanExpression predicate = partners.isNotNull();
        predicate = predicate.and(partners.tenantId.eq(tenantId));
        predicate = predicate.and(partners.partnerName.containsIgnoreCase(consignee));
        predicate = predicate.and(partners.id.in(JPAExpressions.select(partnerTypes.partners.id).from(partnerTypes)
                .where(partnerTypes.lookup.lookupCode.eq(AppConstants.PartnerType.CONSIGNEE))));
        JPQLQuery<Partners> query = new JPAQuery<>(entityManager);
        return query.from(partners).where(predicate).select(partners.id).fetch();
    }

    @Override
    public Page<DropdownPartnerDto> findByNameAndTypeSelectIdAndName(Long tenantId, String term, String type, Pageable pageable, BooleanExpression predicate) {
        QPartners partners = QPartners.partners;
        QPartnerTypes partnerTypes = QPartnerTypes.partnerTypes;
        if (Objects.isNull(predicate)) {
            predicate = partners.isNotNull();
        }
        if (StringUtils.isNotBlank(term)) {
            predicate = predicate.and(partners.partnerName.like("%" + term + "%"));
        }
        predicate = predicate.and(partners.tenantId.eq(tenantId));
        predicate = predicate.and(partners.id.in(JPAExpressions.select(partnerTypes.partners.id).from(partnerTypes)
                .where(partnerTypes.lookup.lookupCode.eq(type))));
        JPQLQuery<DropdownPartnerDto> query = new JPAQuery<>(entityManager);
        query.from(partners)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .select(Projections.bean(DropdownPartnerDto.class, partners.id, partners.partnerName));
        return new PageImpl<>(query.fetch(), pageable, query.fetchCount());
    }
}
