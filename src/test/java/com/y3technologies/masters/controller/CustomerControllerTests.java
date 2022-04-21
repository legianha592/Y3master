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
import com.y3technologies.masters.client.AddrContactClient;
import com.y3technologies.masters.client.EmailClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.CustomerDto;
import com.y3technologies.masters.dto.PartnerTypesDto;
import com.y3technologies.masters.dto.aas.ProfileScopeDTO;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.CountryDTO;
import com.y3technologies.masters.dto.excel.UploadTemplateDetIdDto;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.LookupFilter;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.exception.RestErrorMessage;
import com.y3technologies.masters.model.Equipment;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.PartnerLocation;
import com.y3technologies.masters.model.PartnerTypes;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.model.comm.Country;
import com.y3technologies.masters.repository.LocationRepository;
import com.y3technologies.masters.repository.LookupRepository;
import com.y3technologies.masters.repository.PartnerLocationRepository;
import com.y3technologies.masters.repository.PartnerTypesRepository;
import com.y3technologies.masters.repository.PartnersRepository;
import com.y3technologies.masters.service.CustomerService;
import com.y3technologies.masters.service.LocationService;
import com.y3technologies.masters.service.LookupService;
import com.y3technologies.masters.service.impl.CustomerServiceImpl;
import com.y3technologies.masters.util.DateFormatUtil;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;
import org.junit.Assert;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CustomerControllerTests {

	private static final Logger logger = LoggerFactory.getLogger(CustomerControllerTests.class);
	private String BASE_URL;
	private static ObjectMapper objectMapper;

	private MockMvc mvc;
	@Autowired
	private WebApplicationContext wac;

	@Autowired
	MessagesUtilities messagesUtilities;

	@Autowired
	private PartnersRepository partnersRepository;

	@Autowired
	private CustomerService customerService;

	@SpyBean
	private CustomerService customerServiceSpy;

	@SpyBean
	private CustomerServiceImpl customerServiceImpl;

	@Autowired
	private LocationRepository locationRepository;

	@Autowired
	private LocationService locationService;

	@Autowired
	private PartnerTypesRepository partnerTypesRepository;

	@Autowired
	private PartnerLocationRepository partnerLocationRepository;

	@Autowired
	private LookupService lookupService;

	@Value("/${api.version.masters:v1}")
	private String apiVersion;

	@MockBean
	AddrContactClient addrContactClient;

	@MockBean
	AasClient aasClient;

	@MockBean
	private ExcelClient excelClient;

	@MockBean
	private EmailClient emailClient;

	@Autowired
	private LookupRepository lookupRepository;

  @Autowired
  private MessagesUtilities messageUtilities;

	@SpyBean
	ExcelUtils excelUtils;

	@PostConstruct
	public void setApiVersion() {
		BASE_URL = apiVersion +"/customer";
	}

	private Partners savedPartners;
	private Lookup customerLookup;
	private CustomerDto customerDto;
	SessionUserInfoDTO sessionObj;

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

		sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setAasTenantUserId(2L);
		sessionObj.setTimezone("Asia/Singapore");

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

		LookupFilter lookupFilter = new LookupFilter();
		lookupFilter.setLookupCode("CUSTOMER");
		lookupFilter.setLookupType("PartnerTypes");
		List<Lookup> lookupList = lookupService.findByFilter(lookupFilter);
		customerLookup = lookupList.get(0);

		savedPartners = new Partners();
		savedPartners.setPartnerCode("code");
		savedPartners.setPartnerName("Name");
		savedPartners.setTenantId(1L);

		customerDto= new CustomerDto();
		customerDto.setPartnerCode("code");
		customerDto.setPartnerName("Name");
		customerDto.setMobileNumber1("12312310");
		customerDto.setMobileNumber1CountryShortName("AF");
		customerDto.setEmail("mj@email.com");

		AddrContact addrContact = new AddrContact();
		addrContact.setId(1L);
		when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);
		when(aasClient.updateTenantUserProfileValue(any(String.class), any(Long.class))).thenReturn(ResponseEntity.ok().build());

		when(addrContactClient.getAddressContact(1L)).thenReturn(addrContact);
		when(aasClient.updateTenantUserProfileValue(any(String.class), any(Long.class))).thenReturn(ResponseEntity.ok().build());
	}

	@AfterEach
	public void remove() throws Exception{
		locationRepository.deleteAll();
		partnerTypesRepository.deleteAll();
		partnerLocationRepository.deleteAll();
		partnersRepository.deleteAll();
		lookupRepository.deleteAll();
		partnersRepository = null;
		partnerTypesRepository = null;
		partnerLocationRepository = null;
		locationRepository = null;
		sessionObj = null;
	}

	@Test
	@Transactional
	public void testCreateCustomer() throws Exception {
		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(customerDto))
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		Partners savedPartners = partnersRepository.findByPartnerCodeAndTenantId("code", 1L);
		assertNotNull(savedPartners);
		List<PartnerTypesDto> partnerTypesList = partnerTypesRepository.findByPartnerId(savedPartners.getId());
		assertEquals(1, partnerTypesList.size());
		assertEquals(customerLookup.getId(), partnerTypesList.get(0).getPartnerTypeId());
		List<PartnerLocation> partnerLocationList = partnerLocationRepository.findByPartnerId(savedPartners.getId());
		assertEquals(1, partnerLocationList.size());

		CustomerDto dto = new CustomerDto();
		dto.setId(savedPartners.getId());
		dto.setPartnerCode(savedPartners.getPartnerCode());
		dto.setPartnerName(savedPartners.getPartnerName());
		dto.setMobileNumber1("12312310");
		dto.setMobileNumber1CountryShortName("AF");
		dto.setEmail("mj@email.com");

		response = this.mvc.perform(
				post(BASE_URL + "/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto))
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		Partners updatedPartners = partnersRepository.findByPartnerCodeAndTenantId(savedPartners.getPartnerCode(), 1L);
		assertNotNull(updatedPartners);
		assertEquals(updatedPartners.getPartnerCode(), customerDto.getPartnerCode());
		assertEquals(updatedPartners.getPartnerName(), customerDto.getPartnerName());
		partnerTypesList = partnerTypesRepository.findByPartnerId(updatedPartners.getId());
		assertEquals(1, partnerTypesList.size());
		assertEquals(customerLookup.getId(), partnerTypesList.get(0).getPartnerTypeId());
		List<PartnerLocation> updatedPartnerLocationList = partnerLocationRepository.findByPartnerId(updatedPartners.getId());
		assertEquals(1, updatedPartnerLocationList.size());
		Assert.assertEquals(updatedPartnerLocationList.get(0).getAddressContactId(), partnerLocationList.get(0).getAddressContactId());
	}

	@Test
	@Transactional
	public void testCreateCustomerWithExistingNameAndCode() throws Exception {
		savedPartners = customerService.save(customerDto);

		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(customerDto)))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse();

		RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		ArrayList<String> errorMessages = message.getMessages();
		assertTrue(errorMessages.contains("The customer is already in our system."));
	}

	@Test
	@Transactional
	public void testUpdateCustomerWithSameLocationNameDifferentAddressContact() throws Exception {

		savedPartners = customerService.save(customerDto);
		List<PartnerLocation> partnerLocationList = partnerLocationRepository.findByPartnerId(savedPartners.getId());
		PartnerLocation partnerLocation = partnerLocationList.get(0);
		Long addressContactId = partnerLocation.getAddressContactId();
		assertEquals(addressContactId, 1L);

		CustomerDto dto = new CustomerDto();
		dto.setId(savedPartners.getId());
		dto.setPartnerCode("code");
		dto.setPartnerName("Name");
		dto.setMobileNumber1("12312310");
		dto.setMobileNumber1CountryShortName("SG");
		dto.setEmail("mj@email.com");

		AddrContact addrContact = new AddrContact();
		addrContact.setId(2L);
		when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto))
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		List<PartnerLocation> updatedPartnerLocationList = partnerLocationRepository.findByPartnerId(savedPartners.getId());
		PartnerLocation updatedPartnerLocation = updatedPartnerLocationList.get(0);
		assertEquals(partnerLocation.getLocationId(), updatedPartnerLocation.getLocationId());
		assertNotEquals(java.util.Optional.ofNullable(updatedPartnerLocation.getAddressContactId()), 2L);
	}

	@Test
	@Transactional
	public void testUpdateCustomerWithInvalidId() throws Exception {
		customerDto.setId(0L);

		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(customerDto)))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse();

		RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		ArrayList<String> errorMessages = message.getMessages();
		assertTrue(errorMessages.contains("Customer is invalid."));
	}

	@Test
	@Transactional
	public void testUpdateCustomerWithExistingNameAndCode() throws Exception {
		savedPartners = customerService.save(customerDto);

		customerDto.setPartnerCode("code A");
		customerDto.setPartnerName("Name A");
		Partners newPartner = customerService.save(customerDto);

		assertNotEquals(newPartner.getId(), savedPartners.getId());
		customerDto.setId(savedPartners.getId());

		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(customerDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		Optional<Partners> savedPartner = partnersRepository.findById(savedPartners.getId());
		assertEquals("code A", savedPartner.get().getPartnerCode());
		assertEquals("Name", savedPartner.get().getPartnerName());

	}

	@Test
	@Transactional
	public void testUpdateCustomerStatus() throws Exception {
		savedPartners = customerService.save(customerDto);

		assertNotNull(partnersRepository.findById(savedPartners.getId()));
		assertEquals(true, savedPartners.getActiveInd());

		MockHttpServletResponse response = this.mvc
				.perform(get(BASE_URL + "/updateStatus?id=" + savedPartners.getId() + "&status=" + false).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse();

		String id = objectMapper.readValue(response.getContentAsString(), String.class);
		assertNotNull(id);
	}

	@Test
	@Transactional
	public void testUpdateCustomerStatusWithInvalidId() throws Exception {
		MockHttpServletResponse response = this.mvc
				.perform(get(BASE_URL + "/updateStatus?id=" + 0L + "&status=" + false).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn().getResponse();

		RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		ArrayList<String> errorMessages = message.getMessages();
		assertTrue(errorMessages.contains("Customer is invalid."));
	}

	@Test
	@Transactional
	public void testRetrieveById() throws Exception {
		savedPartners = customerService.save(customerDto);

		MockHttpServletResponse response = this.mvc
				.perform(get(BASE_URL + "/retrieve?id=" + savedPartners.getId()).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse();

		List<PartnerLocation> partnerLocationList = partnerLocationRepository.findByPartnerId(savedPartners.getId());
		assertEquals(1, partnerLocationList.size());
		CustomerDto savedCustomerDto = objectMapper.readValue(response.getContentAsString(), CustomerDto.class);
		assertEquals("code", savedCustomerDto.getPartnerCode());
		assertEquals(partnerLocationList.get(0).getAddressContactId(), savedCustomerDto.getAddrContact().getId());

	}

	@Test
	@Transactional
	public void testRetrieveByInvalidId() throws Exception {

		MockHttpServletResponse response = this.mvc
				.perform(get(BASE_URL + "/retrieve?id=" + 0L).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn().getResponse();

		RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		ArrayList<String> errorMessages = message.getMessages();
		assertTrue(errorMessages.contains("Customer is invalid."));

	}

	@Test
	@Transactional
	public void testFindBySearch() throws Exception {
		PartnersFilter filter = new PartnersFilter();

		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(new ArrayList<>());

		MockHttpServletResponse response = this.mvc
				.perform(post(BASE_URL + "/findBySearch").contentType(MediaType.APPLICATION_JSON).content("{}")
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk()).andReturn().getResponse();
	}

	@Test
	@Transactional
	public void testFindByFilter() throws Exception {

		LookupFilter lookupFilter = new LookupFilter();
		lookupFilter.setLookupCode("TRANSPORTER");
		lookupFilter.setLookupType("PartnerTypes");
		List<Lookup> lookupTransporterList = lookupService.findByFilter(lookupFilter);
		assertEquals(1, lookupTransporterList.size());

		Partners savedPartners = new Partners();
		savedPartners.setActiveInd(Boolean.TRUE);
		savedPartners.setTenantId(1L);
		savedPartners.setPartnerCode("TEST PARTNER CODE");
		savedPartners.setPartnerName("TEST PARTNER NAME");
		partnersRepository.save(savedPartners);

		Partners savedPartners2 = new Partners();
		savedPartners2.setActiveInd(Boolean.FALSE);
		savedPartners2.setTenantId(1L);
		savedPartners2.setPartnerCode("TEST PARTNER CODE 2");
		savedPartners2.setPartnerName("TEST PARTNER NAME 2");
		partnersRepository.save(savedPartners2);

		lookupFilter = new LookupFilter();
		lookupFilter.setLookupCode("CUSTOMER");
		lookupFilter.setLookupType("PartnerTypes");
		List<Lookup> lookupCustomerList = lookupService.findByFilter(lookupFilter);
		assertEquals(1, lookupCustomerList.size());

		PartnerTypes partnerTypes = new PartnerTypes();
		partnerTypes.setPartners(savedPartners);
		partnerTypes.setLookup(lookupTransporterList.get(0));
		partnerTypesRepository.save(partnerTypes);

		PartnerTypes partnerTypes2 = new PartnerTypes();
		partnerTypes2.setPartners(savedPartners2);
		partnerTypes2.setLookup(lookupCustomerList.get(0));
		partnerTypesRepository.save(partnerTypes2);

		//findByName
		MultiValueMap<String, String> params = new HttpHeaders();
		params.add("tenantId", String.valueOf(1L));
		params.add("name", "TEST PARTNER NAME 2");

		MockHttpServletResponse response = this.mvc.perform(
				get(BASE_URL + "/findByFilter")
						.params(params)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		List<Partners> partnersList = objectMapper.readValue(response.getContentAsString(),
				new TypeReference<>() {
				});

		assertEquals(1, partnersList.size());

		//findByPartnerType
		params = new HttpHeaders();
		params.add("partnerType", "CUSTOMER");
		params.add("tenantId", String.valueOf(1L));

		response = this.mvc.perform(
				get(BASE_URL + "/findByFilter")
						.params(params)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		partnersList = objectMapper.readValue(response.getContentAsString(),
				new TypeReference<>() {
				});

		assertEquals(1, partnersList.size());

		//findByActiveInd
		params = new HttpHeaders();
		params.add("tenantId", String.valueOf(1L));
		params.add("activeInd", String.valueOf(Boolean.FALSE));

		response = this.mvc.perform(
				get(BASE_URL + "/findByFilter")
						.params(params)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		partnersList = objectMapper.readValue(response.getContentAsString(),
				new TypeReference<>() {
				});

		assertEquals(1, partnersList.size());
		assertEquals(partnersList.get(0).getActiveInd(), savedPartners2.getActiveInd());

	}

	@Test
	public void testDownloadExcel() throws Exception {

		List<Partners> listCustomer = mockListCustomer();

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CUSTOMER_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);
		Page<Partners> partnersPage = new PageImpl<>(listCustomer);
		Mockito.doReturn(partnersPage).when(customerServiceSpy).findBySearch(any(), any(), any());
		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
	}

	@Test
	void testUploadExcel() throws Exception {
		List<CountryDTO> countryDTOList = new ArrayList<>();
		CountryDTO countryDTO = new CountryDTO();
		countryDTO.setCountryIsdCode("+65");
		countryDTO.setCountryFullName("SINGAPORE");
		countryDTO.setCountryShortName("SG");
		countryDTOList.add(countryDTO);

		//test data errors
		Path dataErrorPath = Paths.get("src/test/resources/excel-templates/Customer/CUSTOMER_ERROR.xlsx");

		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CUSTOMER_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);
		when(addrContactClient.findByCountryIsdCode(any())).thenReturn(countryDTOList);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(),any(),any(),any(),any(),any());

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"CUSTOMER_ERROR.xlsx",
				"text/plain", Files.newInputStream(dataErrorPath));

		MockHttpServletResponse testDataErrorResponse = mvc.perform(
				multipart(BASE_URL+"/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		//test upload excel successfully
		Path successFilePath = Paths.get("src/test/resources/excel-templates/Customer/CUSTOMER_SUCCESS.xlsx");

		mockMultipartFile = new MockMultipartFile("file",
				"CUSTOMER_SUCCESS.xlsx",
				"text/plain", Files.newInputStream(successFilePath));

		MockHttpServletResponse testUploadSuccessResponse = mvc.perform(
				multipart(BASE_URL+"/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		assertEquals(3, partnersRepository.count());
	}

  @Test
	void testUploadFormulaExcelSuccess() throws Exception {
		List<CountryDTO> countryDTOList = new ArrayList<>();
		CountryDTO countryDTO = new CountryDTO();
		countryDTO.setCountryIsdCode("+84");
		countryDTO.setCountryFullName("VIETNAM");
		countryDTO.setCountryShortName("VN");
		countryDTOList.add(countryDTO);
		Path dataErrorPath = Paths.get("src/test/resources/excel-templates/Customer/CUSTOMER_FORMULA_SUCCESS.xlsx");
		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CUSTOMER_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);
		when(addrContactClient.findByCountryIsdCode(any())).thenReturn(countryDTOList);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(),any(),any(),any(),any(),any());
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"CUSTOMER_FORMULA_SUCCESS.xlsx",
				"text/plain", Files.newInputStream(dataErrorPath));
		MockHttpServletResponse response = mvc.perform(
				multipart(BASE_URL+"/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andReturn().getResponse();
    String responseMessage = response.getContentAsString();
    assertEquals(messageUtilities.getMessageWithParam("upload.excel.success", null), responseMessage);
		assertEquals(3, partnersRepository.count());
	}

  @Test
	void testUploadFormulaExcelError() throws Exception {
		List<CountryDTO> countryDTOList = new ArrayList<>();
		CountryDTO countryDTO = new CountryDTO();
		countryDTO.setCountryIsdCode("+84");
		countryDTO.setCountryFullName("VIETNAM");
		countryDTO.setCountryShortName("VN");
		countryDTOList.add(countryDTO);
		Path dataErrorPath = Paths.get("src/test/resources/excel-templates/Customer/CUSTOMER_FORMULA_ERROR.xlsx");
		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CUSTOMER_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);
		when(addrContactClient.findByCountryIsdCode(any())).thenReturn(countryDTOList);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(),any(),any(),any(),any(),any());
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"CUSTOMER_FORMULA_ERROR.xlsx",
				"text/plain", Files.newInputStream(dataErrorPath));
		MockHttpServletResponse response = mvc.perform(
				multipart(BASE_URL+"/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andReturn().getResponse();
    String responseMessage = response.getContentAsString();
    assertEquals(messageUtilities.getMessageWithParam("upload.excel.success", null), responseMessage);
		assertEquals(0, partnersRepository.count());
	}

	private List<CustomerDto> mockListCustomerDtos() {
		List<CustomerDto> customerDtoList = new ArrayList<>();

		CustomerDto customerDto = new CustomerDto();
		customerDto.setPartnerName("Le Gia Nha 31830");
		customerDto.setExcelRowPosition(3);
		customerDtoList.add(customerDto);

		CustomerDto customerDto1 = new CustomerDto();
		customerDto1.setPartnerName("Le Gia Nha 31830");
		customerDto1.setExcelRowPosition(5);
		customerDtoList.add(customerDto1);

		return customerDtoList;
	}

	public List<Partners> mockListCustomer(){
		List<Partners> listCustomer = new ArrayList();
		Partners customer = new Partners();
		customer.setEmail("test@gmail.com");
		customer.setPartnerName("customer Test Name");
		customer.setPartnerCode("");
		customer.setActiveInd(true);
		customer.setPhone("15545");
		customer.setPhoneCountryShortName("VN");
		customer.setDescription("test Description");
		customer.setPhoneAreaCode("");
		listCustomer.add(customer);
		return listCustomer;
	}

	public List<UploadTemplateHdrIdDto> mockExportExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("CUSTOMER_SETTING_EXPORT");
		uploadTemplateHdrIdDto.setTitle("CUSTOMER");
		uploadTemplateHdrIdDto.setFileName("CUSTOMER.xlsx");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
		det1.setFieldName("partnerCode");
		det1.setPosition(1);
		det1.setWidth(6400);
		det1.setAlignment("HorizontalAlignment.CENTER");
		det1.setColumnName("Customer Code");
		det1.setColumnFullName("Customer Code");
		det1.setActiveInd(1);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("partnerName");
		det2.setPosition(0);
		det2.setColumnName("Customer Name");
		det2.setColumnFullName("Customer Name");
		listDetail.add(det2);

		UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
		copier.copy(det1,det3,null);
		det3.setFieldName("activeInd");
		det3.setPosition(2);
		det3.setColumnName("Status");
		det3.setColumnFullName("Status");
		listDetail.add(det3);

		UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
		copier.copy(det1,det4,null);
		det4.setFieldName("email");
		det4.setPosition(3);
		det4.setColumnName("Email");
		det4.setColumnFullName("Email");
		listDetail.add(det4);

		UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
		copier.copy(det1,det5,null);
		det5.setFieldName("phone");
		det5.setPosition(4);
		det5.setColumnName("Phone");
		det5.setColumnFullName("Phone");
		listDetail.add(det5);

		UploadTemplateDetIdDto det6 = new UploadTemplateDetIdDto();
		copier.copy(det1,det6,null);
		det6.setFieldName("phoneCountryShortName");
		det6.setPosition(5);
		det6.setColumnName("Phone Country Short Name");
		det6.setColumnFullName("Phone Country Short Name");
		listDetail.add(det6);

		UploadTemplateDetIdDto det7 = new UploadTemplateDetIdDto();
		copier.copy(det1,det7,null);
		det7.setFieldName("description");
		det7.setPosition(7);
		det7.setColumnName("Description");
		det7.setColumnFullName("Description");
		listDetail.add(det7);

		UploadTemplateDetIdDto det8 = new UploadTemplateDetIdDto();
		copier.copy(det1,det8,null);
		det8.setFieldName("phoneAreaCode");
		det8.setPosition(6);
		det8.setColumnName("Phone Area Code");
		det8.setColumnFullName("Phone Area Code");
		listDetail.add(det8);

		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}


	public List<UploadTemplateHdrIdDto> mockUploadExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("CUSTOMER_SETTING_UPLOAD");
		uploadTemplateHdrIdDto.setTitle("CUSTOMER SETTING");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
		det1.setFieldName("partnerCode");
		det1.setColumnName("Customer Code");
		det1.setColumnFullName("Customer Code");
		det1.setMaxLength(255);
		det1.setMandatoryInd(0);
		det1.setActiveInd(1);
		det1.setNoneDuplicated(1);
		det1.setPosition(0);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("partnerName");
		det2.setColumnName("Customer Name");
		det2.setColumnFullName("Customer Name");
		det2.setMandatoryInd(1);
		det2.setMaxLength(255);
		det2.setNoneDuplicated(1);
		det2.setPosition(1);
		listDetail.add(det2);

		UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
		copier.copy(det1,det3,null);
		det3.setFieldName("activeInd");
		det3.setColumnName("Status");
		det3.setMandatoryInd(0);
		det3.setColumnFullName("Status");
		det3.setNoneDuplicated(1);
		det3.setPosition(2);
		listDetail.add(det3);

		UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
		copier.copy(det1,det4,null);
		det4.setFieldName("email");
		det4.setColumnName("Email");
		det4.setColumnFullName("Email");
		det4.setMandatoryInd(0);
		det4.setMaxLength(255);
		det4.setNoneDuplicated(1);
		det4.setPosition(3);
		listDetail.add(det4);

		UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
		copier.copy(det1,det5,null);
		det5.setFieldName("mobileNumber1CountryShortName");
		det5.setMandatoryInd(1);
		det5.setColumnName("Country Mobile Code");
		det5.setColumnFullName("Country Mobile Code");
		det5.setNoneDuplicated(1);
		det5.setPosition(4);
		listDetail.add(det5);

		UploadTemplateDetIdDto det6 = new UploadTemplateDetIdDto();
		copier.copy(det1,det6,null);
		det6.setFieldName("mobileNumber1");
		det6.setMandatoryInd(1);
		det6.setColumnName("Mobile No.");
		det6.setColumnFullName("Mobile No.");
		det6.setMandatoryInd(1);
		det6.setNoneDuplicated(1);
		det6.setPosition(5);
		listDetail.add(det6);


		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}

	@Test
	public void testFindBySearchWithProfileScope() throws Exception {
		SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");

		this.createCustomerUsingController(sessionObj);

		Partners savedCustomer1 = partnersRepository.findByPartnerCodeAndTenantId("cus1", 1L);
		Partners savedCustomer2 = partnersRepository.findByPartnerCodeAndTenantId("cus2", 1L);

		List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();
		ProfileScopeDTO profileScope1 = new ProfileScopeDTO();
		profileScope1.setProfileCode(AppConstants.ProfileCode.CUSTOMER);
		profileScope1.setRefId(savedCustomer1.getId());
		lstProfileScope.add(profileScope1);
		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);

		MockHttpServletResponse response = mvc.perform(
				post(BASE_URL + "/findBySearch")
						.requestAttr("SESSION_INFO", sessionObj)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}")
		).andExpect(status().isOk()).andReturn().getResponse();

		String json = response.getContentAsString();

		LinkedHashMap mapResponse = objectMapper.readValue(json, LinkedHashMap.class);
		Object listData = mapResponse.get("content");
		String listDataStr = objectMapper.writeValueAsString(listData);
		List<Partners> customerPage = objectMapper.readValue(
				listDataStr,
				objectMapper.getTypeFactory().constructCollectionType(List.class, Partners.class)
		);
		assertEquals(1, customerPage.size());
		assertEquals(savedCustomer1, customerPage.get(0));

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
				post( BASE_URL + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(customerDto1))
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk())
				.andReturn();

		this.mvc.perform(
				post( BASE_URL + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(customerDto2))
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	public void testDownloadExcelWithProfileScope() throws Exception {
		SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");

		this.createCustomerUsingController(sessionObj);

		Partners savedCustomer1 = partnersRepository.findByPartnerCodeAndTenantId("cus1", 1L);
		Partners savedCustomer2 = partnersRepository.findByPartnerCodeAndTenantId("cus2", 1L);

		List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();
		ProfileScopeDTO profileScope1 = new ProfileScopeDTO();
		profileScope1.setProfileCode(AppConstants.ProfileCode.CUSTOMER);
		profileScope1.setRefId(savedCustomer1.getId());
		lstProfileScope.add(profileScope1);
		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CUSTOMER_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		mvc.perform(
				post(BASE_URL + "/downloadExcel")
						.requestAttr("SESSION_INFO", sessionObj)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}")
		).andExpect(status().isOk()).andReturn();
	}

	@Test
	public void testDownloadExcelSorting() throws Exception {
		SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");

		this.createCustomerUsingController(sessionObj);

		List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();
		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CUSTOMER_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		String json1 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.CUSTOMER_CODE + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
		String json2 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.CUSTOMER_NAME + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";

		mvc.perform(
				post(BASE_URL + "/downloadExcel")
						.requestAttr("SESSION_INFO", sessionObj)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json1)
		).andExpect(status().isOk()).andReturn();

		mvc.perform(
				post(BASE_URL + "/downloadExcel")
						.requestAttr("SESSION_INFO", sessionObj)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json2)
		).andExpect(status().isOk()).andReturn();
	}

	@Test
	public void testDownloadExcelFilter() throws Exception {
		SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");

		this.createCustomerUsingController(sessionObj);

		List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();
		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CUSTOMER_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		String json1 = "{\"" + AppConstants.SortPropertyName.CUSTOMER_CODE + "\": \"" + "ABC" + "\"}";
		String json2 = "{\"" + AppConstants.SortPropertyName.CUSTOMER_NAME + "\": \"" + "ABC" + "\"}";

		mvc.perform(
				post(BASE_URL + "/downloadExcel")
						.requestAttr("SESSION_INFO", sessionObj)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json1)
		).andExpect(status().isOk()).andReturn();

		mvc.perform(
				post(BASE_URL + "/downloadExcel")
						.requestAttr("SESSION_INFO", sessionObj)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json2)
		).andExpect(status().isOk()).andReturn();
	}
}
