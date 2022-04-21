package com.y3technologies.masters.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code ConfigCode}
 *
 * @author Su Xia
 * @since 2019/10/31
 */
@Entity
@Table(name = "CONFIG_CODE", uniqueConstraints = {@UniqueConstraint(name = "UNQ_CONFIG_CODE_1", columnNames = {"CODE"})})
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_CONFIG_CODE", allocationSize = 1)
@Getter
@Setter
@JsonIgnoreProperties(value = { "hibernateLazyInitializer" })
public class ConfigCode extends BaseEntity {
	
	@Column(name = "CODE")
	@NotEmpty(message = "Configuration Code {notBlank.message}")
	private String code;
	
	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "USAGE_LEVEL")
	@NotEmpty(message = "Usage level {notBlank.message}")
	private String usageLevel;

	@Column(name = "CONFIG_VALUE")
	private String configValue;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((usageLevel == null) ? 0 : usageLevel.hashCode());
		result = prime * result + ((configValue == null) ? 0 : configValue.hashCode());
		return result;
	}

}