package com.y3technologies.masters.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.y3technologies.masters.client.EmailClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.excel.UploadTemplateDetIdDto;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.table.ReasonCodeTableDto;
import com.y3technologies.masters.model.ReasonCode;
import com.y3technologies.masters.repository.ReasonCodeRepository;
import com.y3technologies.masters.service.ReasonCodeService;
import com.y3technologies.masters.service.impl.ReasonCodeServiceImpl;
import com.y3technologies.masters.util.DateFormatUtil;
import com.y3technologies.masters.util.ExcelUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Transactional
@PropertySource("classpath:application.properties")
@ContextConfiguration
public class ReasonCodeControllerTests {

	private static final Logger logger = LoggerFactory.getLogger(ReasonCodeControllerTests.class);
	SessionUserInfoDTO sessionObj;

	private MockMvc mvc;
	@Autowired
	private WebApplicationContext wac;

	@Value("${api.version.masters}")
	private String apiVersion;

	private String BASE_URL;

	@Autowired
	private ReasonCodeRepository reasonCodeRepository;

	@Autowired
	private ReasonCodeController reasonCodeController;

	@MockBean
	private ExcelClient excelClient;

	@SpyBean
	private ExcelUtils excelUtils;

	@SpyBean
	private ReasonCodeService reasonCodeServiceSpy;

	@SpyBean
	private ReasonCodeServiceImpl reasonCodeServiceImpl;

	@MockBean
	private EmailClient emailClient;

	@PostConstruct
	public void setApiVersion() {
		BASE_URL = "/" + apiVersion + "/reasonCode";
	}

	@Before
	public void setup() {
		this.mvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ModelMapper mapper = new ModelMapper();
		sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");
	}

	@AfterEach
	public void remove() throws Exception {
		reasonCodeRepository.deleteAll();
		reasonCodeRepository = null;
	}

	@Test
	public void testReasonCodeController() throws Exception {

		String result;
		/*** create ***/
		ReasonCode model = new ReasonCode();
		model.setTenantId(1l);
		model.setReasonCode("r1");
		model.setReasonDescription("Reason");
		model.setUsage("usage1");
		model.setCategory("category1|category2");
		ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
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
		model = mapper.readValue(resultJson, ReasonCode.class);
		model.setReasonDescription("Reason2");
		requestJson = ow.writeValueAsString(model);
		result = mvc.perform(post(BASE_URL + "/update").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("update result is {}", result);

		/*** listByParam ***/
		model = new ReasonCode();
		model.setTenantId(1l);
		model.setReasonCode("r1");
		requestJson = ow.writeValueAsString(model);
		result = mvc
				.perform(get(BASE_URL + "/listByParam").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("listByParam result is {}", result);

		/*** updateStatus ***/
		RequestBuilder updateStatus = get(BASE_URL + "/updateStatus?id=" + id + "&status=" + false);
		logger.info("updateStatus result is {}",
				mvc.perform(updateStatus).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());

		/*** query ***/
		DataTablesInput input = new DataTablesInput();
		input.setStart(0);
		input.setLength(5);
		Column column = new Column();
		column.setData("reasonDescription");
		column.setSearch(new Search("Reason2", Boolean.TRUE));
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
	public void testAddDefaultMilestoneForTenant() throws Exception {

		List<ReasonCode> reasonCodeList = reasonCodeRepository.findAll();
		assertTrue(reasonCodeList.size() == 0);

		List<ReasonCode> reasonCodeExcelDtoList = new ArrayList<>();
		ReasonCode dto = new ReasonCode();
		dto.setReasonCode("Reason01");
		dto.setReasonDescription("Reason 01");
		dto.setCategory("SOURCE");
		dto.setUsage("PARTIAL");
		dto.setActiveInd(true);
		reasonCodeExcelDtoList.add(dto);
		reasonCodeRepository.saveAll(reasonCodeExcelDtoList);

		this.mvc.perform(
				put(BASE_URL + "/addReasonCodesForNewUser?tenantId=" + 1L))
				.andExpect(status().isOk());
		reasonCodeList = reasonCodeRepository.findAll();
		assertTrue(reasonCodeList.size() > 0);
	}

	@Test
	public void testDownloadExcelFile() throws Exception {
		List<ReasonCode> listReasonCode = mockListReasonCode();

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.REASONCODE_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);
		Mockito.doReturn(listReasonCode).when(reasonCodeServiceSpy).findByTenantId(any(), any());

		mvc.perform(post(BASE_URL + "/downloadExcel/")
		.content("{}")
		.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
	}

	@Test
	public void testUploadExcelFile() throws Exception {
		//test data errors
		Path dataErrorPath = Paths.get("src/test/resources/excel-templates/ReasonCode/REASONCODE_ERROR.xlsx");

		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.REASONCODE_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(),any(),any(),any(),any(),any());

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"REASONCODE_DATA_ERROR.xlsx",
				"text/plain", Files.newInputStream(dataErrorPath));

		MockHttpServletResponse testDataErrorResponse = mvc.perform(
				multipart(BASE_URL+"/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		//test upload excel successfully
		Path successFilePath = Paths.get("src/test/resources/excel-templates/ReasonCode/REASONCODE_SUCCESS.xlsx");

		mockMultipartFile = new MockMultipartFile("file",
				"REASONCODE_SAVE.xlsx",
				"text/plain", Files.newInputStream(successFilePath));

		MockHttpServletResponse testUploadSuccessResponse = mvc.perform(
				multipart(BASE_URL+"/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		assertEquals(6, reasonCodeRepository.count());
	}

	private List<ReasonCodeTableDto> mockListReasonCodeDto() {
		List<ReasonCodeTableDto> reasonCodeTableDtoList = new ArrayList<>();
		ReasonCodeTableDto reasonCodeTableDto = new ReasonCodeTableDto();
		reasonCodeTableDto.setReasonCode("rscode");
		reasonCodeTableDto.setReasonDescription("rsdescription");
		reasonCodeTableDto.setExcelRowPosition(3);
		reasonCodeTableDtoList.add(reasonCodeTableDto);

		ReasonCodeTableDto reasonCodeTableDto1 = new ReasonCodeTableDto();
		reasonCodeTableDto1.setReasonCode("rscode");
		reasonCodeTableDto1.setReasonDescription("rsdescription");
		reasonCodeTableDto1.setExcelRowPosition(5);
		reasonCodeTableDtoList.add(reasonCodeTableDto1);

		return reasonCodeTableDtoList;
	}

	public List<ReasonCode> mockListReasonCode(){
		List<ReasonCode> listReasonCode = new ArrayList();
		ReasonCode reasonCode = new ReasonCode();
		reasonCode.setReasonCode("ABCDE");
		reasonCode.setReasonDescription("ABCDE");
		reasonCode.setCategory("SOURCE");
		reasonCode.setUsage("PARTIAL");
		reasonCode.setInducedBy("CUSTOMER");
		reasonCode.setActiveInd(true);
		reasonCode.setIsDefault(true);
		listReasonCode.add(reasonCode);
		return listReasonCode;
	}

	public List<UploadTemplateHdrIdDto> mockExportExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("REASONCODE_SETTING_EXPORT");
		uploadTemplateHdrIdDto.setTitle("REASONCODE");
		uploadTemplateHdrIdDto.setFileName("REASONCODE.xlsx");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
		det1.setFieldName("reasonCode");
		det1.setPosition(0);
		det1.setWidth(6400);
		det1.setAlignment("HorizontalAlignment.CENTER");
		det1.setColumnName("Reason Code");
		det1.setColumnFullName("Reason Code");
		det1.setActiveInd(1);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("reasonDescription");
		det2.setPosition(1);
		det2.setColumnName("Description");
		det2.setColumnFullName("Description");
		det2.setActiveInd(1);
		listDetail.add(det2);

		UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
		copier.copy(det1,det3,null);
		det3.setFieldName("category");
		det3.setPosition(2);
		det3.setColumnName("Category");
		det3.setColumnFullName("Category");
		det3.setActiveInd(1);
		listDetail.add(det3);

		UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
		copier.copy(det1,det4,null);
		det4.setFieldName("usage");
		det4.setPosition(3);
		det4.setColumnName("Usage Level");
		det4.setColumnFullName("Usage Level");
		det4.setActiveInd(1);
		listDetail.add(det4);

		UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
		copier.copy(det1,det5,null);
		det5.setFieldName("inducedBy");
		det5.setPosition(4);
		det5.setColumnName("Induced By");
		det5.setColumnFullName("Induced By");
		det5.setActiveInd(1);
		listDetail.add(det5);

		UploadTemplateDetIdDto det6 = new UploadTemplateDetIdDto();
		copier.copy(det1,det6,null);
		det6.setFieldName("activeInd");
		det6.setPosition(5);
		det6.setColumnName("Status");
		det6.setColumnFullName("Status");
		det6.setActiveInd(1);
		listDetail.add(det6);

		UploadTemplateDetIdDto det7 = new UploadTemplateDetIdDto();
		copier.copy(det1,det7,null);
		det7.setFieldName("isDefault");
		det7.setPosition(6);
		det7.setColumnName("Is Default");
		det7.setColumnFullName("Is Default");
		det7.setActiveInd(1);
		listDetail.add(det7);

		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}

	public List<UploadTemplateHdrIdDto> mockUploadExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("REASONCODE_SETTING_UPLOAD");
		uploadTemplateHdrIdDto.setTitle("REASONCODE SETTING");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
		det1.setFieldName("reasonCode");
		det1.setColumnName("Reason Code");
		det1.setColumnFullName("Reason Code");
		det1.setMaxLength(255);
		det1.setMandatoryInd(1);
		det1.setActiveInd(1);
		det1.setNoneDuplicated(1);
		det1.setPosition(0);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("reasonDescription");
		det2.setColumnName("Description");
		det2.setColumnFullName("Description");
		det2.setMandatoryInd(1);
		det2.setMaxLength(255);
		det2.setNoneDuplicated(1);
		det2.setPosition(1);
		listDetail.add(det2);

		UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
		copier.copy(det1,det3,null);
		det3.setFieldName("category");
		det3.setColumnName("Category");
		det3.setMandatoryInd(1);
		det3.setColumnFullName("Category");
		det3.setNoneDuplicated(1);
		det3.setPosition(2);
		listDetail.add(det3);

		UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
		copier.copy(det1,det4,null);
		det4.setFieldName("usage");
		det4.setColumnName("Usage Level");
		det4.setColumnFullName("Usage Level");
		det4.setMandatoryInd(1);
		det4.setMaxLength(255);
		det4.setNoneDuplicated(1);
		det4.setPosition(3);
		listDetail.add(det4);

		UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
		copier.copy(det1,det5,null);
		det5.setFieldName("inducedBy");
		det5.setPosition(4);
		det5.setColumnName("Induced By");
		det5.setColumnFullName("Induced By");
		det5.setMandatoryInd(0);
		det5.setMaxLength(255);
		det5.setNoneDuplicated(1);
		det5.setPosition(4);
		listDetail.add(det5);

		UploadTemplateDetIdDto det6 = new UploadTemplateDetIdDto();
		copier.copy(det1,det6,null);
		det6.setFieldName("activeInd");
		det6.setMandatoryInd(1);
		det6.setColumnName("Active Status");
		det6.setColumnFullName("Active Status");
		det6.setNoneDuplicated(1);
		det6.setPosition(5);
		listDetail.add(det6);

		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}

	@Test
	public void testDownloadExcelFileSorting() throws Exception {
		ReasonCode rc1 = new ReasonCode();
		rc1.setTenantId(1L);
		rc1.setReasonCode("rc1");
		rc1.setReasonDescription("rcd1");
		rc1.setCategory("rcc1");
		rc1.setUsage("rcu1");
		rc1.setInducedBy("rci1");

		ReasonCode rc2 = new ReasonCode();
		rc2.setTenantId(1L);
		rc2.setReasonCode("rc2");
		rc2.setReasonDescription("rcd2");
		rc2.setCategory("rcc2");
		rc2.setUsage("rcu2");
		rc2.setInducedBy("rci2");

		ReasonCode rc3 = new ReasonCode();
		rc3.setTenantId(1L);
		rc3.setReasonCode("rc3");
		rc3.setReasonDescription("rcd3");
		rc3.setCategory("rcc3");
		rc3.setUsage("rcu3");
		rc3.setInducedBy("rci3");

		reasonCodeRepository.save(rc1);
		reasonCodeRepository.save(rc2);
		reasonCodeRepository.save(rc3);

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.REASONCODE_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		String json1 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.REASON_CODES_CODE + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
		String json2 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.REASON_CODES_DESCRIPTION + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";
		String json3 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.REASON_CODES_CATEGORY + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
		String json4 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.REASON_CODES_INDUCED_BY + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";
		String json5 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.REASON_CODES_USAGE_LEVEL + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionObj)
				.content(json1)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionObj)
				.content(json2)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionObj)
				.content(json3)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionObj)
				.content(json4)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionObj)
				.content(json5)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
	}

	@Test
	public void testDownloadExcelFileFilter() throws Exception {
		ReasonCode rc1 = new ReasonCode();
		rc1.setTenantId(1L);
		rc1.setReasonCode("rc1");
		rc1.setReasonDescription("rcd1");
		rc1.setCategory("rcc1");
		rc1.setUsage("rcu1");
		rc1.setInducedBy("rci1");

		ReasonCode rc2 = new ReasonCode();
		rc2.setTenantId(1L);
		rc2.setReasonCode("rc2");
		rc2.setReasonDescription("rcd2");
		rc2.setCategory("rcc2");
		rc2.setUsage("rcu2");
		rc2.setInducedBy("rci2");

		ReasonCode rc3 = new ReasonCode();
		rc3.setTenantId(1L);
		rc3.setReasonCode("rc3");
		rc3.setReasonDescription("rcd3");
		rc3.setCategory("rcc3");
		rc3.setUsage("rcu3");
		rc3.setInducedBy("rci3");

		reasonCodeRepository.save(rc1);
		reasonCodeRepository.save(rc2);
		reasonCodeRepository.save(rc3);

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.REASONCODE_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		String json = "{\"reasonDescription\":\"1\",\"inducedBy\":\"1\",\"reasonCode\":\"1\",\"category\":\"1\", \"usage\":\"1\"}";

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionObj)
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
	}
}
