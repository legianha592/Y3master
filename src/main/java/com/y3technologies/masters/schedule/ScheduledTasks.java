package com.y3technologies.masters.schedule;


import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.service.FtpJSchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private FtpJSchService ftpJSchService;

    @Autowired
    private MastersApplicationPropertiesConfig config;

    @Scheduled(cron = "0 */3 * ? * *")
    public void deleteOutOfDateExcelFile() {
        log.info("Starting Delete Out Of Date Excel Files at: {}", LocalDateTime.now());

        String uploadedFilePath = config.getFileServerBasePath() + "/" + AppConstants.UploadExcelResponse.UPLOADED_EXCEL_FOLDER;
        ftpJSchService.deleteOutOfDateFiles(uploadedFilePath);
    }
}
