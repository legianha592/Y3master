package com.y3technologies.masters.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * ReasonCode
 *
 * @author Su Xia
 * @since 2019/11/18
 */
@Entity
@Table(name = "REASON_CODE", uniqueConstraints = {@UniqueConstraint(name = "UNQ_REASON_CODE_1", columnNames = {"TENANT_ID","REASON_CODE", "REASON_DESCRIPTION"})})
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_REASON_CODE", allocationSize = 1)
@Getter
@Setter
public class ReasonCode extends BaseEntity{

	@Column(name = "TENANT_ID")
	private Long  tenantId;
	
	@Column(name = "REASON_CODE")
	@NotEmpty(message = "{notBlank.message}")
	private String reasonCode;
	
	@Column(name = "REASON_DESCRIPTION")
	@NotEmpty(message = "{notBlank.message}")
	private String reasonDescription;
	
	//tag1|tag2|tag3
	@Column(name = "CATEGORY")
	private String category;
	
	@Transient
	@Schema(hidden=true)
	private List<String> categories;

	public List<String> getCategories(){
		List<String> list = null;
		if(StringUtils.isNotEmpty(this.category)) {
			list = List.of(this.category.split("\\|"));
		}
		return list;
	}
	
	//tag1|tag2|tag3
	@Column(name = "REASON_USAGE")
	@NotEmpty(message = "{notBlank.message}")
	private String usage;
	
	@Transient
	@Schema(hidden=true)
	private List<String> usages;

	public List<String> getUsages(){
		List<String> list = null;
		if(StringUtils.isNotEmpty(this.usage)) {
			list = List.of(this.usage.split("\\|"));
		}
		return list;
	}
	
	@Column(name = "INDUCED_BY")
	private String inducedBy;

	@Column(name = "IS_DEFAULT")
	private Boolean isDefault = Boolean.FALSE;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((inducedBy == null) ? 0 : inducedBy.hashCode());
		result = prime * result + ((reasonCode == null) ? 0 : reasonCode.hashCode());
		result = prime * result + ((reasonDescription == null) ? 0 : reasonDescription.hashCode());
		result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
		result = prime * result + ((usage == null) ? 0 : usage.hashCode());
		return result;
	}

}