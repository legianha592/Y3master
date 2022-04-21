package com.y3technologies.masters.dto;

import com.y3technologies.masters.util.ExcelColumn;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Sivasankari Subramaniam
 */
@Data
public class ReasonCodeExcelDto extends BaseDto {
    @ExcelColumn(col = 1, value = "reasonCode")
    public String reasonCode;
    @ExcelColumn(col = 2, value = "reasonDescription")
    public String reasonDescription;
    @ExcelColumn(col = 3, value = "category")
    public String category;
    @ExcelColumn(col = 4, value = "reasonUsage")
    public String reasonUsage;
    @ExcelColumn(col = 5, value = "inducedBy")
    public String inducedBy;
    @ExcelColumn(col = 6, value = "activeInd")
    public Boolean activeInd = Boolean.TRUE;
}
