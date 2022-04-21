package com.y3technologies.masters.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.y3technologies.masters.client.*;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.*;
import com.y3technologies.masters.dto.aas.ProfileScopeDTO;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.PartnerAddrContactDTO;
import com.y3technologies.masters.dto.comm.PartnerAddrDTO;
import com.y3technologies.masters.dto.comm.UpdateAddrDTO;
import com.y3technologies.masters.dto.excel.UploadTemplateDetIdDto;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.filter.LookupFilter;
import com.y3technologies.masters.dto.filter.PartnerLocationFilter;
import com.y3technologies.masters.dto.filter.PartnersFilter;
import com.y3technologies.masters.dto.filter.PlacesFilter;
import com.y3technologies.masters.exception.RestErrorMessage;
import com.y3technologies.masters.model.*;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.model.comm.AddrDTO;
import com.y3technologies.masters.repository.*;
import com.y3technologies.masters.service.ConsigneeService;
import com.y3technologies.masters.service.LocationService;
import com.y3technologies.masters.service.LookupService;
import com.y3technologies.masters.service.impl.ConsigneeServiceImpl;
import com.y3technologies.masters.util.DateFormatUtil;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.*;
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
import org.springframework.cloud.openfeign.support.PageJacksonModule;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Sivasankari Subramaniam
 */
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConsigneeControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(ConsigneeControllerTest.class);
    private static ObjectMapper objectMapper;
    @Autowired
    MessagesUtilities messagesUtilities;
    private String BASE_URL;
    private MockMvc mvc;
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private PartnersRepository partnersRepository;
    @Autowired
    private ConsigneeService consigneeService;
    @Autowired
    private LocationRepository locationRepository;
    @MockBean
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
    private AddrClient addrClient;
    @MockBean
    private AddrContactClient addrContactClient;
    @Autowired
    private CommonTagRepository commonTagRepository;
    @MockBean
    private EmailClient emailClient;
    @MockBean
    private ExcelClient excelClient;
    @MockBean
    private AasClient aasClient;
    @SpyBean
    private ConsigneeServiceImpl consigneeServiceImpl;
    @SpyBean
    private ExcelUtils excelUtils;
    @Autowired
    private LookupRepository lookupRepository;

    private Partners savedPartners;
    private Partners savedPartnersTwo;
    private ConsigneeDto consigneeDto;
    private ConsigneeDto consigneeDtoTwo;
    private ConsigneePlaceDto consigneePlaceDto;
    private ConsigneePlaceDto consigneePlaceDtoTwo;
    private PartnerAddrDTO partnerAddrDTO;
    private PartnerAddrContactDTO partnerAddrContactDTO;
    private AddrDTO addrDTO;
    private AddrContact addrContact;
    private PartnerLocation partnerLocation;
    private PartnerLocation partnerLocationTwo;
    private Location location;
    private SessionUserInfoDTO sessionObj;
    List<UploadTemplateHdrIdDto> uploadExcelTemplate = new ArrayList<>();
    List<UploadTemplateHdrIdDto> downloadExcelTemplate = new ArrayList<>();

    @BeforeAll
    public static void setup() throws Exception {

        objectMapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .registerModule(new PageJacksonModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);


    }

    @PostConstruct
    public void setApiVersion() {
        BASE_URL = apiVersion + "/consignee";
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
        lkTransporter.setLookupCode(AppConstants.PartnerType.CONSIGNEE);
        lkTransporter.setLookupDescription(AppConstants.PartnerType.CONSIGNEE);
        lkTransporter.setTenantId(0L);
        lookupRepository.save(lkTransporter);

        LookupFilter lookupFilter = new LookupFilter();
        lookupFilter.setLookupCode("CUSTOMER");
        List<Lookup> lookupList = lookupService.findByFilter(lookupFilter);

        savedPartners = new Partners();
        savedPartners.setPartnerCode("code");
        savedPartners.setPartnerName("Name");
        savedPartners.setTenantId(1L);

        savedPartnersTwo = new Partners();
        savedPartnersTwo.setPartnerCode("codeTwo");
        savedPartnersTwo.setPartnerName("NameTwo");
        savedPartnersTwo.setTenantId(1L);

        consigneeDto = new ConsigneeDto();
        consigneeDto.setPartnerCode("code");
        consigneeDto.setPartnerName("Name");
        consigneeDto.setTenantId(1L);
        consigneeDto.setCustomerId(1L);

        consigneeDtoTwo = new ConsigneeDto();
        consigneeDtoTwo.setPartnerCode("codeTwo");
        consigneeDtoTwo.setPartnerName("NameTwo");
        consigneeDtoTwo.setTenantId(1L);
        consigneeDtoTwo.setCustomerId(1L);

        consigneePlaceDto = new ConsigneePlaceDto();
        consigneePlaceDtoTwo = new ConsigneePlaceDto();
        partnerAddrDTO = new PartnerAddrDTO();
        partnerAddrContactDTO = new PartnerAddrContactDTO();

        partnerAddrDTO.setStreet("33");
        partnerAddrDTO.setStreet2("Jurong West Z");
        partnerAddrDTO.setCity("Jurong West Z");
        partnerAddrDTO.setState("Singapore");
        partnerAddrDTO.setCountryShortName("SG");
        partnerAddrDTO.setZipCode("636465");
        partnerAddrDTO.setUnit("Unit A");

        partnerAddrContactDTO.setEmail("test@trx.com");
        partnerAddrContactDTO.setMobileNumber1("12312310");
        partnerAddrContactDTO.setMobileNumber1CountryShortName("SG");
        partnerAddrContactDTO.setPerson("TEST PERSON");

        consigneePlaceDto.setLocationId(1L);
        consigneePlaceDto.setPartnerAddr(partnerAddrDTO);
        consigneePlaceDto.setPartnerAddrContact(partnerAddrContactDTO);

        consigneePlaceDtoTwo.setLocationId(2L);
        consigneePlaceDtoTwo.setPartnerAddr(partnerAddrDTO);
        consigneePlaceDtoTwo.setPartnerAddrContact(partnerAddrContactDTO);

        addrDTO = new AddrDTO();
        addrDTO.setId(1L);
        addrDTO.setStreet("33");
        addrDTO.setStreet2("Jurong West Z");
        addrDTO.setCity("Jurong West Z");
        addrDTO.setState("Singapore");
        addrDTO.setCountryShortName("SG");
        addrDTO.setZipCode("636465");
        addrDTO.setUnit("Unit A");
        addrContact = new AddrContact();
        addrContact.setId(1L);
        addrContact.setEmail("test@trx.com");
        addrContact.setMobileNumber1("12312310");
        addrContact.setMobileNumber1CountryShortName("SG");
        addrContact.setPerson("TEST PERSON");

        location = new Location();
        location.setId(1L);
        location.setActiveInd(true);
        location.setAddressContactId(1L);
        location.setAddressId(1L);
        location.setLocCode("TEST CODE");
        location.setLocName("TEST NAME");
        location.setLocDesc("TEST DESC");
        location.setTenantId(1L);
        location.setLocationTag("1|2|3|4");

        when(locationService.findById(anyLong())).thenReturn(location);

        when(addrClient.createOrUpdateAddress(any(UpdateAddrDTO.class))).thenReturn(addrDTO);
        when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

        when(addrClient.getAddress(anyLong())).thenReturn(addrDTO);
        when(addrContactClient.getAddressContact(anyLong())).thenReturn(addrContact);

        BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

        List<UploadTemplateDetIdDto> listDetailUpload = new ArrayList<>();
        UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
        det1.setFieldName("partnerCode");
        det1.setPosition(2);
        det1.setWidth(6400);
        det1.setAlignment("HorizontalAlignment.CENTER");
        det1.setColumnName("Consignee Code");
        det1.setColumnFullName("Consignee Code");
        det1.setActiveInd(1);
        det1.setMaxLength(255);
        det1.setMandatoryInd(1);
        det1.setNoneDuplicated(1);
        listDetailUpload.add(det1);

        UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
        copier.copy(det1, det2, null);
        det2.setFieldName("partnerName");
        det2.setPosition(1);
        det2.setColumnName("Consignee Name");
        det2.setColumnFullName("Consignee Name");
        det2.setNoneDuplicated(1);
        listDetailUpload.add(det2);

        UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
        copier.copy(det1, det3, null);
        det3.setFieldName("customerName");
        det3.setPosition(0);
        det3.setColumnName("Assigned To");
        det3.setColumnFullName("Assigned To");
        det3.setNoneDuplicated(1);
        listDetailUpload.add(det3);

        UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
        copier.copy(det1, det4, null);
        det4.setFieldName("activeInd");
        det4.setPosition(3);
        det4.setColumnName("Status");
        det4.setColumnFullName("Status");
        det4.setMandatoryInd(0);
        det4.setNoneDuplicated(1);
        listDetailUpload.add(det4);

        List<UploadTemplateHdrIdDto> uploadTemplate = new ArrayList<>();

        UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
        uploadTemplateHdrIdDto.setCode("CONSIGNEE_SETTING_UPLOAD");
        uploadTemplateHdrIdDto.setTitle("CONSIGNEE SETTING");
        uploadTemplateHdrIdDto.setSheetSeqNo(0);
        uploadTemplateHdrIdDto.setStartRow(0);
        uploadTemplateHdrIdDto.setListTempDetail(listDetailUpload);
        uploadTemplate.add(uploadTemplateHdrIdDto);

        List<UploadTemplateDetIdDto> listDetailDownLoad = new ArrayList<>();
        listDetailDownLoad.add(det1);
        listDetailDownLoad.add(det2);
        listDetailDownLoad.add(det3);
        listDetailDownLoad.add(det4);
        UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
        copier.copy(det1, det5, null);
        det5.setFieldName("email");
        det5.setPosition(5);
        det5.setColumnName("Email");
        det5.setColumnFullName("Email");
        det5.setMandatoryInd(0);
        det5.setNoneDuplicated(1);
        listDetailDownLoad.add(det5);

        UploadTemplateDetIdDto det6 = new UploadTemplateDetIdDto();
        copier.copy(det1, det6, null);
        det6.setFieldName("phone");
        det6.setPosition(6);
        det6.setColumnName("Phone");
        det6.setColumnFullName("Phone");
        det6.setMandatoryInd(0);
        det6.setNoneDuplicated(1);
        listDetailDownLoad.add(det6);

        UploadTemplateDetIdDto det7 = new UploadTemplateDetIdDto();
        copier.copy(det1, det7, null);
        det7.setFieldName("phoneCountryShortName");
        det7.setPosition(7);
        det7.setColumnName("Phone Country Short Name");
        det7.setColumnFullName("Phone Country Short Name");
        det7.setMandatoryInd(0);
        det7.setNoneDuplicated(1);
        listDetailDownLoad.add(det7);

        UploadTemplateDetIdDto det8 = new UploadTemplateDetIdDto();
        copier.copy(det1, det8, null);
        det8.setFieldName("phoneAreaCode");
        det8.setPosition(8);
        det8.setColumnName("Phone Area Code");
        det8.setColumnFullName("Phone Area Code");
        det8.setMandatoryInd(0);
        det8.setNoneDuplicated(1);
        listDetailDownLoad.add(det8);

        UploadTemplateDetIdDto det9 = new UploadTemplateDetIdDto();
        copier.copy(det1, det9, null);
        det9.setFieldName("description");
        det9.setPosition(4);
        det9.setColumnName("Description");
        det9.setColumnFullName("Description");
        det9.setMandatoryInd(0);
        det9.setNoneDuplicated(1);
        listDetailDownLoad.add(det9);

        List<UploadTemplateHdrIdDto> downloadTemplate = new ArrayList<>();
        UploadTemplateHdrIdDto downloadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
        downloadTemplateHdrIdDto.setCode("CONSIGNEE_SETTING_EXPORT");
        downloadTemplateHdrIdDto.setTitle("CONSIGNEE");
        downloadTemplateHdrIdDto.setType("EXPORT_TEMPLATE");
        downloadTemplateHdrIdDto.setSheetSeqNo(0);
        downloadTemplateHdrIdDto.setStartRow(0);
        downloadTemplateHdrIdDto.setListTempDetail(listDetailDownLoad);
        downloadTemplateHdrIdDto.setActiveInd(1);
        downloadTemplateHdrIdDto.setFileName("CONSIGNEE.xlsx");
        downloadTemplate.add(downloadTemplateHdrIdDto);

        uploadExcelTemplate = uploadTemplate;
        downloadExcelTemplate = downloadTemplate;
    }

    @AfterEach
    public void remove() throws Exception {
        locationRepository.deleteAll();
        partnerTypesRepository.deleteAll();
        partnerLocationRepository.deleteAll();
        partnersRepository.deleteAll();
        lookupRepository.deleteAll();
        partnersRepository = null;
        partnerTypesRepository = null;
        partnerLocationRepository = null;
        locationRepository = null;
        savedPartners = null;
        consigneeDto = null;
        consigneePlaceDto = null;
        partnerAddrDTO = null;
        partnerAddrContactDTO = null;
        partnerLocation = null;
    }

    @Test
    @Transactional
    public void testCreateConsignee() throws Exception {
        assertNull(savedPartners.getId());

        MockHttpServletResponse response = this.mvc.perform(
                post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consigneeDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        Partners savedPartners = partnersRepository.findByPartnerCodeAndTenantId("code", 1L);
        assertNotNull(savedPartners);
        assertEquals(consigneeDto.getPartnerCode(), savedPartners.getPartnerCode());
        assertEquals(consigneeDto.getPartnerName(), savedPartners.getPartnerName());
        assertEquals(consigneeDto.getCustomerId(), savedPartners.getCustomerId());
        assertEquals(consigneeDto.getTenantId(), savedPartners.getTenantId());
        List<PartnerTypesDto> partnerTypesList = partnerTypesRepository.findByPartnerId(savedPartners.getId());
        assertEquals(1, partnerTypesList.size());
    }

    @Test
    @Transactional
    public void testAddNewPlace() throws Exception {
        assertNull(savedPartners.getId());
        savedPartners = consigneeService.save(consigneeDto);
        assertNotNull(savedPartners.getId());
        consigneePlaceDto.setConsigneeId(savedPartners.getId());
        consigneePlaceDto.getPartnerAddr().setStreet2(null);
        consigneePlaceDto.getPartnerAddr().setCity(null);

        List<PartnerLocation> savedPartnerLocations = partnerLocationRepository.findByPartnerId(1L);
        assertTrue(savedPartnerLocations.size() < 1);

        MockHttpServletResponse response = this.mvc.perform(
                post(BASE_URL + "/addPlace")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consigneePlaceDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        savedPartnerLocations = partnerLocationRepository.findByPartnerId(savedPartners.getId());
        assertNotNull(savedPartnerLocations);
        assertTrue(savedPartnerLocations.size() == 1);
        assertEquals(addrDTO.getId(), savedPartnerLocations.get(0).getAddressId());
        assertEquals(addrContact.getId(), savedPartnerLocations.get(0).getAddressContactId());
        assertEquals(consigneePlaceDto.getLocationId(), savedPartnerLocations.get(0).getLocationId());
        assertEquals(savedPartners.getId(), savedPartnerLocations.get(0).getPartners().getId());
    }

    @Test
    @Transactional
    public void testUpdatePlace() throws Exception {
        assertNull(savedPartners.getId());
        savedPartners = consigneeService.save(consigneeDto);
        assertNotNull(savedPartners.getId());
        consigneePlaceDto.setConsigneeId(savedPartners.getId());

        List<PartnerLocation> savedPartnerLocations = partnerLocationRepository.findByPartnerId(savedPartners.getId());
        assertTrue(savedPartnerLocations.size() < 1);

        // save partner location
        partnerLocation = new PartnerLocation();
        partnerLocation.setAddressContactId(1L);
        partnerLocation.setAddressId(1L);
        partnerLocation.setLocationId(1L);
        partnerLocation.setPartners(savedPartners);
        partnerLocationRepository.save(partnerLocation);

        savedPartnerLocations = partnerLocationRepository.findByPartnerId(savedPartners.getId());
        assertTrue(savedPartnerLocations.size() == 1);
        assertNotNull(savedPartnerLocations);
        assertTrue(savedPartnerLocations.size() == 1);
        assertEquals(1L, savedPartnerLocations.get(0).getAddressId());
        assertEquals(1L, savedPartnerLocations.get(0).getAddressContactId());
        assertEquals(1L, savedPartnerLocations.get(0).getLocationId());
        assertEquals(savedPartners.getId(), savedPartnerLocations.get(0).getPartners().getId());

        addrDTO.setId(2L);
        addrContact.setId(2L);
        consigneePlaceDto.setLocationId(2L);
        consigneePlaceDto.setId(partnerLocation.getId());
        when(addrClient.createOrUpdateAddress(any(UpdateAddrDTO.class))).thenReturn(addrDTO);
        when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

        MockHttpServletResponse response = this.mvc.perform(
                post(BASE_URL + "/updatePlace")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consigneePlaceDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        savedPartnerLocations = partnerLocationRepository.findByPartnerId(savedPartners.getId());
        assertNotNull(savedPartnerLocations);
        assertTrue(savedPartnerLocations.size() == 1);
        assertEquals(addrDTO.getId(), savedPartnerLocations.get(0).getAddressId());
        assertEquals(addrContact.getId(), savedPartnerLocations.get(0).getAddressContactId());
        assertEquals(consigneePlaceDto.getLocationId(), savedPartnerLocations.get(0).getLocationId());
        assertEquals(savedPartners.getId(), savedPartnerLocations.get(0).getPartners().getId());
    }

    @Test
    @Transactional
    public void testUpdateStatus() throws Exception {
        assertNull(savedPartners.getId());
        savedPartners = consigneeService.save(consigneeDto);
        assertNotNull(savedPartners.getId());
        assertEquals(Boolean.TRUE, savedPartners.getActiveInd());
        MockHttpServletResponse response = this.mvc.perform(put(BASE_URL + "/updateStatus").contentType(MediaType.APPLICATION_JSON)
                .param("id", savedPartners.getId().toString()).param("isActive", Boolean.FALSE.toString()))
                .andExpect(status().isOk()).andReturn().getResponse();

        ConsigneeDto consigneeDto = objectMapper.readValue(response.getContentAsString(), ConsigneeDto.class);
        assertNotNull(consigneeDto);
        assertEquals(Boolean.FALSE, consigneeDto.getActiveInd());
    }

    @Test
    @Transactional
    public void testRetrievePlace() throws Exception {
        assertNull(savedPartners.getId());
        savedPartners = consigneeService.save(consigneeDto);
        assertNotNull(savedPartners.getId());
        consigneePlaceDto.setConsigneeId(savedPartners.getId());

        List<PartnerLocation> savedPartnerLocations = partnerLocationRepository.findByPartnerId(savedPartners.getId());
        assertTrue(savedPartnerLocations.size() < 1);

        // save partner location
        partnerLocation = new PartnerLocation();
        partnerLocation.setAddressContactId(1L);
        partnerLocation.setAddressId(1L);
        partnerLocation.setLocationId(1L);
        partnerLocation.setPartners(savedPartners);
        partnerLocationRepository.save(partnerLocation);

        savedPartnerLocations = partnerLocationRepository.findByPartnerId(savedPartners.getId());
        assertTrue(savedPartnerLocations.size() == 1);
        assertNotNull(savedPartnerLocations);
        assertTrue(savedPartnerLocations.size() == 1);
        assertEquals(1L, savedPartnerLocations.get(0).getAddressId());
        assertEquals(1L, savedPartnerLocations.get(0).getAddressContactId());
        assertEquals(1L, savedPartnerLocations.get(0).getLocationId());
        assertEquals(savedPartners.getId(), savedPartnerLocations.get(0).getPartners().getId());

        when(addrClient.createOrUpdateAddress(any(UpdateAddrDTO.class))).thenReturn(addrDTO);
        when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

        MockHttpServletResponse response = this.mvc
                .perform(get(BASE_URL + "/retrievePlace").contentType(MediaType.APPLICATION_JSON).param("id", partnerLocation.getId().toString()))
                .andExpect(status().isOk()).andReturn().getResponse();

        consigneePlaceDto = objectMapper.readValue(response.getContentAsString(), ConsigneePlaceDto.class);
        assertNotNull(consigneePlaceDto);
        assertEquals(addrDTO.getId(), consigneePlaceDto.getPartnerAddr().getId());
        assertEquals(addrContact.getId(), consigneePlaceDto.getPartnerAddrContact().getId());
        assertEquals(partnerLocation.getLocationId(), consigneePlaceDto.getLocationId());
        assertEquals(partnerLocation.getId(), consigneePlaceDto.getId());
    }

    @Test
    @Transactional
    public void testFindByPlaceSearch() throws Exception {

        savedPartners = consigneeService.save(consigneeDto);
        partnerLocation = new PartnerLocation();
        partnerLocation.setAddressContactId(1L);
        partnerLocation.setAddressId(1L);
        partnerLocation.setLocationId(1L);
        partnerLocation.setPartners(savedPartners);
        partnerLocation = partnerLocationRepository.save(partnerLocation);
        PlacesFilter placesFilter = new PlacesFilter();
        placesFilter.setTenantId(savedPartners.getTenantId());
        placesFilter.setConsigneeId(savedPartners.getId());
        placesFilter.setPerson("TEST NAME");
        placesFilter.setLocationId(partnerLocation.getLocationId());
        placesFilter.setCountryShortName("SG");
        placesFilter.setPageNo(0);
        placesFilter.setPageSize(20);

        MultiValueMap<String, String> params = new HttpHeaders();
        params = new HttpHeaders();
        params.add("pageSize", String.valueOf(20));
        params.add("pageNo", String.valueOf(0));
        params.add("consigneeId", String.valueOf(savedPartners.getId()));
        params.add("tenantId", String.valueOf(savedPartners.getTenantId()));
        params.add("person", "");
        params.add("mobileNumber1", "");
        params.add("email", "");
        params.add("locationId", null);
        params.add("locCode", "");
        params.add("unit", "");
        params.add("street", "");
        params.add("street2", "");
        params.add("city", "");
        params.add("state", "");
        params.add("zipCode", "");
        params.add("countryShortName", "");

        Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(new ArrayList<>());

        MockHttpServletResponse response = this.mvc.perform(
                get(BASE_URL + "/findByPlacesSearch")
                        .params(params)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

//        Page<ConsigneePlaceDto> partnersList = objectMapper.readValue(response.getContentAsString(), new TypeReference<Page<ConsigneePlaceDto>>(){});
//        assertEquals(1, partnersList.getTotalElements());
    }

    @Test
    @Transactional
    public void testFindConsigneeByCustomerId() throws Exception {
        List<ConsigneeDto> consigneeDtoList = null;
        assertNull(savedPartners.getId());
        savedPartners = consigneeService.save(consigneeDto);
        assertNotNull(savedPartners.getId());
        consigneePlaceDto.setConsigneeId(savedPartners.getId());

        when(addrClient.createOrUpdateAddress(any(UpdateAddrDTO.class))).thenReturn(addrDTO);
        when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

        MockHttpServletResponse response = this.mvc
                .perform(get(BASE_URL + "/retrieveConsigneeByCustomerId").contentType(MediaType.APPLICATION_JSON).param("id", savedPartners.getCustomerId().toString()))
                .andExpect(status().isOk()).andReturn().getResponse();

        consigneeDtoList = objectMapper.readValue(response.getContentAsString(), new TypeReference<List<ConsigneeDto>>() {
        });
        assertNotNull(consigneePlaceDto);
        assertTrue(consigneeDtoList.size() > 0);
        assertEquals(savedPartners.getCustomerId(), consigneeDtoList.get(0).getCustomerId());
        assertEquals(savedPartners.getId(), consigneeDtoList.get(0).getId());
    }

    @Test
    @Transactional
    public void testFindLocationByConsignee() throws Exception {
        List<LocationDto> locationDtoList = null;
        assertNull(savedPartners.getId());
        savedPartners = consigneeService.save(consigneeDto);
        assertNotNull(savedPartners.getId());
        consigneePlaceDto.setConsigneeId(savedPartners.getId());

        when(addrClient.createOrUpdateAddress(any(UpdateAddrDTO.class))).thenReturn(addrDTO);
        when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

        PartnerLocationFilter partnerLocationFilter = new PartnerLocationFilter();
        partnerLocationFilter.setActiveInd(true);
        partnerLocationFilter.setConsigneeId(savedPartners.getId());
        partnerLocationFilter.setLocationId(1L);

        MockHttpServletResponse response = this.mvc
                .perform(get(BASE_URL + "/retrieveLocationsForConsignee").contentType(MediaType.APPLICATION_JSON).param("id", savedPartners.getId().toString()))
                .andExpect(status().isOk()).andReturn().getResponse();

        locationDtoList = objectMapper.readValue(response.getContentAsString(), new TypeReference<List<LocationDto>>() {
        });
        assertNotNull(consigneePlaceDto);
//        assertTrue(consigneeDtoList.size() > 0);
//        assertEquals(savedPartners.getCustomerId(), consigneeDtoList.get(0).getCustomerId());
//        assertEquals(savedPartners.getId(), consigneeDtoList.get(0).getId());
    }

    @Test
    @Transactional
    public void testFindLocationByConsigneeLocationTag() throws Exception {
        List<LocationDto> locationDtoList = null;
        assertNull(savedPartners.getId());
        savedPartners = consigneeService.save(consigneeDto);
        assertNotNull(savedPartners.getId());
        consigneePlaceDto.setConsigneeId(savedPartners.getId());

        partnerLocation = new PartnerLocation();
        partnerLocation.setAddressContactId(1L);
        partnerLocation.setAddressId(1L);
        partnerLocation.setLocationId(1L);
        partnerLocation.setPartners(savedPartners);
        partnerLocation = partnerLocationRepository.save(partnerLocation);

        when(addrClient.createOrUpdateAddress(any(UpdateAddrDTO.class))).thenReturn(addrDTO);
        when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

        PartnerLocationFilter partnerLocationFilter = new PartnerLocationFilter();
        partnerLocationFilter.setActiveInd(true);
        partnerLocationFilter.setConsigneeId(savedPartners.getId());
        partnerLocationFilter.setLocationId(1L);

        List<CommonTag> commonTagList = commonTagRepository.findAllById(location.getlocationTagIds());

        MockHttpServletResponse response = this.mvc
                .perform(get(BASE_URL + "/retrieveLocationsForConsignee").contentType(MediaType.APPLICATION_JSON).param("id", savedPartners.getId().toString()))
                .andExpect(status().isOk()).andReturn().getResponse();

        locationDtoList = objectMapper.readValue(response.getContentAsString(), new TypeReference<List<LocationDto>>() {
        });
        assertNotNull(consigneePlaceDto);
        assertEquals(commonTagList.size(), locationDtoList.get(0).getLocationTags().size());
    }

    @Test
    @Transactional
    public void testFindPlacesByLocation() throws Exception {
        List<ConsigneePlaceDto> consigneePlaceDtoList = null;
        assertNull(savedPartners.getId());
        savedPartners = consigneeService.save(consigneeDto);
        assertNotNull(savedPartners.getId());
        consigneePlaceDto.setConsigneeId(savedPartners.getId());

        when(addrClient.createOrUpdateAddress(any(UpdateAddrDTO.class))).thenReturn(addrDTO);
        when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

        PartnerLocationFilter partnerLocationFilter = new PartnerLocationFilter();
        partnerLocationFilter.setActiveInd(true);
        partnerLocationFilter.setConsigneeId(savedPartners.getId());
        partnerLocationFilter.setLocationId(1L);

        MockHttpServletResponse response = this.mvc
                .perform(get(BASE_URL + "/retrieveLocationsForConsignee").contentType(MediaType.APPLICATION_JSON).param("id", savedPartners.getId().toString()))
                .andExpect(status().isOk()).andReturn().getResponse();

        consigneePlaceDtoList = objectMapper.readValue(response.getContentAsString(), new TypeReference<List<ConsigneePlaceDto>>() {
        });
        assertNotNull(consigneePlaceDto);
//        assertTrue(consigneeDtoList.size() > 0);
//        assertEquals(savedPartners.getCustomerId(), consigneeDtoList.get(0).getCustomerId());
//        assertEquals(savedPartners.getId(), consigneeDtoList.get(0).getId());
    }

    @Test
    @Transactional
    public void testCreateConsigneeWithExistingNameAndCode() throws Exception {
        savedPartners = consigneeService.save(consigneeDto);

        MockHttpServletResponse response = this.mvc.perform(
                post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consigneeDto)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
        ArrayList<String> errorMessages = message.getMessages();
        assertTrue(errorMessages.contains("The consignee is already in our system."));
    }

    @Test
    @Transactional
    public void testCreateConsigneeWithExistingName() throws Exception {
        savedPartners = consigneeService.save(consigneeDto);
        consigneeDto.setPartnerCode("test");

        MockHttpServletResponse response = this.mvc.perform(
                post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consigneeDto)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
        ArrayList<String> errorMessages = message.getMessages();
        assertTrue(errorMessages.contains("The consignee is already in our system."));
    }

    @Test
    @Transactional
    public void testCreateConsigneeWithExistingNameAndCodeDifferentCustomerId() throws Exception {
        savedPartners = consigneeService.save(consigneeDto);
        consigneeDto.setCustomerId(2L);

        MockHttpServletResponse response = this.mvc.perform(
                post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consigneeDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

//        RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
//        ArrayList<String> errorMessages = message.getMessages();
//        assertTrue(errorMessages.contains("The consignee is already in our system."));
    }

    @Test
    @Transactional
    public void testFindLocationByMultipleConsignee() throws Exception {
        Map<Long, List<LocationDto>> partnerAndLocationMap = new HashMap<>();

        LookupFilter lookupFilter = new LookupFilter();
        lookupFilter.setLookupCode("CONSIGNEE");
        lookupFilter.setLookupType("PartnerTypes");
        List<Lookup> lookupList = lookupService.findByFilter(lookupFilter);
        PartnerTypes partnerTypes = new PartnerTypes();
        partnerTypes.setLookup(lookupList.get(0));
        partnerTypes.setActiveInd(Boolean.TRUE);
        Set<PartnerTypes> partnerTypesSet = new HashSet<>();
        partnerTypesSet.add(partnerTypes);

        assertNull(savedPartners.getId());
        savedPartners.setPartnerTypes(partnerTypesSet);
        savedPartners = consigneeService.save(consigneeDto);
        assertNotNull(savedPartners.getId());

        assertNull(savedPartnersTwo.getId());
        savedPartnersTwo.setPartnerTypes(partnerTypesSet);
        savedPartnersTwo = consigneeService.save(consigneeDtoTwo);
        assertNotNull(savedPartnersTwo.getId());
        consigneePlaceDtoTwo.setConsigneeId(savedPartnersTwo.getId());

        when(addrClient.createOrUpdateAddress(any(UpdateAddrDTO.class))).thenReturn(addrDTO);
        when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

        // save partner location
        partnerLocation = new PartnerLocation();
        partnerLocation.setAddressContactId(1L);
        partnerLocation.setAddressId(1L);
        partnerLocation.setLocationId(1L);
        partnerLocation.setPartners(savedPartners);
        partnerLocationRepository.save(partnerLocation);

        partnerLocationTwo = new PartnerLocation();
        partnerLocationTwo.setAddressContactId(1L);
        partnerLocationTwo.setAddressId(1L);
        partnerLocationTwo.setLocationId(1L);
        partnerLocationTwo.setPartners(savedPartnersTwo);
        partnerLocationRepository.save(partnerLocationTwo);

        PartnerLocationFilter partnerLocationFilter = new PartnerLocationFilter();
        partnerLocationFilter.setActiveInd(true);
        partnerLocationFilter.setLocationId(1L);
        List<Long> idsList = new ArrayList<>();
        idsList.add(savedPartners.getId());
        idsList.add(savedPartnersTwo.getId());
        partnerLocationFilter.setConsigneeIdList(idsList);

        MockHttpServletResponse response = this.mvc
                .perform(post(BASE_URL + "/retrieveLocationsForMultipleConsignee").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idsList)))
                .andExpect(status().isOk()).andReturn().getResponse();

        partnerAndLocationMap = objectMapper.readValue(response.getContentAsString(), new TypeReference<Map<Long , List<LocationDto>>>(){});
        assertEquals(2, partnerAndLocationMap.size());
    }

    @Test
    public void uploadExcelSuccessFile() throws Exception {
        SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
        sessionObj.setAasTenantId(1L);
        sessionObj.setTimezone("Asia/Singapore");

        CustomerDto customerDto = new CustomerDto();
        customerDto.setPartnerCode("code");
        customerDto.setPartnerName("Name");
        customerDto.setMobileNumber1("12312310");
        customerDto.setMobileNumber1CountryShortName("AF");
        customerDto.setEmail("mj@email.com");

        //create customer using customer controller
        MockHttpServletResponse createCustomer = this.mvc.perform(
                post(apiVersion + "/customer" + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDto))
                        .requestAttr("SESSION_INFO", sessionObj))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // check create customer successfully
        Partners customerSaved = partnersRepository.findByPartnerCodeAndTenantId("code", 1L);
        assertNotNull(customerSaved);

        // create mockito data
        UpdateUserProfileDTO user = new UpdateUserProfileDTO();
        user.setEmail("hung34atpp@gmail.com");
        user.setFirstName("Declan");
        user.setLastName("Nalced");

        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONSIGNEE_SETTING_UPLOAD)).thenReturn(uploadExcelTemplate);
        Mockito.when(emailClient.sendUploadExcelEmail(any(), any())).thenReturn(new ResponseEntity<Boolean>(HttpStatus.OK));
        Mockito.when(aasClient.retrieveUserProfile(any())).thenReturn(user);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "CONSIGNEE_SUCCESS.xlsx",
                "\tapplication/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ClassPathResource("excel-templates/Consignee/CONSIGNEE_SUCCESS.xlsx").getInputStream()
        );

        // call upload excel
        MockHttpServletResponse response = this.mvc
                .perform(
                        multipart(BASE_URL + "/uploadFiles")
                                .file(file)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .requestAttr("SESSION_INFO", sessionObj)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        //assert
        Partners savedPartner = partnersRepository.findByPartnerCodeAndTenantId("SaveCode1", 1L);
        assertNotNull(savedPartner);
        assertEquals("SaveName1", savedPartner.getPartnerName());
        savedPartner = partnersRepository.findByPartnerCodeAndTenantId("SaveCode2", 1L);
        assertNotNull(savedPartner);
        assertEquals("SaveName2", savedPartner.getPartnerName());

        String message = response.getContentAsString();
        Assertions.assertEquals("Uploaded data successfully", message);
    }

    @Test
    public void uploadExcelErrorFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "CONSIGNEE_ERROR.xlsx",
                "\tapplication/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ClassPathResource("excel-templates/Consignee/CONSIGNEE_ERROR.xlsx").getInputStream()
        );

        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONSIGNEE_SETTING_UPLOAD)).thenReturn(uploadExcelTemplate);

        Mockito.when(emailClient.sendUploadExcelEmail(any(), any())).thenReturn(new ResponseEntity<Boolean>(HttpStatus.OK));

        MockHttpServletResponse response = this.mvc
                .perform(multipart(BASE_URL + "/uploadFiles")
                        .file(file)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertEquals(0, partnersRepository.count());
        String message = response.getContentAsString();
        Assertions.assertEquals("Uploaded data successfully", message);
    }

    @Test
    public void testUploadFormulaExcelSuccess() throws Exception {
        SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
        sessionObj.setAasTenantId(1L);
        sessionObj.setTimezone("Asia/Singapore");
        CustomerDto customerDto = new CustomerDto();
        customerDto.setPartnerCode("code");
        customerDto.setPartnerName("Miu");
        customerDto.setMobileNumber1("12312310");
        customerDto.setMobileNumber1CountryShortName("AF");
        customerDto.setEmail("mj@email.com");
        //create customer using customer controller
        this.mvc.perform(post(apiVersion + "/customer" + "/create").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDto)).requestAttr("SESSION_INFO", sessionObj))
                        .andExpect(status().isOk()).andReturn().getResponse();
        UpdateUserProfileDTO user = new UpdateUserProfileDTO();
        user.setEmail("hung34atpp@gmail.com");
        user.setFirstName("Declan");
        user.setLastName("Nalced");
        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONSIGNEE_SETTING_UPLOAD)).thenReturn(uploadExcelTemplate);
        Mockito.when(emailClient.sendUploadExcelEmail(any(), any())).thenReturn(new ResponseEntity<Boolean>(HttpStatus.OK));
        Mockito.when(aasClient.retrieveUserProfile(any())).thenReturn(user);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "CONSIGNEE_FORMULA_SUCCESS.xlsx",
                "\tapplication/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ClassPathResource("excel-templates/Consignee/CONSIGNEE_FORMULA_SUCCESS.xlsx").getInputStream()
        );
        // call upload excel
        MockHttpServletResponse response = this.mvc
                .perform(
                        multipart(BASE_URL + "/uploadFiles")
                                .file(file)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .requestAttr("SESSION_INFO", sessionObj)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        String message = response.getContentAsString();
        Assertions.assertEquals(messagesUtilities.getMessageWithParam("upload.excel.success", null), message);
        assertEquals(4, partnersRepository.count());
    }

    @Test
    public void testUploadFormulaExcelError() throws Exception {
        SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
        sessionObj.setAasTenantId(1L);
        sessionObj.setTimezone("Asia/Singapore");
        UpdateUserProfileDTO user = new UpdateUserProfileDTO();
        user.setEmail("hung34atpp@gmail.com");
        user.setFirstName("Declan");
        user.setLastName("Nalced");
        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONSIGNEE_SETTING_UPLOAD)).thenReturn(uploadExcelTemplate);
        Mockito.when(emailClient.sendUploadExcelEmail(any(), any())).thenReturn(new ResponseEntity<Boolean>(HttpStatus.OK));
        Mockito.when(aasClient.retrieveUserProfile(any())).thenReturn(user);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "CONSIGNEE_FORMULA_FAIL.xlsx",
                "\tapplication/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ClassPathResource("excel-templates/Consignee/CONSIGNEE_FORMULA_FAIL.xlsx").getInputStream()
        );
        // call upload excel
        MockHttpServletResponse response = this.mvc
                .perform(
                        multipart(BASE_URL + "/uploadFiles")
                                .file(file)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .requestAttr("SESSION_INFO", sessionObj)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        String message = response.getContentAsString();
        Assertions.assertEquals(messagesUtilities.getMessageWithParam("upload.excel.success", null), message);
        assertEquals(0, partnersRepository.count());
    }


    @Test
    public void downloadExcelSuccessfully() throws Exception {

            Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONSIGNEE_SETTING_EXPORT))
                            .thenReturn(downloadExcelTemplate);

            this.mvc.perform(post(BASE_URL + "/downloadExcel")
            .content("{}")
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

    }

    private List<ConsigneeDto> mockListConsigneeDtos() {
        List<ConsigneeDto> consigneeDtoList = new ArrayList<>();

        ConsigneeDto consigneeDto = new ConsigneeDto();
        consigneeDto.setPartnerName("Le Gia Nha 80175");
        consigneeDto.setExcelRowPosition(3);
        consigneeDtoList.add(consigneeDto);

        ConsigneeDto consigneeDto1 = new ConsigneeDto();
        consigneeDto1.setPartnerName("Le Gia Nha 80175");
        consigneeDto1.setExcelRowPosition(5);
        consigneeDtoList.add(consigneeDto1);

        return consigneeDtoList;
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
        this.createConsigneeUsingController(sessionObj);

        Partners savedCustomer1 = partnersRepository.findByPartnerCodeAndTenantId("cus1", 1L);
        Partners savedCustomer2 = partnersRepository.findByPartnerCodeAndTenantId("cus2", 1L);
        Partners savedConsignee1 = partnersRepository.findByPartnerCodeAndTenantId("con1", 1L);

        List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();
        ProfileScopeDTO profileScope1 = new ProfileScopeDTO();
        ProfileScopeDTO profileScope2 = new ProfileScopeDTO();
        ProfileScopeDTO profileScope3 = new ProfileScopeDTO();

        profileScope1.setProfileCode(AppConstants.ProfileCode.CUSTOMER);
        profileScope2.setProfileCode(AppConstants.ProfileCode.CUSTOMER);
        profileScope3.setProfileCode(AppConstants.ProfileCode.CONSIGNEE);

        profileScope1.setRefId(savedCustomer1.getId());
        profileScope2.setRefId(savedCustomer2.getId());
        profileScope3.setRefId(savedConsignee1.getId());

        lstProfileScope.add(profileScope1);
        lstProfileScope.add(profileScope2);
        lstProfileScope.add(profileScope3);

        Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);

        MockHttpServletResponse response = this.mvc.perform(
                post(BASE_URL + "/findBySearch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        String json = response.getContentAsString();

        LinkedHashMap mapResponse = objectMapper.readValue(json, LinkedHashMap.class);
        Object listData = mapResponse.get("content");
        String listDataStr = objectMapper.writeValueAsString(listData);
        List<ConsigneeDto> consigneePage = objectMapper.readValue(
                listDataStr,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ConsigneeDto.class)
        );

        assertEquals(1, consigneePage.size());
        assertEquals(savedConsignee1.getId(), consigneePage.get(0).getId());
    }

    @Test
    public void testFindByPlacesSearchWithProfileScope() throws Exception {
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
        this.createConsigneeUsingController(sessionObj);

        Partners savedCustomer1 = partnersRepository.findByPartnerCodeAndTenantId("cus1", 1L);
        Partners savedCustomer2 = partnersRepository.findByPartnerCodeAndTenantId("cus2", 1L);
        Partners savedConsignee1 = partnersRepository.findByPartnerCodeAndTenantId("con1", 1L);
        Partners savedConsignee2 = partnersRepository.findByPartnerCodeAndTenantId("con2", 1L);

        PartnerLocation partnerLocation1 = new PartnerLocation();
        partnerLocation1.setAddressContactId(1L);
        partnerLocation1.setAddressId(1L);
        partnerLocation1.setLocationId(1L);
        partnerLocation1.setPartners(savedConsignee1);
        partnerLocation1 = partnerLocationRepository.save(partnerLocation1);

        PartnerLocation partnerLocation2 = new PartnerLocation();
        partnerLocation2.setAddressContactId(1L);
        partnerLocation2.setAddressId(1L);
        partnerLocation2.setLocationId(1L);
        partnerLocation2.setPartners(savedConsignee2);
        partnerLocation2 = partnerLocationRepository.save(partnerLocation2);

        List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();
        ProfileScopeDTO profileScope1 = new ProfileScopeDTO();
        ProfileScopeDTO profileScope2 = new ProfileScopeDTO();
        ProfileScopeDTO profileScope3 = new ProfileScopeDTO();

        profileScope1.setProfileCode(AppConstants.ProfileCode.CUSTOMER);
        profileScope2.setProfileCode(AppConstants.ProfileCode.CUSTOMER);
        profileScope3.setProfileCode(AppConstants.ProfileCode.CONSIGNEE);

        profileScope1.setRefId(savedCustomer1.getId());
        profileScope2.setRefId(savedCustomer2.getId());
        profileScope3.setRefId(savedConsignee1.getId());

        lstProfileScope.add(profileScope1);
        lstProfileScope.add(profileScope2);
        lstProfileScope.add(profileScope3);
        Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);

        MockHttpServletResponse response = this.mvc.perform(
                get(BASE_URL + "/findByPlacesSearch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("SESSION_INFO", sessionObj))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        String json = response.getContentAsString();

        LinkedHashMap mapResponse = objectMapper.readValue(json, LinkedHashMap.class);
        Object listData = mapResponse.get("content");
        String listDataStr = objectMapper.writeValueAsString(listData);
        List<ConsigneePlaceDto> consigneePlacePage = objectMapper.readValue(
                listDataStr,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ConsigneePlaceDto.class)
        );

        assertEquals(1, consigneePlacePage.size());
        assertEquals(partnerLocation1.getId(), consigneePlacePage.get(0).getId());
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

    private void createConsigneeUsingController(SessionUserInfoDTO sessionObj) throws Exception {
        Partners savedCustomer1 = partnersRepository.findByPartnerCodeAndTenantId("cus1", 1L);
        Partners savedCustomer2 = partnersRepository.findByPartnerCodeAndTenantId("cus2", 1L);

        ConsigneeDto consigneeDto1 = new ConsigneeDto();
        consigneeDto1.setPartnerCode("con1");
        consigneeDto1.setPartnerName("conName1");
        consigneeDto1.setTenantId(1L);
        consigneeDto1.setCustomerId(savedCustomer1.getId());

        ConsigneeDto consigneeDto2 = new ConsigneeDto();
        consigneeDto2.setPartnerCode("con2");
        consigneeDto2.setPartnerName("conName2");
        consigneeDto2.setTenantId(1L);
        consigneeDto2.setCustomerId(savedCustomer1.getId());

        ConsigneeDto consigneeDto3 = new ConsigneeDto();
        consigneeDto3.setPartnerCode("con3");
        consigneeDto3.setPartnerName("conName3");
        consigneeDto3.setTenantId(1L);
        consigneeDto3.setCustomerId(savedCustomer2.getId());

        this.mvc.perform(
                post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consigneeDto1))
                        .requestAttr("SESSION_INFO", sessionObj))
                .andExpect(status().isOk())
                .andReturn();

        this.mvc.perform(
                post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consigneeDto2))
                        .requestAttr("SESSION_INFO", sessionObj))
                .andExpect(status().isOk())
                .andReturn();

        this.mvc.perform(
                post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consigneeDto3))
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
        addrContact.setId(1L);
        when(addrContactClient.createOrUpdateAddressContact(any(AddrContactDTO.class))).thenReturn(addrContact);

        List<AddrContact> list = new ArrayList<>();
        list.add(addrContact);
        when(addrContactClient.getAddressContactList(any(List.class))).thenReturn(list);

        this.createCustomerUsingController(sessionObj);
        this.createConsigneeUsingController(sessionObj);

        Partners savedCustomer1 = partnersRepository.findByPartnerCodeAndTenantId("cus1", 1L);
        Partners savedCustomer2 = partnersRepository.findByPartnerCodeAndTenantId("cus2", 1L);
        Partners savedConsignee1 = partnersRepository.findByPartnerCodeAndTenantId("con1", 1L);

        List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();
        ProfileScopeDTO profileScope1 = new ProfileScopeDTO();
        ProfileScopeDTO profileScope2 = new ProfileScopeDTO();
        ProfileScopeDTO profileScope3 = new ProfileScopeDTO();

        profileScope1.setProfileCode(AppConstants.ProfileCode.CUSTOMER);
        profileScope2.setProfileCode(AppConstants.ProfileCode.CUSTOMER);
        profileScope3.setProfileCode(AppConstants.ProfileCode.CONSIGNEE);

        profileScope1.setRefId(savedCustomer1.getId());
        profileScope2.setRefId(savedCustomer2.getId());
        profileScope3.setRefId(savedConsignee1.getId());

        lstProfileScope.add(profileScope1);
        lstProfileScope.add(profileScope2);
        lstProfileScope.add(profileScope3);

        Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);
        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONSIGNEE_SETTING_EXPORT))
                .thenReturn(downloadExcelTemplate);

        this.mvc.perform(
                post(BASE_URL + "/downloadExcel")
                        .requestAttr("SESSION_INFO", sessionObj)
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testDownloadExcelSorting() throws Exception {
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
        this.createConsigneeUsingController(sessionObj);

        List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();
        Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);
        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONSIGNEE_SETTING_EXPORT))
                .thenReturn(downloadExcelTemplate);

        String json1 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.CONSIGNEE_NAME + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
        String json2 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.CONSIGNEE_CODE + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";
        String json3 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.CONSIGNEE_ASSIGNED_TO + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";

        this.mvc.perform(
                post(BASE_URL + "/downloadExcel")
                        .requestAttr("SESSION_INFO", sessionObj)
                        .content(json1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        this.mvc.perform(
                post(BASE_URL + "/downloadExcel")
                        .requestAttr("SESSION_INFO", sessionObj)
                        .content(json2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        this.mvc.perform(
                post(BASE_URL + "/downloadExcel")
                        .requestAttr("SESSION_INFO", sessionObj)
                        .content(json3)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testDownloadExcelFilter() throws Exception {
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
        this.createConsigneeUsingController(sessionObj);

        List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();
        Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);
        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONSIGNEE_SETTING_EXPORT))
                .thenReturn(downloadExcelTemplate);

        String json1 = "{\"" + AppConstants.SortPropertyName.CONSIGNEE_NAME + "\": \"" + "ABC" + "\"}";
        String json2 = "{\"" + AppConstants.SortPropertyName.CONSIGNEE_CODE + "\": \"" + "ABC" + "\"}";
        String json3 = "{\"" + AppConstants.SortPropertyName.CONSIGNEE_ASSIGNED_TO + "\": \"" + "ABC" + "\"}";

        this.mvc.perform(
                post(BASE_URL + "/downloadExcel")
                        .requestAttr("SESSION_INFO", sessionObj)
                        .content(json1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        this.mvc.perform(
                post(BASE_URL + "/downloadExcel")
                        .requestAttr("SESSION_INFO", sessionObj)
                        .content(json2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        this.mvc.perform(
                post(BASE_URL + "/downloadExcel")
                        .requestAttr("SESSION_INFO", sessionObj)
                        .content(json3)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }
}
