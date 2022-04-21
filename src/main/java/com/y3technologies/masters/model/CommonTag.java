package com.y3technologies.masters.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code CommonTag}
 *
 * @author Su Xia
 * @since 2019/11/18
 */
@Entity
@Table(name = "COMMON_TAG")
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_COMMON_TAG", allocationSize = 1)
@Getter
@Setter
public class CommonTag extends BaseEntity {
	
	@Column(name = "TAG_TYPE")
	private String tagType;
	
	@Column(name = "TAG")
	private String tag;
	
	@Column(name = "TENANT_ID")
	private Long tenantId;
	
	@Column(name = "REFERENCE_FUNCTION")
	private String referenceFunction;

	@Column(name = "REFERENCE_ID")
	private Long referenceId;
	
}