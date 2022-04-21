package com.y3technologies.masters.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Shao Hui
 * @since 2019-10-31
 */
@Entity
@Getter
@Setter
@Table(name = "DRIVER", uniqueConstraints = {@UniqueConstraint(name = "UNQ_DRIVER_NAME_TENANT_ID", columnNames = {"NAME","TENANT_ID"}),
		@UniqueConstraint(name = "UNQ_DRIVER_EMAIL_TENANT_ID", columnNames = {"EMAIL","TENANT_ID"})})
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_DRIVER", allocationSize = 1)
@JsonIgnoreProperties(value = { "hibernateLazyInitializer" })
public class Driver extends BaseEntity {
	
	@NotEmpty(message = "{notBlank.message}")
	@Column(name = "NAME")
	private String name;

	@Column(name = "AVAILABLE")
	private Boolean available;

	@NotEmpty(message = "{notBlank.message}")
	@Column(name = "LICENCE_TYPE")
	private String licenceType;

	@NotEmpty(message = "{notBlank.message}")
	@Column(name = "LICENCE_NUMBER")
	private String licenceNumber;

	@Column(name = "TENANT_ID")
	private Long tenantId;

	@Column(name = "ADDRESS_CONTACT_ID")
	private Long addressContactId;
	
	@Column(name = "EMAIL")
	private String email;

	@OneToOne
	@JoinColumn(name = "TRANSPORTER_ID")
	@ToString.Exclude
	private Partners partners;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((available == null) ? 0 : available.hashCode());
		result = prime * result + ((licenceNumber == null) ? 0 : licenceNumber.hashCode());
		result = prime * result + ((licenceType == null) ? 0 : licenceType.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
		return result;
	}
	
}
