package com.y3technologies.masters.dto.table;

import com.y3technologies.masters.dto.BaseDto;
import com.y3technologies.masters.dto.ConsigneeDto;
import com.y3technologies.masters.dto.ConsigneePlaceDto;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.UpdateAddrDTO;
import com.y3technologies.masters.model.Location;
import com.y3technologies.masters.model.PartnerLocation;
import com.y3technologies.masters.model.Partners;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExcelConsigneeDto extends BaseDto {
    private Long tenantId;

    @NotNull(message = "partnerCode {notBlank.message}")
    private String partnerName;

    private String partnerCode;

    @NotNull(message = "customerId {notBlank.message}")
    private Long customerId;

    private String customerName;

    private String locationName;

    private String country;

    private String state;

    private String city;

    private String postalCode;

    private String unitNo;

    private String addressLine1;

    private String addressLine2;

    private String name;

    private String mobileCountryCode;

    private String mobileCountryNo;

    private String email;

    private String placesStatus;

    public ConsigneeDto toConsigneeDto() {
        ConsigneeDto consigneeDto = new ConsigneeDto();
        consigneeDto.setPartnerCode(this.partnerCode);
        consigneeDto.setCustomerId(this.customerId);
        consigneeDto.setCustomerName(this.customerName);
        consigneeDto.setTenantId(this.tenantId);
        consigneeDto.setPartnerName(this.partnerName);
        consigneeDto.setActiveInd(this.activeInd);
        return consigneeDto;
    }

    public UpdateAddrDTO toUpdateAddrDTO(String countryShortName) {
        UpdateAddrDTO updateAddrDTO = new UpdateAddrDTO();
        updateAddrDTO.setCity(this.city);
        updateAddrDTO.setState(this.state);
        updateAddrDTO.setStreet(this.addressLine1);
        updateAddrDTO.setStreet2(this.addressLine2);
        updateAddrDTO.setUnit(this.unitNo);
        updateAddrDTO.setZipCode(this.postalCode);
        updateAddrDTO.setCountryShortName(countryShortName);
        return updateAddrDTO;
    }

    public AddrContactDTO toAddrContactDTO() {
        AddrContactDTO addrContactDTO = new AddrContactDTO();
        addrContactDTO.setEmail(this.email);
        addrContactDTO.setMobileNumber1(this.mobileCountryNo);
        addrContactDTO.setPerson(this.name);
        return addrContactDTO;
    }

    public Location toLocation(Long addressId, Long addressContactId, Long tenantId) {
        Location location = new Location();
        location.setLocName(this.locationName);
        location.setLocCode(this.locationName);
        location.setLocContactEmail(this.email);
        location.setLocContactName(this.name);
        location.setLocContactPhone(this.mobileCountryNo);
        location.setAddressId(addressId);
        location.setAddressContactId(addressContactId);
        location.setTenantId(tenantId);
        return location;
    }

    public PartnerLocation toPartnerLocation(Partners consignee, Location location, Long addrContactId) {
        PartnerLocation partnerLocation = new PartnerLocation();
        partnerLocation.setAddressContactId(addrContactId);
        partnerLocation.setPartners(consignee);
        partnerLocation.setAddressId(location.getAddressId());
        partnerLocation.setLocationId(location.getId());
        partnerLocation.setActiveInd("active".equalsIgnoreCase(this.placesStatus.toLowerCase().trim()));
        return partnerLocation;
    }

    public ExcelConsigneeDto(Partners consignee, ConsigneePlaceDto consigneePlaceDto) {
        this.id = consignee.getId();
        this.tenantId = consignee.getTenantId();
        this.partnerCode = consignee.getPartnerCode();
        this.partnerName = consignee.getPartnerName();
        this.customerId = consignee.getCustomerId();
        this.customerName = consignee.getCustomerName();
        this.activeInd = consignee.getActiveInd();
        if (Objects.nonNull(consigneePlaceDto)) {
            if (Objects.nonNull(consigneePlaceDto.getPartnerAddr())) {
                if (Objects.nonNull(consigneePlaceDto.getPartnerAddr().getCountry())) {
                    this.country = consigneePlaceDto.getPartnerAddr().getCountry().getCountryFullName();
                    this.mobileCountryCode = consigneePlaceDto.getPartnerAddr().getCountry().getCountryIsdCode();
                }
                this.state = consigneePlaceDto.getPartnerAddr().getState();
                this.city = consigneePlaceDto.getPartnerAddr().getCity();
                this.postalCode = consigneePlaceDto.getPartnerAddr().getZipCode();
                this.unitNo = consigneePlaceDto.getPartnerAddr().getUnit();
                this.addressLine1 = consigneePlaceDto.getPartnerAddr().getStreet();
                this.addressLine2 = consigneePlaceDto.getPartnerAddr().getStreet2();
            }
            if (Objects.nonNull(consigneePlaceDto.getPartnerAddrContact())) {
                this.name = consigneePlaceDto.getPartnerAddrContact().getPerson();
                this.mobileCountryNo = consigneePlaceDto.getPartnerAddrContact().getMobileNumber1();
                this.email = consigneePlaceDto.getPartnerAddrContact().getEmail();
            }
            this.locationName = consigneePlaceDto.getLocName();
            if (Boolean.TRUE.equals(consigneePlaceDto.getActiveInd())) {
                this.placesStatus = "active";
            } else {
                this.placesStatus = "inactive";
            }
        }
    }
}
