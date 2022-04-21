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
import com.y3technologies.masters.dto.table.EquipmentTableDto;
import com.y3technologies.masters.model.Equipment;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.repository.EquipmentRepository;
import com.y3technologies.masters.service.EquipmentService;
import com.y3technologies.masters.service.LookupService;
import com.y3technologies.masters.service.impl.EquipmentServiceImpl;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
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


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Transactional
@PropertySource("classpath:application.properties")
public class EquipmentControllerTests {
	
	private static final Logger logger = LoggerFactory.getLogger(EquipmentControllerTests.class);

	private MockMvc mvc;
	@Autowired
	private WebApplicationContext wac;

	@SpyBean
	private EquipmentService equipmentServiceSpy;

	@SpyBean
	private EquipmentServiceImpl equipmentServiceSpyImpl;

	@SpyBean
	private LookupService lookupServiceSpy;

	@MockBean
	private ExcelClient excelClient;

	@MockBean
	private EmailClient emailClient;

	SessionUserInfoDTO sessionUserInfoDTO;

	@Autowired
	private EquipmentRepository equipmentRepository;

	@Autowired
	private MessagesUtilities messagesUtilities;

	@SpyBean
	ExcelUtils excelUtils;

	@Before
	public void setup() {
		sessionUserInfoDTO = new SessionUserInfoDTO();
		sessionUserInfoDTO.setAasTenantId(1L);
		this.mvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}
	
	@Value("${api.version.masters}")
	private String apiVersion;

	private String BASE_URL;

	@PostConstruct
	public void setApiVersion() {
		BASE_URL = "/" + apiVersion + "/equipment";
	}

	@AfterEach
	private void clearTestData() {
		equipmentRepository.deleteAll();
	}

	@Test
	public void testEquipmentController() throws Exception {

		String result;
		/***create***/
		Equipment model = new Equipment();
		model.setTenantId(482l);
		model.setEquipmentType("3");
		model.setUnitAidc1("123465");
		model.setUnitType("1");
	    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	    ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
	    String requestJson = ow.writeValueAsString(model);
	    result = mvc.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON)
	                .content(requestJson))
	            .andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
	    logger.info("create result is {}", result);
	    
	    Long id = Long.valueOf(result);
	    
	    /***retrieve***/
	    RequestBuilder request = get(BASE_URL + "/retrieve?id="+id);
	    String resultJson = mvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
	    logger.info("retrieve result is {}", resultJson);
	    
	    /***update***/
	    model = mapper.readValue(resultJson, Equipment.class);
	    model.setUnitAidc1("654321");
	    requestJson = ow.writeValueAsString(model);
	    result = mvc.perform(post(BASE_URL + "/update").contentType(MediaType.APPLICATION_JSON)
	                .content(requestJson))
	            .andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
	    logger.info("update result is {}", result);
	    
	    /***listByParam***/
	    model = new Equipment();
	    model.setTenantId(482l);
	    model.setUnitAidc1("654321");
	    requestJson = ow.writeValueAsString(model);
	    result = mvc.perform(get(BASE_URL + "/listByParam").contentType(MediaType.APPLICATION_JSON)
	                .content(requestJson))
	            .andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
	    logger.info("listByParam result is {}", result);
	    
	    /***updateStatus***/
	    RequestBuilder updateStatus = get(BASE_URL + "/updateStatus?id="+id+"&status="+false);
	    logger.info("updateStatus result is {}", mvc.perform(updateStatus).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
	    
	    /***query***/
	    DataTablesInput input = new DataTablesInput();
	    input.setStart(0);
	    input.setLength(5);
	    Column column = new Column();
	    column.setData("unitAidc1");
	    column.setSearch(new Search("6543",Boolean.TRUE));
	    column.setSearchable(Boolean.TRUE);
	    List<Column> cols = new ArrayList<Column>();
	    cols.add(column);
	    input.setColumns(cols);
	    String queryJson = ow.writeValueAsString(input);
	    result = mvc.perform(post(BASE_URL + "/query").contentType(MediaType.APPLICATION_JSON)
                .content(queryJson))
            .andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
	    logger.info("query result is {}", result);
	}

	@Test
	public void testDownloadExcel() throws Exception {

		List<Equipment> listEquipment = mockListEquipment();

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.EQUIPMENT_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);
		Mockito.doReturn(listEquipment).when(equipmentServiceSpy).findByTenantId(any(), any());
		mvc.perform(post(BASE_URL + "/downloadExcel/")
		.contentType(MediaType.APPLICATION_JSON)
		.content("{}")).andExpect(status().isOk()).andReturn();
	}

	@Test
	public void testUploadExcel() throws Exception {
		//test data errors
		Path dataErrorPath = Paths.get("src/test/resources/excel-templates/Equipment/EQUIPMENT_ERROR.xlsx");

		//save Lookup-equipment
		Lookup lookup = new Lookup();
		lookup.setTenantId(sessionUserInfoDTO.getAasTenantId());
		lookup.setLookupType(AppConstants.LookupType.EQUIPMENT_TYPE);
		lookup.setLookupCode("EquipmentType-464-2");
		lookup.setLookupDescription("BOX (464)");
		lookup.setActiveInd(true);
		lookupServiceSpy.save(lookup);

		//save Lookup-lookupEquipmentType
		Lookup lookupEquipmentType = new Lookup();
		lookupEquipmentType.setTenantId(sessionUserInfoDTO.getAasTenantId());
		lookupEquipmentType.setLookupType(AppConstants.LookupType.EQUIPMENT_UNIT_TYPE);
		lookupEquipmentType.setLookupCode("UNIT1");
		lookupEquipmentType.setLookupDescription("UNIT1");
		lookupEquipmentType.setActiveInd(true);
		lookupServiceSpy.save(lookupEquipmentType);

		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.EQUIPMENT_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);
		//Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(),any(),any(),any(),any(),any());
		Mockito.doNothing().when(excelUtils).buildCellErrors(any(),any());

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"EQUIPMENT_ERROR.xlsx",
				"text/plain", Files.newInputStream(dataErrorPath));

		MockHttpServletResponse testDataErrorResponse = mvc.perform(
				multipart(BASE_URL+"/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		//test upload excel successfully
		Path successFilePath = Paths.get("src/test/resources/excel-templates/Equipment/EQUIPMENT_SUCCESS.xlsx");

		mockMultipartFile = new MockMultipartFile("file",
				"EQUIPMENT_SUCCESS.xlsx",
				"text/plain", Files.newInputStream(successFilePath));

		MockHttpServletResponse testUploadSuccessResponse = mvc.perform(
				multipart(BASE_URL+"/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA).requestAttr("SESSION_INFO", sessionUserInfoDTO))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		assertEquals(3, equipmentRepository.count());
	}

	@Test
	public void testUploadFormulaExcelSuccess() throws Exception {
		Path filePath = Paths.get("src/test/resources/excel-templates/Equipment/EQUIPMENT_FORMULA_SUCCESS.xlsx");

		Lookup lookup = new Lookup();
		lookup.setTenantId(sessionUserInfoDTO.getAasTenantId());
		lookup.setLookupType(AppConstants.LookupType.EQUIPMENT_TYPE);
		lookup.setLookupCode("EquipmentType-464-2");
		lookup.setLookupDescription("BOX (464)");
		lookup.setActiveInd(true);
		lookupServiceSpy.save(lookup);

		Lookup lookupEquipmentType = new Lookup();
		lookupEquipmentType.setTenantId(sessionUserInfoDTO.getAasTenantId());
		lookupEquipmentType.setLookupType(AppConstants.LookupType.EQUIPMENT_UNIT_TYPE);
		lookupEquipmentType.setLookupCode("UNIT1");
		lookupEquipmentType.setLookupDescription("UNIT1");
		lookupEquipmentType.setActiveInd(true);
		lookupServiceSpy.save(lookupEquipmentType);

		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.EQUIPMENT_SETTING_UPLOAD))
				.thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(), any(), any(), any(), any(), any());

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "EQUIPMENT_FORMULA_SUCCESS.xlsx", "text/plain",
				Files.newInputStream(filePath));

		MockHttpServletResponse testUploadSuccessResponse = mvc
				.perform(multipart(BASE_URL + "/uploadFiles/").file(mockMultipartFile)
						.contentType(MediaType.MULTIPART_FORM_DATA).requestAttr("SESSION_INFO", sessionUserInfoDTO))
				.andExpect(status().isOk()).andReturn().getResponse();

		String successMessage = testUploadSuccessResponse.getContentAsString();
		assertEquals(messagesUtilities.getResourceMessage("upload.excel.success", null), successMessage);

		assertEquals(3, equipmentRepository.count());
	}

	@Test
	public void testUploadFormulaExcelError() throws Exception {
		Path filePath = Paths.get("src/test/resources/excel-templates/Equipment/EQUIPMENT_FORMULA_ERROR.xlsx");

		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.EQUIPMENT_SETTING_UPLOAD))
				.thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(), any(), any(), any(), any(), any());

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "EQUIPMENT_FORMULA_ERROR.xlsx", "text/plain",
				Files.newInputStream(filePath));

		MockHttpServletResponse testUploadSuccessResponse = mvc
				.perform(multipart(BASE_URL + "/uploadFiles/").file(mockMultipartFile)
						.contentType(MediaType.MULTIPART_FORM_DATA).requestAttr("SESSION_INFO", sessionUserInfoDTO))
				.andExpect(status().isOk()).andReturn().getResponse();

		String successMessage = testUploadSuccessResponse.getContentAsString();
		assertEquals(messagesUtilities.getResourceMessage("upload.excel.success", null), successMessage);

		assertEquals(0, equipmentRepository.count());
	}

	private List<EquipmentTableDto> mockListEquipmentDto() {
		List<EquipmentTableDto> equipmentTableDtoList = new ArrayList<>();

		 EquipmentTableDto equipmentTableDto = new EquipmentTableDto();
		 equipmentTableDto.setEquipmentType("BOX (464)");
		 equipmentTableDto.setUnitAidc1("LGN19578");
		 equipmentTableDto.setExcelRowPosition(3);
		 equipmentTableDtoList.add(equipmentTableDto);

		EquipmentTableDto equipmentTableDto1 = new EquipmentTableDto();
		equipmentTableDto1.setEquipmentType("BOX (464)");
		equipmentTableDto1.setUnitAidc1("LGN19578");
		equipmentTableDto1.setExcelRowPosition(5);
		equipmentTableDtoList.add(equipmentTableDto1);

		return equipmentTableDtoList;

	}

	public List<Equipment> mockListEquipment(){
		List<Equipment> listEquipment = new ArrayList();
		Equipment equipment = new Equipment();
		equipment.setTenantId(2L);
		equipment.setUnitAidc1("Unit AIDC1");
		equipment.setUnitAidc2("Unit AIDC2");
		equipment.setEquipmentType("Equipment Type test");
		equipment.setUnitType("Unit Type test");
		equipment.setActiveInd(true);
		equipment.setTareWeight(new BigDecimal(1));
		equipment.setMaxWeight(new BigDecimal(1));
		equipment.setVolumn(new BigDecimal(1));
		equipment.setRemark("remark");
		equipment.setUnitOwner("Unit owner");
		listEquipment.add(equipment);
		return listEquipment;
	}

	public List<UploadTemplateHdrIdDto> mockExportExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("EQUIPMENT_SETTING_EXPORT");
		uploadTemplateHdrIdDto.setTitle("EQUIPMENT");
		uploadTemplateHdrIdDto.setFileName("EQUIPMENT.xlsx");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
		det1.setFieldName("unitAidc1");
		det1.setPosition(0);
		det1.setWidth(6400);
		det1.setAlignment("HorizontalAlignment.CENTER");
		det1.setColumnName("Unit AIDC1");
		det1.setColumnFullName("Unit AIDC1");
		det1.setActiveInd(1);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("unitAidc2");
		det2.setPosition(1);
		det2.setColumnName("Unit AIDC2");
		det2.setColumnFullName("Unit AIDC2");
		listDetail.add(det2);

		UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
		copier.copy(det1,det3,null);
		det3.setFieldName("equipmentType");
		det3.setPosition(2);
		det3.setColumnName("Equipment Type");
		det3.setColumnFullName("Equipment Type");
		listDetail.add(det3);

		UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
		copier.copy(det1,det4,null);
		det4.setFieldName("unitType");
		det4.setPosition(3);
		det4.setColumnName("Unit Type");
		det4.setColumnFullName("Unit Type");
		listDetail.add(det4);

		UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
		copier.copy(det1,det5,null);
		det5.setFieldName("activeInd");
		det5.setPosition(4);
		det5.setColumnName("Status");
		det5.setColumnFullName("Status");
		listDetail.add(det5);

		UploadTemplateDetIdDto det6 = new UploadTemplateDetIdDto();
		copier.copy(det1,det6,null);
		det6.setFieldName("tareWeight");
		det6.setPosition(5);
		det6.setColumnName("Tare Weight (kg) ");
		det6.setColumnFullName("Tare Weight (kg) ");
		listDetail.add(det6);

		UploadTemplateDetIdDto det7 = new UploadTemplateDetIdDto();
		copier.copy(det1,det7,null);
		det7.setFieldName("maxWeight");
		det7.setPosition(6);
		det7.setColumnName("Max Weight (kg)");
		det7.setColumnFullName("Max Weight (kg)");
		listDetail.add(det7);

		UploadTemplateDetIdDto det8 = new UploadTemplateDetIdDto();
		copier.copy(det1,det8,null);
		det8.setFieldName("volumn");
		det8.setPosition(7);
		det8.setColumnName("Volume");
		det8.setColumnFullName("Volume");
		listDetail.add(det8);

		UploadTemplateDetIdDto det9 = new UploadTemplateDetIdDto();
		copier.copy(det1,det9,null);
		det9.setFieldName("remark");
		det9.setPosition(8);
		det9.setColumnName("Remarks");
		det9.setColumnFullName("Remarks");
		listDetail.add(det9);

		UploadTemplateDetIdDto det10 = new UploadTemplateDetIdDto();
		copier.copy(det1,det10,null);
		det10.setFieldName("unitOwner");
		det10.setPosition(9);
		det10.setColumnName("Unit Owner");
		det10.setColumnFullName("Unit Owner");
		listDetail.add(det10);

		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}


	public List<UploadTemplateHdrIdDto> mockUploadExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("EQUIPMENT_SETTING_UPLOAD");
		uploadTemplateHdrIdDto.setTitle("EQUIPMENT SETTING");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();

		det1.setFieldName("unitAidc1");
		det1.setColumnName("Unit AIDC1");
		det1.setColumnFullName("Unit AIDC1");
		det1.setMaxLength(255);
		det1.setMandatoryInd(1);
		det1.setActiveInd(1);
		det1.setNoneDuplicated(1);
		det1.setPosition(0);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("unitAidc2");
		det2.setColumnName("Unit AIDC2");
		det2.setColumnFullName("Unit AIDC2");
		det2.setMaxLength(255);
		det2.setMandatoryInd(1);
		det2.setActiveInd(1);
		det2.setNoneDuplicated(1);
		det2.setPosition(1);
		listDetail.add(det2);

		UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
		copier.copy(det1,det3,null);
		det3.setFieldName("equipmentType");
		det3.setColumnName("Equipment Type");
		det3.setColumnFullName("Equipment Type");
		det3.setMaxLength(255);
		det3.setMandatoryInd(1);
		det3.setActiveInd(1);
		det3.setNoneDuplicated(1);
		det3.setPosition(2);
		listDetail.add(det3);

		UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
		copier.copy(det1,det4,null);
		det4.setFieldName("unitType");
		det4.setColumnName("Unit Type");
		det4.setColumnFullName("Unit Type");
		det4.setMaxLength(255);
		det4.setMandatoryInd(0);
		det4.setActiveInd(1);
		det4.setNoneDuplicated(1);
		det4.setPosition(3);
		listDetail.add(det4);

		UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
		copier.copy(det1,det5,null);
		det5.setFieldName("activeInd");
		det5.setColumnName("Status");
		det5.setColumnFullName("Status");
		det5.setMaxLength(255);
		det5.setMandatoryInd(1);
		det5.setActiveInd(1);
		det5.setNoneDuplicated(1);
		det5.setPosition(4);
		listDetail.add(det5);

		UploadTemplateDetIdDto det6 = new UploadTemplateDetIdDto();
		copier.copy(det1,det6,null);
		det6.setFieldName("tareWeight");
		det6.setColumnName("Tare Weight (kg)");
		det6.setColumnFullName("Tare Weight (kg)");
		det6.setMaxLength(255);
		det6.setMandatoryInd(0);
		det6.setActiveInd(1);
		det6.setNoneDuplicated(1);
		det6.setPosition(5);
		listDetail.add(det6);

		UploadTemplateDetIdDto det7 = new UploadTemplateDetIdDto();
		copier.copy(det1,det7,null);
		det7.setFieldName("maxWeight");
		det7.setColumnName("Max Weight (kg)");
		det7.setColumnFullName("Max Weight (kg)");
		det7.setMaxLength(255);
		det7.setMandatoryInd(0);
		det7.setActiveInd(1);
		det7.setNoneDuplicated(1);
		det7.setPosition(6);
		listDetail.add(det7);

		UploadTemplateDetIdDto det8 = new UploadTemplateDetIdDto();
		copier.copy(det1,det8,null);
		det8.setFieldName("volumn");
		det8.setColumnName("Volume");
		det8.setColumnFullName("Volume");
		det8.setMaxLength(255);
		det8.setMandatoryInd(0);
		det8.setActiveInd(1);
		det8.setNoneDuplicated(1);
		det8.setPosition(7);
		listDetail.add(det8);

		UploadTemplateDetIdDto det9 = new UploadTemplateDetIdDto();
		copier.copy(det1,det9,null);
		det9.setFieldName("remark");
		det9.setColumnName("Remarks");
		det9.setColumnFullName("Remarks");
		det9.setMaxLength(255);
		det9.setMandatoryInd(0);
		det9.setActiveInd(1);
		det9.setNoneDuplicated(1);
		det9.setPosition(8);
		listDetail.add(det9);

		UploadTemplateDetIdDto det10 = new UploadTemplateDetIdDto();
		copier.copy(det1,det10,null);
		det10.setFieldName("unitOwner");
		det10.setColumnName("Last Location");
		det10.setColumnFullName("Last Location");
		det10.setMaxLength(255);
		det10.setMandatoryInd(0);
		det10.setActiveInd(1);
		det10.setNoneDuplicated(1);
		det10.setPosition(9);
		listDetail.add(det10);


		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}

	@Test
	public void testDownloadExcelSorting() throws Exception {
		List<Equipment> listEquipment = mockListEquipment();
		equipmentRepository.saveAll(listEquipment);

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.EQUIPMENT_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		String json1 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.EQUIPMENT_TYPE + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
		String json2 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.EQUIPMENT_UNIT_TYPE + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";
		String json3 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.EQUIPMENT_UNITAIDC1 + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
		String json4 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.EQUIPMENT_UNITAIDC2 + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionUserInfoDTO)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json1)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionUserInfoDTO)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json2)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionUserInfoDTO)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json3)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionUserInfoDTO)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json4)).andExpect(status().isOk()).andReturn();
	}

	@Test
	public void testDownloadExcelFilter() throws Exception {
		List<Equipment> listEquipment = mockListEquipment();
		equipmentRepository.saveAll(listEquipment);

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.EQUIPMENT_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		String json = "{\"unitAidc1\":\"1\",\"unitAidc2\":\"2\",\"unitType\":\"type\",\"equipmentType\":\"type\"}";

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionUserInfoDTO)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json)).andExpect(status().isOk()).andReturn();
	}
}
