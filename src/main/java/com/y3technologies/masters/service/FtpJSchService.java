package com.y3technologies.masters.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface FtpJSchService {

	public boolean upload(InputStream inputStream, String filePath, String filename);

	public void download(String downloadFileName, String fileDirectory, String fileSavePath);

	public InputStream readFile(String fileUrl);

	public void download(String downloadFileName, String fileDirectory, OutputStream outputStream) throws Exception;

	List<String> deleteFiles(String directory, List<String> lstFileName);

	public void deleteOutOfDateFiles(String uploadedFileDirectory);
}
