package com.y3technologies.masters.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.y3technologies.masters.MastersApplicationPropertiesConfig;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.client.EmailClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.excel.EmailSenderDto;
import com.y3technologies.masters.dto.excel.ExcelResponseMessage;
import com.y3technologies.masters.dto.excel.UploadTemplateDetIdDto;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.exception.TransactionException;
import com.y3technologies.masters.service.FtpJSchService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ExcelUtils {

    @Autowired
    private AasClient aasClient;
    @Autowired
    private EmailClient emailClient;
    @Autowired
    private MastersApplicationPropertiesConfig propertiesConfig;
    @Autowired
    private MessagesUtilities messagesUtilities;
    @Autowired
    private FtpJSchService ftpJSchService;
    @Autowired
    private ExcelClient excelClient;

    private final Logger log = LoggerFactory.getLogger(ExcelUtils.class);
    private final String EXCEL2003 = "xls";
    private final String EXCEL2007 = "xlsx";
    private int MILLISECOND = 1000;
    private int SECOND = 60;
    private final int MAX_ROW_ACCESS_WINDOW = 100;
    private final String EXCEL_ROW_POSITION = "excelRowPosition";
    private final String ERROR_EXCEL_FILE_NAME_INFIX = "_error_";
    private final String XLS_EXTENSION = ".xls";
    private final String XLSX_EXTENSION = ".xlsx";
    private final String ACTIVE_CELL_VALUE = "active";
    private final String INACTIVE_CELL_VALUE = "inactive";
    private Map<String, HorizontalAlignment> mapAlignment = new HashMap<>();

    public ExcelUtils (){
        mapAlignment.put("LEFT",HorizontalAlignment.LEFT);
        mapAlignment.put("RIGHT",HorizontalAlignment.RIGHT);
        mapAlignment.put("CENTER",HorizontalAlignment.CENTER);
    }

    public <T> List<T> readExcel(Class<T> cls,MultipartFile file){

        String fileName = file.getOriginalFilename();
        if (!fileName.matches("^.+\\.(?i)(xls)$") && !fileName.matches("^.+\\.(?i)(xlsx)$")) {
            log.error("upload file should be xls or xlsx.");
        }
        List<T> dataList = new ArrayList<>();
        Workbook workbook = null;
        try {
            InputStream is = file.getInputStream();
            if (fileName.endsWith(EXCEL2007)) {
                workbook = new XSSFWorkbook(is);
            }
            if (fileName.endsWith(EXCEL2003)) {
                workbook = new HSSFWorkbook(is);
            }
            if (workbook != null) {
                Map<String, List<Field>> classMap = new HashMap<>();
                List<Field> fields = Stream.of(cls.getDeclaredFields()).collect(Collectors.toList());
                fields.forEach(
                        field -> {
                            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                            if (annotation != null) {
                                String value = annotation.value();
                                if (StringUtils.isBlank(value)) {
                                    return;
                                }
                                if (!classMap.containsKey(value)) {
                                    classMap.put(value, new ArrayList<>());
                                }
                                field.setAccessible(true);
                                classMap.get(value).add(field);
                            }
                        }
                );
                Map<Integer, List<Field>> reflectionMap = new HashMap<>();
                Sheet sheet = workbook.getSheetAt(0);

                boolean firstRow = true;
                for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (firstRow) {
                        for (int j = row.getFirstCellNum(); j <= row.getLastCellNum(); j++) {
                            Cell cell = row.getCell(j);
                            String cellValue = getCellValue(cell);
                            if (classMap.containsKey(cellValue)) {
                                reflectionMap.put(j, classMap.get(cellValue));
                            }
                        }
                        firstRow = false;
                    } else {
                        if (row == null) {
                            continue;
                        }
                        try {
                            T t = cls.getDeclaredConstructor().newInstance();
                            boolean allBlank = true;
                            for (int j = row.getFirstCellNum(); j <= row.getLastCellNum(); j++) {
                                if (reflectionMap.containsKey(j)) {
                                    Cell cell = row.getCell(j);
                                    String cellValue = getCellValue(cell);
                                    if (StringUtils.isNotBlank(cellValue)) {
                                        allBlank = false;
                                    }
                                    List<Field> fieldList = reflectionMap.get(j);
                                    fieldList.forEach(
                                            x -> {
                                                try {
                                                    handleField(t, cellValue, x);
                                                } catch (Exception e) {
                                                    log.error(String.format("reflect field:%s value:%s exception!", x.getName(), cellValue), e);
                                                }
                                            }
                                    );
                                }
                            }
                            if (!allBlank) {
                                dataList.add(t);
                            } else {
                                log.warn(String.format("row:%s is blank ignore!", i));
                            }
                        } catch (Exception e) {
                            log.error(String.format("parse row:%s exception!", i), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(String.format("parse excel exception!"), e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    log.error(String.format("parse excel exception!"), e);
                }
            }
        }
        return dataList;
    }

    private <T> void handleField(T t, String value, Field field) throws Exception {
        if(field == null){
            return;
        }

        Class<?> type = field.getType();
        if (type == null || type == void.class || StringUtils.isBlank(value)) {
            return;
        }
        if (type == Object.class) {
            field.set(t, value);
        } else if ((type.getSuperclass() == null || type.getSuperclass() == Number.class) &&  NumberUtils.isCreatable(value)) {
            if (type == int.class || type == Integer.class) {
                field.set(t, NumberUtils.toInt(value));
            } else if (type == long.class || type == Long.class) {
                field.set(t, NumberUtils.toLong(value));
            } else if (type == byte.class || type == Byte.class) {
                field.set(t, NumberUtils.toByte(value));
            } else if (type == short.class || type == Short.class) {
                field.set(t, NumberUtils.toShort(value));
            } else if (type == double.class || type == Double.class) {
                field.set(t, NumberUtils.toDouble(value));
            } else if (type == float.class || type == Float.class) {
                field.set(t, NumberUtils.toFloat(value));
            } else if (type == char.class || type == Character.class) {
                field.set(t, CharUtils.toChar(value));
            } else if (type == boolean.class) {
                field.set(t, BooleanUtils.toBoolean(value));
            } else if (type == BigDecimal.class) {
                field.set(t, new BigDecimal(value));
            }
        } else if (type == Boolean.class) {
            field.set(t, BooleanUtils.toBoolean(value));
        } else if (type == Date.class) {
            field.set(t, value);
        } else if (type == String.class) {
            field.set(t, value);
        }
        else {
            log.error("- cell type "+type+", value "+value+", field "+field.getName()+", class "+field.getClass().getName());
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return new SimpleDateFormat("mm/dd/yyyy").format(DateUtil.getJavaDate(cell.getNumericCellValue()));
            } else {
                return new BigDecimal(cell.getNumericCellValue()).toString();
            }
        } else if (cell.getCellType() == CellType.STRING) {
            return StringUtils.trimToEmpty(cell.getStringCellValue());
        } else if (cell.getCellType() == CellType.FORMULA) {
            return StringUtils.trimToEmpty(cell.getCellFormula());
        } else if (cell.getCellType() == CellType.BLANK) {
            return "";
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellType() == CellType.ERROR) {
            return "ERROR";
        } else {
            return cell.toString().trim();
        }
    }

    public <T> void exportExcel(HttpServletResponse response, List<T> dataList, Class<T> cls,
            List<UploadTemplateHdrIdDto> lstTemplate) {

        if (lstTemplate.isEmpty()) {
            throw new TransactionException("exception.excel.template.not.exist");
        }

        UploadTemplateHdrIdDto exportTemplate = lstTemplate.get(0);
        Map<String, UploadTemplateDetIdDto> mapFieldAndColumn = new HashMap<>();

        for (UploadTemplateDetIdDto detailColumn : exportTemplate.getListTempDetail()) {
            mapFieldAndColumn.put(detailColumn.getFieldName(), detailColumn);
        }

        List<Field> fieldList = new ArrayList<>();
        getFieldsOfAClass(cls, fieldList);
        fieldList.forEach(field -> {
            field.setAccessible(true);
        });

        SXSSFWorkbook wb = new SXSSFWorkbook(MAX_ROW_ACCESS_WINDOW);
        int rowNumber = exportTemplate.getStartRow() != null ? exportTemplate.getStartRow() : 0;
        Sheet sheet = wb.createSheet(exportTemplate.getTitle());
        Row row = sheet.createRow(rowNumber);

        // define cell style
        CellStyle columnNameStyle = getColumnNameStyle(wb);
        CellStyle rowNumStyle = getRowNumStyle(wb);
        Map<String, CellStyle> cellWithAlignmentStyles = getCellWithAlignmentStyles(wb);

        for (UploadTemplateDetIdDto detailColumn : exportTemplate.getListTempDetail()) {
            Cell columnNameCell = row.createCell(detailColumn.getPosition());
            columnNameCell.setCellStyle(columnNameStyle);
            columnNameCell.setCellValue(detailColumn.getColumnName());
            sheet.setColumnWidth(detailColumn.getPosition(), detailColumn.getWidth());
        }

        if (CollectionUtils.isNotEmpty(dataList)) {

            for (T rowData : dataList) {
                rowNumber++;
                Row rowForWriting = sheet.createRow(rowNumber);

                // create row num field
                UploadTemplateDetIdDto rowNumColumnDetail = mapFieldAndColumn.get("#");
                if (rowNumColumnDetail != null) {
                    Cell cell = rowForWriting.createCell(rowNumColumnDetail.getPosition());
                    cell.setCellStyle(rowNumStyle);
                    cell.setCellValue(rowNumber);
                }

                fieldList.forEach(field -> {
                    Class<?> type = field.getType();
                    Object value = StringUtils.EMPTY;
                    try {
                        value = field.get(rowData);
                    } catch (Exception e) {
                        log.error(e.toString());
                    }

                    // get detail conlumn configuration data by field name
                    UploadTemplateDetIdDto detailColumnConfigData = mapFieldAndColumn.get(field.getName());

                    // set position for a cell
                    if (detailColumnConfigData != null) {
                        Cell cell = rowForWriting.createCell(detailColumnConfigData.getPosition());
                        CellStyle cellStyle = cellWithAlignmentStyles.get(detailColumnConfigData.getAlignment());
                        cell.setCellStyle(cellStyle);

                        if (value != null) {
                            if (type == Date.class) {
                                cell.setCellValue(value.toString());
                            } else if (field.getName().equals("activeInd")) {
                                cell.setCellValue(value.toString().equals("true") ? ACTIVE_CELL_VALUE : INACTIVE_CELL_VALUE);
                            } else {
                                cell.setCellValue(value.toString().replaceAll("\\|", ","));
                            }
                        }
                    }
                });
            }
        }
        buildExcelDocument(exportTemplate.getFileName(), wb, response);
    }

    private CellStyle getColumnNameStyle(Workbook wb) {
        CellStyle columnNameStyle = wb.createCellStyle();
        columnNameStyle.setAlignment(HorizontalAlignment.CENTER);
        columnNameStyle.setBorderBottom(BorderStyle.THIN);
        columnNameStyle.setBorderTop(BorderStyle.THIN);
        columnNameStyle.setBorderRight(BorderStyle.THIN);
        columnNameStyle.setBorderLeft(BorderStyle.THIN);
        Font font = wb.createFont();
        font.setBold(true);
        columnNameStyle.setFont(font);
        return columnNameStyle;
    }

    private CellStyle getRowNumStyle(Workbook wb) {
        CellStyle rowNumStyle = wb.createCellStyle();
        rowNumStyle.setAlignment(HorizontalAlignment.CENTER);
        rowNumStyle.setBorderBottom(BorderStyle.THIN);
        rowNumStyle.setBorderTop(BorderStyle.THIN);
        rowNumStyle.setBorderRight(BorderStyle.THIN);
        rowNumStyle.setBorderLeft(BorderStyle.THIN);
        return rowNumStyle;
    }

    private Map<String, CellStyle> getCellWithAlignmentStyles(Workbook wb) {
        Map<String, CellStyle> result = new HashMap<>();
        mapAlignment.keySet().forEach(alignKey -> {
            CellStyle cellStyle = wb.createCellStyle();
            HorizontalAlignment horizontalAlignment = mapAlignment.get(alignKey);
            if (horizontalAlignment != null) {
                cellStyle.setAlignment(horizontalAlignment);
            }
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            result.put(alignKey, cellStyle);
        });
        return result;
    }

    private void buildExcelDocument(String fileName, SXSSFWorkbook wb, HttpServletResponse response) {
        try {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", "attachment;filename="+URLEncoder.encode(fileName, "utf-8"));
            response.flushBuffer();
            wb.write(response.getOutputStream());
            wb.dispose();
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

    public void getFieldsOfAClass(Class cls, List<Field> fieldList){
        if(cls.getDeclaredFields().length>0){
            Field[] fields = cls.getDeclaredFields();
            fieldList.addAll(Arrays.asList(fields));
        }

        if (cls.getSuperclass() != null && !cls.getSuperclass().equals(Object.class)){
            getFieldsOfAClass(cls.getSuperclass(),fieldList);
        }
    }

    public void sendNotificationEmail(String templateName, String pageName, String errorsContent, byte[] byteArr, String fileName, UpdateUserProfileDTO userInfo) {
        String fileNameConverted = fileName.lastIndexOf(".") == -1 ?  fileName : fileName.substring(0, fileName.lastIndexOf("."));
        String modifiedName = fileName;
        if (templateName.equals(propertiesConfig.getTemplateFailedName())){
            if (fileName.endsWith(EXCEL2003)){
                modifiedName = fileNameConverted + ERROR_EXCEL_FILE_NAME_INFIX + DateFormatUtil.getCurrentUTCMilisecond() + XLS_EXTENSION;
            }
            else{
                modifiedName = fileNameConverted + ERROR_EXCEL_FILE_NAME_INFIX + DateFormatUtil.getCurrentUTCMilisecond() + XLSX_EXTENSION;
            }
        }

        final String newFileName = modifiedName;

        //upload the excel file to ftp server
        InputStream inputStream = new ByteArrayInputStream(byteArr);
        ftpJSchService.upload(inputStream,AppConstants.UploadExcelResponse.UPLOADED_EXCEL_FOLDER,newFileName);

        EmailSenderDto emailSenderDTO = new EmailSenderDto();
        emailSenderDTO.setTo(userInfo. getEmail());
        emailSenderDTO.setFrom(propertiesConfig.getEmailFromAddr());
        emailSenderDTO.setFromName(propertiesConfig.getEmailFromName());
        emailSenderDTO.setTemplateName(templateName);

        HashMap<String, String> parametersMap = new HashMap<>();
        parametersMap.put("UserName", userInfo.getLastName());
        parametersMap.put("pageName", pageName);
        parametersMap.put("time", new SimpleDateFormat("hh:mm:ss").format(new Date()));

        if (!StringUtils.isEmpty(errorsContent)) {
            parametersMap.put("errors", errorsContent);
        }

        parametersMap.put("fileUrl", propertiesConfig.getUploadedExcelFileUrl()+propertiesConfig.getMastersApiVersion()+"/excel/downloadFile/"+newFileName);
        parametersMap.put("expireDay", "3");

        emailSenderDTO.setParameters(parametersMap);
        try {
            String emailSenderJson = new ObjectMapper().writeValueAsString(emailSenderDTO);
            MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
            emailClient.sendUploadExcelEmail(emailSenderJson, multiValueMap);
        } catch (JsonProcessingException e) {
            log.error(e.toString());
        }
    }

    public <T> List<T> parseExcelToDto(Class<T> cls, Sheet sheet, String fileName,
                                       UploadTemplateHdrIdDto uploadExcelTemplate,
                                       Map<Integer, StringBuilder> mapExcelErrors, Set<String> excelEmailContent)
            throws TransactionException
    {
        List<T> dataList = new ArrayList<>();
        Workbook workbook = null;
        try {
            if (sheet == null) {
                excelEmailContent.add(messagesUtilities.getMessageWithParam("upload.excel.email.read.file.fail",null));
            }

            Map<String, UploadTemplateDetIdDto> columnDetailByColumnName = new HashMap<>();
            Map<Integer, UploadTemplateDetIdDto> columnDetailByColumnPosition = new HashMap<>();
            Map<Integer, Field> fieldByColumnPosition = new HashMap<>();
            Map<String, Field> fieldByFieldName = new HashMap<>();
            StringBuilder fieldHeaderError = new StringBuilder();

            for (UploadTemplateDetIdDto detailColumn : uploadExcelTemplate.getListTempDetail()) {
                if (!StringUtils.isEmpty(detailColumn.getColumnName())) {
                    columnDetailByColumnName.put(detailColumn.getColumnName().trim().toLowerCase(), detailColumn);
                }
            }

            List<Field> fieldList = new ArrayList<>();
            getFieldsOfAClass(cls, fieldList);

            for (Field field : fieldList) {
                field.setAccessible(true);
                fieldByFieldName.put(field.getName(), field);
            }

            Integer validatedDataIndex = 0;

            // init field by column position
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Iterator<Cell> cellIterator = headerRow.cellIterator();
            while (cellIterator.hasNext()) {
                Cell headerCell = cellIterator.next();
                String columnName = getCellValue(headerCell);
                initFieldByColumnPosition(fieldByColumnPosition, fieldByFieldName, columnDetailByColumnName, columnName,
                        headerCell, columnDetailByColumnPosition);
            }

            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                if (checkIfRowIsEmpty(row)) {
                    continue;
                }

                T validatedItem = cls.getDeclaredConstructor().newInstance();

                for (Field field : fieldList) {
                    if (field.getName().equalsIgnoreCase(EXCEL_ROW_POSITION)){
                        field.setInt(validatedItem, rowIndex+1);
                    }
                }

                for (int cellIndex = 0;cellIndex < row.getLastCellNum();cellIndex++) {
                    Cell cell = row.getCell(cellIndex);
                    if(cell == null){
                        cell = row.createCell(cellIndex);
                    }

                    UploadTemplateDetIdDto detailColumn = columnDetailByColumnPosition.get(cell.getColumnIndex());
                    if (detailColumn != null)
                    {
                        Field field = fieldByColumnPosition.get(cell.getColumnIndex());
                        String cellValue = getCellValueAndCatchErrors(cell, mapExcelErrors, detailColumn, validatedDataIndex, excelEmailContent).replaceAll(",", "\\|");
                        handleField(validatedItem, cellValue, field);
                    }
                }

                dataList.add(validatedItem);
                validatedDataIndex++;

            }

            //append fieldHeaderError to mapExcelErrors: if there is no error before, create error
            for (int itemIndex = 0; itemIndex < dataList.size(); itemIndex++){
                if (mapExcelErrors.containsKey(itemIndex)){
                    StringBuilder cellErrors = mapExcelErrors.get(itemIndex);
                    buildCellErrors(cellErrors, fieldHeaderError.toString());
                }
                else{
                    StringBuilder cellErrors = new StringBuilder();
                    if (!StringUtils.isEmpty(fieldHeaderError)){
                        buildCellErrors(cellErrors, fieldHeaderError.toString());
                        mapExcelErrors.put(itemIndex, cellErrors);
                    }
                }
            }

        } catch (Exception e){
            log.error(e.toString());
            excelEmailContent.add(messagesUtilities.getMessageWithParam("upload.excel.email.read.file.fail",null));
        }

        finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    log.error(String.format("parse excel exception!"), e);
                    excelEmailContent.add(messagesUtilities.getMessageWithParam("upload.excel.email.read.file.fail",null));
                }
            }
        }
        return dataList;
    }

    public ExcelResponseMessage handlingColumnHeader(Row row, Set<String> excelEmailContent, Map<Integer, Field> fieldByColumnPosition, Map<String, Field> fieldByFieldName
            , Map<String, UploadTemplateDetIdDto> columnDetailByColumnName, Map<Integer, UploadTemplateDetIdDto> columnDetailByColumnPosition,
                                     List<UploadTemplateDetIdDto> columnTemplateDetail, StringBuilder fieldHeaderError, Map<Integer, StringBuilder> mapExcelErrors, ExcelResponseMessage excelResponseMessage) {
        List<String> listColumnName = new ArrayList<>();

        if (checkIfRowIsEmpty(row)) {
            excelEmailContent.add(messagesUtilities.getMessageWithParam("upload.excel.email.missing.header",null));
            StringBuilder missingHeaderError = new StringBuilder();
            columnTemplateDetail.stream().forEach(detailColumn -> {
                if (missingHeaderError.length() == 0) {
                    missingHeaderError.append(messagesUtilities.getMessageWithParam("upload.excel.error.missing.header", new String []{detailColumn.getColumnName()}));
                } else {
                    missingHeaderError.append("\n- "+messagesUtilities.getMessageWithParam("upload.excel.error.missing.header", new String []{detailColumn.getColumnName()}));
                }
            });

            mapExcelErrors.put(0, missingHeaderError);

            excelResponseMessage
                    .setMessage(messagesUtilities.getMessageWithParam("upload.excel.response.invalid.headers", null));
            excelResponseMessage.setCode(AppConstants.UploadExcelResponse.INVALID_HEADERS);
            excelResponseMessage.setType(AppConstants.UploadExcelResponse.INVALID_FILE);
        }

        // get all cells of the selected row
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Cell headerCell = cellIterator.next();
            String columnName = getCellValue(headerCell);

            initFieldByColumnPosition(fieldByColumnPosition, fieldByFieldName, columnDetailByColumnName, columnName, headerCell, columnDetailByColumnPosition);

            if (!StringUtils.isEmpty(columnName) && !listColumnName.contains(columnName.trim().toLowerCase())) {
                listColumnName.add(columnName.trim().toLowerCase());
            } else if (!StringUtils.isEmpty(columnName) && listColumnName.contains(columnName.trim().toLowerCase())) {
                buildEmailErrorList(excelEmailContent, messagesUtilities.getMessageWithParam("upload.excel.email.duplicate.header", null));
                if (fieldHeaderError.length() == 0) {
                    fieldHeaderError.append(messagesUtilities.getMessageWithParam("upload.excel.error.duplicate.header",new String[] {columnName}));
                } else {
                    fieldHeaderError.append("\n- "+messagesUtilities.getMessageWithParam("upload.excel.error.duplicate.header",new String[] {columnName}));
                }
            }
        }

        for (UploadTemplateDetIdDto detailColumn : columnTemplateDetail) {
            if (!listColumnName.contains(detailColumn.getColumnName().toLowerCase())) {
                buildEmailErrorList(excelEmailContent, messagesUtilities.getMessageWithParam("upload.excel.email.missing.header", null));
                if (fieldHeaderError.length() == 0) {
                    fieldHeaderError.append(messagesUtilities.getMessageWithParam("upload.excel.error.missing.header",new String []{detailColumn.getColumnName()}));
                } else {
                    fieldHeaderError.append("\n- "+messagesUtilities.getMessageWithParam("upload.excel.error.missing.header",new String []{detailColumn.getColumnName()}));
                }
            }
        }

        if (fieldHeaderError.length() > 0) {
            excelResponseMessage
                    .setMessage(messagesUtilities.getMessageWithParam("upload.excel.response.invalid.headers", null));
            excelResponseMessage.setCode(AppConstants.UploadExcelResponse.INVALID_HEADERS);
            excelResponseMessage.setType(AppConstants.UploadExcelResponse.INVALID_FILE);
        }
        return excelResponseMessage;
    }

    public void initFieldByColumnPosition(Map<Integer, Field> mapPositionAndField, Map<String, Field> mapFieldsOfAClass,
                                          Map<String, UploadTemplateDetIdDto> mapDetailColumn, String columnName,
                                          Cell headerCell, Map<Integer, UploadTemplateDetIdDto> mapPositionAndColumnDetail)
    {
        UploadTemplateDetIdDto columnDetail = mapDetailColumn.get(columnName.replaceAll("\n", "").trim().toLowerCase());
        if (columnDetail != null) {
            mapPositionAndColumnDetail.put(headerCell.getColumnIndex(), columnDetail);
            Field field = mapFieldsOfAClass.get(columnDetail.getFieldName());

            if (field != null) {
                mapPositionAndField.put(headerCell.getColumnIndex(), field);
            }
        }
    }

    private String getCellValueAndCatchErrors(Cell cell, Map<Integer, StringBuilder> mapExcelError,
                                              UploadTemplateDetIdDto detailColumn, Integer validatedDataIndex, Set<String> excelEmailContent)
    {
        String cellValue = StringUtils.EMPTY;
        StringBuilder cellErrors = mapExcelError.get(validatedDataIndex);

        if (cellErrors == null) {
            cellErrors = new StringBuilder();
        }

        String columnName = detailColumn.getColumnName();
        CellType cellType = cell.getCellType();

        if(cellType == CellType.FORMULA) {
            cellType = cell.getCachedFormulaResultType();
        }

        //check mandatory field
        if ((cellType == CellType.BLANK || (cellType == CellType.STRING && cell.getStringCellValue().equals("")))
                && detailColumn.getMandatoryInd() != null && detailColumn.getMandatoryInd().equals(1)) {
            buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.mandatory",new String[] {columnName}));
            buildEmailErrorList(excelEmailContent, messagesUtilities.getMessageWithParam("upload.excel.email.mandatory.header", null));
        }

        //check data type
        if (!StringUtils.isEmpty(detailColumn.getCellType()) && !cellType.toString().equals(detailColumn.getCellType())) {
            String valueType = !StringUtils.isEmpty(messagesUtilities.getMessageWithParam(detailColumn.getCellType(), null)) ?
                    messagesUtilities.getMessageWithParam(detailColumn.getCellType(), null) : detailColumn.getCellType();
            buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.type.incorrect",new String[] {columnName,valueType}));
            buildEmailErrorList(excelEmailContent, messagesUtilities.getMessageWithParam("upload.excel.email.incorrect.data.format", null));
        }

        //remove trailing zeros if cellType is NUMERIC
        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        //get cell's value
        switch(cellType) {
            case _NONE:
                cellValue = StringUtils.EMPTY;
                break;
            case NUMERIC:
                cellValue = StringUtils.EMPTY + decimalFormat.format(cell.getNumericCellValue());
                break;
            case STRING:
                cellValue = cell.getStringCellValue();
                break;
            case FORMULA:
                cellValue = cell.getCellFormula();
                break;
            case BLANK:
                cellValue = StringUtils.EMPTY;
                break;
            case BOOLEAN:
                cellValue = StringUtils.EMPTY + cell.getBooleanCellValue();
                break;
        }

        //check status(active--inactive)
        if (!StringUtils.isEmpty(detailColumn.getFieldName()) && (detailColumn.getFieldName().equals("activeInd") || detailColumn.getFieldName().equals("isActive"))) {
            if (cellValue.trim().equalsIgnoreCase(ACTIVE_CELL_VALUE)) {
                cellValue = String.valueOf(Boolean.TRUE);
            } else if (cellValue.trim().equalsIgnoreCase(INACTIVE_CELL_VALUE)) {
                cellValue = String.valueOf(Boolean.FALSE);
            } else {
                buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.data.invalid.option", new String[] {"Status","Active/Inactive"}));
                buildEmailErrorList(excelEmailContent, messagesUtilities.getMessageWithParam("upload.excel.email.invalid.entries", null));
            }
        }

        //check data formats
        if (!StringUtils.isEmpty(detailColumn.getPattern()) && !cellValue.matches(detailColumn.getPattern())) {
            buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.invalid.data.format", new String[] {columnName}));
            buildEmailErrorList(excelEmailContent, messagesUtilities.getMessageWithParam("upload.excel.email.incorrect.data.format", null));
        }

        //check max length
        if (detailColumn.getMaxLength() != null && (cellValue.length() > detailColumn.getMaxLength())) {
            buildCellErrors(cellErrors, messagesUtilities.getMessageWithParam("upload.excel.error.above.max.length" , new String[] {columnName,detailColumn.getMaxLength().toString()}));
            buildEmailErrorList(excelEmailContent, messagesUtilities.getMessageWithParam("upload.excel.email.invalid.max.length", null));
        }

        if (!StringUtils.isEmpty(cellErrors.toString())) {
            mapExcelError.put(validatedDataIndex, cellErrors);
        }

        return cellValue;
    }

    public byte[] createAttachedFile(Workbook workbook, Sheet sheet, String fileName, Map<Integer, StringBuilder> mapExcelErrors,
                                            UploadTemplateHdrIdDto exportTemplate, Long timeStartReadingFile)
    {
        Integer startRow = exportTemplate.getStartRow();

        List<UploadTemplateDetIdDto> listTempDetail = exportTemplate.getListTempDetail();
        Map<String, UploadTemplateDetIdDto> detailByColumnName = listTempDetail.stream()
                .collect(Collectors.toMap(UploadTemplateDetIdDto::getColumnFullName, Function.identity()));

        try {
            if (workbook != null) {
                Map<Integer, String> headerInputValueMap = getRowHeaderDataInputExcelFile(sheet, exportTemplate);
                Map<Integer, String> redundantFieldMap = new HashMap<>();
                List<String> listHeaderTemplate = new ArrayList<>();

                exportTemplate.getListTempDetail().forEach(uploadTemplateDetIdDto -> {
                    listHeaderTemplate.add(uploadTemplateDetIdDto.getColumnName());
                });
                headerInputValueMap.forEach((key, val) -> {
                    if (!listHeaderTemplate.contains(val)){
                        redundantFieldMap.put(key, val);
                    }
                });

                int lastCell = 0;
                CellStyle cellStyle = getAttachCellStyle(workbook);

                //build map detailByUniqueColumnPosition
                Map<Integer, UploadTemplateDetIdDto> detailByUniqueColumnPosition = new HashMap();

                Row columnHeaderRow = sheet.getRow(sheet.getFirstRowNum());
                Iterator<Cell> cellIterator = columnHeaderRow.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell headerCell = cellIterator.next();
                    String columnName = getCellValue(headerCell);

                    UploadTemplateDetIdDto templateDetail = detailByColumnName.get(columnName);

                    if (templateDetail != null && templateDetail.getNoneDuplicated().equals(1)){
                        detailByUniqueColumnPosition.put(headerCell.getColumnIndex(), templateDetail);
                    }
                }

                for (Integer rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (checkIfRowIsEmpty(row)) continue;

                    // put unique columns to the 1st column
                    detailByUniqueColumnPosition.keySet().forEach(uniqueColumnPosition -> {
                            Integer toCellPosition = detailByUniqueColumnPosition.get(uniqueColumnPosition).getPosition();
                            changeColumnPosition(row, uniqueColumnPosition, toCellPosition, cellStyle);
                        }
                    );

                    int lastCellNum = row.getLastCellNum();
                    if (lastCell < lastCellNum) {
                        lastCell = lastCellNum;
                        //set column errors' width
                        sheet.setColumnWidth(lastCell, AppConstants.ExcelTemplateCodes.ERROR_COLUMN_WIDTH);
                    }
                }

                Boolean emptySheet = checkEmptySheet(sheet);
                int rowDataIndex = 0;
                if (emptySheet){
                    buildErrorForEmptyFile(sheet,workbook,mapExcelErrors,startRow);
                }else if (mapExcelErrors.size() > 0){
                    int firstDataRow = sheet.getFirstRowNum();
                    Font font = workbook.createFont();
                    font.setColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
                    CellStyle errorHeaderStyle = getAttachErrorHeaderStyle(workbook, font);
                    CellStyle errorStyle = getAttachErrorStyle(workbook, font);
                    for (int rowIndex = firstDataRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                        Row row = sheet.getRow(rowIndex);
                        if (checkIfRowIsEmpty(row)) {
                            continue;
                        }

                        if (row.getRowNum() == firstDataRow) {
                            Cell errorColumnName = row.createCell(lastCell);
                            errorColumnName.setCellStyle(errorHeaderStyle);
                            errorColumnName.setCellValue(messagesUtilities.getMessageWithParam("upload.excel.attach.file.error.column", null));

                        } else {
                            //writing errors at the last cell of every row;
                            StringBuilder errors = mapExcelErrors.get(rowDataIndex);
                            if (errors != null)
                            {
                                Cell cellToPutErrors = row.createCell(lastCell);
                                cellToPutErrors.setCellStyle(errorStyle);
                                cellToPutErrors.setCellValue(errors.toString().replace(";", "\n"));
                            }
                            rowDataIndex++;
                        }
                    }
                }
                //approximate processing time
                String processingTime = calculateProcessingTime(timeStartReadingFile);
                putProcessingTime(mapExcelErrors, sheet, workbook, lastCell, processingTime);

                //sortMapByValue
                Map<Integer, String> result = redundantFieldMap.entrySet().stream()
                        .sorted( (c1, c2) -> c2.getKey().compareTo(c1.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue, LinkedHashMap::new));
                //remove unnecessary columns of the inputted file
                if (!emptySheet){
                    result.forEach((position, redundantField) -> {
                        deleteColumn(sheet, position);
                    });
                }
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            workbook.close();

            byte[] attachedFileByteArr = bos.toByteArray();

            bos.close();

            return attachedFileByteArr;
        } catch (Exception e) {
            log.error(e.toString());
        }
        return null;
    }

    public String calculateProcessingTime(Long timeStartReadingFile){
        Long timeEndReadingFile = DateFormatUtil.getCurrentUTCMilisecond();
        Long processingTime = timeEndReadingFile - timeStartReadingFile;
        int second = processingTime < 1000 ? 1 : ((int) (processingTime / MILLISECOND)) % SECOND;
        int minute = (int) (processingTime / (MILLISECOND * SECOND));

        return messagesUtilities.getMessageWithParam("upload.excel.processing.time",new String[] {String.valueOf(minute), String.valueOf(second)});
    }

    public void putProcessingTime(Map<Integer, StringBuilder> mapExcelErrors, Sheet sheet, Workbook workbook,
                                  int lastCell, String processingTime){
        Integer firstDataRow = sheet.getFirstRowNum();
        Row headerRow = sheet.getRow(firstDataRow);

        CellStyle errorHeaderStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
        errorHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        errorHeaderStyle.setFont(font);
        errorHeaderStyle.setWrapText(true);

        Integer processingCellPosition = mapExcelErrors.size() == 0 ? lastCell : lastCell + 1;
        Cell processingTimeCell = headerRow.createCell(processingCellPosition);
        processingTimeCell.setCellStyle(errorHeaderStyle);
        processingTimeCell.setCellValue(processingTime);
        sheet.setColumnWidth(processingCellPosition, AppConstants.ExcelTemplateCodes.ERROR_COLUMN_WIDTH);
    }

    private CellStyle getAttachCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        return cellStyle;
    }

    private CellStyle getAttachErrorHeaderStyle(Workbook workbook, Font font) {
        CellStyle errorHeaderStyle = workbook.createCellStyle();
        errorHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        errorHeaderStyle.setFont(font);
        errorHeaderStyle.setWrapText(true);
        return errorHeaderStyle;
    }

    private CellStyle getAttachErrorStyle(Workbook workbook, Font font) {
        CellStyle errorStyle = workbook.createCellStyle();
        errorStyle.setVerticalAlignment(VerticalAlignment.TOP);
        errorStyle.setAlignment(HorizontalAlignment.LEFT);
        errorStyle.setFont(font);
        errorStyle.setWrapText(true);
        return errorStyle;
    }

    public void buildErrorForEmptyFile(Sheet sheet, Workbook workbook, Map<Integer, StringBuilder> mapExcelErrors, Integer startRow){
        Row errorHeader = sheet.createRow(0);

        CellStyle errorHeaderStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
        errorHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        errorHeaderStyle.setFont(font);
        errorHeaderStyle.setWrapText(true);

        CellStyle errorStyle = workbook.createCellStyle();
        errorStyle.setVerticalAlignment(VerticalAlignment.TOP);
        errorStyle.setAlignment(HorizontalAlignment.LEFT);
        errorStyle.setFont(font);
        errorStyle.setWrapText(true);

        Cell errorColumnName = errorHeader.createCell(0);
        errorColumnName.setCellStyle(errorHeaderStyle);
        errorColumnName.setCellValue(messagesUtilities.getMessageWithParam("upload.excel.attach.file.error.column", null));

        //writing errors at the last cell of every row
        List<StringBuilder> listError = new ArrayList<>(mapExcelErrors.values());
        StringBuilder errors = listError.size() > 0 ? listError.get(0) : null;
        if (errors != null)
        {
            Row errorRow = sheet.createRow(1);
            Cell cellToPutErrors = errorRow.createCell(0);
            cellToPutErrors.setCellStyle(errorStyle);
            cellToPutErrors.setCellValue(errors.toString());
        }
    }

    public boolean checkEmptySheet(Sheet sheet)
    {
        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++)
        {
            Row row = sheet.getRow(rowIndex);
            if (!checkIfRowIsEmpty(row)){
                return false;
            }
        }

        return true;
    }

    public Map<Integer, String> getRowHeaderDataInputExcelFile(Sheet sheet, UploadTemplateHdrIdDto exportTemplate){
        Map<Integer, String> headerMap = new HashMap<>();
        try {
            if (sheet == null) {
                return headerMap;
            }

            Row row = sheet.getRow(sheet.getFirstRowNum());

            int maxLengthOfCell = 0;
            for (Integer rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row1 = sheet.getRow(rowIndex);
                if (row1 == null) continue;
                int lastRow = row1.getLastCellNum();
                if (maxLengthOfCell < lastRow) {
                    maxLengthOfCell = lastRow;
                }
            }

            for (int columnIndex = 0;columnIndex < maxLengthOfCell;columnIndex++){
                Cell headerCell = row.getCell(columnIndex);
                String columnName = getCellValue(headerCell);
                headerMap.put(columnIndex, columnName);
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return headerMap;
    }

    public void deleteColumn(Sheet sheet, int columnToDelete) {
        for (int rId = 0; rId <= sheet.getLastRowNum(); rId++) {
            Row row = sheet.getRow(rId);

            if (row == null){
                continue;
            }

            for (int cID = columnToDelete; cID < row.getLastCellNum(); cID++) {
                Cell cOld = row.getCell(cID);
                if (cOld != null) {
                    row.removeCell(cOld);
                }
                Cell cNext = row.getCell(cID + 1);
                if (cNext != null) {
                    Cell cNew = row.createCell(cID, cNext.getCellType());
                    cloneCell(cNew, cNext);
                    if(rId == 0) {
                        sheet.setColumnWidth(cID, sheet.getColumnWidth(cID + 1));

                    }
                }
            }
        }
    }

    public void changeColumnPosition(Row row, Integer fromCellPosition, Integer toCellPosition, CellStyle cellStyle) {
        if(fromCellPosition.equals(toCellPosition)){
            return;
        }

        // get data of fromCell
        Cell fromCell = row.getCell(fromCellPosition);
        String fromCellValue = getCellValue(fromCell);

        // get data of toCell
        Cell toCell = row.getCell(toCellPosition);
        String toCellValue = getCellValue(toCell);

        // create new fromCell and toCell
        fromCell.setCellValue(toCellValue);
        toCell.setCellValue(fromCellValue);
    }

    public void cloneCell(Cell cNew, Cell cOld) {

        cNew.setCellComment(cOld.getCellComment());
        cNew.setCellStyle(cOld.getCellStyle());
        if (CellType.BOOLEAN == cNew.getCellType()) {
            cNew.setCellValue(cOld.getBooleanCellValue());
        } else if (CellType.NUMERIC == cNew.getCellType()) {
            cNew.setCellValue(cOld.getNumericCellValue());
        } else if (CellType.STRING == cNew.getCellType()) {
            cNew.setCellValue(cOld.getStringCellValue());
        } else if (CellType.ERROR == cNew.getCellType()) {
            cNew.setCellValue(cOld.getErrorCellValue());
        } else if (CellType.FORMULA == cNew.getCellType()) {
            cNew.setCellValue(cOld.getCellFormula());
        }
    }

    public void buildCellErrors(StringBuilder originalCellErrors, String newError)
    {
        if (StringUtils.isEmpty(newError)) {
            return;
        }

        if (StringUtils.isEmpty(originalCellErrors.toString())) {
            originalCellErrors.append("- ").append(newError);
        } else {
            originalCellErrors.append("\n- ").append(newError);
        }
    }

    public void buildEmailErrorList(Set<String> excelEmailContent, String newError)
    {
        excelEmailContent.add(newError);
    }

    private boolean checkIfRowIsEmpty(Row row) {
        if (row == null) {
            return true;
        }
        if (row.getLastCellNum() <= 0) {
            return true;
        }

        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK && StringUtils.isNotBlank(cell.toString())) {
                return false;
            }
        }
        return true;
    }


    public boolean validateExcelHeader(Class cls, Workbook workbook, Sheet sheet, String fileName,
                                                    UploadTemplateHdrIdDto uploadTemplateHdrIdDto, Map<Integer, StringBuilder> mapExcelErrors,
                                                    Set<String> excelEmailContent, ExcelResponseMessage excelResponseMessage) throws TransactionException {
        calculateTimeForPopup(sheet,excelResponseMessage);

        List<UploadTemplateDetIdDto> columnTemplateDetail = uploadTemplateHdrIdDto.getListTempDetail();
        try {
            Map<String, UploadTemplateDetIdDto> columnDetailByColumnName = new HashMap<>();
            Map<Integer, UploadTemplateDetIdDto> columnDetailByColumnPosition = new HashMap<>();
            Map<Integer, Field> fieldByColumnPosition = new HashMap<>();
            Map<String, Field> fieldByFieldName = new HashMap<>();
            StringBuilder fieldHeaderError = new StringBuilder();

            for (UploadTemplateDetIdDto detailColumn : uploadTemplateHdrIdDto.getListTempDetail()) {
                if (!StringUtils.isEmpty(detailColumn.getColumnName())) {
                    columnDetailByColumnName.put(detailColumn.getColumnName().trim().toLowerCase(), detailColumn);
                }
            }

            List<Field> fieldList = new ArrayList<>();
            getFieldsOfAClass(cls, fieldList);

            for (Field field : fieldList) {
                field.setAccessible(true);
                fieldByFieldName.put(field.getName(), field);
            }

            // handling columnName
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            excelResponseMessage = handlingColumnHeader(headerRow, excelEmailContent, fieldByColumnPosition,
                    fieldByFieldName, columnDetailByColumnName, columnDetailByColumnPosition, columnTemplateDetail,
                    fieldHeaderError, mapExcelErrors, excelResponseMessage);

            if (!StringUtils.isEmpty(excelResponseMessage.getType())
                    && excelResponseMessage.getCode().equals(AppConstants.UploadExcelResponse.INVALID_HEADERS)){
                return false;
            }

        } catch (Exception e) {
            log.error(e.toString());
            excelResponseMessage
                    .setMessage(messagesUtilities.getMessageWithParam("upload.excel.response.invalid.headers", null));
            excelResponseMessage.setCode(AppConstants.UploadExcelResponse.INVALID_HEADERS);
            excelResponseMessage.setType(AppConstants.UploadExcelResponse.INVALID_FILE);
            return false;
        }

        return true;
    }

    public boolean checkEmptyFile(Sheet sheet, Integer headerRowNumber, ExcelResponseMessage excelResponseMessage) {
        boolean noRecord = true;
        for (int rowIndex = headerRowNumber+1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (!checkIfRowIsEmpty(row)){
                noRecord = false;
                break;
            }
        }

        if (!noRecord){
            return false;
        }

        excelResponseMessage
                .setMessage(messagesUtilities.getResourceMessage("upload.excel.response.empty.file", null));
        excelResponseMessage.setCode(AppConstants.UploadExcelResponse.EMPTY_FILE);
        excelResponseMessage.setType(AppConstants.UploadExcelResponse.INVALID_FILE);

        return true;
    }

    public UploadTemplateHdrIdDto getExcelTemplate (String excelTemplateCode){
        List<UploadTemplateHdrIdDto> listTemplate = excelClient.findTemplateByCode(excelTemplateCode);

        if (listTemplate.isEmpty()){
            throw new TransactionException("exception.excel.template.not.exist");
        }

        UploadTemplateHdrIdDto uploadTemplateHdrIdDto = listTemplate.get(0);
        return uploadTemplateHdrIdDto;
    }

    public byte[] getByteArrOfUploadedFile(MultipartFile file){
        byte[] byteArr = null;
        try{
            byteArr = file.getBytes();
        } catch (IOException e) {
            throw new TransactionException("exception.excel.read.excel.file.fail");
        }
        return byteArr;
    }

    public Workbook initWorkbook(byte[] byteArr, String fileName){
        InputStream inputStream = new ByteArrayInputStream(byteArr);
        Workbook workbook = null;
        try{
            if (fileName.endsWith("xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                workbook = new HSSFWorkbook(inputStream);
            }
        } catch (IOException e){
            throw new TransactionException("exception.excel.read.excel.file.fail");
        }
        return workbook;
    }

    public boolean validFileType(String fileName, ExcelResponseMessage excelResponseMessage){
        if (fileName.endsWith("xlsx") || fileName.endsWith("xls")){
            return true;
        } else {
            excelResponseMessage
                    .setMessage(messagesUtilities.getMessageWithParam("upload.excel.response.invalid.file.type", null));
            excelResponseMessage.setCode(AppConstants.UploadExcelResponse.INVALID_FILE_TYPE);
            excelResponseMessage.setType(AppConstants.UploadExcelResponse.INVALID_FILE);
            return false;
        }
    }

    public boolean validSheetNumber(Workbook workbook,int dataSheetIndex, ExcelResponseMessage excelResponseMessage){
        if (dataSheetIndex < workbook.getNumberOfSheets()){
            return true;
        } else {
            excelResponseMessage
                    .setMessage(messagesUtilities.getMessageWithParam("upload.excel.response.invalid.sheets", null));
            excelResponseMessage.setCode(AppConstants.UploadExcelResponse.INVALID_HEADERS);
            excelResponseMessage.setType(AppConstants.UploadExcelResponse.INVALID_FILE);
            return false;
        }
    }

    public void calculateTimeForPopup(Sheet sheet, ExcelResponseMessage excelResponseMessage){
        int numberOfRecords = 0;
        for (Integer rowIndex = sheet.getFirstRowNum()+1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if(!checkIfRowIsEmpty(row)){
                numberOfRecords ++;
            }
        }
        //avg time to upload a recode: second, estimateProcessingTime: minute
        double estimateProcessingTime = (AppConstants.UploadExcelResponse.AVG_RECORD_PROCESSING_TIME * numberOfRecords * 2) / 60;
        int roundedProcessingTime = (int) Math.ceil(estimateProcessingTime);

        if (roundedProcessingTime <1)
            roundedProcessingTime = 1;

        excelResponseMessage.setType(AppConstants.UploadExcelResponse.UPLOADED_FILE);
        excelResponseMessage
                .setMessage(messagesUtilities.getMessageWithParam("upload.excel.response.uploaded.file", new String[] {String.valueOf(roundedProcessingTime)}));
        excelResponseMessage.setCode(AppConstants.UploadExcelResponse.UPLOADED_FILE);
    }
}