package com.y3technologies.masters.repository.impl;

import com.y3technologies.masters.model.UomSetting;
import com.y3technologies.masters.repository.UomSettingRepositoryCustom;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class UomSettingRepositoryImpl extends SimpleJpaRepository<UomSetting, Long> implements UomSettingRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    public UomSettingRepositoryImpl(EntityManager entityManager) {
        super(UomSetting.class, entityManager);
    }

    @Override
    public int countByCondition(String uom, String uomGroup) {
        StringBuilder query = new StringBuilder("select count(u) from UomSetting u where 1 = 1");
        if (!StringUtils.isEmpty(uom)) {
            query.append(" and u.uom = '" + uom+ "'");
        }
        if (!StringUtils.isEmpty(uomGroup)) {
            query.append(" and replace(u.uomGroup,'\r\n','') = '" + uomGroup + "'");
        }
        return Integer.parseInt(entityManager.createQuery(query.toString()).getSingleResult().toString());
    }
}
