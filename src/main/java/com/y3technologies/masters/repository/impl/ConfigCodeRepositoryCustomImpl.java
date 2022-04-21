package com.y3technologies.masters.repository.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.y3technologies.masters.repository.ConfigCodeRepositoryCustom;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.y3technologies.masters.model.ConfigCode;

public class ConfigCodeRepositoryCustomImpl implements ConfigCodeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    //Old Method
    @Transactional
    public Page<ConfigCode> findAllConfigCodeWithPagination2(Pageable pageable, List<String> searchBy, List<String> searchValues) {
        java.util.Map<String, Object> params = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("from ConfigCode configCode ");

        if(!searchBy.isEmpty()) {
            sb.append("where ");
            int count=0;
            for (String field : searchBy) {
                if (sb.indexOf("(") == sb.length() - 1) {
                    sb.append(" or ");
                }

                if(field.equalsIgnoreCase("activeInd")) {
                    sb.append("UPPER(".concat(parseProperty(field)).concat(")"));
                    sb.append(" = :".concat(field));
                    params.put(field, Boolean.parseBoolean(searchValues.get(count)));
                }else {
                    sb.append("UPPER(".concat(parseProperty(field)).concat(")"));
                    sb.append(" like :".concat(field + count));
                    if(searchValues.get(count).toUpperCase().contains("%")){
                        params.put(field + count, searchValues.get(count).toUpperCase());
                    }else {
                        params.put(field + count, "%" + searchValues.get(count).toUpperCase() + "%");
                    }
                }
                if (count != searchValues.size() - 1) {
                    sb.append(" and ");
                }
                count++;
            }
        }

        Query query = entityManager.createQuery("select configCode " + sb.toString() + sort(pageable));
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        params.forEach(query::setParameter);

        Query queryCount = entityManager.createQuery("select count(distinct configCode.id) " + sb.toString());
        params.forEach(queryCount::setParameter);
        Long count = (Long) queryCount.getResultList().get(0);
        return new PageImpl<ConfigCode>(query.getResultList(), pageable, count);
    }

    @Override
    @Transactional
    public Page<ConfigCode> findAllConfigCodeWithPagination(Pageable pageable, Map<String, String> map) {
        java.util.Map<String, Object> params = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("from ConfigCode configCode ");

        if(!map.isEmpty()){
            sb.append("where ");
            int count=0;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
                if (sb.indexOf("(") == sb.length() - 1) {
                    sb.append(" or ");
                }
                if(entry.getKey().equalsIgnoreCase("activeInd")) {
                    sb.append("UPPER(".concat(parseProperty(entry.getKey())).concat(")"));
                    sb.append(" = :".concat(entry.getKey()));
                    params.put(entry.getKey(), Boolean.parseBoolean(entry.getValue()));
                }else {
                    sb.append("UPPER(".concat(parseProperty(entry.getKey())).concat(")"));
                    sb.append(" like :".concat(entry.getKey() + count));
                    if(entry.getValue().toUpperCase().contains("%")){
                        params.put(entry.getKey() + count, entry.getValue().toUpperCase());
                    }else {
                        params.put(entry.getKey() + count, "%" + entry.getValue().toUpperCase() + "%");
                    }
                }
                if (count != map.size() - 1) {
                    sb.append(" and ");
                }
                count++;
            }
        }

        Query query = entityManager.createQuery("select configCode " + sb.toString() + sort(pageable));
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        params.forEach(query::setParameter);

        Query queryCount = entityManager.createQuery("select count(distinct configCode.id) " + sb.toString());
        params.forEach(queryCount::setParameter);
        Long count = (Long) queryCount.getResultList().get(0);
        return new PageImpl<ConfigCode>(query.getResultList(), pageable, count);
    }
    protected String parseProperty(String property) {
        return "configCode."+property;
    }

    protected String sort(Pageable pageable) {
        if (pageable.getSort() != null) {
            StringBuilder sb = new StringBuilder(" ORDER BY ");
            for (Iterator<Sort.Order> iter = pageable.getSort().iterator(); iter.hasNext(); ) {
                Sort.Order order = iter.next();
                if (sb.toString().contains("ASC") || sb.toString().contains("DESC")) {
                    sb.append(", ");
                }
                sb.append(parseProperty(order.getProperty()));
                sb.append(order.isAscending() ? " ASC " : " DESC ");
            }
            return sb.toString();
        }
        return StringUtils.EMPTY;
    }
}
