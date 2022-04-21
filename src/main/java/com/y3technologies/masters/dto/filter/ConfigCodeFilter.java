package com.y3technologies.masters.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigCodeFilter extends ListingFilter implements Serializable {
    private String code;

    private String description;

    private String usageLevel;

    private String configValue;

    private Boolean activeInd;
}
