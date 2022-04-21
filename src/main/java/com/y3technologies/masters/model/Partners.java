package com.y3technologies.masters.model;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonBackReference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Partners
 *
 * @author Su Xia
 * @since 2019/10/29
 */
@Entity
@Table(name = "PARTNERS")
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_PARTNERS", allocationSize = 1)
@Getter
@Setter
@JsonIgnoreProperties(value = { "hibernateLazyInitializer" })
public class Partners extends BaseEntity{
	
	@Column(name = "TENANT_ID")
	private Long tenantId;	
	
	@Column(name = "PARTNER_CODE")
	private String partnerCode;	
	
	@Column(name = "PARTNER_NAME")
	@NotEmpty(message = "{notBlank.message}")
	private String partnerName;
	
	@Column(name = "DESCRIPTION")
	private String description;

	@JsonBackReference
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "partners", orphanRemoval = true)
	@ToString.Exclude
	private Set<PartnerTypes> partnerTypes = new HashSet<>();

	@Size(min = 1, message = "{notBlank.message}")
	@Transient
	private List<Long> partnerTypeIds = null;

	@Column(name = "ADDRESS_ID")
	private Long addressId;

	@Column(name = "ADDRESS_CONTACT_ID")
	private Long addressContactId;

	@Column(name = "CUSTOMER_ID")
	private Long customerId;

	@Column(name = "EMAIL")
	private String email;

	@Column(name = "PHONE")
	private String phone;

	@Transient
	private String customerName;

	@Column(name = "PHONE_COUNTRY_SHORT_NAME")
	private String phoneCountryShortName;

	@Column(name = "PHONE_AREA_CODE")
	private String phoneAreaCode;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((partnerCode == null) ? 0 : partnerCode.hashCode());
		result = prime * result + ((partnerName == null) ? 0 : partnerName.hashCode());
		result = prime * result + ((partnerTypeIds == null) ? 0 : partnerTypeIds.hashCode());
		result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
		result = prime * result + ((addressId == null) ? 0 : addressId.hashCode());
		result = prime * result + ((customerId == null) ? 0 : customerId.hashCode());
		return result;
	}
	
}