package com.y3technologies.masters.controller;

import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.service.FtpJSchService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

/**
 * @author TungLLT
 */
@RestController
@RequestMapping(value = "/${api.version.masters}/excel", consumes = { MediaType.APPLICATION_JSON_VALUE,
		MediaType.ALL_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
public class ExcelController extends BaseController {
	@Autowired
	private MastersApplicationPropertiesConfig mastersApplicationPropertiesConfig;

	@Autowired
	private FtpJSchService ftpJSchService;

	@GetMapping(value = "/downloadTemplate/{code}")
	@ResponseBody
	public void downloadTptRequestUploadTemplate (HttpServletResponse response, @PathVariable("code") String code) {
		if (StringUtils.isEmpty(code)){
			throw new TransactionException("exception.excel.download.template.wrong.code");
		}

		String fileName = initFileName(code);

		String resourceName = mastersApplicationPropertiesConfig.getUploadExcelTemplatePath() + fileName;

		try(InputStream inputStream = new ClassPathResource(resourceName).getInputStream()) {
			response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			response.setHeader("Content-Disposition", "attachment;filename="+ URLEncoder.encode(fileName, "utf-8"));
			response.flushBuffer();
			IOUtils.copy(inputStream, response.getOutputStream());
		} catch (IOException e) {
			throw new TransactionException("exception.excel.download.template.not.exist");
		}
	}

	private String initFileName (String settingPageCode){
		String fileName = StringUtils.EMPTY;
		switch (settingPageCode) {
			case AppConstants.DownloadTemplateCode.CONFIGCODE:
				fileName = AppConstants.UploadExcelTemplate.CONFIGCODE_SETTING;
				break;
			case AppConstants.DownloadTemplateCode.CONSIGNEE:
				fileName = AppConstants.UploadExcelTemplate.CONSIGNEE_SETTING;
				break;
			case AppConstants.DownloadTemplateCode.CUSTOMER:
				fileName = AppConstants.UploadExcelTemplate.CUSTOMER_SETTING;
				break;
			case AppConstants.DownloadTemplateCode.DRIVER:
				fileName = AppConstants.UploadExcelTemplate.DRIVER_SETTING;
				break;
			case AppConstants.DownloadTemplateCode.EQUIPMENT:
				fileName = AppConstants.UploadExcelTemplate.EQUIPMENT_SETTING;
				break;
			case AppConstants.DownloadTemplateCode.LOCATION:
				fileName = AppConstants.UploadExcelTemplate.LOCATION_SETTING;
				break;
			case AppConstants.DownloadTemplateCode.MILESTONE:
				fileName = AppConstants.UploadExcelTemplate.MILESTONE_SETTING;
				break;
			case AppConstants.DownloadTemplateCode.REASONCODE:
				fileName = AppConstants.UploadExcelTemplate.REASONCODE_SETTING;
				break;
			case AppConstants.DownloadTemplateCode.TRANSPORTER:
				fileName = AppConstants.UploadExcelTemplate.TRANSPORTER_SETTING;
				break;
			case AppConstants.DownloadTemplateCode.UOM:
				fileName = AppConstants.UploadExcelTemplate.UOM_SETTING;
				break;
			case AppConstants.DownloadTemplateCode.VEHICLE:
				fileName = AppConstants.UploadExcelTemplate.VEHICLE_SETTING;
				break;
			case AppConstants.DownloadTemplateCode.LOOKUP:
				fileName = AppConstants.UploadExcelTemplate.LOOKUP_SETTING;
				break;
			default:
				throw new TransactionException("exception.excel.download.template.wrong.code");
		}

		return fileName;
	}

	@GetMapping(value = "/downloadFile/{fileName}")
	public void downloadFile(HttpServletResponse response, @PathVariable("fileName") String fileName){
		if (!StringUtils.isEmpty(fileName) && fileName.contains("/") || fileName.contains("..") || fileName.contains("%")){
			throw new TransactionException("exception.excel.file.invalid.name");
		}

		response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		try{
			String directory = "/" + AppConstants.UploadExcelResponse.UPLOADED_EXCEL_FOLDER;
			ftpJSchService.download(fileName, directory, response.getOutputStream());
		} catch (Exception e) {
			throw new TransactionException("exception.excel.file.not.found");
		}
	}

}
