package com.y3technologies.masters.model;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code PartnerConfig}
 *
 * @author Su Xia
 * @since 2019-10-31
 */
@Entity
@Table(name = "PARTNER_CONFIG", uniqueConstraints = {@UniqueConstraint(name = "UNQ_PARTNER_CONFIG", columnNames = {"PARTNER_ID","CONFIG_CODE_ID"})})
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_PARTNER_CONFIG", allocationSize = 1)
@Getter
@Setter
public class PartnerConfig extends BaseEntity {
	
	@Column(name = "VALUE")
	@NotEmpty(message = "{notBlank.message}")
	private String value;
	
	@Column(name = "DESCRIPTION")
	private String description;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CONFIG_CODE_ID")
	private ConfigCode configCode;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PARTNER_ID")
	private Partners partners;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configCode == null||configCode.getId()==null) ? 0 : configCode.getId().hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((partners == null||partners.getId()==null) ? 0 : partners.getId().hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

}
