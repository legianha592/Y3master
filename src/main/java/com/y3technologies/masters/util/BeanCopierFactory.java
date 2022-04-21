package com.y3technologies.masters.util;

import com.y3technologies.masters.dto.*;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.PartnerAddrContactDTO;
import com.y3technologies.masters.dto.comm.PartnerAddrDTO;
import com.y3technologies.masters.dto.comm.UpdateAddrDTO;
import com.y3technologies.masters.dto.filter.PlacesFilter;
import com.y3technologies.masters.model.Location;
import com.y3technologies.masters.model.Milestone;
import com.y3technologies.masters.model.PartnerConfig;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.model.comm.AddrDTO;
import org.springframework.cglib.beans.BeanCopier;

public class BeanCopierFactory {
	public final static BeanCopier TransporterDto_updateAddressDto_copier = BeanCopier.create(TransporterDto.class, UpdateAddrDTO.class, false);
	public final static BeanCopier TransporterDto_addressContactDto_copier = BeanCopier.create(TransporterDto.class, AddrContactDTO.class, false);
	public final static BeanCopier CustomerDto_addressContactDto_copier = BeanCopier.create(CustomerDto.class, AddrContactDTO.class, false);
	public final static BeanCopier CustomerDto_partners_copier = BeanCopier.create(CustomerDto.class, Partners.class, false);
	public final static BeanCopier Partners_customerDto_copier = BeanCopier.create(Partners.class, CustomerDto.class, false);
	public final static BeanCopier Partners_transporterDto_copier = BeanCopier.create(Partners.class, TransporterDto.class, false);
	public final static BeanCopier TransporterDto_partners_copier = BeanCopier.create(TransporterDto.class, Partners.class, false);
	public final static BeanCopier PartnerConfigDto_partnerConfig_copier = BeanCopier.create(PartnerConfigDto.class, PartnerConfig.class, false);

	public final static BeanCopier ConsigneeDto_Partners_copier = BeanCopier.create(ConsigneeDto.class, Partners.class, false);
	public final static BeanCopier Partners_ConsigneeDto_copier = BeanCopier.create(Partners.class, ConsigneeDto.class, false);
	public final static BeanCopier AddrContact_PartnerAddrContactDto_copier = BeanCopier.create(AddrContact.class, PartnerAddrContactDTO.class, false);
	public final static BeanCopier AddrDto_PartnerAddrDto_copier = BeanCopier.create(AddrDTO.class, PartnerAddrDTO.class, false);
	public final static BeanCopier PartnerAddrDTO_UpdateAddrDto_copier = BeanCopier.create(PartnerAddrDTO.class, UpdateAddrDTO.class, false);
	public final static BeanCopier PartnerAddrContactDTO_UpdateAddrContactDto_copier = BeanCopier.create(PartnerAddrContactDTO.class, AddrContactDTO.class, false);
	public final static BeanCopier Location_LocationDto_copier = BeanCopier.create(Location.class, LocationDto.class, false);
	public final static BeanCopier Milestone_MilestoneDto_copier = BeanCopier.create(Milestone.class, MilestoneDTO.class, false);
	public final static BeanCopier Partners_PartnersDto_copier = BeanCopier.create(Partners.class, PartnersDto.class, false);
}
