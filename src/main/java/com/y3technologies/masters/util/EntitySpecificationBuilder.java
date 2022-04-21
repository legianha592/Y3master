package com.y3technologies.masters.util;


import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.y3technologies.masters.model.BaseEntity;

@SuppressWarnings("deprecation")
public class EntitySpecificationBuilder<T extends BaseEntity> {

	private final List<SearchCriteria> params;

	public EntitySpecificationBuilder() {
		params = new ArrayList<SearchCriteria>();
	}

	public EntitySpecificationBuilder<T> with(String key, String operation, Object value, String connect) {
		params.add(new SearchCriteria(key, operation, value, connect));
		return this;
	}


	public Specification<T> build() {
		if (params.size() == 0) {
			return null;
		}

		List<Specification<T>> specs = new ArrayList<Specification<T>>();
		for (SearchCriteria searchCriteria : params) {
			specs.add(new EntitySpecification<T>(searchCriteria));
		}
		Specification<T> result = specs.get(0);

		for (int i = 1; i < specs.size(); i++) {
			if ("and".equals(params.get(i).getConnect())) {
				result = Specification.where(result).and(specs.get(i));
			} else if ("or".equals(params.get(i).getConnect())) {
				result = Specification.where(result).or(specs.get(i));
			}
		}
		return result;
	}
}
