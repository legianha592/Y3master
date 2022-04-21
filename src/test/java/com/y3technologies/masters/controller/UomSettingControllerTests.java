package com.y3technologies.masters.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.y3technologies.masters.client.EmailClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.UomSettingDto;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.excel.UploadTemplateDetIdDto;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.model.UomSetting;
import com.y3technologies.masters.repository.UomSettingRepository;
import com.y3technologies.masters.service.UomSettingService;
import com.y3technologies.masters.service.impl.UomSettingServiceImpl;
import com.y3technologies.masters.util.ExcelUtils;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.datatables.mapping.Column;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.Search;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Transactional
@PropertySource("classpath:application.properties")
public class UomSettingControllerTests {

	private static final Logger logger = LoggerFactory.getLogger(UomSettingControllerTests.class);

	private MockMvc mvc;
	private static ObjectMapper objectMapper;
	private UomSetting model;

	@Autowired
	private WebApplicationContext wac;

	@SpyBean
	private UomSettingController uomSettingController;

	@Autowired
	private UomSettingRepository uomSettingRepository;

	@MockBean
	private ExcelClient excelClient;

	@SpyBean
	private UomSettingService uomSettingServiceSpy;

	@SpyBean
	private UomSettingServiceImpl uomSettingServiceImpl;

	@MockBean
	private EmailClient emailClient;

	@SpyBean
	ExcelUtils excelUtils;

	SessionUserInfoDTO sessionObj;

	@Value("${api.version.masters}")
	private String apiVersion;

	private String BASE_URL;

	@PostConstruct
	public void setApiVersion() {
		BASE_URL = "/" + apiVersion + "/uomsetting";
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	public static void setUp() throws Exception {
		objectMapper = new ObjectMapper().registerModule(new ParameterNamesModule()).registerModule(new Jdk8Module())
				.registerModule(new JavaTimeModule());
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}

	@BeforeEach
	public void init() {
		sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(0L);
		sessionObj.setTimezone("Asia/Singapore");

		mvc = MockMvcBuilders.webAppContextSetup(wac).build();
		model = new UomSetting();
		model.setUom("m");
		model.setDescription("meter");
		model.setUomGroup("Length");
		model.setRemark("Remarks");
	}

	@AfterEach
	public void tearDown() {
		uomSettingRepository.deleteAll();
		uomSettingRepository = null;
	}

	@Test
	public void testUomSettingController() throws Exception {

		String result;
		/*** create ***/
		ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(model);

		result = mvc.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("create result is {}", result);

		Long id = Long.valueOf(result);

		/*** retrieve ***/
		RequestBuilder request = get(BASE_URL + "/retrieve?id=" + id);
		String resultJson = mvc.perform(request).andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
		logger.info("retrieve result is {}", resultJson);

		/*** update ***/
		UomSetting update = new UomSetting();
		update.setId(id);
		update.setUom("m");
		update.setDescription("meter");
		update.setUomGroup("packets");
		update.setRemark("Remarks");
		requestJson = ow.writeValueAsString(update);
		result = mvc.perform(post(BASE_URL + "/update").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("update result is {}", result);

		/*** updateStatus ***/
		RequestBuilder updateStatus = get(BASE_URL + "/updateStatus?id=" + id + "&status=" + false);
		logger.info("updateStatus result is {}",
				mvc.perform(updateStatus).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());

		/*** query ***/
		DataTablesInput input = new DataTablesInput();
		input.setStart(0);
		input.setLength(5);
		Column column = new Column();
		column.setData("uom");
		column.setSearch(new Search("m", Boolean.TRUE));
		column.setSearchable(Boolean.TRUE);
		List<Column> cols = new ArrayList<Column>();
		cols.add(column);
		input.setColumns(cols);
		String queryJson = ow.writeValueAsString(input);
		result = mvc.perform(post(BASE_URL + "/query").contentType(MediaType.APPLICATION_JSON).content(queryJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("query result is {}", result);
	}

	@Test
	public void testFindAllActiveUOM() throws Exception {
		int count = uomSettingRepository.findAllActiveUOM().size();
		uomSettingRepository.save(model);
		MockHttpServletResponse response = mvc.perform(get(BASE_URL + "/getAllActiveUom")).andExpect(status().isOk())
				.andReturn().getResponse();
		List<UomSettingDto> resultList = objectMapper.readValue(response.getContentAsString(), objectMapper.getTypeFactory().constructCollectionType(List.class, UomSettingDto.class));
		assertEquals(count + 1, resultList.size());
	}

	@Test
	public void testDownloadExcelFile() throws Exception {
		List<UomSetting> listUomSetting = mockListUomSetting();

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.UOM_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);
		Mockito.doReturn(listUomSetting).when(uomSettingServiceSpy).getAll(any());

		mvc.perform(post(BASE_URL + "/downloadExcel/").requestAttr(("SESSION_INFO"), sessionObj)
		.contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk())
				.andReturn();
	}

	@Test
	public void testUploadErrorExcelFile() throws Exception {
		// test data errors
		Path dataErrorPath = Paths.get("src/test/resources/excel-templates/UOM/UOM_ERROR.xlsx");

		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.UOM_SETTING_UPLOAD))
				.thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(), any(), any(), any(), any(), any());
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "UOM_ERROR.xlsx", "text/plain",
				Files.newInputStream(dataErrorPath));

		mvc.perform(
				multipart(BASE_URL + "/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk()).andReturn();
	}

	@Test
	public void testUploadSuccessExcelFile() throws Exception {
		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.UOM_SETTING_UPLOAD))
				.thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(), any(), any(), any(), any(), any());

		// test upload excel successfully
		Path successFilePath = Paths.get("src/test/resources/excel-templates/UOM/UOM_SUCCESS.xlsx");

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "UOM_SUCCESS.xlsx", "text/plain",
				Files.newInputStream(successFilePath));

		mvc.perform(
				multipart(BASE_URL + "/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk()).andReturn();

		assertEquals(3, uomSettingRepository.count());
	}

	@Test
	void uploadFormulaExcelSuccessFile() throws Exception {
		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.UOM_SETTING_UPLOAD))
				.thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(), any(), any(), any(), any(), any());

		// test upload excel successfully
		Path successFilePath = Paths.get("src/test/resources/excel-templates/UOM/UOM_FORMULA_SUCCESS.xlsx");

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "UOM_FORMULA_SUCCESS.xlsx", "text/plain",
				Files.newInputStream(successFilePath));

		mvc.perform(
				multipart(BASE_URL + "/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk()).andReturn();

		assertEquals(3, uomSettingRepository.count());
	}

	@Test
	void uploadFormulaExcelErrorFile() throws Exception {
		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.UOM_SETTING_UPLOAD))
				.thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(), any(), any(), any(), any(), any());

		// test upload excel successfully
		Path successFilePath = Paths.get("src/test/resources/excel-templates/UOM/UOM_FORMULA_FAIL.xlsx");

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "UOM_FORMULA_FAIL.xlsx", "text/plain",
				Files.newInputStream(successFilePath));

		mvc.perform(
				multipart(BASE_URL + "/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk()).andReturn();

		assertEquals(0, uomSettingRepository.count());
	}

	public List<UomSettingDto> mockListUomSettingDto(){
		List<UomSettingDto> uomSettingDtoList = new ArrayList();
		UomSettingDto uomSettingDto = new UomSettingDto();
		uomSettingDto.setUom("SAVE79");
		uomSettingDto.setDescription("DESCRIPTION1");
		uomSettingDto.setUomGroup("Weight");
		uomSettingDto.setRemark("Remark1");
		uomSettingDto.setActiveInd(true);
		uomSettingDto.setExcelRowPosition(4);
		uomSettingDtoList.add(uomSettingDto);

		UomSettingDto uomSettingDto2 = new UomSettingDto();
		uomSettingDto2.setUom("SAVE80");
		uomSettingDto2.setDescription("DESCRIPTION2");
		uomSettingDto2.setUomGroup("Weight");
		uomSettingDto2.setRemark("Remark2");
		uomSettingDto2.setActiveInd(true);
		uomSettingDto2.setExcelRowPosition(6);
		uomSettingDtoList.add(uomSettingDto2);

		UomSettingDto uomSettingDto3 = new UomSettingDto();
		uomSettingDto3.setUom("SAVE79");
		uomSettingDto3.setDescription("DESCRIPTION3");
		uomSettingDto3.setUomGroup("Weight");
		uomSettingDto3.setRemark("Remark3");
		uomSettingDto3.setActiveInd(true);
		uomSettingDto3.setExcelRowPosition(7);
		uomSettingDtoList.add(uomSettingDto3);

		return uomSettingDtoList;
	}

	public List<UomSetting> mockListUomSetting(){
		List<UomSetting> listUomSetting = new ArrayList();
		UomSetting uomSetting = new UomSetting();
		uomSetting.setUom("ABCDE");
		uomSetting.setDescription("123456");
		uomSetting.setUomGroup("Weight");
		uomSetting.setRemark("Remark");
		uomSetting.setActiveInd(true);
		listUomSetting.add(uomSetting);
		return listUomSetting;
	}

	public List<UploadTemplateHdrIdDto> mockExportExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("UOM_SETTING_EXPORT");
		uploadTemplateHdrIdDto.setTitle("UOM");
		uploadTemplateHdrIdDto.setFileName("UOM.xlsx");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
		det1.setFieldName("uom");
		det1.setPosition(0);
		det1.setWidth(6400);
		det1.setAlignment("HorizontalAlignment.CENTER");
		det1.setColumnName("UOM");
		det1.setColumnFullName("UOM");
		det1.setActiveInd(1);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("description");
		det2.setPosition(1);
		det2.setColumnName("Description");
		det2.setColumnFullName("Description");
		det2.setActiveInd(1);
		listDetail.add(det2);

		UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
		copier.copy(det1,det3,null);
		det3.setFieldName("uomGroup");
		det3.setPosition(2);
		det3.setColumnName("UOM Group");
		det3.setColumnFullName("UOM Group");
		det3.setActiveInd(1);
		listDetail.add(det3);

		UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
		copier.copy(det1,det4,null);
		det4.setFieldName("activeInd");
		det4.setPosition(3);
		det4.setColumnName("Status");
		det4.setColumnFullName("Status");
		det4.setActiveInd(1);
		listDetail.add(det4);

		UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
		copier.copy(det1,det5,null);
		det5.setFieldName("remark");
		det5.setPosition(4);
		det5.setColumnName("Remark");
		det5.setColumnFullName("Remark");
		det5.setActiveInd(1);
		listDetail.add(det5);

		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}

	public List<UploadTemplateHdrIdDto> mockUploadExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("UOM_SETTING_UPLOAD");
		uploadTemplateHdrIdDto.setTitle("UOM SETTING");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
		det1.setFieldName("uom");
		det1.setColumnName("UOM");
		det1.setColumnFullName("UOM");
		det1.setMaxLength(255);
		det1.setMandatoryInd(1);
		det1.setNoneDuplicated(1);
		det1.setPosition(0);
		det1.setActiveInd(1);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("description");
		det2.setColumnName("Description");
		det2.setColumnFullName("Description");
		det2.setMandatoryInd(1);
		det2.setNoneDuplicated(1);
		det2.setPosition(1);
		det2.setMaxLength(255);
		listDetail.add(det2);

		UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
		copier.copy(det1,det3,null);
		det3.setFieldName("uomGroup");
		det3.setColumnName("UOM Group");
		det3.setMandatoryInd(1);
		det3.setNoneDuplicated(1);
		det3.setPosition(2);
		det3.setColumnFullName("UOM Group");
		listDetail.add(det3);

		UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
		copier.copy(det1,det4,null);
		det4.setFieldName("activeInd");
		det4.setColumnName("Status");
		det4.setColumnFullName("Status");
		det4.setMandatoryInd(1);
		det4.setNoneDuplicated(1);
		det4.setPosition(3);
		det4.setMaxLength(255);
		listDetail.add(det4);

		UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
		copier.copy(det1,det5,null);
		det5.setFieldName("remark");
		det5.setPosition(4);
		det5.setColumnName("Remark");
		det5.setColumnFullName("Remark");
		det5.setMandatoryInd(1);
		det5.setNoneDuplicated(1);
		det5.setPosition(4);
		det5.setMaxLength(255);
		listDetail.add(det5);

		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}

	@Test
	public void testDownloadExcelFileSorting() throws Exception {
		this.initDataUom();

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.UOM_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		String json1 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.UOM_UOM + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
		String json2 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.UOM_DESCRIPTION + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";
		String json3 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.UOM_GROUP + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";

		mvc.perform(post(BASE_URL + "/downloadExcel/").requestAttr(("SESSION_INFO"), sessionObj)
				.contentType(MediaType.APPLICATION_JSON).content(json1)).andExpect(status().isOk())
				.andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel/").requestAttr(("SESSION_INFO"), sessionObj)
				.contentType(MediaType.APPLICATION_JSON).content(json2)).andExpect(status().isOk())
				.andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel/").requestAttr(("SESSION_INFO"), sessionObj)
				.contentType(MediaType.APPLICATION_JSON).content(json3)).andExpect(status().isOk())
				.andReturn();
	}

	@Test
	public void testDownloadExcelFilter() throws Exception {
		this.initDataUom();

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.UOM_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		String json = "{\"description\":\"1\",\"uom\":\"1\",\"uomGroup\":\"1\"}";

		mvc.perform(post(BASE_URL + "/downloadExcel/").requestAttr(("SESSION_INFO"), sessionObj)
				.contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isOk())
				.andReturn();
	}

	private void initDataUom() {
		UomSetting uom1 = new UomSetting();
		uom1.setUom("uom1");
		uom1.setDescription("uomd1");
		uom1.setRemark("uomr1");
		uom1.setUomGroup("packages1");

		UomSetting uom2 = new UomSetting();
		uom2.setUom("uom2");
		uom2.setDescription("uomd2");
		uom2.setRemark("uomr2");
		uom2.setUomGroup("packages2");

		UomSetting uom3 = new UomSetting();
		uom3.setUom("uom3");
		uom3.setDescription("uomd3");
		uom3.setRemark("uomr3");
		uom3.setUomGroup("packages3");

		uomSettingRepository.save(uom1);
		uomSettingRepository.save(uom2);
		uomSettingRepository.save(uom3);
	}
}
