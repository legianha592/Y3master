package com.y3technologies.masters.dto.excel;

import lombok.Data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vhtu1@cmc.com.vn
 */
@Data
public class UploadTemplateHdrIdDto
{
    private String code;
    private String type;
    private String title;
    private String fileName;
    private Integer sheetSeqNo;
    private Integer startRow;
    private Integer activeInd;
    private String createdBy;
    private Timestamp createdDate;
    private String modifiedBy;
    private Timestamp modifiedDate;
    private String inactBy;
    private Timestamp inactDate;
    private List<UploadTemplateDetIdDto> listTempDetail = new ArrayList<UploadTemplateDetIdDto>();
}
