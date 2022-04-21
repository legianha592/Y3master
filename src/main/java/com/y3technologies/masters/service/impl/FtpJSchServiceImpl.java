package com.y3technologies.masters.service.impl;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.y3technologies.masters.MastersApplicationPropertiesConfig;

import com.y3technologies.masters.schedule.ScheduledTasks;
import com.y3technologies.masters.service.FtpJSchService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Vector;

@Service
@RequiredArgsConstructor
public class FtpJSchServiceImpl implements FtpJSchService {

	private final MastersApplicationPropertiesConfig mastersApplicationPropertiesConfig;
	private final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

	private ChannelSftp getSftp() {
		ChannelSftp sftp = null;
		try {
			JSch jsch = new JSch();
			int port = !StringUtils.isEmpty(mastersApplicationPropertiesConfig.getFileServerPort()) ?
					Integer.parseInt(mastersApplicationPropertiesConfig.getFileServerPort()) : 0;
			Session sshSession = jsch.getSession(mastersApplicationPropertiesConfig.getFileServerUsername(),
					mastersApplicationPropertiesConfig.getFileServerHostName(),
					port);
			sshSession.setPassword(mastersApplicationPropertiesConfig.getFileServerPassword());
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");
			sshSession.setConfig(sshConfig);
			sshSession.connect();
			Channel channel = sshSession.openChannel("sftp");
			channel.connect();
			sftp = (ChannelSftp) channel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sftp;
	}

	@Override
	public boolean upload(InputStream inputStream, String filePath, String filename) {
		ChannelSftp sftp = getSftp();
		SftpATTRS sftpATTRS = null;
		boolean dirExist = true;
		try {
			sftpATTRS = sftp.lstat(mastersApplicationPropertiesConfig.getFileServerBasePath() + "/" + filePath);
		} catch (SftpException e) {
			dirExist = false;
		}
		try {
			if (!dirExist || !sftpATTRS.isDir()) {
				sftp.mkdir(mastersApplicationPropertiesConfig.getFileServerBasePath() + "/" + filePath);
			}
			sftp.cd(mastersApplicationPropertiesConfig.getFileServerBasePath() + "/" + filePath);
			sftp.put(inputStream, filename);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public void download(String downloadFileName, String fileDirectory, String fileSavePath) {
		ChannelSftp sftp = getSftp();
		try {
			sftp.cd(mastersApplicationPropertiesConfig.getFileServerBasePath() + fileDirectory);
			File file = new File(fileSavePath);
			sftp.get(downloadFileName, new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void download(String downloadFileName, String fileDirectory, OutputStream outputStream) throws Exception{
		ChannelSftp sftp = getSftp();
			//check existing file, if not existing, throw an exception
			SftpATTRS sftpATTRS = sftp.lstat(mastersApplicationPropertiesConfig.getFileServerBasePath() + fileDirectory + "/" + downloadFileName);
			sftp.cd(mastersApplicationPropertiesConfig.getFileServerBasePath() + fileDirectory);
			sftp.get(downloadFileName, outputStream);
	}

	public Vector listFiles(String directory) throws SftpException {
		ChannelSftp sftp = getSftp();
		Vector result = sftp.ls(directory);
		sftp.disconnect();
		return result;
	}

	@Override
	public InputStream readFile(String fileUrl) {

		ChannelSftp sftp = this.getSftp();

		try {

			return sftp.get(fileUrl);

		} catch (SftpException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<String> deleteFiles(String directory, List<String> lstFileName) {
		List<String> fileNameDeleted = new ArrayList<>();
		ChannelSftp sftp = this.getSftp();
		if (Objects.isNull(sftp)) {
			return fileNameDeleted;
		}
		try {
			sftp.cd(mastersApplicationPropertiesConfig.getFileServerBasePath());
			sftp.cd(directory);
		} catch (SftpException e) {
			e.printStackTrace();
			return fileNameDeleted;
		}
		for (String fileName : lstFileName) {
			try {
				sftp.rm(fileName);
				fileNameDeleted.add(fileName);
			} catch (SftpException e) {
				e.printStackTrace();
				if (e.getMessage().equalsIgnoreCase("No such file")) {
					fileNameDeleted.add(fileName);
				}
			}
		}
		return fileNameDeleted;
	}

	@Override
	public void deleteOutOfDateFiles(String uploadedFileDirectory) {
		String outDateUploadedExcelFileDuration = mastersApplicationPropertiesConfig.getOutDateUploadedExcelFileDuration();

		if (StringUtils.isEmpty(outDateUploadedExcelFileDuration)){
			return;
		}

		String[] durationArr = outDateUploadedExcelFileDuration.split("\\s+");

		if (durationArr.length < 2){
			return;
		}

		long durationValue = Long.parseLong(durationArr[0]);
		String durationUnit = durationArr[1];

		try {
			ChannelSftp sftp = getSftp();
			Vector listUploadedFile = sftp.ls(uploadedFileDirectory);

			List<String> listDeleteFile = new ArrayList();
			if (listUploadedFile != null) {
				for (Object uploadedFile : listUploadedFile){
					ChannelSftp.LsEntry details = (ChannelSftp.LsEntry) uploadedFile;

					if (details.getFilename().equals(".") || details.getFilename().equals("..")){
						continue;
					}

					SftpATTRS attrs = details.getAttrs();
					int mTime = attrs.getMTime();
					Date createdDate = new Date(mTime * 1000L);
					Date currentDate = new Date();
					long diff = currentDate.getTime() - createdDate.getTime();

					if (durationUnit.toLowerCase().equals("minutes")){
						long diffMinutes = diff / (60 * 1000) % 60;
						if (diffMinutes>=durationValue){
							listDeleteFile.add(details.getFilename());
						}
					} else if (durationUnit.toLowerCase().equals("hours")){
						long diffHours = diff / (60 * 60 * 1000);
						if (diffHours>=durationValue){
							listDeleteFile.add(details.getFilename());
						}
					} else {
						long diffDays = diff / (24 * 60 * 60 * 1000);
						if (diffDays>=durationValue){
							listDeleteFile.add(details.getFilename());
						}
					}
				}

				for (String fileName : listDeleteFile){
					if (fileName.length()<3){
						continue;
					}

					try {
						sftp.rm(uploadedFileDirectory+"/"+fileName);
					} catch (SftpException e) {
						log.error("delete "+fileName + ", error: " + e.getMessage());
					}
				}
			}

			log.info("deleted "+listDeleteFile.size()+" files: "+listDeleteFile.toString());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}
