package com.y3technologies.masters.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationIdAndPartnerIdDto {
    private Long locationId;
    private Long partnerId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationIdAndPartnerIdDto that = (LocationIdAndPartnerIdDto) o;
        return locationId.equals(that.locationId) && partnerId.equals(that.partnerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationId, partnerId);
    }
}
