package com.y3technologies.masters.client;

import com.y3technologies.masters.dto.excel.EmailSenderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import javax.validation.Valid;

/**
 * @author vhtu1
 */
@FeignClient(contextId = "sendApi-service", name = "notification", path="/${api.version.notification}")
public interface EmailClient
{

    @PostMapping(value = "/sendApi/sendEmail")
    ResponseEntity sendEmail(@RequestBody @Valid EmailSenderDto emailSenderDTO);

    @PostMapping(value = "/sendApi/sendUploadEmail")
    ResponseEntity sendUploadEmail(@RequestBody @Valid EmailSenderDto emailSenderDTO);

    @PostMapping(value = "/sendApi/sendUploadExcelEmail")
    ResponseEntity sendUploadExcelEmail(@RequestParam(value = "template") String emailSenderJson,
                                              @RequestPart("files") MultiValueMap<String, Object> file);

}
