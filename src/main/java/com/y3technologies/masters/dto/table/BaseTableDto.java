package com.y3technologies.masters.dto.table;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Shao Hui
 * @since 2020-02-10
 */
@Getter
@Setter
public class BaseTableDto {

	protected Long id;

	protected Boolean activeInd;

	private Long tenantId;

	protected int excelRowPosition;

}
