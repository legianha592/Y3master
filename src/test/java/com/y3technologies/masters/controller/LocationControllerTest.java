package com.y3technologies.masters.controller;

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
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.CreateLocationDto;
import com.y3technologies.masters.dto.LocationDto;
import com.y3technologies.masters.dto.aas.ProfileScopeDTO;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.TenantUserProfileScopeDTO;
import com.y3technologies.masters.dto.comm.*;
import com.y3technologies.masters.dto.excel.UploadTemplateDetIdDto;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.LocationFilter;
import com.y3technologies.masters.model.Location;
import com.y3technologies.masters.model.CommonTag;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.model.comm.AddrDTO;
import com.y3technologies.masters.repository.CommonTagRepository;
import com.y3technologies.masters.repository.LocationRepository;
import com.y3technologies.masters.repository.LookupRepository;
import com.y3technologies.masters.service.LocationService;
import com.y3technologies.masters.service.impl.LocationServiceImpl;
import com.y3technologies.masters.util.DateFormatUtil;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
public class LocationControllerTest {

	private static final Logger logger = LoggerFactory.getLogger(LocationControllerTest.class);
	private static ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext wac;

	Location savedLocation;
	CreateLocationDto createLocationDto;

	@MockBean
	ExcelClient excelClient;

	@SpyBean
	ExcelUtils excelUtils;

	@MockBean
	AddrContactClient addrContactClient;

	@MockBean
	AddrClient addrClient;

	@MockBean
	AasClient aasClient;

	@SpyBean
	LocationService locationService;

	@SpyBean
	LocationServiceImpl locationServiceImpl;

	@Value("${api.version.masters}")
	private String apiVersion;

	private String BASE_URL;

	@Autowired
	private CommonTagRepository lookupRepository;

	@Autowired
	private LocationRepository locationRepository;

	@Autowired
	MessagesUtilities messagesUtilities;

	List<TenantUserProfileScopeDTO> tenantUserProfileScopeDTOList = new ArrayList<>();

	@PostConstruct
	public void setApiVersion() {
		BASE_URL = "/" + apiVersion + "/location";
	}

	SessionUserInfoDTO sessionObj;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

		sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");

		createLocationDto = new CreateLocationDto();
		createLocationDto.setLocCode("loc code");
		createLocationDto.setLocName("loc Name");
		createLocationDto.setTenantId(1L);

		AddrDTO addrDTO = new AddrDTO();
		addrDTO.setStreet("33");
		addrDTO.setStreet2("Jurong West Z");
		addrDTO.setCity("Jurong West Z");
		addrDTO.setState("Singapore");
		addrDTO.setCountryShortName("SG");
		addrDTO.setZipCode("636465");

		createLocationDto.setAddr(addrDTO);
		CommonTag CommonTag = new CommonTag();
		CommonTag.setTag("Test Tag");

		CommonTag.setTenantId(1L);
		CommonTag.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		CommonTag savedLookup = lookupRepository.save(CommonTag);
		createLocationDto.setLocationTags(new ArrayList<>(Arrays.asList(savedLookup)));

		AddrContact addrContact = new AddrContact();
		addrContact.setId(1L);
		when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);
		when(addrContactClient.getAddressContact(1L)).thenReturn(addrContact);

		AddrDTO addr = new AddrDTO();
		addr.setId(1L);
		when(addrClient.createOrUpdateAddress(any(UpdateAddrDTO.class))).thenReturn(addr);
		when(addrClient.getAddress(1L)).thenReturn(addr);
	}

	@AfterEach
	public void cleanData() {
		locationRepository.deleteAll();
	}

	@Test
	public void testLocationController() throws Exception {
		/*** create location ***/
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(createLocationDto);
		String result = mockMvc
				.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		logger.info("create result is {}", result);
		savedLocation = mapper.readValue(result, Location.class);

		/*** Retrieve saved location ***/
		String retrievedResult = mockMvc.perform(get(BASE_URL+"/retrieve?id=" + savedLocation.getId()))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		logger.info("Retrieve result is {}", retrievedResult);
		LocationDto retrievedLoc = mapper.readValue(retrievedResult, LocationDto.class);
		assertEquals(retrievedLoc.getAddr().getId(), savedLocation.getAddressId());
		assertEquals(retrievedLoc.getLocCode(), savedLocation.getLocCode());

		/*** Update saved location- Change LocCode, Change Address, Address Contact ***/
		CreateLocationDto updateLocationDto = new CreateLocationDto();
		updateLocationDto.setId(savedLocation.getId());
		updateLocationDto.setLocCode("loc code 2");
		updateLocationDto.setLocName("loc Name update");
		updateLocationDto.setTenantId(1L);

		AddrDTO updateAddrDTO = new AddrDTO();
		updateAddrDTO.setStreet("33");
		updateAddrDTO.setStreet2("Jurong West Z");
		updateAddrDTO.setCity("Jurong West Z");
		updateAddrDTO.setState("Singapore");
		updateAddrDTO.setCountryShortName("SG");
		updateAddrDTO.setZipCode("636465");
		updateAddrDTO.setUnit("Unit A");

		updateLocationDto.setAddr(updateAddrDTO);

		AddrDTO addr = new AddrDTO();
		addr.setId(2L);
		when(addrClient.createOrUpdateAddress(any(UpdateAddrDTO.class))).thenReturn(addr);

		requestJson = ow.writeValueAsString(updateLocationDto);
		String updatedResult = mockMvc
				.perform(post(BASE_URL + "/update").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		/*** Retrieve saved location ***/
		retrievedResult = mockMvc.perform(get(BASE_URL+"/retrieve?id=" + savedLocation.getId()))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		retrievedLoc = mapper.readValue(retrievedResult, LocationDto.class);
		logger.info("Update result is {}", updatedResult);
		Location updatedLocation = mapper.readValue(updatedResult, Location.class);
		assertEquals(updatedLocation.getLocCode(), retrievedLoc.getLocCode());
		assertEquals(updatedLocation.getLocName(), retrievedLoc.getLocName());
		assertNotEquals(updatedLocation.getAddressId(), retrievedLoc.getAddressId());

		/*** updateStatus ***/
		RequestBuilder updateStatus = get(BASE_URL + "/updateStatus?id=" + savedLocation.getId() + "&status=" + false);
		logger.info("updateStatus result is {}", mockMvc.perform(updateStatus).andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString());

		/*** query ***/
		DataTablesInput input = new DataTablesInput();
		input.setStart(0);
		input.setLength(5);
		Column column = new Column();
		column.setData("locName");
		column.setSearch(new Search("loc", Boolean.TRUE));
		column.setSearchable(Boolean.TRUE);
		List<Column> cols = new ArrayList<Column>();
		cols.add(column);
		input.setColumns(cols);
		String queryJson = ow.writeValueAsString(input);
		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(new ArrayList<>());
		result = mockMvc.perform(post(BASE_URL + "/findBySearch").requestAttr("SESSION_INFO", sessionObj)
				.contentType(MediaType.APPLICATION_JSON).content(queryJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("query result is {}", result);
	}

	@Test
	public void testFindOrCreate() throws Exception {
		/*** create location ***/

		createLocationDto.setId(null);
		AddrDTO addr = new AddrDTO();
		createLocationDto.setAddr(addr);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(createLocationDto);
		String result = mockMvc
				.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		logger.info("create result is {}", result);
		savedLocation = mapper.readValue(result, Location.class);
		assertNotNull(savedLocation.getId());
	}

	@Test
	public void testFindOrCreateWithNewAndExistingLocationTags() throws Exception {
		/*** create location ***/

		// delete existing CommonTag for the tenant
		List<CommonTag> existingLookups = lookupRepository.findAllByTenantId(1L);
		lookupRepository.deleteInBatch(existingLookups);

		List<CommonTag> lookupList = new ArrayList<>();
		CommonTag lookupOne = new CommonTag();
		lookupOne.setTag("lookupOne");
		lookupOne.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		lookupOne.setTenantId(1L);
		CommonTag savedLookupOne = lookupRepository.save(lookupOne);
		CommonTag lookupTwo = new CommonTag();
		lookupTwo.setTag("lookupTwo");
		lookupTwo.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		lookupTwo.setTenantId(1L);
		lookupList.add(savedLookupOne);
		lookupList.add(lookupTwo);

		createLocationDto.setId(null);
		createLocationDto.setLocationTags(lookupList);
		AddrDTO addr = new AddrDTO();
		createLocationDto.setAddr(addr);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(createLocationDto);
		String result = mockMvc
				.perform(post(BASE_URL + "/create")
						.requestAttr("SESSION_INFO", sessionObj)
				.contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		logger.info("create result is {}", result);
		savedLocation = mapper.readValue(result, Location.class);
		assertNotNull(savedLocation.getId());

		List<CommonTag> lookups = lookupRepository.findAllByTenantId(1L);
		StringBuilder lookupBuilder = new StringBuilder();
		lookups.forEach(i -> {
			if(lookupBuilder.length() != 0) {
				lookupBuilder.append("|");
			}
			lookupBuilder.append(i.getId());
		});
		assertEquals(savedLocation.getLocationTag(), lookupBuilder.toString());
		lookupRepository.deleteInBatch(lookups);
	}

	@Test
	public void testFindOrCreateWithNewLocationTags() throws Exception {
		/*** create location ***/

		// delete existing CommonTag for the tenant
		List<CommonTag> existingLookups = lookupRepository.findAllByTenantId(1L);
		lookupRepository.deleteInBatch(existingLookups);

		List<CommonTag> lookupList = new ArrayList<>();
		CommonTag lookupOne = new CommonTag();
		lookupOne.setTag("lookupOne");
		CommonTag lookupTwo = new CommonTag();
		lookupTwo.setTag("lookupTwo");
		assertNull(lookupOne.getId());
		assertNull(lookupTwo.getId());
		lookupList.add(lookupOne);
		lookupList.add(lookupTwo);

		createLocationDto.setId(null);
		createLocationDto.setLocationTags(lookupList);
		AddrDTO addr = new AddrDTO();
		createLocationDto.setAddr(addr);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(createLocationDto);
		String result = mockMvc
				.perform(post(BASE_URL + "/create")						.requestAttr("SESSION_INFO", sessionObj)
						.contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		logger.info("create result is {}", result);
		savedLocation = mapper.readValue(result, Location.class);
		assertNotNull(savedLocation.getId());

		List<CommonTag> lookups = lookupRepository.findAllByTenantId(1L);
		StringBuilder lookupBuilder = new StringBuilder();
		lookups.forEach(i -> {
			assertEquals(i.getTagType(), AppConstants.CommonTag.LOCATION_TAG);
			if(lookupBuilder.length() >0) {
				lookupBuilder.append("|");
			}
			lookupBuilder.append(i.getId());
		});
		assertEquals(savedLocation.getLocationTag(), lookupBuilder.toString());
		lookupRepository.deleteInBatch(lookups);
	}

	@Test
	public void testFindOrCreateWithNullLocationTags() throws Exception {
		/*** create location ***/

		// delete existing CommonTag for the tenant
		List<CommonTag> existingLookups = lookupRepository.findAllByTenantId(1L);
		lookupRepository.deleteInBatch(existingLookups);

		List<CommonTag> lookupList = new ArrayList<>();
		CommonTag lookupOne = new CommonTag();
		lookupOne.setTag("lookupOne");
		lookupOne.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		lookupOne.setTenantId(1L);
		CommonTag lookupTwo = new CommonTag();
		lookupTwo.setTag("lookupTwo");
		lookupTwo.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		lookupTwo.setTenantId(1L);
		assertNull(lookupOne.getId());
		assertNull(lookupTwo.getId());
		lookupList.add(lookupOne);
		lookupList.add(lookupTwo);
		lookupRepository.saveAll(lookupList);

		createLocationDto.setId(null);
		createLocationDto.setLocationTags(null);
		AddrDTO addr = new AddrDTO();
		createLocationDto.setAddr(addr);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(createLocationDto);
		String result = mockMvc
				.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		logger.info("create result is {}", result);
		savedLocation = mapper.readValue(result, Location.class);
		assertNotNull(savedLocation.getId());

		List<CommonTag> lookups = lookupRepository.findAllByTenantId(1L);
		StringBuilder lookupBuilder = new StringBuilder();
		lookups.forEach(i -> {
			if(lookupBuilder.length() >0) {
				lookupBuilder.append("|");
			}
			lookupBuilder.append(i.getId());
		});
		assertNull(savedLocation.getLocationTag());
		lookupRepository.deleteInBatch(lookups);
	}

	@Test
	public void testRetrieveWithLocationTags() throws Exception {
		/*** retrieve location ***/

		Location location = new Location();
		BeanCopier copier = BeanCopier.create(CreateLocationDto.class, Location.class, false);
		copier.copy(createLocationDto, location, null);

		// delete existing CommonTag for the tenant
		List<CommonTag> existingLookups = lookupRepository.findAllByTenantId(1L);
		lookupRepository.deleteInBatch(existingLookups);

		List<CommonTag> lookupList = new ArrayList<>();
		CommonTag lookupOne = new CommonTag();
		lookupOne.setTag("lookupOne");
		lookupOne.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		lookupOne.setTenantId(1L);
		CommonTag lookupTwo = new CommonTag();
		lookupTwo.setTag("lookupTwo");
		lookupTwo.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		lookupTwo.setTenantId(1L);
		lookupList.add(lookupOne);
		lookupList.add(lookupTwo);
		lookupRepository.saveAll(lookupList);
		assertNotNull(lookupOne.getId());
		assertNotNull(lookupTwo.getId());
		StringBuilder lookupBuilder = new StringBuilder();
		lookupList.forEach(i -> {
			if(lookupBuilder.length() >0) {
				lookupBuilder.append("|");
			}
			lookupBuilder.append(i.getId());
		});
		location.setLocationTag(lookupBuilder.toString());
		Location savedLocation = locationRepository.save(location);

		String retrievedResult = mockMvc.perform(get(BASE_URL+"/retrieve?id=" + savedLocation.getId()))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		logger.info("create result is {}", retrievedResult);
		CreateLocationDto savedCreateLocationDto = mapper.readValue(retrievedResult, CreateLocationDto.class);
		assertNotNull(savedCreateLocationDto.getId());
		assertEquals(savedCreateLocationDto.getLocationTags().size(), lookupList.size());

		List<CommonTag> lookups = lookupRepository.findAllByTenantId(1L);
		assertEquals(savedLocation.getLocationTag(), lookupBuilder.toString());
		lookupRepository.deleteInBatch(lookups);
	}

	@Test
	public void testRetrieveWithoutLocationTags() throws Exception {
		/*** retrieve location ***/

		Location location = new Location();
		BeanCopier copier = BeanCopier.create(CreateLocationDto.class, Location.class, false);
		copier.copy(createLocationDto, location, null);

		// delete existing CommonTag for the tenant
		List<CommonTag> existingLookups = lookupRepository.findAllByTenantId(1L);
		lookupRepository.deleteInBatch(existingLookups);

		location.setLocationTag(null);
		Location savedLocation = locationRepository.save(location);

		String retrievedResult = mockMvc.perform(get(BASE_URL+"/retrieve?id=" + savedLocation.getId()))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		logger.info("create result is {}", retrievedResult);
		CreateLocationDto savedCreateLocationDto = mapper.readValue(retrievedResult, CreateLocationDto.class);
		assertNotNull(savedCreateLocationDto.getId());
		assertEquals(savedCreateLocationDto.getLocationTags().size(), 0);

		List<CommonTag> lookups = lookupRepository.findAllByTenantId(1L);
		lookupRepository.deleteInBatch(lookups);
	}

	@Test
	@Transactional
	public void testUpdateWithNewAndExistingLocationTags() throws Exception {
		/*** update location ***/

		Location location = new Location();

		// delete existing CommonTag for the tenant
		List<CommonTag> existingLookups = lookupRepository.findAllByTenantId(1L);
		lookupRepository.deleteInBatch(existingLookups);

		List<CommonTag> lookupList = new ArrayList<>();
		CommonTag lookupOne = new CommonTag();
		lookupOne.setTag("lookupOne");
		lookupOne.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		lookupOne.setTenantId(1L);
		CommonTag savedLookupOne = lookupRepository.save(lookupOne);
		CommonTag lookupTwo = new CommonTag();
		lookupTwo.setTag("lookupTwo");
		lookupTwo.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		lookupTwo.setTenantId(1L);
		lookupList.add(savedLookupOne);
		lookupList.add(lookupTwo);

		createLocationDto.setId(null);
		createLocationDto.setLocationTags(lookupList);

		BeanCopier copier = BeanCopier.create(CreateLocationDto.class, Location.class, false);
		copier.copy(createLocationDto, location, null);
		Location savedLocation = locationRepository.save(location);
		createLocationDto.setId(savedLocation.getId());
		createLocationDto.setVersion(savedLocation.getVersion());

		AddrDTO addr = new AddrDTO();
		createLocationDto.setAddr(addr);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(createLocationDto);
		String result = mockMvc
				.perform(post(BASE_URL + "/update").contentType(MediaType.APPLICATION_JSON).content(requestJson).requestAttr("SESSION_INFO", sessionObj))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("create result is {}", result);
		savedLocation = mapper.readValue(result, Location.class);
		assertNotNull(savedLocation.getId());

		List<CommonTag> lookups = lookupRepository.findAllByTenantId(1L);
		StringBuilder lookupBuilder = new StringBuilder();
		lookups.forEach(i -> {
			if(lookupBuilder.length() >0) {
				lookupBuilder.append("|");
			}
			lookupBuilder.append(i.getId());
		});
		assertEquals(savedLocation.getLocationTag(), lookupBuilder.toString());
		lookupRepository.deleteInBatch(lookups);
	}

	@Test
	public void testDownloadExcel() throws Exception {
		List<LocationDto> locationDtoList = mockListLocation();
		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();

		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.LOCATION_SETTING_EXPORT)).thenReturn(listExportExcelTemplate);
		Mockito.when(aasClient.getTenantUserProfileScope(any(),any())).thenReturn(tenantUserProfileScopeDTOList);
		Page<LocationDto> dataPage = new PageImpl<>(locationDtoList);
		Mockito.doReturn(dataPage).when(locationService).findBySearch(any(),any());
		mockMvc.perform(post(BASE_URL + "/downloadExcel")
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	public void testUploadExcelError() throws Exception {
		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.LOCATION_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(),any(),any(),any(),any(),any());
		Path pathError = Paths.get("src/test/resources/excel-templates/Location/LOCATION_UPLOAD_ERROR.xlsx");
		MockMultipartFile fileError = new MockMultipartFile(
				"file",
				"LOCATION_UPLOAD_ERROR.xlsx",
				"text/plain",
				Files.newInputStream(pathError)
		);
		MockHttpServletResponse responseError = this.mockMvc
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
	public void testUploadExcelSuccess() throws Exception{
		CountryDTO countryDTO = new CountryDTO();
		countryDTO.setId(1L);
		countryDTO.setCountryIsdCode("+93");
		countryDTO.setCountryShortName("AF");
		countryDTO.setCountryFullName("AFGHANISTAN");
		countryDTO.setActive(true);

		List<StateDTO> stateDTOList = new ArrayList<>();
		StateDTO stateDTO = new StateDTO();
		stateDTO.setActive(true);
		stateDTO.setId(3870L);
		stateDTO.setStateFullName("Ghazni");
		stateDTOList.add(stateDTO);

		List<CityDTO> cityDTOList = new ArrayList<>();
		CityDTO cityDTO = new CityDTO();
		cityDTO.setActive(true);
		cityDTO.setId(25L);
		cityDTO.setCityFullName("Ghazni");
		cityDTOList.add(cityDTO);

		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.LOCATION_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(),any(),any(),any(),any(),any());
		Mockito.when(addrClient.getCountryByCountryShortName(any())).thenReturn(countryDTO);
		Mockito.when(addrClient.getStatesByCountryShortName(any())).thenReturn(stateDTOList);
		Mockito.when(addrClient.getCitiesByStateId(any())).thenReturn(cityDTOList);
		Path pathSuccess = Paths.get("src/test/resources/excel-templates/Location/LOCATION_UPLOAD_SUCCESS.xlsx");
		MockMultipartFile fileSuccess = new MockMultipartFile(
				"file",
				"LOCATION_UPLOAD_SUCCESS.xlsx",
				"text/plain",
				Files.newInputStream(pathSuccess)
		);
		MockHttpServletResponse responseSuccess = this.mockMvc
				.perform(multipart(BASE_URL + "/uploadFiles")
						.file(fileSuccess)
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.requestAttr("SESSION_INFO",sessionObj)
				)
				.andExpect(status().isOk())
				.andReturn()
				.getResponse();

		String messageSuccess = responseSuccess.getContentAsString();
		Assertions.assertEquals("Uploaded data successfully", messageSuccess);
		assertEquals(1, locationRepository.count());
	}

	@Test
	public void testUploadFormulaExcelSuccess() throws Exception{
		CountryDTO countryDTO = new CountryDTO();
		countryDTO.setId(1L);
		countryDTO.setCountryIsdCode("+65");
		countryDTO.setCountryShortName("SG");
		countryDTO.setCountryFullName("SINGAPORE");
		countryDTO.setActive(true);

		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.LOCATION_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(),any(),any(),any(),any(),any());
		Mockito.when(addrClient.getCountryByCountryShortName(any())).thenReturn(countryDTO);
		Path pathSuccess = Paths.get("src/test/resources/excel-templates/Location/LOCATION_FORMULA_SUCCESS.xlsx");
		MockMultipartFile fileSuccess = new MockMultipartFile(
				"file",
				"LOCATION_FORMULA_SUCCESS.xlsx",
				"text/plain",
				Files.newInputStream(pathSuccess)
		);
		MockHttpServletResponse responseSuccess = this.mockMvc
				.perform(multipart(BASE_URL + "/uploadFiles")
						.file(fileSuccess)
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.requestAttr("SESSION_INFO",sessionObj)
				)
				.andExpect(status().isOk())
				.andReturn()
				.getResponse();

		String messageSuccess = responseSuccess.getContentAsString();
		Assertions.assertEquals(messagesUtilities.getMessageWithParam("upload.excel.success", null), messageSuccess);
		assertEquals(3, locationRepository.count());
	}

	@Test
	public void testUploadFormulaExcelError() throws Exception{
		List<UploadTemplateHdrIdDto> listUploadExcelTemplate = mockUploadExcelTemplate();
		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.LOCATION_SETTING_UPLOAD)).thenReturn(listUploadExcelTemplate);
		Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(),any(),any(),any(),any(),any());
		Path pathError = Paths.get("src/test/resources/excel-templates/Location/LOCATION_FORMULA_FAIL.xlsx");
		MockMultipartFile fileError = new MockMultipartFile(
				"file",
				"LOCATION_FORMULA_FAIL.xlsx",
				"text/plain",
				Files.newInputStream(pathError)
		);
		MockHttpServletResponse responseError = this.mockMvc
				.perform(multipart(BASE_URL + "/uploadFiles")
						.file(fileError)
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				)
				.andExpect(status().isOk())
				.andReturn()
				.getResponse();

		String message = responseError.getContentAsString();
		Assertions.assertEquals(messagesUtilities.getMessageWithParam("upload.excel.success", null), message);
		assertEquals(0, locationRepository.count());
	}

	private List<CreateLocationDto> mockListLocationDto() {
		List<CreateLocationDto> locationDtoList = new ArrayList<>();

		CreateLocationDto locationDto = new CreateLocationDto();
		locationDto.setLocName("Location149");
		locationDto.setLocCode("LocCode149");
		locationDto.setExcelRowPosition(3);
		locationDtoList.add(locationDto);

		CreateLocationDto locationDto1 = new CreateLocationDto();
		locationDto1.setLocName("Location149");
		locationDto1.setLocCode("LocCode149");
		locationDto1.setExcelRowPosition(5);
		locationDtoList.add(locationDto1);

		return locationDtoList;
	}

	public List<LocationDto> mockListLocation(){
		List<LocationDto> locationDtoList = new ArrayList<>();

		LocationDto location1 = new LocationDto();
		location1.setLocName("Location1");
		location1.setLocCode("LocationCode1");
		location1.setLocationTag("location1, Tag BK 1");
		location1.setActiveInd(true);
		location1.setStreet("abc1");
		location1.setStreet2("xyz1");
		location1.setZipCode("10000");
		locationDtoList.add(location1);

		LocationDto location2 = new LocationDto();
		location2.setLocName("Location2");
		location2.setLocCode("LocationCode2");
		location2.setLocationTag("location1, Tag BK 1");
		location2.setActiveInd(false);
		location2.setStreet("abc2");
		location2.setStreet2("xyz2");
		location2.setZipCode("20000");
		locationDtoList.add(location2);
		return locationDtoList;
	}

	public List<UploadTemplateHdrIdDto> mockExportExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("LOCATION_SETTING_EXPORT");
		uploadTemplateHdrIdDto.setTitle("LOCATION_SETTING");
		uploadTemplateHdrIdDto.setFileName("LOCATION_EXPORT.xlsx");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
		det1.setFieldName("locName");
		det1.setPosition(0);
		det1.setWidth(6400);
		det1.setAlignment("HorizontalAlignment.CENTER");
		det1.setColumnName("Location Name");
		det1.setColumnFullName("Location Name");
		det1.setActiveInd(1);
		det1.setMandatoryInd(1);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("locCode");
		det2.setPosition(1);
		det2.setColumnName("Location Code");
		det2.setColumnFullName("Location Code");
		det2.setMandatoryInd(0);
		listDetail.add(det2);

		UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
		copier.copy(det1,det3,null);
		det3.setFieldName("locationTag");
		det3.setPosition(2);
		det3.setColumnName("Location Tags");
		det3.setColumnFullName("Location Tags");
		det3.setMandatoryInd(0);
		listDetail.add(det3);

		UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
		copier.copy(det1,det4,null);
		det4.setFieldName("zipCode");
		det4.setPosition(3);
		det4.setColumnName("Postal Code / ZIP");
		det4.setColumnFullName("Postal Code / ZIP");
		det4.setMandatoryInd(0);
		listDetail.add(det4);

		UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
		copier.copy(det1,det5,null);
		det5.setFieldName("street");
		det5.setPosition(4);
		det5.setColumnName("Address Line 1");
		det5.setColumnFullName("Address Line 1");
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
		det7.setFieldName("locContactEmail");
		det7.setPosition(6);
		det7.setColumnName("Contact Email");
		det7.setColumnFullName("Contact Email");
		det7.setMandatoryInd(0);
		listDetail.add(det7);

		UploadTemplateDetIdDto det8 = new UploadTemplateDetIdDto();
		copier.copy(det1,det8,null);
		det8.setFieldName("locContactName");
		det8.setPosition(7);
		det8.setColumnName("Contact Name");
		det8.setColumnFullName("Contact Name");
		det8.setMandatoryInd(0);
		listDetail.add(det8);

		UploadTemplateDetIdDto det9 = new UploadTemplateDetIdDto();
		copier.copy(det1,det9,null);
		det9.setFieldName("locContactPhone");
		det9.setPosition(8);
		det9.setColumnName("Contact Phone");
		det9.setColumnFullName("Contact Phone");
		det9.setMandatoryInd(0);
		listDetail.add(det9);

		
		UploadTemplateDetIdDto det10 = new UploadTemplateDetIdDto();
		copier.copy(det1,det10,null);
		det10.setFieldName("locDesc");
		det10.setPosition(9);
		det10.setColumnName("Description");
		det10.setColumnFullName("Description");
		det10.setMandatoryInd(0);
		listDetail.add(det10);

		UploadTemplateDetIdDto det11 = new UploadTemplateDetIdDto();
		copier.copy(det1,det11,null);
		det11.setFieldName("multiPartnerAddresses");
		det11.setPosition(10);
		det11.setColumnName("Multi Partner Addresses");
		det11.setColumnFullName("Multi Partner Addresses");
		det11.setMandatoryInd(0);
		listDetail.add(det11);

		UploadTemplateDetIdDto det12 = new UploadTemplateDetIdDto();
		copier.copy(det1,det12,null);
		det12.setFieldName("locContactOfficeNumber");
		det12.setPosition(11);
		det12.setColumnName("Contract Office Number");
		det12.setColumnFullName("Contract Office Number");
		det12.setMandatoryInd(0);
		listDetail.add(det12);

		UploadTemplateDetIdDto det13 = new UploadTemplateDetIdDto();
		copier.copy(det1,det13,null);
		det13.setFieldName("street2");
		det13.setPosition(12);
		det13.setColumnName("Address Line 2");
		det13.setColumnFullName("Address Line 2");
		det13.setMandatoryInd(0);
		listDetail.add(det13);

		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}


	public List<UploadTemplateHdrIdDto> mockUploadExcelTemplate(){
		List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
		UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
		uploadTemplateHdrIdDto.setCode("LOCATION_SETTING_EXPORT");
		uploadTemplateHdrIdDto.setTitle("LOCATION_SETTING");
		uploadTemplateHdrIdDto.setFileName("LOCATION_EXPORT.xlsx");
		uploadTemplateHdrIdDto.setSheetSeqNo(0);
		uploadTemplateHdrIdDto.setStartRow(0);

		BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

		List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
		UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
		det1.setFieldName("locName");
		det1.setPosition(0);
		det1.setWidth(6400);
		det1.setAlignment("HorizontalAlignment.CENTER");
		det1.setColumnName("Location Name");
		det1.setColumnFullName("Location Name");
		det1.setActiveInd(1);
		det1.setMandatoryInd(1);
		det1.setMaxLength(255);
		det1.setNoneDuplicated(1);
		listDetail.add(det1);

		UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
		copier.copy(det1,det2,null);
		det2.setFieldName("locCode");
		det2.setPosition(1);
		det2.setColumnName("Location Code");
		det2.setColumnFullName("Location Code");
		det2.setMandatoryInd(0);
		det2.setNoneDuplicated(1);
		listDetail.add(det2);

		UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
		copier.copy(det1,det3,null);
		det3.setFieldName("locDesc");
		det3.setPosition(2);
		det3.setColumnName("Description");
		det3.setColumnFullName("Description");
		det3.setMandatoryInd(0);
		det3.setNoneDuplicated(1);
		listDetail.add(det3);

		UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
		copier.copy(det1,det4,null);
		det4.setFieldName("locationTagsString");
		det4.setPosition(3);
		det4.setColumnName("Location Tags");
		det4.setColumnFullName("Location Tags");
		det4.setMandatoryInd(0);
		det4.setNoneDuplicated(1);
		listDetail.add(det4);

		UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
		copier.copy(det1,det5,null);
		det5.setFieldName("countryShortName");
		det5.setPosition(4);
		det5.setColumnName("Country Short Name");
		det5.setColumnFullName("Country Short Name");
		det5.setMandatoryInd(0);
		det5.setMaxLength(null);
		det5.setNoneDuplicated(1);
		listDetail.add(det5);

		UploadTemplateDetIdDto det6 = new UploadTemplateDetIdDto();
		copier.copy(det1,det6,null);
		det6.setFieldName("zipCode");
		det6.setPosition(5);
		det6.setColumnName("Postal Code / ZIP");
		det6.setColumnFullName("Postal Code / ZIP");
		det6.setMandatoryInd(0);
		det6.setMaxLength(null);
		det6.setNoneDuplicated(1);
		listDetail.add(det6);

		UploadTemplateDetIdDto det7 = new UploadTemplateDetIdDto();
		copier.copy(det1,det7,null);
		det7.setFieldName("street");
		det7.setPosition(6);
		det7.setColumnName("Address Line 1");
		det7.setColumnFullName("Address Line 1");
		det7.setMandatoryInd(0);
		det7.setMaxLength(null);
		det7.setNoneDuplicated(1);
		listDetail.add(det7);

		UploadTemplateDetIdDto det8 = new UploadTemplateDetIdDto();
		copier.copy(det1,det8,null);
		det8.setFieldName("street2");
		det8.setPosition(7);
		det8.setColumnName("Address Line 2");
		det8.setColumnFullName("Address Line 2");
		det8.setMandatoryInd(0);
		det8.setMaxLength(null);
		det8.setNoneDuplicated(1);
		listDetail.add(det8);

		UploadTemplateDetIdDto det9 = new UploadTemplateDetIdDto();
		copier.copy(det1,det9,null);
		det9.setFieldName("state");
		det9.setPosition(8);
		det9.setColumnName("State / Province");
		det9.setColumnFullName("State / Province");
		det9.setMandatoryInd(0);
		det9.setMaxLength(null);
		det9.setNoneDuplicated(1);
		listDetail.add(det9);

		UploadTemplateDetIdDto det10 = new UploadTemplateDetIdDto();
		copier.copy(det1,det10,null);
		det10.setFieldName("city");
		det10.setPosition(9);
		det10.setColumnName("City Name");
		det10.setColumnFullName("City Name");
		det10.setMandatoryInd(0);
		det10.setMaxLength(null);
		det10.setNoneDuplicated(1);
		listDetail.add(det10);

		UploadTemplateDetIdDto det11 = new UploadTemplateDetIdDto();
		copier.copy(det1,det11,null);
		det11.setFieldName("activeInd");
		det11.setPosition(10);
		det11.setColumnName("Status");
		det11.setColumnFullName("Status");
		det11.setMandatoryInd(0);
		det11.setMaxLength(null);
		det11.setNoneDuplicated(1);
		listDetail.add(det11);

		uploadTemplateHdrIdDto.setListTempDetail(listDetail);
		listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

		return listUploadTemplateHdrIdDto;
	}

	@Test
	public void testFindBySearchWithProfileScope() throws Exception {
		Location location1 = new Location();
		Location location2 = new Location();
		Location location3 = new Location();
		Location location4 = new Location();
		Location location5 = new Location();

		location1.setLocCode("loc 1");
		location1.setLocName("loc Name 1");
		location1.setTenantId(1L);

		location2.setLocCode("loc 2");
		location2.setLocName("loc Name 2");
		location2.setTenantId(1L);

		location3.setLocCode("loc 3");
		location3.setLocName("loc Name 3");
		location3.setTenantId(1L);

		location4.setLocCode("loc 4");
		location4.setLocName("loc Name 4");
		location4.setTenantId(1L);

		location5.setLocCode("loc 5");
		location5.setLocName("loc Name 5");
		location5.setTenantId(1L);

		Location locationSaved1 = locationRepository.save(location1);
		Location locationSaved2 = locationRepository.save(location2);
		Location locationSaved3 = locationRepository.save(location3);
		Location locationSaved4 = locationRepository.save(location4);
		Location locationSaved5 = locationRepository.save(location5);

		List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();
		ProfileScopeDTO profileScope1 = new ProfileScopeDTO();
		ProfileScopeDTO profileScope2 = new ProfileScopeDTO();

		profileScope1.setProfileCode(AppConstants.ProfileCode.LOCATION);
		profileScope2.setProfileCode(AppConstants.ProfileCode.LOCATION);

		profileScope1.setRefId(locationSaved1.getId());
		profileScope2.setRefId(locationSaved2.getId());

		lstProfileScope.add(profileScope1);
		lstProfileScope.add(profileScope2);

		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);
		MockHttpServletResponse response = mockMvc.perform(
				post(BASE_URL + "/findBySearch")
						.requestAttr("SESSION_INFO", sessionObj)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}")
		).andExpect(status().isOk()).andReturn().getResponse();

		String json = response.getContentAsString();

		LinkedHashMap mapResponse = mapper.readValue(json, LinkedHashMap.class);
		Object listData = mapResponse.get("content");

		String listDataStr = mapper.writeValueAsString(listData);
		List<LocationDto> locationPage = mapper.readValue(
				listDataStr,
				mapper.getTypeFactory().constructCollectionType(List.class, LocationDto.class)
		);
		assertEquals(2, locationPage.size());
		Map<Long, String> mapIdAndLocationNameValueAssert = new HashMap<>();
		mapIdAndLocationNameValueAssert.put(locationSaved1.getId(), locationSaved1.getLocName());
		mapIdAndLocationNameValueAssert.put(locationSaved2.getId(), locationSaved2.getLocName());
		assertTrue(locationPage.stream().anyMatch(el -> !Objects.isNull(mapIdAndLocationNameValueAssert.get(el.getId()))
				&& mapIdAndLocationNameValueAssert.get(el.getId()).equals(el.getLocName())));
	}

	@Test
	public void testDownloadExcelWithProfileScope() throws Exception {
		Location location1 = new Location();
		Location location2 = new Location();
		Location location3 = new Location();
		Location location4 = new Location();
		Location location5 = new Location();

		location1.setLocCode("loc 1");
		location1.setLocName("loc Name 1");
		location1.setTenantId(1L);

		location2.setLocCode("loc 2");
		location2.setLocName("loc Name 2");
		location2.setTenantId(1L);

		location3.setLocCode("loc 3");
		location3.setLocName("loc Name 3");
		location3.setTenantId(1L);

		location4.setLocCode("loc 4");
		location4.setLocName("loc Name 4");
		location4.setTenantId(1L);

		location5.setLocCode("loc 5");
		location5.setLocName("loc Name 5");
		location5.setTenantId(1L);

		Location locationSaved1 = locationRepository.save(location1);
		Location locationSaved2 = locationRepository.save(location2);
		Location locationSaved3 = locationRepository.save(location3);
		Location locationSaved4 = locationRepository.save(location4);
		Location locationSaved5 = locationRepository.save(location5);

		List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();
		ProfileScopeDTO profileScope1 = new ProfileScopeDTO();
		ProfileScopeDTO profileScope2 = new ProfileScopeDTO();

		profileScope1.setProfileCode(AppConstants.ProfileCode.LOCATION);
		profileScope2.setProfileCode(AppConstants.ProfileCode.LOCATION);

		profileScope1.setRefId(locationSaved1.getId());
		profileScope2.setRefId(locationSaved2.getId());

		lstProfileScope.add(profileScope1);
		lstProfileScope.add(profileScope2);
		Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);
		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();
		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.LOCATION_SETTING_EXPORT)).thenReturn(listExportExcelTemplate);
		mockMvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO",sessionObj)
				.content("{}").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	public void testDownloadExcelSorting() throws Exception {
		Location location1 = new Location();
		Location location2 = new Location();
		Location location3 = new Location();
		Location location4 = new Location();
		Location location5 = new Location();

		location1.setLocCode("loc 1");
		location1.setLocName("loc Name 1");
		location1.setTenantId(1L);

		location2.setLocCode("loc 2");
		location2.setLocName("loc Name 2");
		location2.setTenantId(1L);

		location3.setLocCode("loc 3");
		location3.setLocName("loc Name 3");
		location3.setTenantId(1L);

		location4.setLocCode("loc 4");
		location4.setLocName("loc Name 4");
		location4.setTenantId(1L);

		location5.setLocCode("loc 5");
		location5.setLocName("loc Name 5");
		location5.setTenantId(1L);

		locationRepository.save(location1);
		locationRepository.save(location2);
		locationRepository.save(location3);
		locationRepository.save(location4);
		locationRepository.save(location5);

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();

		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.LOCATION_SETTING_EXPORT)).thenReturn(listExportExcelTemplate);
		Mockito.when(aasClient.getTenantUserProfileScope(any(),any())).thenReturn(tenantUserProfileScopeDTOList);

		String json1 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.LOCATION_NAME + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
		String json2 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.LOCATION_CODE + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";
		String json3 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.LOCATION_ADDRESS + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
		String json4 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.LOCATION_TAG + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";
		String json5 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.LOCATION_ZIPCODE + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";

		mockMvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO",sessionObj)
				.content(json1)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		mockMvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO",sessionObj)
				.content(json2)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		mockMvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO",sessionObj)
				.content(json3)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		mockMvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO",sessionObj)
				.content(json4)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		mockMvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO",sessionObj)
				.content(json5)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	public void testDownloadExcelFilter() throws Exception {
		Location location1 = new Location();
		Location location2 = new Location();
		Location location3 = new Location();
		Location location4 = new Location();
		Location location5 = new Location();

		location1.setLocCode("loc 1");
		location1.setLocName("loc Name 1");
		location1.setTenantId(1L);

		location2.setLocCode("loc 2");
		location2.setLocName("loc Name 2");
		location2.setTenantId(1L);

		location3.setLocCode("loc 3");
		location3.setLocName("loc Name 3");
		location3.setTenantId(1L);

		location4.setLocCode("loc 4");
		location4.setLocName("loc Name 4");
		location4.setTenantId(1L);

		location5.setLocCode("loc 5");
		location5.setLocName("loc Name 5");
		location5.setTenantId(1L);

		locationRepository.save(location1);
		locationRepository.save(location2);
		locationRepository.save(location3);
		locationRepository.save(location4);
		locationRepository.save(location5);

		List<UploadTemplateHdrIdDto> listExportExcelTemplate = mockExportExcelTemplate();

		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.LOCATION_SETTING_EXPORT)).thenReturn(listExportExcelTemplate);
		Mockito.when(aasClient.getTenantUserProfileScope(any(),any())).thenReturn(tenantUserProfileScopeDTOList);

		String json1 = "{\"" + AppConstants.SortPropertyName.LOCATION_NAME + "\": \"" + "ABC" + "\"}";
		String json2 = "{\"" + AppConstants.SortPropertyName.LOCATION_CODE + "\": \"" + "ABC" + "\"}";
		String json3 = "{\"" + AppConstants.SortPropertyName.LOCATION_ADDRESS + "\": \"" + "ABC" + "\"}";
		String json4 = "{\"" + AppConstants.SortPropertyName.LOCATION_TAG + "\": \"" + "100000" + "\"}";
		String json5 = "{\"" + AppConstants.SortPropertyName.LOCATION_ZIPCODE + "\": \"" + "100000" + "\"}";

		mockMvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO",sessionObj)
				.content(json1)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		mockMvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO",sessionObj)
				.content(json2)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		mockMvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO",sessionObj)
				.content(json3)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		mockMvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO",sessionObj)
				.content(json4)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		mockMvc.perform(post(BASE_URL + "/downloadExcel")
				.requestAttr("SESSION_INFO",sessionObj)
				.content(json5)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}
}
