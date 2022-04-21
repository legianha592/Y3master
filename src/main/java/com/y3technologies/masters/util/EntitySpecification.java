package com.y3technologies.masters.util;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.y3technologies.masters.model.BaseEntity;

public class EntitySpecification<T extends BaseEntity> implements Specification<T> {

	private static final long serialVersionUID = 3435959967678485774L;
	
	private SearchCriteria criteria;

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		if (criteria.getOperation().equalsIgnoreCase(">")) {
			return builder.greaterThanOrEqualTo(
					root.<String>get(criteria.getKey()), criteria.getValue().toString());
//            return builder.greaterThanOrEqualTo(
//                    root.<Integer> get(criteria.getKey()), Integer.parseInt(criteria.getValue().toString()));
		} else if (criteria.getOperation().equalsIgnoreCase("<")) {
			return builder.lessThanOrEqualTo(
					root.<String>get(criteria.getKey()), criteria.getValue().toString());
		} else {
			if (criteria.getOperation().equalsIgnoreCase(":")) {
				String key = criteria.getKey();
				/*if (root.get(key).getJavaType() == Collection.class) {
                    root.fetch("commRoles", JoinType.LEFT);
                }*/
				if (key.indexOf(".") > 0) {
					String objectStr = key.substring(0, key.indexOf("."));
					String attrStr = key.substring(key.indexOf(".") + 1);
					if (root.get(objectStr).<String>get(attrStr).getJavaType() == String.class) {
						return builder.like(
								builder.upper(root.get(objectStr).<String>get(attrStr)),
								"%" + criteria.getValue().toString().toUpperCase() + "%");
					} else {
						return builder.equal(
								root.get(objectStr).<String>get(attrStr),
								criteria.getValue());
					}
				}
				if (root.get(criteria.getKey()).getJavaType() == String.class) {
					return builder.like(
							builder.upper(root.<String>get(criteria.getKey())), "%" + criteria.getValue().toString().toUpperCase() + "%");
				} else {
					return builder.equal(root.get(criteria.getKey()), criteria.getValue());
				}
			}
		}
		return null;
	}

	public SearchCriteria getCriteria() {
		return criteria;
	}

	public void setCriteria(SearchCriteria criteria) {
		this.criteria = criteria;
	}

	public EntitySpecification(SearchCriteria criteria) {
		super();
		this.criteria = criteria;
	}

	public EntitySpecification() {
		super();
	}


}
