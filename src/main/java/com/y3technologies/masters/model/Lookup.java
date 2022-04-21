package com.y3technologies.masters.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

/**
 * Equipment
 *
 * @author Su Xia
 * @since 2019/12/16
 */
@Entity
@Table(name = "LOOKUP", uniqueConstraints = {@UniqueConstraint(name = "UNQ_LOOKUP_1", columnNames = {"TENANT_ID","LOOKUP_CODE", "LOOKUP_TYPE"})})
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_LOOKUP", allocationSize = 1)
@Getter
@Setter
public class Lookup extends BaseEntity {

	@Column(name = "TENANT_ID")
	private Long tenantId;

	@Column(name = "CUSTOMER_ID")
	private Long customerId;

	@Column(name = "LOOKUP_TYPE")
	@NotEmpty(message = "{notBlank.message}")
	private String lookupType;

	@Column(name = "LOOKUP_CODE")
	private String lookupCode;

	@Column(name = "LOOKUP_DESCRIPTION")
	private String lookupDescription;

	@Column(name = "SERVICE_BEAN")
	private String serviceBean;

	@Column(name = "LANGUAGE_LABEL_CODE")
	private String languageLabelCode;

	@Column(name = "SEQ")
	private Integer seq;

}