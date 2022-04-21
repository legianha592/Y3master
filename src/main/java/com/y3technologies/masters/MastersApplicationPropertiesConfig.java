package com.y3technologies.masters;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@EnableDiscoveryClient
@EnableAutoConfiguration
@RefreshScope
@Component
@Setter
@Getter
public class MastersApplicationPropertiesConfig implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Value("${api.version.aas}")
	private String aasApiVersion;

	@Value("${api.version.common}")
	private String commonApiVersion;

	@Value("${api.version.masters}")
	private String mastersApiVersion;

	@Value("${api.version.notification}")
	private String notificationApiVersion;

	@Value("${api.version.transportation}")
	private String transportationApiVersion;

	@Value("${api.version.scheduler}")
	private String schedulerApiVersion;

	@Value("${fileServer.filePath}")
	private String fileServerBasePath;

	@Value("${fileServer.hostname}")
	private String fileServerHostName;

	@Value("${fileServer.username}")
	private String fileServerUsername;

	@Value("${fileServer.password}")
	private String fileServerPassword;

	@Value("${fileServer.port}")
	private String fileServerPort;

	@Value("${email.default.fromAddr}")
	private String emailFromAddr;

	@Value("${email.default.fromName}")
	private String emailFromName;

	@Value("${email.default.template.success.name}")
	private String templateSuccessName;

	@Value("${email.default.template.failed.name}")
	private String templateFailedName;

	@Value("${email.default.template.type}")
	private String templateType;

	@Value("${upload.excel.template.path}")
	private String uploadExcelTemplatePath;

	@Value("${uploaded.excel.file.url}")
	private String uploadedExcelFileUrl;

	@Value("${out.date.uploaded.excel.file.duration}")
	private String outDateUploadedExcelFileDuration;
}
