package com.y3technologies.masters.dto.excel;

import lombok.Data;

/**
 * @author vhtu1@cmc.com.vn
 */
@Data
public class UploadTemplateDetIdDto
{
    private int uploadTemplateDetId;
    private Integer uploadTemplateHdrId;
    private String fieldName;
    private String fieldType;
    private Integer position;
    private String columnName;
    private String columnFullName;
    private String pattern;
    private Integer mandatoryInd;
    private Integer maxLength;
    private String defaultValue;
    private Integer needCapitalized;
    private Integer columnSize;
    private Integer activeInd;
    private Integer width;
    private String alignment;
    private Integer noneDuplicated;
    private String cellType;
}
