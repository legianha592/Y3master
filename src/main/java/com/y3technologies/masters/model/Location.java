package com.y3technologies.masters.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Shao Hui
 * @since 2019-10-31
 */
@Entity
@Getter
@Setter
@Table(name = "LOCATION", uniqueConstraints = {@UniqueConstraint(name = "UNQ_LOCATION_LOC_NAME_TENANT_ID", columnNames = {"LOC_NAME","TENANT_ID"})})
@JsonIgnoreProperties(value = { "hibernateLazyInitializer" })
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_LOCATION", allocationSize = 1)
public class Location extends BaseEntity {

	@Column(name = "LOC_CODE")
	private String locCode;

	@Column(name = "LOC_DESC")
	private String locDesc;

	@NotEmpty(message = "{notBlank.message}")
	@Column(name = "LOC_NAME")
	private String locName;

	@Column(name = "ADDRESS_ID")
	private Long addressId;

	@Column(name = "ADDRESS_CONTACT_ID")
	private Long addressContactId;

	@Column(name = "TENANT_ID")
	private Long tenantId;

	//1|2|3
	@Column(name = "LOCATION_TAG")
	private String locationTag;

	@Column(name = "LOC_CONTACT_EMAIL")
	private String locContactEmail;

	@Column(name = "LOC_CONTACT_NAME")
	private String locContactName;

	@Column(name = "LOC_CONTACT_PHONE")
	private String locContactPhone;

	@Column(name = "LOC_CONTACT_OFFICE_NUMBER")
	private String locContactOfficeNumber;

	@Column(name = "MULTI_PARTNER_ADDRESSES")
	private String multiPartnerAddresses;

	@Transient
	@Schema(hidden=true)
	private List<Long> locationTagIds;

	public List<Long> getlocationTagIds(){
		List<Long> list = null;
		if(StringUtils.isNotEmpty(this.locationTag)) {
			list = Stream.of(this.locationTag.split("\\|"))
					.map(Long::parseLong)
					.collect(Collectors.toList());
		}
		return list;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((locCode == null) ? 0 : locCode.hashCode());
		result = prime * result + ((locDesc == null) ? 0 : locDesc.hashCode());
		result = prime * result + ((locName == null) ? 0 : locName.hashCode());
		result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
		result = prime * result + ((addressId == null) ? 0 : addressId.hashCode());
		result = prime * result + ((addressContactId == null) ? 0 : addressContactId.hashCode());
		return result;
	}
}
