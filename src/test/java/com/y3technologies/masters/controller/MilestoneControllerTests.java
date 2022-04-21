package com.y3technologies.masters.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.MilestoneDTO;
import com.y3technologies.masters.dto.MilestoneStatusConfigDTO;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.excel.UploadTemplateDetIdDto;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.model.CommonTag;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.Milestone;
import com.y3technologies.masters.repository.CommonTagRepository;
import com.y3technologies.masters.repository.LookupRepository;
import com.y3technologies.masters.repository.MilestoneRepository;
import com.y3technologies.masters.service.MilestoneService;
import com.y3technologies.masters.service.impl.MilestoneServiceImpl;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;

import org.junit.FixMethodOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.data.jpa.datatables.mapping.Column;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.Search;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MilestoneControllerTests {

	private static final Logger logger = LoggerFactory.getLogger(MilestoneControllerTests.class);

	private MockMvc mvc;
	private ObjectMapper mapper;
	@Autowired
	private WebApplicationContext wac;

	@SpyBean
	MilestoneService milestoneService;

	@SpyBean
	MilestoneServiceImpl milestoneServiceImpl;

	@MockBean
	ExcelClient excelClient;

	@SpyBean
	ExcelUtils excelUtils;

	@Value("${api.version.masters}")
	private String apiVersion;

	private String BASE_URL;

	@Autowired
	private MilestoneRepository milestoneRepository;

	@Autowired
	CommonTagRepository commonTagRepository;

	@Autowired
	private MessagesUtilities messageUtilities;

	@Autowired
	private LookupRepository lookupRepository;

	@PostConstruct
	public void setApiVersion() {
		BASE_URL = "/" + apiVersion + "/milestone";
	}

	SessionUserInfoDTO sessionObj;

	Lookup lk1Save;
	Lookup lk2Save;
	Lookup lk3Save;
	Lookup lk4Save;

	@BeforeEach
	public void initData() {
		sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");

		mvc = MockMvcBuilders.webAppContextSetup(wac).build();
		Lookup lk1 = new Lookup();
		lk1.setLookupType("MilestoneUpdateStatusType");
		lk1.setLookupCode("COMPLETE");
		lk1.setLookupDescription("COMPLETE");
		lk1.setTenantId(0L);

		Lookup lk2 = new Lookup();
		lk2.setLookupType("MilestoneUpdateStatusType");
		lk2.setLookupCode("PARTIAL");
		lk2.setLookupDescription("PARTIAL");
		lk2.setTenantId(0L);

		Lookup lk3 = new Lookup();
		lk3.setLookupType("MilestoneUpdateStatusType");
		lk3.setLookupCode("FAILED");
		lk3.setLookupDescription("FAILED");
		lk3.setTenantId(0L);

		Lookup lk4 = new Lookup();
		lk4.setLookupType("MilestoneUpdateStatusType");
		lk4.setLookupCode("SUCCESS");
		lk4.setLookupDescription("SUCCESS");
		lk4.setTenantId(0L);

		lk1Save = lookupRepository.save(lk1);
		lk2Save = lookupRepository.save(lk2);
		lk3Save = lookupRepository.save(lk3);
		lk4Save = lookupRepository.save(lk4);
	}

	@AfterEach
	public void remove() throws Exception {
		milestoneRepository.deleteAll();
		milestoneRepository = null;
		commonTagRepository.deleteAll();
		lookupRepository.deleteAll();
		lk1Save = null;
		lk2Save = null;
		lk3Save = null;
		lk4Save = null;
	}

	@Test
	public void testMilestoneController() throws Exception {
		
		List<MilestoneStatusConfigDTO> configDTOList = new ArrayList<>();
		MilestoneStatusConfigDTO configDTO = new MilestoneStatusConfigDTO();
		configDTO.setLookupId(lk1Save.getId());
		configDTO.setActivated(true);
		
		configDTOList.add(configDTO);
		
		String result;
		/*** create ***/
		MilestoneDTO modelDTO = new MilestoneDTO();
		Milestone model = new Milestone();
		modelDTO.setTenantId(1l);
		modelDTO.setMilestoneCode("OO");
		modelDTO.setMilestoneDescription("Order Creation");
		modelDTO.setCustomerDescription("customer desc");
		modelDTO.setMilestoneCategory("category1|category2");
		modelDTO.setMilestoneStatusConfigDtoList(configDTOList);
		mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(modelDTO);
		result = mvc.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("create result is {}", result);
		
		model = mapper.readValue(result, Milestone.class);

		Long id = Long.valueOf(model.getId());

		/*** retrieve ***/
		RequestBuilder request = get(BASE_URL + "/retrieve?id=" + id);
		String resultJson = mvc.perform(request).andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
		logger.info("retrieve result is {}", resultJson);

//		/*** update ***/
//		modelDTO.setId(model.getId());
//		modelDTO.setCustomerDescription("customer desc 2");
//		requestJson = ow.writeValueAsString(modelDTO);
//		result = mvc.perform(put(BASE_URL + "/update").contentType(MediaType.APPLICATION_JSON).content(requestJson))
//				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
//		logger.info("update result is {}", result);

		/*** listByParam ***/
		model.setTenantId(1l);
		model.setMilestoneCode("OC");
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
		column.setData("milestoneDescription");
		column.setSearch(new Search("Order Creation", Boolean.TRUE));
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

		List<Milestone> milestoneList = milestoneRepository.findAll();
		assertTrue(milestoneList.size() == 0);

		List<Milestone> milestoneList1 = new ArrayList<>();
		Milestone milestone1 = new Milestone();
		milestone1.setMilestoneCode("MilestoneCode1");
		milestone1.setMilestoneDescription("Description1");
		milestone1.setCustomerDescription("CustomerDescription1");
		milestone1.setMilestoneCategory("source,transit");
		milestone1.setIsInternal(true);
		milestoneList1.add(milestone1);
		Milestone milestone2 = new Milestone();
		milestone2.setMilestoneCode("MilestoneCode2");
		milestone2.setMilestoneDescription("Description2");
		milestone2.setCustomerDescription("CustomerDescription2");
		milestone2.setMilestoneCategory("source,transit");
		milestone2.setIsInternal(true);
		milestoneList1.add(milestone2);
		milestoneRepository.saveAll(milestoneList1);

		this.mvc.perform(
				put(BASE_URL + "/addMilestonesForNewUser?tenantId=" + 1L))
				.andExpect(status().isOk());
		milestoneList = milestoneRepository.findAll();
		assertTrue(milestoneList.size() > 0);
	}

	@Test
	public void testDownloadExcel() throws Exception {
		List<Milestone> milestoneList = mockListMilestone();
		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.MILESTONES_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);
		Mockito.doReturn(milestoneList).when(milestoneService).findByTenantId(any(), any());
		mvc.perform(post(BASE_URL + "/downloadExcel")
		.contentType(MediaType.APPLICATION_JSON)
		.content("{}")).andExpect(status().isOk()).andReturn();
	}

	@Test
	public void testUploadExcelError() throws Exception {
		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.MILESTONES_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(),any(),any(),any(),any(),any());
		Path pathError = Paths.get("src/test/resources/excel-templates/Milestone/MILESTONE_UPLOAD_ERROR.xlsx");
		MockMultipartFile fileError = new MockMultipartFile(
				"file",
				"MILESTONE_UPLOAD_ERROR.xlsx",
				"text/plain",
				Files.newInputStream(pathError)
		);
		MockHttpServletResponse responseError = new MockHttpServletResponse();
			responseError = this.mvc
					.perform(multipart(BASE_URL + "/uploadFiles")
							.file(fileError)
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					)
					.andExpect(status().isOk())
					.andReturn()
					.getResponse();

		String message = responseError.getContentAsString();
		Assertions.assertEquals("Uploaded data successfully", message);
	}

	@Test
	public void testUploadExcelSuccess() throws Exception {
		CommonTag commonTag = new CommonTag();
		commonTag.setTag("SOURCE");
		commonTag.setTagType("MilestoneCategory");
		commonTag.setTenantId(sessionObj.getAasTenantId());
		commonTagRepository.save(commonTag);

		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.MILESTONES_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(),any(),any(),any(),any(),any());
		Path pathSuccess = Paths.get("src/test/resources/excel-templates/Milestone/MILESTONE_UPLOAD_SUCCESS.xlsx");
		MockMultipartFile fileSuccess = new MockMultipartFile(
				"file",
				"MILESTONE_UPLOAD_SUCCESS.xlsx",
				"text/plain",
				Files.newInputStream(pathSuccess)
		);
		MockHttpServletResponse responseSuccess = this.mvc
				.perform(multipart(BASE_URL + "/uploadFiles")
						.file(fileSuccess)
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.requestAttr("SESSION_INFO", sessionObj)
				)
				.andExpect(status().isOk())
				.andReturn()
				.getResponse();

		String messageSuccess = responseSuccess.getContentAsString();
		Assertions.assertEquals("Uploaded data successfully", messageSuccess);
		assertEquals(3, milestoneRepository.count());
	}

	@Test
	public void testUploadFormulaExcelSuccess() throws Exception {
		CommonTag commonTag = new CommonTag();
		commonTag.setTag("SOURCE");
		commonTag.setTagType("MilestoneCategory");
		commonTag.setTenantId(sessionObj.getAasTenantId());
		commonTagRepository.save(commonTag);
		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.MILESTONES_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(),any(),any(),any(),any(),any());
		Path pathSuccess = Paths.get("src/test/resources/excel-templates/Milestone/MILESTONE_FORMULA_SUCCESS.xlsx");
		MockMultipartFile fileSuccess = new MockMultipartFile(
				"file",
				"MILESTONE_FORMULA_SUCCESS.xlsx",
				"text/plain",
				Files.newInputStream(pathSuccess)
		);
		MockHttpServletResponse responseSuccess = this.mvc
				.perform(multipart(BASE_URL + "/uploadFiles")
						.file(fileSuccess)
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.requestAttr("SESSION_INFO", sessionObj)
				)
				.andExpect(status().isOk())
				.andReturn()
				.getResponse();

		String messageSuccess = responseSuccess.getContentAsString();
		Assertions.assertEquals(messageUtilities.getMessageWithParam("upload.excel.success", null), messageSuccess);
		assertEquals(3, milestoneRepository.count());
	}

	@Test
	public void testUploadFormulaExcelError() throws Exception {
		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.MILESTONES_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(),any(),any(),any(),any(),any());
		Path pathSuccess = Paths.get("src/test/resources/excel-templates/Milestone/MILESTONE_FORMULA_FAIL.xlsx");
		MockMultipartFile fileSuccess = new MockMultipartFile(
				"file",
				"MILESTONE_FORMULA_FAIL.xlsx",
				"text/plain",
				Files.newInputStream(pathSuccess)
		);
		MockHttpServletResponse responseSuccess = this.mvc
				.perform(multipart(BASE_URL + "/uploadFiles")
						.file(fileSuccess)
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.requestAttr("SESSION_INFO", sessionObj)
				)
				.andExpect(status().isOk())
				.andReturn()
				.getResponse();
		String messageSuccess = responseSuccess.getContentAsString();
		Assertions.assertEquals(messageUtilities.getMessageWithParam("upload.excel.success", null), messageSuccess);
		assertEquals(0, milestoneRepository.count());
	}

	private List<MilestoneDTO> mockListMilestoneDto() {
		List<MilestoneDTO> milestoneDTOList = new ArrayList<>();
		MilestoneDTO milestone = new MilestoneDTO();
		milestone.setMilestoneCode("LGN78924");
		milestone.setMilestoneDescription("Description");
		milestone.setExcelRowPosition(3);

		MilestoneDTO milestone1 = new MilestoneDTO();
		milestone1.setMilestoneCode("LGN78924");
		milestone1.setMilestoneDescription("Description2");
		milestone1.setExcelRowPosition(5);

		return milestoneDTOList;
	}

	public List<Milestone> mockListMilestone(){
		List<Milestone> milestoneList = new ArrayList<>();

		Milestone milestone1 = new Milestone();
		milestone1.setId(100L);
		milestone1.setTenantId(1l);
		milestone1.setMilestoneCode("MilestoneCode1");
		milestone1.setMilestoneDescription("Description1");
		milestone1.setCustomerDescription("CustomerDescription1");
		milestone1.setMilestoneCategory("source,transit");
		milestone1.setIsInternal(true);
		milestoneList.add(milestone1);

		Milestone milestone2 = new Milestone();
		milestone1.setId(200L);
		milestone2.setTenantId(1l);
		milestone2.setMilestoneCode("MilestoneCode2");
		milestone2.setMilestoneDescription("Description2");
		milestone2.setCustomerDescription("CustomerDescription2");
		milestone2.setMilestoneCategory("source,transit");
		milestone2.setIsInternal(true);
		milestoneList.add(milestone2);
		return milestoneList;
	}

	public List<UploadTemplateHdrIdDto> mockExportExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("MILESTONES_SETTING_EXPORT");
		uploadTemplateHdrIdDto.setTitle("MILESTONES_SETTING");
		uploadTemplateHdrIdDto.setFileName("MILESTONES_EXPORT.xlsx");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
		det1.setFieldName("milestoneCode");
		det1.setPosition(0);
		det1.setWidth(6400);
		det1.setAlignment("HorizontalAlignment.CENTER");
		det1.setColumnName("Milestone Code");
		det1.setColumnFullName("Milestone Code");
		det1.setActiveInd(1);
		det1.setMandatoryInd(1);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("milestoneDescription");
		det2.setPosition(1);
		det2.setColumnName("Description");
		det2.setColumnFullName("Description");
		det2.setMandatoryInd(1);
		listDetail.add(det2);

		UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
		copier.copy(det1,det3,null);
		det3.setFieldName("milestoneCategory");
		det3.setPosition(2);
		det3.setColumnName("Category");
		det3.setColumnFullName("Category");
		det3.setMandatoryInd(1);
		listDetail.add(det3);

		UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
		copier.copy(det1,det4,null);
		det4.setFieldName("customerDescription");
		det4.setPosition(3);
		det4.setColumnName("Customized Description");
		det4.setColumnFullName("Customized Description");
		det4.setMandatoryInd(0);
		listDetail.add(det4);

		UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
		copier.copy(det1,det5,null);
		det5.setFieldName("external");
		det5.setPosition(4);
		det5.setColumnName("External");
		det5.setColumnFullName("External");
		det5.setMandatoryInd(0);
		listDetail.add(det5);

		UploadTemplateDetIdDto det6 = new UploadTemplateDetIdDto();
		copier.copy(det1,det6,null);
		det6.setFieldName("activeInd");
		det6.setPosition(5);
		det6.setColumnName("Status");
		det6.setColumnFullName("Status");
		det6.setMandatoryInd(0);
		listDetail.add(det6);

		UploadTemplateDetIdDto det7 = new UploadTemplateDetIdDto();
		copier.copy(det1,det7,null);
		det7.setFieldName("milestoneGroup");
		det7.setPosition(6);
		det7.setColumnName("Milestone Group");
		det7.setColumnFullName("Milestone Group");
		det7.setMandatoryInd(0);
		listDetail.add(det7);

		UploadTemplateDetIdDto det8 = new UploadTemplateDetIdDto();
		copier.copy(det1,det8,null);
		det8.setFieldName("isDefault");
		det8.setPosition(7);
		det8.setColumnName("Is Default");
		det8.setColumnFullName("Is Default");
		det8.setMandatoryInd(0);
		listDetail.add(det8);

		UploadTemplateDetIdDto det9 = new UploadTemplateDetIdDto();
		copier.copy(det1,det9,null);
		det9.setFieldName("sequence");
		det9.setPosition(7);
		det9.setColumnName("Sequence");
		det9.setColumnFullName("Sequence");
		det9.setMandatoryInd(0);
		listDetail.add(det9);

		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}


	public List<UploadTemplateHdrIdDto> mockUploadExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("MILESTONES_SETTING_EXPORT");
		uploadTemplateHdrIdDto.setTitle("MILESTONES_SETTING");
		uploadTemplateHdrIdDto.setFileName("MILESTONES_EXPORT.xlsx");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
		det1.setFieldName("milestoneCode");
		det1.setPosition(1);
		det1.setWidth(6400);
		det1.setAlignment("HorizontalAlignment.CENTER");
		det1.setColumnName("Milestone Code");
		det1.setColumnFullName("Milestone Code");
		det1.setActiveInd(1);
		det1.setMandatoryInd(1);
		det1.setMaxLength(255);
		det1.setNoneDuplicated(1);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("milestoneDescription");
		det2.setPosition(2);
		det2.setColumnName("Description");
		det2.setColumnFullName("Description");
		det2.setMandatoryInd(1);
		det2.setNoneDuplicated(1);
		listDetail.add(det2);

		UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
		copier.copy(det1,det3,null);
		det3.setFieldName("milestoneCategory");
		det3.setPosition(3);
		det3.setColumnName("Category");
		det3.setColumnFullName("Category");
		det3.setMandatoryInd(1);
		det3.setNoneDuplicated(1);
		listDetail.add(det3);

		UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
		copier.copy(det1,det4,null);
		det4.setFieldName("customerDescription");
		det4.setPosition(4);
		det4.setColumnName("Customized Description");
		det4.setColumnFullName("Customized Description");
		det4.setMandatoryInd(0);
		det4.setNoneDuplicated(1);
		listDetail.add(det4);

		UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
		copier.copy(det1,det5,null);
		det5.setFieldName("milestoneGroup");
		det5.setPosition(5);
		det5.setColumnName("Milestone Group");
		det5.setColumnFullName("Milestone Group");
		det5.setMandatoryInd(0);
		det5.setNoneDuplicated(1);
		listDetail.add(det5);

		UploadTemplateDetIdDto det6 = new UploadTemplateDetIdDto();
		copier.copy(det1,det6,null);
		det6.setFieldName("activeInd");
		det6.setPosition(6);
		det6.setColumnName("Status");
		det6.setColumnFullName("Status");
		det6.setMandatoryInd(0);
		det6.setMaxLength(null);
		det6.setNoneDuplicated(1);
		listDetail.add(det6);

		UploadTemplateDetIdDto det7 = new UploadTemplateDetIdDto();
		copier.copy(det1,det7,null);
		det7.setFieldName("enableMilestoneStatus");
		det7.setPosition(7);
		det7.setColumnName("Enable milestone status");
		det7.setColumnFullName("Enable milestone status");
		det7.setMandatoryInd(0);
		det7.setNoneDuplicated(1);
		listDetail.add(det7);

		UploadTemplateDetIdDto det8 = new UploadTemplateDetIdDto();
		copier.copy(det1,det8,null);
		det8.setFieldName("external");
		det8.setPosition(0);
		det8.setColumnName("External");
		det8.setColumnFullName("External");
		det8.setMandatoryInd(0);
		det8.setMaxLength(null);
		det8.setNoneDuplicated(1);
		listDetail.add(det8);

		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}

	@Test
	public void testDownloadExcelSorting() throws Exception {
		List<MilestoneStatusConfigDTO> configDTOList = new ArrayList<>();
		MilestoneStatusConfigDTO configDTO = new MilestoneStatusConfigDTO();
		configDTO.setLookupId(lk1Save.getId());
		configDTO.setActivated(true);

		configDTOList.add(configDTO);
		MilestoneDTO modelDTO = new MilestoneDTO();
		modelDTO.setTenantId(1l);
		modelDTO.setMilestoneCode("OO");
		modelDTO.setMilestoneDescription("Order Creation");
		modelDTO.setCustomerDescription("customer desc");
		modelDTO.setMilestoneCategory("category1|category2");
		modelDTO.setMilestoneStatusConfigDtoList(configDTOList);
		mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(modelDTO);
		mvc.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn();

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.MILESTONES_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		String json1 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.MILESTONES_CATEGORY + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
		String json2 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.MILESTONES_CODE + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";
		String json3 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.MILESTONES_CUSTOMIZED_DESCRIPTION + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
		String json4 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.MILESTONES_DESCRIPTION + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
		String json5 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.MILESTONES_EXTERNAL + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";

		mvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO", sessionObj)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json1)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO", sessionObj)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json2)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO", sessionObj)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json3)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO", sessionObj)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json4)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO", sessionObj)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json5)).andExpect(status().isOk()).andReturn();
	}

	@Test
	public void testDownloadExcelFilter() throws Exception {
		List<MilestoneStatusConfigDTO> configDTOList = new ArrayList<>();
		MilestoneStatusConfigDTO configDTO = new MilestoneStatusConfigDTO();
		configDTO.setLookupId(lk1Save.getId());
		configDTO.setActivated(true);

		configDTOList.add(configDTO);
		MilestoneDTO modelDTO = new MilestoneDTO();
		modelDTO.setTenantId(1l);
		modelDTO.setMilestoneCode("OO");
		modelDTO.setMilestoneDescription("Order Creation");
		modelDTO.setCustomerDescription("customer desc");
		modelDTO.setMilestoneCategory("category1|category2");
		modelDTO.setMilestoneStatusConfigDtoList(configDTOList);
		mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(modelDTO);
		mvc.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn();

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.MILESTONES_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		String json = "{\"customerDescription\":\"customer\",\"milestoneCategory\":\"category1\",\"milestoneDescription\":\"Order\",\"isInternal\":true, \"milestoneCode\":\"O\"}";
		mvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO", sessionObj)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json)).andExpect(status().isOk()).andReturn();
	}
}
