package com.y3technologies.masters.client;

import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient (contextId = "excel-service", name = "common", path = "/${api.version.common}")
public interface ExcelClient
{
    @GetMapping ("/excel/getExcelTemplateByCode/{code}")
    List<UploadTemplateHdrIdDto> findTemplateByCode(@PathVariable ("code") String code);


}
