package com.y3technologies.masters.dto.filter;

import com.y3technologies.masters.constants.AppConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.springframework.data.domain.Sort.Direction.fromString;

@Setter
@Getter
@NoArgsConstructor
public class ListingFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private int pageSize = 20;
	private int pageNo =0;
	protected String sortBy;
	private String sortField;
	private String sortDirection;

	public List<Sort.Order> getSort() {
		List<Sort.Order> orders = new ArrayList<>();
		if (StringUtils.isBlank(this.sortBy)) {
			this.sortField = AppConstants.CommonPropertyName.CREATED_DATE;
			this.sortDirection = AppConstants.CommonSortDirection.DESCENDING;
			orders.add(new Sort.Order(fromString(this.sortDirection), this.sortField));
			return orders;
		}
		String[] sorts = sortBy.split("\\|");
		for (String sort : sorts) {
			if (StringUtils.isBlank(sort)) {
				break;
			}
			String[] sortFieldAndDirection = sort.split(",");
			boolean emptyElement = Arrays.stream(sortFieldAndDirection).anyMatch(StringUtils::isBlank);
			if (Objects.equals(sortFieldAndDirection.length, 2) && !emptyElement) {
				this.sortField = sortFieldAndDirection[0];
				this.parseSortDirection(sortFieldAndDirection[1]);
				orders.add(new Sort.Order(fromString(this.sortDirection), this.sortField));
			}
		}
		if (orders.isEmpty()) {
			this.sortField = AppConstants.CommonPropertyName.CREATED_DATE;
			this.sortDirection = AppConstants.CommonSortDirection.DESCENDING;
			orders.add(new Sort.Order(fromString(this.sortDirection), this.sortField));
			return orders;
		}
		this.sortField = AppConstants.CommonPropertyName.ID;
		this.sortDirection = AppConstants.CommonSortDirection.DESCENDING;
		orders.add(new Sort.Order(fromString(this.sortDirection), this.sortField));
		return orders;
	}

	private void parseSortDirection(String sortRequest) {
		if (sortRequest.equalsIgnoreCase(AppConstants.CommonSortDirection.ASCENDING)) {
			this.sortDirection = AppConstants.CommonSortDirection.ASCENDING;
			return;
		}
		this.sortDirection = AppConstants.CommonSortDirection.DESCENDING;
	}
}
