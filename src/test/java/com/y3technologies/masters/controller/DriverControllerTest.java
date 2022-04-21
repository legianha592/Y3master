package com.y3technologies.masters.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.client.AddrClient;
import com.y3technologies.masters.client.AddrContactClient;
import com.y3technologies.masters.client.EmailClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.CustomerDto;
import com.y3technologies.masters.dto.DriverDto;
import com.y3technologies.masters.dto.TransporterDto;
import com.y3technologies.masters.dto.aas.ProfileScopeDTO;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.CountryDTO;
import com.y3technologies.masters.dto.comm.UpdateAddrDTO;
import com.y3technologies.masters.dto.excel.UploadTemplateDetIdDto;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.AddrContactFilter;
import com.y3technologies.masters.dto.table.DriverTableDto;
import com.y3technologies.masters.exception.RestErrorMessage;
import com.y3technologies.masters.model.Driver;
import com.y3technologies.masters.model.Location;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.aas.AasUser;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.model.comm.AddrDTO;
import com.y3technologies.masters.repository.DriverRepository;
import com.y3technologies.masters.repository.LocationRepository;
import com.y3technologies.masters.repository.PartnerLocationRepository;
import com.y3technologies.masters.repository.PartnerTypesRepository;
import com.y3technologies.masters.repository.PartnersRepository;
import com.y3technologies.masters.repository.LookupRepository;
import com.y3technologies.masters.service.DriverService;
import com.y3technologies.masters.service.PartnersService;
import com.y3technologies.masters.service.impl.DriverServiceImpl;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;

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
import org.springframework.data.jpa.datatables.mapping.Column;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.Search;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DriverControllerTest {
	private static final String TEST_EMAIL = "trx_testing@outlook.com";
	private static final String TEST_EMAIL2 = "trx_testing2@outlook.com";
	private static final Logger logger = LoggerFactory.getLogger(DriverControllerTest.class);
	private static ObjectMapper objectMapper;

	private MockMvc mvc;

	@Autowired
	private WebApplicationContext wac;

	@MockBean
	private AddrContactClient addrContactClient;
	
	@MockBean
	private AasClient aasClient;

	@MockBean
	ExcelClient excelClient;

	@MockBean
	EmailClient emailClient;

	@SpyBean
	DriverService driverServiceSpy;

	@SpyBean
	DriverServiceImpl driverServiceImpl;

	@Value("${api.version.masters}")
	private String apiVersion;

	private String BASE_URL;

	private Partners savedPartners;
	private Driver savedDriver;

	@Autowired
	private PartnersRepository partnersRepository;

	@Autowired
	private DriverRepository driverRepository;

  @Autowired
  private MessagesUtilities messageUtilities;
  
	@Autowired
	private LocationRepository locationRepository;

	@MockBean
	AddrClient addrClient;

	@Autowired
	private PartnerTypesRepository partnerTypesRepository;

	@Autowired
	private PartnerLocationRepository partnerLocationRepository;

	@PostConstruct
	public void setApiVersion() {
		BASE_URL = "/" + apiVersion + "/driver";
	}

	@SpyBean
	PartnersService partnersService;

	@SpyBean
	private ExcelUtils excelUtils;

	@Autowired
	private LookupRepository lookupRepository;

	@BeforeAll
	public static void setup() throws Exception {

		objectMapper = new ObjectMapper()
				.registerModule(new ParameterNamesModule())
				.registerModule(new Jdk8Module())
				.registerModule(new JavaTimeModule());
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);


	}

	@BeforeEach
		public void init() {
		mvc = MockMvcBuilders.webAppContextSetup(wac).build();

		AddrContact addrContact = new AddrContact();
		addrContact.setId(1L);
		when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

		List<AddrContact> list = new ArrayList<>();
		list.add(addrContact);
		when(addrContactClient.getAddressContactList(any(List.class))).thenReturn(list);
		
		AasUser aasUser = new AasUser();
		aasUser.setEmail("test@y3.com");
		
		when(aasClient.proceedCreateTenantUserAccount(any(String.class), any(Long.class))).thenReturn(false);

		List<AddrContact> addrContactList = new ArrayList<>();
		AddrContact addrContact1 = new AddrContact();
		addrContact1.setId(1L);
		addrContact1.setMobileNumber1("123123");
		addrContactList.add(addrContact1);

		when(addrContactClient.findByFilter(any(AddrContactFilter.class))).thenReturn(addrContactList);

		Lookup lkCustomer = new Lookup();
		lkCustomer.setLookupType(AppConstants.PARTNER_TYPE);
		lkCustomer.setLookupCode(AppConstants.PartnerType.CUSTOMER);
		lkCustomer.setLookupDescription(AppConstants.PartnerType.CUSTOMER);
		lkCustomer.setTenantId(0L);
        lookupRepository.save(lkCustomer);

		Lookup lkTransporter = new Lookup();
		lkTransporter.setLookupType(AppConstants.PARTNER_TYPE);
		lkTransporter.setLookupCode(AppConstants.PartnerType.TRANSPORTER);
		lkTransporter.setLookupDescription(AppConstants.PartnerType.TRANSPORTER);
		lkTransporter.setTenantId(0L);
		lookupRepository.save(lkTransporter);

	}

	@AfterEach
	public void remove() throws Exception{
		if(savedPartners != null) partnersRepository.delete(savedPartners);
		locationRepository.deleteAll();
		partnerLocationRepository.deleteAll();
		driverRepository.deleteAll();
		partnerTypesRepository.deleteAll();
		partnersRepository.deleteAll();
		lookupRepository.deleteAll();
		partnersRepository = null;
		driverRepository = null;
	}

	@Test
	public void testDriverController() throws Exception {
		when(aasClient.retrieveUserProfile(any(Long.class))).thenReturn(new UpdateUserProfileDTO());

		/*** create ***/
		Random random = new Random();
		DriverDto driver = new DriverDto();
		driver.setName(random.toString());
		driver.setActiveInd(Boolean.TRUE);
		driver.setLicenceNumber("test");
		driver.setLicenceType("1|3|4");
		driver.setAvailable(Boolean.TRUE);
		driver.setTenantId(1L);
		driver.setEmail("123oi@q.com");
		driver.setMobileNumber1("12345678");
		driver.setMobileNumber1CountryShortName("SG");
		ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(driver);
		String result = mvc
				.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("create result is {}", result);
		Long id = Long.valueOf(result);

		/*** retrieve ***/
		String retrievedResult = mvc.perform(get(BASE_URL + "/retrieve?id=" + id)).andDo(print())
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		logger.info("retrieve result is {}", retrievedResult);
		/*** update ***/

		DriverDto updateDriver = new DriverDto();
		updateDriver.setId(id);
		updateDriver.setName("driverTest");
		updateDriver.setActiveInd(Boolean.TRUE);
		updateDriver.setLicenceNumber("test");
		updateDriver.setLicenceType("1|3");
		updateDriver.setAvailable(Boolean.TRUE);
		updateDriver.setTenantId(1L);
		updateDriver.setEmail("123oi@g.com");
		updateDriver.setMobileNumber1("987654");
		updateDriver.setMobileNumber1CountryShortName("SG");
		requestJson = ow.writeValueAsString(updateDriver);
		String updateResult = mvc
				.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("update result is {}", updateResult);

		/*** updateStatus ***/
		RequestBuilder updateStatus = get(BASE_URL + "/updateStatus?id=" + id + "&status=" + false);
		logger.info("updateStatus result is {}", mvc.perform(updateStatus).andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString());

		/*** query ***/
		DataTablesInput input = new DataTablesInput();
		input.setStart(0);
		input.setLength(5);
		Column column = new Column();
		column.setData("name");
		column.setSearch(new Search("driverTest", Boolean.TRUE));
		column.setSearchable(Boolean.TRUE);
		List<Column> cols = new ArrayList<Column>();
		cols.add(column);
		input.setColumns(cols);
		String queryJson = ow.writeValueAsString(input);
		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(new ArrayList<>());
		result = mvc.perform(post(BASE_URL + "/query").contentType(MediaType.APPLICATION_JSON).content(queryJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("query result is {}", result);
	}

	@Test
	public void testCreateDriverWithInvalidTransporterId() throws Exception {

		DriverDto driverDto = new DriverDto();
		driverDto.setName("Test Name");
		driverDto.setEmail(TEST_EMAIL);
		driverDto.setMobileNumber1("99998888");
		driverDto.setTransporterId(0L);
		driverDto.setLicenceNumber("Test");
		driverDto.setLicenceType("1|3");

		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(driverDto)))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse();

		RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		assertEquals("Transporter is invalid.", message.getMessages().get(0));
	}

	@Test
	@Transactional
	public void testFindByFilterWithInvalidTransporterId() throws Exception {

		savedDriver = new Driver();
		Random random = new Random();
		savedDriver.setName(random.toString());
		savedDriver.setActiveInd(Boolean.TRUE);
		savedDriver.setLicenceNumber("test");
		savedDriver.setLicenceType("1|3|4");
		savedDriver.setTenantId(1L);
		savedDriver.setEmail(TEST_EMAIL);
		savedDriver.setAddressContactId(1L);
		savedDriver.setPartners(savedPartners);
		driverRepository.save(savedDriver);

		MultiValueMap<String, String> params = new HttpHeaders();
		params.add("transporterId", String.valueOf(1L));

		MockHttpServletResponse response = this.mvc.perform(
				get(BASE_URL + "/findByFilter")
						.params(params)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse();

		RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		assertEquals("Transporter is invalid.", message.getMessages().get(0));
	}

	@Test
	@Transactional
	public void testFindByFilter() throws Exception {
		savedPartners = new Partners();
		savedPartners.setPartnerCode("TEST PARTNER CODE");
		savedPartners.setPartnerName("TEST PARTNER NAME");
		partnersRepository.save(savedPartners);

		savedDriver = new Driver();
		Random random = new Random();
		savedDriver.setName(random.toString());
		savedDriver.setActiveInd(Boolean.TRUE);
		savedDriver.setLicenceNumber("test");
		savedDriver.setLicenceType("1|3|4");
		savedDriver.setTenantId(1L);
		savedDriver.setEmail(TEST_EMAIL);
		savedDriver.setAddressContactId(1L);
		savedDriver.setPartners(savedPartners);
		driverRepository.save(savedDriver);

		Driver savedDriver2 = new Driver();
		random = new Random();
		savedDriver2.setName(random.toString());
		savedDriver2.setActiveInd(Boolean.FALSE);
		savedDriver2.setLicenceNumber("test");
		savedDriver2.setLicenceType("1|3|4");
		savedDriver2.setTenantId(2L);
		savedDriver2.setEmail(TEST_EMAIL2);
		savedDriver2.setAddressContactId(1L);
		driverRepository.save(savedDriver2);

		MultiValueMap<String, String> params = new HttpHeaders();
		params.add("transporterId", String.valueOf(savedPartners.getId()));

		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(new ArrayList<>());

		MockHttpServletResponse response = this.mvc.perform(
				get(BASE_URL + "/findByFilter")
						.params(params)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		List<DriverDto> driverList = objectMapper.readValue(response.getContentAsString(),
				new TypeReference<>() {
				});

		assertEquals(1, driverList.size());
		assertEquals(driverList.get(0).getTransporterId(), savedDriver.getPartners().getId());

		//findByTENANTID
		params = new HttpHeaders();
		params.add("tenantId", String.valueOf(2L));

		response = this.mvc.perform(
				get(BASE_URL + "/findByFilter")
						.params(params)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		driverList = objectMapper.readValue(response.getContentAsString(),
				new TypeReference<>() {
				});

		assertEquals(1, driverList.size());
		assertEquals(driverList.get(0).getId(), savedDriver2.getId());

		//findByDriverName
		params = new HttpHeaders();
		params.add("tenantId", String.valueOf(2L));
		params.add("name", savedDriver2.getName());

		response = this.mvc.perform(
				get(BASE_URL + "/findByFilter")
						.params(params)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		driverList = objectMapper.readValue(response.getContentAsString(),
				new TypeReference<>() {
				});

		assertEquals(1, driverList.size());
		assertEquals(driverList.get(0).getName(), savedDriver2.getName());

		//findByAssignedTransporterFalse
		params = new HttpHeaders();
		params.add("assignedTransporter", String.valueOf(Boolean.FALSE));

		response = this.mvc.perform(
				get(BASE_URL + "/findByFilter")
						.params(params)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		driverList = objectMapper.readValue(response.getContentAsString(),
				new TypeReference<>() {
				});

		assertEquals(1, driverList.size());
		assertEquals(driverList.get(0).getName(), savedDriver2.getName());
		assertNull(driverList.get(0).getTransporterId());

		//findByAssignedTransporterTrue
		params = new HttpHeaders();
		params.add("assignedTransporter", String.valueOf(Boolean.TRUE));

		response = this.mvc.perform(
				get(BASE_URL + "/findByFilter")
						.params(params)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		driverList = objectMapper.readValue(response.getContentAsString(),
				new TypeReference<>() {
				});

		assertEquals(1, driverList.size());
		assertEquals(driverList.get(0).getName(), savedDriver.getName());
		assertNotNull(driverList.get(0).getTransporterId());

		//findByActiveInd
		params = new HttpHeaders();
		params.add("activeInd", String.valueOf(Boolean.FALSE));

		response = this.mvc.perform(
				get(BASE_URL + "/findByFilter")
						.params(params)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		driverList = objectMapper.readValue(response.getContentAsString(),
				new TypeReference<>() {
				});

		assertEquals(1, driverList.size());
		assertEquals(driverList.get(0).getName(), savedDriver2.getName());
		assertEquals(driverList.get(0).getActiveInd(), savedDriver2.getActiveInd());
	}

	@Test
	void testDownloadExcel() throws Exception {
		List<DriverDto> listDriverDto = mockListDriverDto();
		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.DRIVER_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);
		Mockito.doReturn(listDriverDto).when(driverServiceSpy).findByTenantId(any(), any(), any());
		mvc.perform(post(BASE_URL + "/downloadExcel/").content("{}").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
	}

	@Test
	void testUploadExcelError() throws Exception {
		//test header errors
		Path headerErrorPath = Paths.get("src/test/resources/excel-templates/Driver/DRIVER_DATA_ERROR.xlsx");

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"DRIVER_DATA_ERROR.xlsx",
				"text/plain", Files.newInputStream(headerErrorPath));

		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.DRIVER_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);

		MockHttpServletResponse testHeaderErrorResponse = mvc.perform(
				multipart(BASE_URL+"/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andReturn().getResponse();

	}

	@Test
	void testUploadExcelSuccess() throws Exception{
		//test upload excel successfully
		Path successFilePath = Paths.get("src/test/resources/excel-templates/Driver/DRIVER_SAVE.xlsx");

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"DRIVER_SAVE.xlsx",
				"text/plain", Files.newInputStream(successFilePath));

		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.DRIVER_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);

		//mock mobile country code
		List<CountryDTO> countryDTOList = new ArrayList<>();
		CountryDTO countryDTO = new CountryDTO();
		countryDTO.setCountryIsdCode("VN");
		countryDTO.setCountryFullName("VIETNAM");
		countryDTO.setCountryIsdCode("+84");
		countryDTOList.add(countryDTO);
		when(addrContactClient.findByCountryIsdCode(any())).thenReturn(countryDTOList);

		AddrContact addrContact = new AddrContact();
		addrContact.setMobileNumber1("12345677888");
		addrContact.setMobileNumber1CountryShortName("VN");
		addrContact.setId(1L);
		when(addrContactClient.createOrUpdateAddressContact(any())).thenReturn(addrContact);

		Partners partners = new Partners();
		doReturn(null).when(partnersService).findById(any());

		MockHttpServletResponse testUploadSuccessResponse = mvc.perform(
				multipart(BASE_URL+"/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		List<Driver> listNewDriver = driverServiceSpy.findByName("Le Gia Nha 19483");

		assertEquals(1, listNewDriver.size());
	}

  @Test
	void testUploadFormulaExcelSuccess() throws Exception{
		Path successFilePath = Paths.get("src/test/resources/excel-templates/Driver/DRIVER_FORMULA_SUCCESS.xlsx");
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"DRIVER_FORMULA_SUCCESS.xlsx",
				"text/plain", Files.newInputStream(successFilePath));
		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.DRIVER_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);
		List<CountryDTO> countryDTOList = new ArrayList<>();
		CountryDTO countryDTO = new CountryDTO();
		countryDTO.setCountryIsdCode("VN");
		countryDTO.setCountryFullName("VIETNAM");
		countryDTO.setCountryIsdCode("+84");
		countryDTOList.add(countryDTO);
		when(addrContactClient.findByCountryIsdCode(any())).thenReturn(countryDTOList);
		AddrContact addrContact = new AddrContact();
		addrContact.setMobileNumber1("12345677888");
		addrContact.setMobileNumber1CountryShortName("VN");
		addrContact.setId(1L);
		when(addrContactClient.createOrUpdateAddressContact(any())).thenReturn(addrContact);
		MockHttpServletResponse testUploadSuccessResponse = mvc.perform(
				multipart(BASE_URL+"/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andReturn().getResponse();
    String responseMessage = testUploadSuccessResponse.getContentAsString();
    assertEquals(messageUtilities.getMessageWithParam("upload.excel.success", null), responseMessage);
		assertEquals(3, driverRepository.count());
	}

  @Test
	void testUploadFormulaExcelError() throws Exception{
		Path successFilePath = Paths.get("src/test/resources/excel-templates/Driver/DRIVER_FORMULA_FAIL.xlsx");
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"DRIVER_FORMULA_FAIL.xlsx",
				"text/plain", Files.newInputStream(successFilePath));
		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.DRIVER_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);
		List<CountryDTO> countryDTOList = new ArrayList<>();
		CountryDTO countryDTO = new CountryDTO();
		countryDTO.setCountryIsdCode("VN");
		countryDTO.setCountryFullName("VIETNAM");
		countryDTO.setCountryIsdCode("+84");
		countryDTOList.add(countryDTO);
		when(addrContactClient.findByCountryIsdCode(any())).thenReturn(countryDTOList);
		AddrContact addrContact = new AddrContact();
		addrContact.setMobileNumber1("12345677888");
		addrContact.setMobileNumber1CountryShortName("VN");
		addrContact.setId(1L);
		when(addrContactClient.createOrUpdateAddressContact(any())).thenReturn(addrContact);
		MockHttpServletResponse response = mvc.perform(
				multipart(BASE_URL+"/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andReturn().getResponse();
    String responseMessage = response.getContentAsString();
    assertEquals(messageUtilities.getMessageWithParam("upload.excel.success", null), responseMessage);
		assertEquals(0, driverRepository.count());
	}

	private List<DriverDto> mockListDriverDtos() {
		List<DriverDto> driverDtoList = new ArrayList<>();
		DriverDto driverDto = new DriverDto();
		driverDto.setName("Le Gia Nha 19483");
		driverDto.setExcelRowPosition(3);
		driverDtoList.add(driverDto);

		DriverDto driverDto1 = new DriverDto();
		driverDto1.setName("Le Gia Nha 19483");
		driverDto1.setExcelRowPosition(5);
		driverDtoList.add(driverDto1);

		return driverDtoList;
	}

	public List<DriverDto> mockListDriverDto(){
		List<DriverDto> listDriverDto = new ArrayList();
		DriverDto driverDto = new DriverDto();
		driverDto.setName("Test Driver Name");
		driverDto.setTransporterName("Test Transporter Name");
		driverDto.setMobileNumber1("979582692");
		driverDto.setLicenceType("Test License Type");
		driverDto.setActiveInd(true);
		driverDto.setEmail("testemail@gmail.com");
		driverDto.setLicenceNumber("Test License Number");
		listDriverDto.add(driverDto);
		return listDriverDto;
	}


	public List<UploadTemplateHdrIdDto> mockExportExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("TRANSPORTER_DRIVER_EXPORT");
		uploadTemplateHdrIdDto.setTitle("DRIVER SETTING");
		uploadTemplateHdrIdDto.setFileName("DRIVER.xlsx");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
		det1.setFieldName("name");
		det1.setPosition(0);
		det1.setWidth(6400);
		det1.setAlignment("HorizontalAlignment.CENTER");
		det1.setColumnName("Driver Name");
		det1.setColumnFullName("Driver Name");
		det1.setActiveInd(1);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("transporterName");
		det2.setPosition(2);
		det2.setColumnName("transporterName");
		det2.setColumnFullName("transporterName");
		listDetail.add(det2);

		UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
		copier.copy(det1,det3,null);
		det3.setFieldName("activeInd");
		det3.setPosition(5);
		det3.setColumnName("Status");
		det3.setColumnFullName("Status");
		listDetail.add(det3);

		UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
		copier.copy(det1,det4,null);
		det4.setFieldName("email");
		det4.setPosition(1);
		det4.setColumnName("Email");
		det4.setColumnFullName("Email");
		listDetail.add(det4);

		UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
		copier.copy(det1,det5,null);
		det5.setFieldName("mobileNumber1");
		det5.setPosition(3);
		det5.setColumnName("Mobile No.");
		det5.setColumnFullName("Mobile No.");
		listDetail.add(det5);

		UploadTemplateDetIdDto det6 = new UploadTemplateDetIdDto();
		copier.copy(det1,det6,null);
		det6.setFieldName("licenceType");
		det6.setPosition(4);
		det6.setColumnName("License Type");
		det6.setColumnFullName("License Type");
		listDetail.add(det6);

		UploadTemplateDetIdDto det7 = new UploadTemplateDetIdDto();
		copier.copy(det1,det7,null);
		det7.setFieldName("licenceNumber");
		det7.setPosition(6);
		det7.setColumnName("Licence Number");
		det7.setColumnFullName("Licence Number");
		listDetail.add(det7);

		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}


	public List<UploadTemplateHdrIdDto> mockUploadExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("DRIVER_SETTING_UPLOAD");
		uploadTemplateHdrIdDto.setTitle("DRIVER SETTING");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
		det1.setFieldName("name");
		det1.setColumnName("Driver Name");
		det1.setColumnFullName("Driver Name");
		det1.setMaxLength(255);
		det1.setMandatoryInd(1);
		det1.setActiveInd(1);
		det1.setNoneDuplicated(1);
		det1.setPosition(0);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("transporterName");
		det2.setColumnName("Transporter");
		det2.setColumnFullName("Transporter");
		det2.setMandatoryInd(0);
		det2.setMaxLength(255);
		det2.setNoneDuplicated(1);
		det2.setPosition(1);
		listDetail.add(det2);

		UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
		copier.copy(det1,det3,null);
		det3.setFieldName("licenceType");
		det3.setColumnName("License Type");
		det3.setMandatoryInd(1);
		det3.setColumnFullName("License Type");
		det3.setNoneDuplicated(1);
		det3.setPosition(2);
		listDetail.add(det3);

		UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
		copier.copy(det1,det4,null);
		det4.setFieldName("licenceNumber");
		det4.setColumnName("License No.");
		det4.setColumnFullName("License No.");
		det4.setMandatoryInd(1);
		det4.setMaxLength(255);
		det4.setNoneDuplicated(1);
		det4.setPosition(3);
		listDetail.add(det4);

		UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
		copier.copy(det1,det5,null);
		det5.setFieldName("mobileNumber1");
		det5.setPosition(5);
		det5.setColumnName("Mobile No.");
		det5.setColumnFullName("Mobile No.");
		det5.setNoneDuplicated(1);
		det5.setPosition(4);
		listDetail.add(det5);

		UploadTemplateDetIdDto det6 = new UploadTemplateDetIdDto();
		copier.copy(det1,det6,null);
		det6.setFieldName("email");
		det6.setMandatoryInd(1);
		det6.setColumnName("Email");
		det6.setColumnFullName("Email");
		det6.setNoneDuplicated(1);
		det6.setPosition(5);
		listDetail.add(det6);

		UploadTemplateDetIdDto det7 = new UploadTemplateDetIdDto();
		copier.copy(det1,det7,null);
		det7.setFieldName("activeInd");
		det7.setColumnName("Status");
		det7.setColumnFullName("Status");
		det7.setMandatoryInd(1);
		det7.setMaxLength(null);
		det7.setNoneDuplicated(1);
		det7.setPosition(6);
		listDetail.add(det7);

		UploadTemplateDetIdDto det8 = new UploadTemplateDetIdDto();
		copier.copy(det1,det8,null);
		det8.setFieldName("mobileNumber1CountryShortName");
		det8.setMaxLength(255);
		det8.setMandatoryInd(1);
		det8.setColumnName("Country Mobile Code");
		det8.setColumnFullName("Country Mobile Code");
		det8.setNoneDuplicated(1);
		det8.setPosition(7);
		listDetail.add(det8);

		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}

	@Test
	public void testQueryWithProfileScope() throws Exception {
		SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");

		this.createCustomerUsingController(sessionObj);
		this.createTransporterUsingController(sessionObj);

		Partners savedCustomer1 = partnersRepository.findByPartnerCodeAndTenantId("cus1", 1L);
		Partners savedCustomer2 = partnersRepository.findByPartnerCodeAndTenantId("cus2", 1L);
		Partners savedTransporter1 = partnersRepository.findByPartnerCodeAndTenantId("trans1", 1L);
		Partners savedTransporter2 = partnersRepository.findByPartnerCodeAndTenantId("trans2", 1L);

		this.createDriverUsingController(sessionObj);

		Driver driverSave1 = driverRepository.findByUserEmailAndTenantId("123oi1@q.com", 1L);
		Driver driverSave2 = driverRepository.findByUserEmailAndTenantId("123oi2@q.com", 1L);
		Driver driverSave3 = driverRepository.findByUserEmailAndTenantId("123oi3@q.com", 1L);
		ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
		DataTablesInput input = new DataTablesInput();
		String queryJson = ow.writeValueAsString(input);

		List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();
		ProfileScopeDTO profileScope1 = new ProfileScopeDTO();
		ProfileScopeDTO profileScope2 = new ProfileScopeDTO();
		ProfileScopeDTO profileScope3 = new ProfileScopeDTO();

		profileScope1.setProfileCode(AppConstants.ProfileCode.CUSTOMER);
		profileScope2.setProfileCode(AppConstants.ProfileCode.CUSTOMER);
		profileScope3.setProfileCode(AppConstants.ProfileCode.TRANSPORTER);

		profileScope1.setRefId(savedCustomer1.getId());
		profileScope2.setRefId(savedCustomer2.getId());
		profileScope3.setRefId(savedTransporter1.getId());

		lstProfileScope.add(profileScope1);
		lstProfileScope.add(profileScope2);
		lstProfileScope.add(profileScope3);

		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);

		MockHttpServletResponse response = mvc.perform(
				post(BASE_URL + "/query")
						.requestAttr("SESSION_INFO", sessionObj)
						.contentType(MediaType.APPLICATION_JSON)
						.content(queryJson)
		).andExpect(status().isOk()).andReturn().getResponse();

		String json = response.getContentAsString();

		ObjectMapper objectMapper = new ObjectMapper().registerModule(new ParameterNamesModule()).registerModule(new Jdk8Module())
				.registerModule(new JavaTimeModule());
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false);

		LinkedHashMap mapResponse = objectMapper.readValue(json, LinkedHashMap.class);
		Object listData = mapResponse.get("data");
		String listDataStr = objectMapper.writeValueAsString(listData);
		List<DriverTableDto> driverPage = objectMapper.readValue(
				listDataStr,
				objectMapper.getTypeFactory().constructCollectionType(List.class, DriverTableDto.class)
		);

		assertEquals(2, driverPage.size());
		Map<Long, String> assertValue = new HashMap<>();
		assertValue.put(driverSave1.getId(), driverSave1.getName());
		assertValue.put(driverSave2.getId(), driverSave2.getName());
		assertTrue(driverPage.stream().anyMatch(el -> !Objects.isNull(assertValue.get(el.getId()))
				&& assertValue.get(el.getId()).equals(el.getName())));
	}

	private void createCustomerUsingController(SessionUserInfoDTO sessionObj) throws Exception {
		CustomerDto customerDto1 = new CustomerDto();
		customerDto1.setPartnerCode("cus1");
		customerDto1.setPartnerName("Name1");
		customerDto1.setMobileNumber1("12312310");
		customerDto1.setMobileNumber1CountryShortName("AF");
		customerDto1.setEmail("mj@email.com");

		CustomerDto customerDto2 = new CustomerDto();
		customerDto2.setPartnerCode("cus2");
		customerDto2.setPartnerName("Name2");
		customerDto2.setMobileNumber1("123123102");
		customerDto2.setMobileNumber1CountryShortName("AF2");
		customerDto2.setEmail("mj@email2.com");

		this.mvc.perform(
				post( "/" + apiVersion + "/customer" + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(customerDto1))
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk())
				.andReturn();

		this.mvc.perform(
				post( "/" + apiVersion + "/customer" + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(customerDto2))
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk())
				.andReturn();
	}

	private void createTransporterUsingController(SessionUserInfoDTO sessionObj) throws Exception {
		Partners savedCustomer1 = partnersRepository.findByPartnerCodeAndTenantId("cus1", 1L);
		Partners savedCustomer2 = partnersRepository.findByPartnerCodeAndTenantId("cus2", 1L);

		Location location = new Location();
		location.setTenantId(1L);
		location.setLocName("Location A");
		location.setLocCode("Location A");
		Location savedLocation = locationRepository.save(location);

		TransporterDto transporterDto1 = new TransporterDto();
//		transporterDto1.setCustomerId(savedCustomer1.getId());
		transporterDto1.setPartnerCode("trans1");
		transporterDto1.setPartnerName("transName1");
		transporterDto1.setLocationId(savedLocation.getId());
		transporterDto1.setTenantId(1L);
		transporterDto1.setMobileNumber1("12312310");
		transporterDto1.setMobileNumber1CountryShortName("SG");
		transporterDto1.setEmail("mj@email.com");
		transporterDto1.setUnit("Unit ZZ");
		transporterDto1.setStreet("33");
		transporterDto1.setStreet2("Jurong West Z");
		transporterDto1.setCity("Jurong West Z");
		transporterDto1.setState("Singapore");
		transporterDto1.setCountryShortName("SG");
		transporterDto1.setZipCode("636465");

		TransporterDto transporterDto2 = new TransporterDto();
//		transporterDto2.setCustomerId(savedCustomer2.getId());
		transporterDto2.setPartnerCode("trans2");
		transporterDto2.setPartnerName("transName2");
		transporterDto2.setLocationId(savedLocation.getId());
		transporterDto2.setTenantId(1L);
		transporterDto2.setMobileNumber1("12312310");
		transporterDto2.setMobileNumber1CountryShortName("SG");
		transporterDto2.setEmail("mj@email.com");
		transporterDto2.setUnit("Unit ZZ");
		transporterDto2.setStreet("33");
		transporterDto2.setStreet2("Jurong West Z");
		transporterDto2.setCity("Jurong West Z");
		transporterDto2.setState("Singapore");
		transporterDto2.setCountryShortName("SG");
		transporterDto2.setZipCode("636465");

		AddrContact addrContact = new AddrContact();
		addrContact.setId(2L);
		addrContact.setMobileNumber1("12312310");
		addrContact.setMobileNumber1CountryShortName("SG");
		when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

		AddrDTO addr = new AddrDTO();
		addr.setId(2L);
		when(addrClient.createOrUpdateAddress(any(UpdateAddrDTO.class))).thenReturn(addr);

		this.mvc.perform(
				post("/" + apiVersion +"/transporter" + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transporterDto1))
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk())
				.andReturn();

		this.mvc.perform(
				post("/" + apiVersion +"/transporter" + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transporterDto2))
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	void testDownloadExcelWithProfileScope() throws Exception {
		SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");

		this.createCustomerUsingController(sessionObj);
		this.createTransporterUsingController(sessionObj);

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.DRIVER_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		Partners savedCustomer1 = partnersRepository.findByPartnerCodeAndTenantId("cus1", 1L);
		Partners savedCustomer2 = partnersRepository.findByPartnerCodeAndTenantId("cus2", 1L);
		Partners savedTransporter1 = partnersRepository.findByPartnerCodeAndTenantId("trans1", 1L);
		Partners savedTransporter2 = partnersRepository.findByPartnerCodeAndTenantId("trans2", 1L);

		this.createDriverUsingController(sessionObj);

		List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();
		ProfileScopeDTO profileScope1 = new ProfileScopeDTO();
		ProfileScopeDTO profileScope2 = new ProfileScopeDTO();
		ProfileScopeDTO profileScope3 = new ProfileScopeDTO();

		profileScope1.setProfileCode(AppConstants.ProfileCode.CUSTOMER);
		profileScope2.setProfileCode(AppConstants.ProfileCode.CUSTOMER);
		profileScope3.setProfileCode(AppConstants.ProfileCode.TRANSPORTER);

		profileScope1.setRefId(savedCustomer1.getId());
		profileScope2.setRefId(savedCustomer2.getId());
		profileScope3.setRefId(savedTransporter1.getId());

		lstProfileScope.add(profileScope1);
		lstProfileScope.add(profileScope2);
		lstProfileScope.add(profileScope3);

		AddrContact addrContact = new AddrContact();
		addrContact.setId(2L);
		addrContact.setMobileNumber1("12312310");
		addrContact.setMobileNumber1CountryShortName("SG");
		Mockito.when(addrContactClient.getAddressContact(any())).thenReturn(addrContact);
		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionObj)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}")).andExpect(status().isOk()).andReturn();
	}

	private void createDriverUsingController(SessionUserInfoDTO sessionObj) throws Exception {
		Partners savedTransporter1 = partnersRepository.findByPartnerCodeAndTenantId("trans1", 1L);
		Partners savedTransporter2 = partnersRepository.findByPartnerCodeAndTenantId("trans2", 1L);

		//Create driver using driver controller
		DriverDto driver1 = new DriverDto();
		driver1.setName("driver1");
		driver1.setActiveInd(Boolean.TRUE);
		driver1.setLicenceNumber("test1");
		driver1.setLicenceType("1|3|4");
		driver1.setAvailable(Boolean.TRUE);
		driver1.setTenantId(1L);
		driver1.setEmail("123oi1@q.com");
		driver1.setMobileNumber1("123456781");
		driver1.setMobileNumber1CountryShortName("SG");
		driver1.setTransporterId(savedTransporter1.getId());

		DriverDto driver2 = new DriverDto();
		driver2.setName("driver2");
		driver2.setActiveInd(Boolean.TRUE);
		driver2.setLicenceNumber("test2");
		driver2.setLicenceType("1|3|4");
		driver2.setAvailable(Boolean.TRUE);
		driver2.setTenantId(1L);
		driver2.setEmail("123oi2@q.com");
		driver2.setMobileNumber1("123456782");
		driver2.setMobileNumber1CountryShortName("SG");
		driver2.setTransporterId(savedTransporter1.getId());

		DriverDto driver3 = new DriverDto();
		driver3.setName("driver3");
		driver3.setActiveInd(Boolean.TRUE);
		driver3.setLicenceNumber("test2");
		driver3.setLicenceType("1|3|4");
		driver3.setAvailable(Boolean.TRUE);
		driver3.setTenantId(1L);
		driver3.setEmail("123oi3@q.com");
		driver3.setMobileNumber1("123456783");
		driver3.setMobileNumber1CountryShortName("SG");
		driver3.setTransporterId(savedTransporter2.getId());

		ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
		String driverJson1 = ow.writeValueAsString(driver1);
		String driverJson2 = ow.writeValueAsString(driver2);
		String driverJson3 = ow.writeValueAsString(driver3);

		mvc.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON).content(driverJson1)
				.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk()).andReturn();
		mvc.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON).content(driverJson2)
				.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk()).andReturn();
		mvc.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON).content(driverJson3)
				.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk()).andReturn();
	}

	@Test
	void testDownloadExcelSorting() throws Exception {
		SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");

		this.createCustomerUsingController(sessionObj);
		this.createTransporterUsingController(sessionObj);

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.DRIVER_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		this.createDriverUsingController(sessionObj);

		List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();

		AddrContact addrContact = new AddrContact();
		addrContact.setId(2L);
		addrContact.setMobileNumber1("12312310");
		addrContact.setMobileNumber1CountryShortName("SG");
		Mockito.when(addrContactClient.getAddressContact(any())).thenReturn(addrContact);
		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);

		String json1 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.DRIVER_NAME + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
		String json2 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.DRIVER_LICENCE_TYPE + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";
		String json3 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.DRIVER_TRANSPORTER_NAME + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
		String json4 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.DRIVER_MOBILE_NUMBER + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionObj)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json1)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionObj)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json2)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionObj)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json3)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionObj)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json4)).andExpect(status().isOk()).andReturn();
	}

	@Test
	void testDownloadExcelFilter() throws Exception {
		SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");

		this.createCustomerUsingController(sessionObj);
		this.createTransporterUsingController(sessionObj);

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.DRIVER_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		this.createDriverUsingController(sessionObj);

		List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();

		AddrContact addrContact = new AddrContact();
		addrContact.setId(2L);
		addrContact.setMobileNumber1("12312310");
		addrContact.setMobileNumber1CountryShortName("SG");
		Mockito.when(addrContactClient.getAddressContact(any())).thenReturn(addrContact);
		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);

		Partners savedTransporter1 = partnersRepository.findByPartnerCodeAndTenantId("trans1", 1L);
		String json = "{\"name\":\"1\",\"licenceType\":\"1\",\"mobileNumber1\":\"12312310\",\"partnerId\":" + savedTransporter1.getId() + "}";

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.requestAttr("SESSION_INFO", sessionObj)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json)).andExpect(status().isOk()).andReturn();
	}
}
