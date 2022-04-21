package com.y3technologies.masters.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.y3technologies.masters.dto.DriverDto;
import com.y3technologies.masters.dto.TenantDetail;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.QDriver;
import com.y3technologies.masters.repository.DriverRepositoryCustom;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverRepositoryImpl implements DriverRepositoryCustom {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public int countByCondition(DriverDto driverDto) {
        StringBuilder queryStr = new StringBuilder("select count(d) from Driver d where 1 = 1");

        Map<String, Object> mapParameter = new HashMap<>();
        if (!StringUtils.isEmpty(driverDto.getTenantId())) {
            queryStr.append(" and d.tenantId = :tenantId " );
            mapParameter.put("tenantId",driverDto.getTenantId());
        }
        if (!StringUtils.isEmpty(driverDto.getName())) {
            queryStr.append(" and d.name = :driverName ");
            mapParameter.put("driverName",driverDto.getName());
        }
        if (!StringUtils.isEmpty(driverDto.getEmail())) {
            queryStr.append(" and d.email = :email ");
            mapParameter.put("email",driverDto.getEmail());
        }

        Integer result = null;

        try
        {
            Query query = entityManager.createQuery(queryStr.toString());
            for (Object key: mapParameter.keySet()){
                query.setParameter(key.toString(),mapParameter.get(key));
            }

            result = Integer.parseInt(query.getSingleResult().toString());
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Long> findDriverIdsByDriverName(Long tenantId, String driverName) {
        QDriver driver = QDriver.driver;
        BooleanExpression predicate = driver.isNotNull();
        predicate = predicate.and(driver.tenantId.eq(tenantId));
        predicate = predicate.and(driver.name.containsIgnoreCase(driverName));
        JPQLQuery<Partners> query = new JPAQuery<>(entityManager);
        return query.from(driver).where(predicate).select(driver.id).fetch();
    }

    @Override
    public List<TenantDetail> getListNoForTenantDTO(String sortParam) {
        StringBuilder query = new StringBuilder("select d.tenantId, count(d) from Driver d " +
                "where d.tenantId is not null and d.activeInd = true " +
                "group by d.tenantId " +
                "order by count(d) ");
        if (!StringUtils.isEmpty(sortParam)) {
            query.append(sortParam);
        }

        List<Object[]> dtoList = entityManager.createQuery(query.toString()).getResultList();
        List<TenantDetail> result = new ArrayList<>();
        dtoList.forEach(objArr -> {
            TenantDetail tenantDetail = new TenantDetail();
            tenantDetail.setTenantId(Long.parseLong(objArr[0].toString()));
            tenantDetail.setNumber(Long.parseLong(objArr[1].toString()));
            result.add(tenantDetail);
        });
        return result;
    }
}
