package com.y3technologies.masters.dto.aas;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileScopeDTO {
    private String profileCode;
    private Long refId;
    private String value;
}
