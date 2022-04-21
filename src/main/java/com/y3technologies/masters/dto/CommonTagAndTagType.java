package com.y3technologies.masters.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommonTagAndTagType {
    private String tag;
    private String tagType;
    private Long tenantId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommonTagAndTagType that = (CommonTagAndTagType) o;
        return tag.equals(that.tag) && tagType.equals(that.tagType) && tenantId.equals(that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, tagType, tenantId);
    }
}
