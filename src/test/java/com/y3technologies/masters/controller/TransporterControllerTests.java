package com.y3technologies.masters.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.y3technologies.masters.client.*;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.CustomerDto;
import com.y3technologies.masters.dto.LocationDto;
import com.y3technologies.masters.dto.PartnerTypesDto;
import com.y3technologies.masters.dto.TransporterDto;
import com.y3technologies.masters.dto.aas.ProfileScopeDTO;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.CountryDTO;
import com.y3technologies.masters.dto.comm.UpdateAddrDTO;
import com.y3technologies.masters.dto.excel.UploadTemplateDetIdDto;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.LookupFilter;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.exception.RestErrorMessage;
import com.y3technologies.masters.model.Location;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.PartnerLocation;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.model.comm.AddrDTO;
import com.y3technologies.masters.repository.LocationRepository;
import com.y3technologies.masters.repository.LookupRepository;
import com.y3technologies.masters.repository.PartnerLocationRepository;
import com.y3technologies.masters.repository.PartnerTypesRepository;
import com.y3technologies.masters.repository.PartnersRepository;
import com.y3technologies.masters.service.*;
import com.y3technologies.masters.service.impl.LocationServiceImpl;
import com.y3technologies.masters.service.impl.TransporterServiceImpl;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
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
import java.util.Optional;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TransporterControllerTests {
	
	private static final Logger logger = LoggerFactory.getLogger(TransporterControllerTests.class);
	private String BASE_URL;
	private static ObjectMapper objectMapper;
	SessionUserInfoDTO sessionObj;

	private MockMvc mvc;
	@Autowired
	private WebApplicationContext wac;

	@Autowired
	MessagesUtilities messagesUtilities;

	@Autowired
	private PartnersRepository partnersRepository;

	@Autowired
	private TransporterService transporterService;

	@SpyBean
	private TransporterService transporterServiceSpy;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private LocationRepository locationRepository;

	@SpyBean
	private LocationService locationService;

	@SpyBean
	private TransporterServiceImpl transporterServiceImpl;

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
	AddrClient addrClient;

	@MockBean
	AasClient aasClient;

	@MockBean
	ExcelClient excelClient;

	@MockBean
	EmailClient emailClient;

	@SpyBean
	PartnersService partnersService;

	@SpyBean
	LocationServiceImpl locationServiceImpl;

	@SpyBean
	private ExcelUtils excelUtils;

	@Autowired
	private LookupRepository lookupRepository;

	@PostConstruct
	public void setApiVersion() {
		BASE_URL = apiVersion +"/transporter";
	}

	private Partners savedPartners;
	private Partners savedCustomer;
	private Lookup transporterLookup;
	private TransporterDto transporterDto;
	private Location savedLocation;

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

		AddrContact addrContact = new AddrContact();
		addrContact.setId(1L);
		when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

		AddrDTO addr = new AddrDTO();
		addr.setId(1L);
		when(addrClient.createOrUpdateAddress(any(UpdateAddrDTO.class))).thenReturn(addr);

		when(aasClient.updateTenantUserProfileValue(any(String.class), any(Long.class))).thenReturn(ResponseEntity.ok().build());

		when(addrClient.getAddress(1L)).thenReturn(addr);
		when(addrContactClient.getAddressContact(1L)).thenReturn(addrContact);

		LookupFilter lookupFilter = new LookupFilter();
		lookupFilter.setLookupCode("TRANSPORTER");
		lookupFilter.setLookupType("PartnerTypes");
		List<Lookup> lookupList = lookupService.findByFilter(lookupFilter);
		transporterLookup = lookupList.get(0);

		Location location = new Location();
		location.setTenantId(1L);
		location.setLocName("Location A");
		location.setLocCode("Location A");
		savedLocation = locationRepository.save(location);

		savedPartners = new Partners();
		savedPartners.setPartnerCode("code");
		savedPartners.setPartnerName("Name");
		savedPartners.setTenantId(1L);

		CustomerDto customerDto = new CustomerDto();
		customerDto.setPartnerCode("Customer A");
		customerDto.setPartnerName("Customer A");
		customerDto.setTenantId(1L);
		customerDto.setMobileNumber1("12312310");
		customerDto.setMobileNumber1CountryShortName("AF");
		customerDto.setEmail("mj@email.com");
		savedCustomer = customerService.save(customerDto);

		transporterDto= new TransporterDto();
		transporterDto.setPartnerCode("code");
		transporterDto.setPartnerName("Name");
		transporterDto.setTenantId(1L);
		transporterDto.setMobileNumber1("12312310");
		transporterDto.setMobileNumber1CountryShortName("AF");
		transporterDto.setEmail("mj@email.com");
		transporterDto.setUnit("Unit Z");
		transporterDto.setStreet("33");
		transporterDto.setStreet2("Jurong West Z");
		transporterDto.setCity("Jurong West Z");
		transporterDto.setState("Singapore");
		transporterDto.setCountryShortName("SG");
		transporterDto.setZipCode("636465");
//		transporterDto.setCustomerId(savedCustomer.getId());
		transporterDto.setLocationId(savedLocation.getId());

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
	}

	@Test
	@Transactional
	public void testCreateTransporterWithMissingCustomerId() throws Exception {
		TransporterDto dto = new TransporterDto();
		dto.setPartnerCode("code");
		dto.setPartnerName("Name");
		dto.setLocationId(savedLocation.getId());
		dto.setTenantId(1L);
		dto.setMobileNumber1("12312310");
		dto.setMobileNumber1CountryShortName("AF");
		dto.setEmail("mj@email.com");
		dto.setUnit("Unit Z");
		dto.setStreet("33");
		dto.setStreet2("Jurong West Z");
		dto.setCity("Jurong West Z");
		dto.setState("Singapore");
		dto.setCountryShortName("SG");
		dto.setZipCode("636465");

		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isUnprocessableEntity())
				.andReturn().getResponse();

		RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		ArrayList<String> errorMessages = message.getMessages();
		assertTrue(errorMessages.contains("Customer Id required"));
	}

	@Test
	@Transactional
	public void testCreateTransporterWithInvalidCustomerId() throws Exception {
		TransporterDto dto = new TransporterDto();
		dto.setPartnerCode("code");
		dto.setPartnerName("Name");
		dto.setLocationId(savedLocation.getId());
		dto.setTenantId(1L);
//		dto.setCustomerId(0L);
		dto.setMobileNumber1("12312310");
		dto.setMobileNumber1CountryShortName("AF");
		dto.setEmail("mj@email.com");
		dto.setUnit("Unit Z");
		dto.setStreet("33");
		dto.setStreet2("Jurong West Z");
		dto.setCity("Jurong West Z");
		dto.setState("Singapore");
		dto.setCountryShortName("SG");
		dto.setZipCode("636465");

		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse();

		RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		ArrayList<String> errorMessages = message.getMessages();
		assertTrue(errorMessages.contains("Customer is invalid."));
	}

	@Test
	@Transactional
	public void testCreateTransporter() throws Exception {
		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transporterDto))
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		Partners savedPartners = partnersRepository.findByPartnerCodeAndTenantId("code", 1L);
		assertNotNull(savedPartners);

		List<PartnerTypesDto> partnerTypesList = partnerTypesRepository.findByPartnerId(savedPartners.getId());
		assertEquals(1, partnerTypesList.size());
		assertEquals(transporterLookup.getId(), partnerTypesList.get(0).getPartnerTypeId());

		assertEquals(savedPartners.getCustomerId(), savedCustomer.getId());

		List<PartnerLocation> partnerLocationList = partnerLocationRepository.findByPartnerId(savedPartners.getId());
		assertEquals(1, partnerLocationList.size());
		Long id = partnerLocationList.get(0).getLocationId();
		Location location = locationRepository.findById(id).get();

		TransporterDto dto = new TransporterDto();
		dto.setId(savedPartners.getId());
//		dto.setCustomerId(savedCustomer.getId());
		dto.setPartnerCode(savedPartners.getPartnerCode());
		dto.setPartnerName(savedPartners.getPartnerName());
		dto.setLocationId(savedLocation.getId());
		dto.setTenantId(1L);
		dto.setMobileNumber1("12312310");
		dto.setMobileNumber1CountryShortName("AF");
		dto.setEmail("mj@email.com");
		dto.setUnit("Unit Z");
		dto.setStreet("33");
		dto.setStreet2("Jurong West Z");
		dto.setCity("Jurong West Z");
		dto.setState("Singapore");
		dto.setCountryShortName("SG");
		dto.setZipCode("636465");

		response = this.mvc.perform(
				post(BASE_URL + "/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		Partners updatedPartners = partnersRepository.findByPartnerCodeAndTenantId(savedPartners.getPartnerCode(), 1L);
		assertNotNull(updatedPartners);
		assertEquals(updatedPartners.getPartnerCode(), transporterDto.getPartnerCode());
		assertEquals(updatedPartners.getPartnerName(), transporterDto.getPartnerName());

		partnerTypesList = partnerTypesRepository.findByPartnerId(updatedPartners.getId());
		assertEquals(1, partnerTypesList.size());
		assertEquals(transporterLookup.getId(), partnerTypesList.get(0).getPartnerTypeId());

		assertEquals(updatedPartners.getCustomerId(), savedCustomer.getId());

		List<PartnerLocation> updatedPartnerLocationList = partnerLocationRepository.findByPartnerId(updatedPartners.getId());
		assertEquals(1, updatedPartnerLocationList.size());
		Location updatedLocation = locationService.findById(updatedPartnerLocationList.get(0).getLocationId());
		assertEquals(updatedLocation.getId(), location.getId());
		Assert.assertEquals(updatedPartnerLocationList.get(0).getAddressId(), partnerLocationList.get(0).getAddressId());
		Assert.assertEquals(updatedPartnerLocationList.get(0).getAddressContactId(), partnerLocationList.get(0).getAddressContactId());
	}

	@Test
	@Transactional
	public void testCreateTransporterWithExistingNameAndCode() throws Exception {
		savedPartners = transporterService.save(transporterDto);

		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transporterDto)))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse();

		RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		ArrayList<String> errorMessages = message.getMessages();
		assertTrue(errorMessages.contains("The transporter is already in our system."));
	}

	@Test
	@Transactional
	public void testUpdateTransporterWithSameLocationNameDifferentAddress() throws Exception {
		savedPartners = transporterService.save(transporterDto);
		List<PartnerLocation> partnerLocationList = partnerLocationRepository.findByPartnerId(savedPartners.getId());
		PartnerLocation partnerLocation = partnerLocationList.get(0);
		Long addressId = partnerLocation.getAddressId();
		Long addressContactId = partnerLocation.getAddressContactId();
		assertEquals(1L, addressId);
		assertEquals(1L, addressContactId);

		TransporterDto dto = new TransporterDto();
		dto.setId(savedPartners.getId());
//		dto.setCustomerId(savedCustomer.getId());
		dto.setPartnerCode("code");
		dto.setPartnerName("Name");
		dto.setLocationId(savedLocation.getId());
		dto.setTenantId(1L);
		dto.setMobileNumber1("12312310");
		dto.setMobileNumber1CountryShortName("SG");
		dto.setEmail("mj@email.com");
		dto.setUnit("Unit ZZ");
		dto.setStreet("33");
		dto.setStreet2("Jurong West Z");
		dto.setCity("Jurong West Z");
		dto.setState("Singapore");
		dto.setCountryShortName("SG");
		dto.setZipCode("636465");

		AddrContact addrContact = new AddrContact();
		addrContact.setId(2L);
		when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

		AddrDTO addr = new AddrDTO();
		addr.setId(2L);
		when(addrClient.createOrUpdateAddress(any(UpdateAddrDTO.class))).thenReturn(addr);

		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		List<PartnerLocation> updatedPartnerLocationList = partnerLocationRepository.findByPartnerId(savedPartners.getId());
		PartnerLocation updatedPartnerLocation = updatedPartnerLocationList.get(0);
		assertEquals(partnerLocation.getLocationId(), updatedPartnerLocation.getLocationId());
		assertNotEquals(Optional.ofNullable(updatedPartnerLocation.getAddressId()), 2L);
		assertNotEquals(Optional.ofNullable(updatedPartnerLocation.getAddressContactId()), 2L);
	}

	@Test
	@Transactional
	public void testUpdateTransporterWithExistingName() throws Exception {
		savedPartners = transporterService.save(transporterDto);

		transporterDto.setPartnerName("Name A");
		Partners newPartner = transporterService.save(transporterDto);

		assertNotEquals(newPartner.getId(), savedPartners.getId());
		transporterDto.setId(savedPartners.getId());

		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transporterDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		Optional<Partners> savedPartner = partnersRepository.findById(savedPartners.getId());
		assertEquals("Name", savedPartner.get().getPartnerName());
	}

	@Test
	@Transactional
	public void testUpdateTransporterWithInvalidId() throws Exception {
		transporterDto.setId(0L);

		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transporterDto)))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse();

		RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		ArrayList<String> errorMessages = message.getMessages();
		assertTrue(errorMessages.contains("Transporter is invalid."));
	}

	@Test
	@Transactional
	public void testUpdateTransporterStatus() throws Exception {
		savedPartners = transporterService.save(transporterDto);

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
	public void testUpdateTransporterStatusWithInvalidId() throws Exception {

		MockHttpServletResponse response = this.mvc
				.perform(get(BASE_URL + "/updateStatus?id=" + 0L + "&status=" + false).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn().getResponse();

		RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		ArrayList<String> errorMessages = message.getMessages();
		assertTrue(errorMessages.contains("Transporter is invalid."));
	}

	@Test
	@Transactional
	public void testRetrieveById() throws Exception {
		savedPartners = transporterService.save(transporterDto);

		MockHttpServletResponse response = this.mvc
				.perform(get(BASE_URL + "/retrieve?id=" + savedPartners.getId()).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse();

		List<PartnerLocation> partnerLocationList = partnerLocationRepository.findByPartnerId(savedPartners.getId());
		assertEquals(1, partnerLocationList.size());
		TransporterDto transporterDto = objectMapper.readValue(response.getContentAsString(), TransporterDto.class);
		assertEquals("code", transporterDto.getPartnerCode());
		assertEquals(partnerLocationList.get(0).getAddressId(), transporterDto.getAddr().getId());
		assertEquals(partnerLocationList.get(0).getAddressContactId(), transporterDto.getAddrContact().getId());
		assertEquals(transporterDto.getLocationId(), partnerLocationList.get(0).getLocationId());
		assertEquals("Location A", transporterDto.getLocationName());

	}

	@Test
	@Transactional
	public void testRetrieveByInvalidId() throws Exception {

		MockHttpServletResponse response = this.mvc
				.perform(get(BASE_URL + "/retrieve?id=" + 0L).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn().getResponse();

		RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		ArrayList<String> errorMessages = message.getMessages();
		assertTrue(errorMessages.contains("Transporter is invalid."));

	}

	@Test
	@Transactional
	public void testFindBySearch() throws Exception {
		savedPartners = transporterService.save(transporterDto);

		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(new ArrayList<>());

		MockHttpServletResponse response = this.mvc
				.perform(post(BASE_URL + "/findBySearch").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isOk()).andReturn().getResponse();

	}

	@Test
	void testDownloadExcel() throws Exception {
		List<Partners> listTransporter = mockListTransporter();

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.TRANSPORTER_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);
		Mockito.doReturn(listTransporter).when(transporterServiceSpy).getTranporterToExport(any(),any(), any());

		mvc.perform(post(BASE_URL + "/downloadExcel/")
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
	}

	@Test
	void testUploadExcelError() throws Exception {
		//test data errors
		Path dataErrorPath = Paths.get("src/test/resources/excel-templates/Transporter/TRANSPORTER_DATA_ERROR.xlsx");

		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.TRANSPORTER_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"TRANSPORTER_DATA_ERROR.xlsx",
				"text/plain", Files.newInputStream(dataErrorPath));

		MockHttpServletResponse testDataErrorResponse = mvc.perform(
				multipart(BASE_URL+"/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		//test upload excel successfully
		Path successFilePath = Paths.get("src/test/resources/excel-templates/Transporter/TRANSPORTER_SAVE.xlsx");

		mockMultipartFile = new MockMultipartFile("file",
				"TRANSPORTER_SAVE.xlsx",
				"text/plain", Files.newInputStream(successFilePath));

		MockHttpServletResponse testUploadSuccessResponse = mvc.perform(
				multipart(BASE_URL+"/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk())
				.andReturn().getResponse();


	}

	@Test
	void testUploadExcelSuccess() throws Exception {
		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.TRANSPORTER_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		when(emailClient.sendUploadExcelEmail(null, null)).thenReturn(null);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(), any(), any(), any(), any(),any());

		//test upload excel successfully
		Path successFilePath = Paths.get("src/test/resources/excel-templates/Transporter/TRANSPORTER_SAVE.xlsx");

		//mock mobile country code
		List<CountryDTO> countryDTOList = new ArrayList<>();
		CountryDTO countryDTO = new CountryDTO();
		countryDTO.setCountryIsdCode("VN");
		countryDTO.setCountryFullName("VIETNAM");
		countryDTO.setCountryIsdCode("+84");
		countryDTOList.add(countryDTO);
		when(addrContactClient.findBySetCountryIsdCode(any())).thenReturn(countryDTOList);

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"TRANSPORTER_SAVE.xlsx",
				"text/plain", Files.newInputStream(successFilePath));

		MockHttpServletResponse testUploadSuccessResponse = mvc.perform(
				multipart(BASE_URL+"/uploadFiles/").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA).requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		PartnersFilter partnerFilter = new PartnersFilter();
		partnerFilter.setName("West Mall226");
		List<Partners> listPartner = transporterService.findByFilter(partnerFilter, 0L, false);
		assertEquals(1,listPartner.size());
	}

	private List<TransporterDto> mockListTransporterDto() {
		List<TransporterDto> transporterDtoList = new ArrayList<>();
		TransporterDto transporterDto = new TransporterDto();
		transporterDto.setPartnerName("West Mall226");
		transporterDto.setPartnerCode("TRAN226");
		transporterDto.setExcelRowPosition(3);
		transporterDtoList.add(transporterDto);

		TransporterDto transporterDto1 = new TransporterDto();
		transporterDto1.setPartnerName("West Mall226");
		transporterDto1.setPartnerCode("TRAN227");
		transporterDto1.setExcelRowPosition(5);
		transporterDtoList.add(transporterDto1);

		return transporterDtoList;
	}

	public List<Partners> mockListTransporter(){
		List<Partners> listTransporter = new ArrayList();
		Partners transporter = new Partners();
		transporter.setEmail("test@gmail.com");
		transporter.setPartnerName("transporter Test Name");
		transporter.setPartnerCode("");
		transporter.setActiveInd(true);
		transporter.setPhone("+84919232434");
		transporter.setPhoneCountryShortName("VN");
		transporter.setCustomerName("customer Test Name");
		transporter.setDescription("test Description");
		transporter.setPhoneAreaCode("");
		listTransporter.add(transporter);
		return listTransporter;
	}

	public List<UploadTemplateHdrIdDto> mockExportExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("TRANSPORTER_SETTING_EXPORT");
		uploadTemplateHdrIdDto.setTitle("TRANSPORTER SETTING");
		uploadTemplateHdrIdDto.setFileName("TRANSPORTER.xlsx");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
		det1.setFieldName("partnerCode");
		det1.setPosition(1);
		det1.setWidth(6400);
		det1.setAlignment("HorizontalAlignment.CENTER");
		det1.setColumnName("Transporter Code");
		det1.setColumnFullName("Transporter Code");
		det1.setActiveInd(1);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("partnerName");
		det2.setPosition(0);
		det2.setColumnName("Transporter Name");
		det2.setColumnFullName("Transporter Name");
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
		det7.setFieldName("customerName");
		det7.setPosition(7);
		det7.setColumnName("Dedicated To");
		det7.setColumnFullName("Dedicated To");
		listDetail.add(det7);

		UploadTemplateDetIdDto det8 = new UploadTemplateDetIdDto();
		copier.copy(det1,det8,null);
		det8.setFieldName("description");
		det8.setPosition(8);
		det8.setColumnName("Description");
		det8.setColumnFullName("Description");
		listDetail.add(det8);

		UploadTemplateDetIdDto det9 = new UploadTemplateDetIdDto();
		copier.copy(det1,det9,null);
		det9.setFieldName("phoneAreaCode");
		det9.setPosition(6);
		det9.setColumnName("Phone Area Code");
		det9.setColumnFullName("Phone Area Code");
		listDetail.add(det9);

		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}


	public List<UploadTemplateHdrIdDto> mockUploadExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("TRANSPORTER_SETTING_UPLOAD");
		uploadTemplateHdrIdDto.setTitle("TRANSPORTER SETTING");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
		det1.setFieldName("partnerCode");
		det1.setColumnName("Transporter Code");
		det1.setColumnFullName("Transporter Code");
		det1.setMaxLength(255);
		det1.setMandatoryInd(0);
		det1.setActiveInd(1);
		det1.setNoneDuplicated(1);
		det1.setPosition(0);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("partnerName");
		det2.setColumnName("Transporter Name");
		det2.setColumnFullName("Transporter Name");
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

		UploadTemplateDetIdDto det6 = new UploadTemplateDetIdDto();
		copier.copy(det1,det6,null);
		det6.setFieldName("mobileNumber1CountryShortName");
		det6.setMandatoryInd(0);
		det6.setColumnName("Country Mobile Code");
		det6.setColumnFullName("Country Mobile Code");
		det6.setNoneDuplicated(1);
		det6.setPosition(5);
		listDetail.add(det6);

		UploadTemplateDetIdDto det7 = new UploadTemplateDetIdDto();
		copier.copy(det1,det7,null);
		det7.setFieldName("customerName");
		det7.setColumnName("Customer");
		det7.setColumnFullName("Customer");
		det7.setMandatoryInd(1);
		det7.setMaxLength(255);
		det7.setNoneDuplicated(1);
		det7.setPosition(6);
		listDetail.add(det7);

		UploadTemplateDetIdDto det8 = new UploadTemplateDetIdDto();
		copier.copy(det1,det8,null);
		det8.setFieldName("locationName");
		det8.setPosition(0);
		det8.setMaxLength(255);
		det8.setMandatoryInd(0);
		det8.setColumnName("Location Name");
		det8.setColumnFullName("Location Name");
		det7.setNoneDuplicated(1);
		det7.setPosition(7);
		listDetail.add(det8);

		UploadTemplateDetIdDto det9 = new UploadTemplateDetIdDto();
		copier.copy(det1,det9,null);
		det9.setFieldName("country");
		det9.setMandatoryInd(1);
		det9.setColumnName("Country");
		det9.setColumnFullName("Country");
		det9.setNoneDuplicated(1);
		det9.setPosition(8);
		listDetail.add(det9);

		UploadTemplateDetIdDto det10 = new UploadTemplateDetIdDto();
		copier.copy(det1,det10,null);
		det10.setFieldName("state");
		det10.setMandatoryInd(1);
		det10.setColumnName("State / Province");
		det10.setColumnFullName("State / Province");
		det10.setMandatoryInd(0);
		det10.setNoneDuplicated(1);
		det10.setPosition(9);
		listDetail.add(det10);

		UploadTemplateDetIdDto det11 = new UploadTemplateDetIdDto();
		copier.copy(det1,det11,null);
		det11.setFieldName("zipCode");
		det11.setMandatoryInd(1);
		det11.setColumnName("Postal Code / ZIP");
		det11.setColumnFullName("Postal Code / ZIP");
		det11.setMandatoryInd(1);
		det11.setNoneDuplicated(1);
		det11.setPosition(10);
		listDetail.add(det11);

		UploadTemplateDetIdDto det12 = new UploadTemplateDetIdDto();
		copier.copy(det1,det12,null);
		det12.setFieldName("unit");
		det12.setMandatoryInd(1);
		det12.setColumnName("Unit No");
		det12.setColumnFullName("Unit No");
		det12.setMandatoryInd(1);
		det12.setNoneDuplicated(1);
		det12.setPosition(11);
		listDetail.add(det12);

		UploadTemplateDetIdDto det13 = new UploadTemplateDetIdDto();
		copier.copy(det1,det13,null);
		det13.setFieldName("street");
		det13.setMandatoryInd(1);
		det13.setColumnName("Address Line 1");
		det13.setColumnFullName("Address Line 1");
		det13.setMandatoryInd(1);
		det13.setNoneDuplicated(1);
		det13.setPosition(12);
		listDetail.add(det13);

		UploadTemplateDetIdDto det14 = new UploadTemplateDetIdDto();
		copier.copy(det1,det14,null);
		det14.setFieldName("street2");
		det14.setMandatoryInd(1);
		det14.setColumnName("Address Line 2");
		det14.setColumnFullName("Address Line 2");
		det14.setMandatoryInd(0);
		det14.setNoneDuplicated(1);
		det14.setPosition(13);
		listDetail.add(det14);

		UploadTemplateDetIdDto det15 = new UploadTemplateDetIdDto();
		copier.copy(det1,det15,null);
		det15.setFieldName("mobileNumber1");
		det15.setMandatoryInd(1);
		det15.setColumnName("Mobile No");
		det15.setColumnFullName("Mobile No");
		det15.setMandatoryInd(1);
		det15.setCellType("STRING");
		det15.setNoneDuplicated(1);
		det15.setPosition(14);
		listDetail.add(det15);

		UploadTemplateDetIdDto det16 = new UploadTemplateDetIdDto();
		copier.copy(det1,det16,null);
		det16.setFieldName("city");
		det16.setMandatoryInd(1);
		det16.setColumnName("City");
		det16.setColumnFullName("City");
		det16.setMandatoryInd(0);
		det16.setNoneDuplicated(1);
		det16.setPosition(15);
		listDetail.add(det16);

		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}

	@Test
	public void testFindBySearchWithProfileScope() throws Exception {
		SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");

		AddrContact addrContact = new AddrContact();
		addrContact.setId(1L);
		when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

		List<AddrContact> list = new ArrayList<>();
		list.add(addrContact);
		when(addrContactClient.getAddressContactList(any(List.class))).thenReturn(list);

		this.createCustomerUsingController(sessionObj);
		this.createTransporterUsingController(sessionObj);

		Partners savedCustomer1 = partnersRepository.findByPartnerCodeAndTenantId("cus1", 1L);
		Partners savedCustomer2 = partnersRepository.findByPartnerCodeAndTenantId("cus2", 1L);
		Partners savedTransporter1 = partnersRepository.findByPartnerCodeAndTenantId("trans1", 1L);
		Partners savedTransporter2 = partnersRepository.findByPartnerCodeAndTenantId("trans2", 1L);

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
				post(BASE_URL + "/findBySearch")
						.requestAttr("SESSION_INFO", sessionObj)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}")
		).andExpect(status().isOk()).andReturn().getResponse();

		String json = response.getContentAsString();

		LinkedHashMap mapResponse = objectMapper.readValue(json, LinkedHashMap.class);
		Object listData = mapResponse.get("content");
		String listDataStr = objectMapper.writeValueAsString(listData);
		List<TransporterDto> transporterPage = objectMapper.readValue(
				listDataStr,
				objectMapper.getTypeFactory().constructCollectionType(List.class, TransporterDto.class)
		);
		assertEquals(1, transporterPage.size());
		assertEquals(savedTransporter1.getId(), transporterPage.get(0).getId());
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
				post( apiVersion + "/customer" + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(customerDto1))
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk())
				.andReturn();

		this.mvc.perform(
				post( apiVersion + "/customer" + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(customerDto2))
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk())
				.andReturn();
	}

	private void createTransporterUsingController(SessionUserInfoDTO sessionObj) throws Exception {
		Partners savedCustomer1 = partnersRepository.findByPartnerCodeAndTenantId("cus1", 1L);
		Partners savedCustomer2 = partnersRepository.findByPartnerCodeAndTenantId("cus2", 1L);

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
				post(apiVersion +"/transporter" + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transporterDto1))
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk())
				.andReturn();

		this.mvc.perform(
				post(apiVersion +"/transporter" + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transporterDto2))
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	public void testDownloadExcelWithProfileScope() throws Exception {
		SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");

		AddrContact addrContact = new AddrContact();
		addrContact.setId(2L);
		when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

		List<AddrContact> list = new ArrayList<>();
		list.add(addrContact);
		when(addrContactClient.getAddressContactList(any(List.class))).thenReturn(list);

		this.createCustomerUsingController(sessionObj);
		this.createTransporterUsingController(sessionObj);

		Partners savedCustomer1 = partnersRepository.findByPartnerCodeAndTenantId("cus1", 1L);
		Partners savedCustomer2 = partnersRepository.findByPartnerCodeAndTenantId("cus2", 1L);
		Partners savedTransporter1 = partnersRepository.findByPartnerCodeAndTenantId("trans1", 1L);
		Partners savedTransporter2 = partnersRepository.findByPartnerCodeAndTenantId("trans2", 1L);

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

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.TRANSPORTER_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		mvc.perform(post(BASE_URL + "/downloadExcel").requestAttr("SESSION_INFO", sessionObj)
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

	}

	@Test
	public void testDownloadExcelSorting() throws Exception {
		SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");

		AddrContact addrContact = new AddrContact();
		addrContact.setId(2L);
		when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

		List<AddrContact> list = new ArrayList<>();
		list.add(addrContact);
		when(addrContactClient.getAddressContactList(any(List.class))).thenReturn(list);

		this.createCustomerUsingController(sessionObj);
		this.createTransporterUsingController(sessionObj);

		List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();
		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.TRANSPORTER_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		String json1 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.TRANSPORTER_CODE + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
		String json2 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.TRANSPORTER_NAME + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";
		String json3 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.TRANSPORTER_DEDICATED_TO + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";

		mvc.perform(post(BASE_URL + "/downloadExcel").requestAttr("SESSION_INFO", sessionObj)
				.content(json1)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel").requestAttr("SESSION_INFO", sessionObj)
				.content(json2)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel").requestAttr("SESSION_INFO", sessionObj)
				.content(json3)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
	}

	@Test
	public void testDownloadExcelFilter() throws Exception {
		SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");

		AddrContact addrContact = new AddrContact();
		addrContact.setId(2L);
		when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

		List<AddrContact> list = new ArrayList<>();
		list.add(addrContact);
		when(addrContactClient.getAddressContactList(any(List.class))).thenReturn(list);

		this.createCustomerUsingController(sessionObj);
		this.createTransporterUsingController(sessionObj);

		List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();
		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.TRANSPORTER_SETTING_EXPORT))
				.thenReturn(listExportExcelTemplate);

		String json1 = "{\"" + AppConstants.SortPropertyName.TRANSPORTER_CODE + "\": \"" + "ABC" + "\"}";
		String json2 = "{\"" + AppConstants.SortPropertyName.TRANSPORTER_NAME + "\": \"" + "ABC" + "\"}";
		String json3 = "{\"" + AppConstants.SortPropertyName.TRANSPORTER_DEDICATED_TO + "\": \"" + "ABC" + "\"}";

		mvc.perform(post(BASE_URL + "/downloadExcel").requestAttr("SESSION_INFO", sessionObj)
				.content(json1)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel").requestAttr("SESSION_INFO", sessionObj)
				.content(json2)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		mvc.perform(post(BASE_URL + "/downloadExcel").requestAttr("SESSION_INFO", sessionObj)
				.content(json3)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
	}
}
