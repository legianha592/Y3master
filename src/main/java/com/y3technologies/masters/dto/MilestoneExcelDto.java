package com.y3technologies.masters.dto;

import com.y3technologies.masters.util.ExcelColumn;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Sivasankari Subramaniam
 */
@Data
public class MilestoneExcelDto {
    @ExcelColumn(col = 1, value = "id")
    public Long id;
    @ExcelColumn(col = 2, value = "activeInd")
    public Boolean activeInd = Boolean.TRUE;
    @ExcelColumn(col = 3, value = "createdBy")
    public String createdBy;
    @ExcelColumn(col = 4, value = "createdDate")
    public LocalDateTime createdDate;
    @ExcelColumn(col = 5, value = "updatedBy")
    public String updatedBy;
    @ExcelColumn(col = 6, value = "updatedDate")
    public LocalDateTime updatedDate;
    @ExcelColumn(col = 7, value = "milestoneCategory")
    public String milestoneCategory;
    @ExcelColumn(col = 8, value = "milestoneCode")
    public String milestoneCode;
    @ExcelColumn(col = 9, value = "milestoneDescription")
    public String milestoneDescription;
    @ExcelColumn(col = 10, value = "milestoneGroup")
    public String milestoneGroup;
    @ExcelColumn(col = 11, value = "tenantId")
    public Long tenantId;
    @ExcelColumn(col = 12, value = "customerDescription")
    public String customerDescription;
    @ExcelColumn(col = 13, value = "isInternal")
    public Boolean isInternal = true;
    @ExcelColumn(col = 14, value = "hashcode")
    public String hashcode;
    @ExcelColumn(col = 15, value = "version")
    public String version;
    @ExcelColumn(col = 16, value = "sequence")
    public String sequence;
    @ExcelColumn(col = 17, value = "milestoneStatus")
    public String milestoneStatus;
    @ExcelColumn(col = 18, value = "tptRequestStatus")
    public String tptRequestStatus;
}
