package com.y3technologies.masters.dto;

import lombok.Data;

import javax.persistence.Column;

/**
 * @author Sivasankari Subramaniam
 */
@Data
public class CommonTagDto extends BaseDto {
    private String tagType;

    private String tag;

    private Long tenantId;

    private String referenceFunction;

    private Long referenceId;
}
