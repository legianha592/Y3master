package com.y3technologies.masters.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.jpa.domain.Specification;


public interface ProfileScopeService {
    BooleanExpression getQueryFromProfileScope(Long tenantUserId, BooleanExpression query, String profileScopeSettings, String token);

    <T> Specification<T> getQueryFromProfileScope(Long tenantUserId, Specification<T> sQuery, String profileScopeSettings);
}
