package com.y3technologies.masters.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * {@code PartnerLocationConfig}
 *
 * @author Su Xia
 * @since 2019-10-31
 */
@Entity
@Table(name = "PARTNER_LOCATION")
@SequenceGenerator(name = "TABLE_SEQ", sequenceName = "SEQ_PARTNER_LOCATION", allocationSize = 1)
@Getter
@Setter
public class PartnerLocation extends BaseEntity {
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PARTNER_ID")
	private Partners partners;

	@Column(name = "LOCATION_ID")
	private Long locationId;

	@Column(name = "ADDRESS_ID")
	private Long addressId;

	@Column(name = "ADDRESS_CONTACT_ID")
	private Long addressContactId;

}
