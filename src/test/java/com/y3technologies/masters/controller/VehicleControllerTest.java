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
import com.y3technologies.masters.dto.TransporterDto;
import com.y3technologies.masters.dto.VehicleDto;
import com.y3technologies.masters.dto.aas.ProfileScopeDTO;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.aas.UpdateUserProfileDTO;
import com.y3technologies.masters.dto.comm.AddrContactDTO;
import com.y3technologies.masters.dto.comm.UpdateAddrDTO;
import com.y3technologies.masters.dto.excel.UploadTemplateDetIdDto;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.dto.table.VehicleTableDto;
import com.y3technologies.masters.exception.RestErrorMessage;
import com.y3technologies.masters.model.*;
import com.y3technologies.masters.model.comm.AddrContact;
import com.y3technologies.masters.model.comm.AddrDTO;
import com.y3technologies.masters.repository.*;
import com.y3technologies.masters.service.*;
import com.y3technologies.masters.util.ExcelUtils;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.*;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.datatables.mapping.Column;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.Search;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VehicleControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(VehicleControllerTest.class);
    private MockMvc mvc;
    private static ObjectMapper objectMapper;

    @MockBean
    private AasClient aasClient;

    @Autowired
    PartnersService partnersService;

    @Autowired
    private PartnersRepository partnersRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private LookupRepository lookupRepository;

    @Autowired
    private WebApplicationContext wac;

    @MockBean
    private ExcelClient excelClient;

    @MockBean
    private EmailClient emailClient;

    @SpyBean
    private VehicleService vehicleServiceSpy;

    @Autowired
    DriverRepository driverRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    PartnerLocationRepository partnerLocationRepository;

    @SpyBean
    private ExcelUtils excelUtils;

    @MockBean
    private AddrContactClient addrContactClient;

    @MockBean
    AddrClient addrClient;

    @Autowired
    private PartnerTypesRepository partnerTypesRepository;

    @Value("${api.version.masters}")
    private String apiVersion;

    private String BASE_URL;

    private Partners savedPartners;
    private Vehicle savedVehicle;

    @PostConstruct
    public void setApiVersion() {
        BASE_URL = "/" + apiVersion + "/vehicle";
    }

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
    public void remove() throws Exception {
        if (savedPartners != null) partnersRepository.delete(savedPartners);
        vehicleRepository.deleteAll();
        locationRepository.deleteAll();
        partnerLocationRepository.deleteAll();
        driverRepository.deleteAll();
        partnerTypesRepository.deleteAll();
        partnersRepository.deleteAll();
        lookupRepository.deleteAll();
        partnersRepository = null;
        vehicleRepository = null;
        partnerLocationRepository = null;
    }

    @Test
    public void testVehicleController() throws Exception {
        when(aasClient.retrieveUserProfile(any(Long.class))).thenReturn(new UpdateUserProfileDTO());

        String result;
        /*** create ***/
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleRegNumber("SDE1234A");
        vehicle.setLicenceTypeRequired("Licence Class 3|Licence Class 2");
        vehicle.setVehicleType("VehicleType1|VehicleType3");
        vehicle.setTenantId(1L);
        Location location = new Location();
        location.setId(1068L);
        vehicle.setAssetDefaultLoc(location);
        Driver driver = new Driver();
        driver.setId(2l);
        vehicle.setDefaultDriver(driver);
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(vehicle);
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
        VehicleDto update = new VehicleDto();
        update.setId(id);
        update.setVehicleRegNumber("SDE4567A");
        update.setLicenceTypeRequired("Licence Class 2");
        update.setVehicleType("VehicleType1|VehicleType3");
        update.setTenantId(1L);
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
        column.setData("vehicleRegNumber");
        column.setSearch(new Search("SDE4567A", Boolean.TRUE));
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
    public void testCreateVehicleWithInvalidTransporterId() throws Exception {
        VehicleDto update = new VehicleDto();
        update.setVehicleRegNumber("SDE4567A");
        update.setLicenceTypeRequired("Licence Class 2");
        update.setVehicleType("VehicleType1|VehicleType3");
        update.setTenantId(1L);
        update.setTransporterId(0L);

        MockHttpServletResponse response = this.mvc.perform(
                post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
        assertEquals("Transporter is invalid.", message.getMessages().get(0));
    }

    @Test
    @Transactional
    public void testFindByFilterWithInvalidTransporterId() throws Exception {

        savedVehicle = new Vehicle();
        savedVehicle.setVehicleRegNumber("Test1");
        savedVehicle.setLicenceTypeRequired("Licence Class 3|Licence Class 2");
        savedVehicle.setVehicleType("VehicleType1|VehicleType3");
        savedVehicle.setTenantId(1L);
        vehicleRepository.save(savedVehicle);

        MultiValueMap<String, String> params = new HttpHeaders();
        params.add("transporterId", String.valueOf(1L));

        MockHttpServletResponse response = this.mvc.perform(
                get(BASE_URL + "/findByFilter").params(params)
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

        savedVehicle = new Vehicle();
        savedVehicle.setActiveInd(Boolean.TRUE);
        savedVehicle.setVehicleRegNumber("Test1234");
        savedVehicle.setLicenceTypeRequired("Licence Class 3|Licence Class 2");
        savedVehicle.setVehicleType("VehicleType1|VehicleType3");
        savedVehicle.setTenantId(1L);
        savedVehicle.setPartners(savedPartners);
        vehicleRepository.save(savedVehicle);

        Vehicle savedVehicle2 = new Vehicle();
        savedVehicle2.setActiveInd(Boolean.FALSE);
        savedVehicle2.setVehicleRegNumber("1234");
        savedVehicle2.setLicenceTypeRequired("Licence Class 3|Licence Class 2");
        savedVehicle2.setVehicleType("VehicleType1|VehicleType3");
        savedVehicle2.setTenantId(1L);
        vehicleRepository.save(savedVehicle2);

        MultiValueMap<String, String> params = new HttpHeaders();
        params.add("tenantId", "1");
        params.add("vehicleRegisterNo", "1234");
        params.add("transporterId", String.valueOf(savedPartners.getId()));

        MockHttpServletResponse response = this.mvc.perform(
                get(BASE_URL + "/findByFilter").params(params)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        List<Vehicle> vehicleList = objectMapper.readValue(response.getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(1, vehicleList.size());
        assertEquals(vehicleList.get(0).getTenantId(), savedVehicle2.getTenantId());
        assertTrue(vehicleList.get(0).getVehicleRegNumber().contains("1234"));
        assertEquals(vehicleList.get(0).getPartners().getId(), savedVehicle.getPartners().getId());

        //findByTransporterId
        params = new HttpHeaders();
        params.add("tenantId", "1");
        params.add("vehicleRegisterNo", "1234");

        response = this.mvc.perform(
                get(BASE_URL + "/findByFilter").params(params)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        vehicleList = objectMapper.readValue(response.getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(2, vehicleList.size());
        assertEquals(vehicleList.get(0).getTenantId(), savedVehicle2.getTenantId());
        assertTrue(vehicleList.get(0).getVehicleRegNumber().contains("1234"));

        //findByActiveInd
        params.add("tenantId", "1");
        params.add("activeInd", String.valueOf(Boolean.FALSE));

        response = this.mvc.perform(
                get(BASE_URL + "/findByFilter").params(params)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        vehicleList = objectMapper.readValue(response.getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(1, vehicleList.size());
        assertEquals(vehicleList.get(0).getTenantId(), savedVehicle2.getTenantId());
        assertEquals(vehicleList.get(0).getActiveInd(), savedVehicle2.getActiveInd());

        //findByAssignedTransporterFalse
        params = new HttpHeaders();
        params.add("assignedTransporter", String.valueOf(Boolean.FALSE));

        response = this.mvc.perform(
                get(BASE_URL + "/findByFilter")
                        .params(params)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        vehicleList = objectMapper.readValue(response.getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(1, vehicleList.size());
        assertNull(vehicleList.get(0).getPartners());

        //findByAssignedTransporterTrue
        params = new HttpHeaders();
        params.add("assignedTransporter", String.valueOf(Boolean.TRUE));

        response = this.mvc.perform(
                get(BASE_URL + "/findByFilter")
                        .params(params)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        vehicleList = objectMapper.readValue(response.getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(1, vehicleList.size());
        assertNotNull(vehicleList.get(0).getPartners());
    }

    @Test
    void uploadExcelSuccessFile() throws Exception {
        SessionUserInfoDTO sessionObj = new SessionUserInfoDTO();
        sessionObj.setAasTenantId(1L);
        sessionObj.setTimezone("Asia/Singapore");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "VEHICLE_SUCCESS.xlsx",
                "\tapplication/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ClassPathResource("excel-templates/Vehicle/VEHICLE_SUCCESS.xlsx").getInputStream()
        );

        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.VEHICLE_SETTING_UPLOAD)).thenReturn(mockUploadExcelTemplate());
        Mockito.when(emailClient.sendUploadExcelEmail(any(), any())).thenReturn(new ResponseEntity<Boolean>(HttpStatus.OK));
        Mockito.doNothing().when(excelUtils).sendNotificationEmail(any(), any(), any(), any(), any(), any());

        Lookup vehicleLookUp = new Lookup();
        vehicleLookUp.setLookupType("vehicleType");
        vehicleLookUp.setLookupDescription("Car");
        vehicleLookUp.setTenantId(0l);
        lookupRepository.save(vehicleLookUp);

        Lookup licenseTypeLookUp = new Lookup();
        licenseTypeLookUp.setLookupType("VehicleLicenceClass");
        licenseTypeLookUp.setLookupDescription("Class 3");
        licenseTypeLookUp.setTenantId(1L);
        lookupRepository.save(licenseTypeLookUp);

        Location testLocationDto = new Location();
        testLocationDto.setLocName("Test location");
        testLocationDto.setLocCode("Test location");
        testLocationDto.setTenantId(1L);
        locationRepository.save(testLocationDto);

        Driver testDriver = new Driver();
        testDriver.setName("Test driver");
        testDriver.setEmail("testDriver@gmail.com");
        testDriver.setTenantId(1L);
        testDriver.setLicenceNumber("Test number");
        testDriver.setLicenceType("Test type");
        driverRepository.save(testDriver);

        Lookup lookUpPartnerType = new Lookup();
        lookUpPartnerType.setLookupType("VehicleLicenceClass");
        lookUpPartnerType.setTenantId(1L);
        lookUpPartnerType.setLookupDescription("test");
        lookUpPartnerType.setLookupCode(AppConstants.PartnerType.TRANSPORTER);
        lookupRepository.save(lookUpPartnerType);

        Partners testPartner = new Partners();
        testPartner.setPartnerName("Test partner");
        testPartner.setTenantId(1L );
        partnersRepository.save(testPartner);

        PartnerTypes partnerTypes = new PartnerTypes();
        partnerTypes.setPartners(testPartner);
        partnerTypes.setLookup(lookUpPartnerType);
        partnerTypesRepository.save(partnerTypes);

        MockHttpServletResponse response = this.mvc
                .perform(multipart(BASE_URL + "/uploadFiles")
                        .file(file)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .requestAttr("SESSION_INFO", sessionObj)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        String message = response.getContentAsString();
        Assertions.assertEquals("Uploaded data successfully", message);

        Assertions.assertEquals(1, vehicleRepository.count());
    }

    @Test
    void uploadExcelErrorFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "VEHICLE_ERROR.xlsx",
                "\tapplication/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ClassPathResource("excel-templates/Vehicle/VEHICLE_ERROR.xlsx").getInputStream()
        );

        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.VEHICLE_SETTING_UPLOAD)).thenReturn(mockUploadExcelTemplate());

        Mockito.when(emailClient.sendUploadExcelEmail(any(), any())).thenReturn(new ResponseEntity<Boolean>(HttpStatus.OK));

        MockHttpServletResponse response = this.mvc
                .perform(multipart(BASE_URL + "/uploadFiles")
                        .file(file)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        String message = response.getContentAsString();
        Assertions.assertEquals("Uploaded data successfully", message);

        Assertions.assertEquals(0, vehicleRepository.count());
    }

    @Test
    void testDownloadExcel() throws Exception {

            Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.VEHICLE_SETTING_EXPORT))
                            .thenReturn(mockExportExcelTemplate());
            Mockito.doReturn(mockListVehicleDto()).when(vehicleServiceSpy).findByTenantId(any(),any(), any());

            this.mvc.perform(post(BASE_URL + "/downloadExcel")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}")).andExpect(status().isOk()).andReturn();
    }

    public List<VehicleDto> mockListVehicleDto() {
        List<VehicleDto> listVehicleDto = new ArrayList();

        VehicleDto vehicleDto = new VehicleDto();
        vehicleDto.setId(100L);
        vehicleDto.setVehicleType("Tag BK 1");
        vehicleDto.setVehicleRegNumber("29X161969");
        vehicleDto.setExcelRowPosition(3);
        listVehicleDto.add(vehicleDto);

        VehicleDto vehicleDto1 = new VehicleDto();
        vehicleDto1.setId(200L);
        vehicleDto1.setVehicleType("Tag BK 1");
        vehicleDto1.setVehicleRegNumber("29X161969");
        vehicleDto1.setExcelRowPosition(5);
        listVehicleDto.add(vehicleDto1);

        return listVehicleDto;
    }

    public List<UploadTemplateHdrIdDto> mockExportExcelTemplate() {
        List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
        UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
        uploadTemplateHdrIdDto.setCode("VEHICLE_SETTING_EXPORT");
        uploadTemplateHdrIdDto.setTitle("VEHICLE");
        uploadTemplateHdrIdDto.setFileName("VEHICLE.xlsx");
        uploadTemplateHdrIdDto.setSheetSeqNo(0);
        uploadTemplateHdrIdDto.setStartRow(0);

        BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

        List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
        UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
        det1.setFieldName("vehicleRegNumber");
        det1.setPosition(0);
        det1.setWidth(6400);
        det1.setAlignment("HorizontalAlignment.CENTER");
        det1.setColumnName("Vehicle Plate No.");
        det1.setColumnFullName("Vehicle Plate No.");
        det1.setActiveInd(1);
        listDetail.add(det1);

        UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
        copier.copy(det1, det2, null);
        det2.setFieldName("vehicleType");
        det2.setPosition(1);
        det2.setColumnName("Vehicle Type");
        det2.setColumnFullName("Vehicle Type");
        listDetail.add(det2);

        UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
        copier.copy(det1, det3, null);
        det3.setFieldName("licenceTypeRequired");
        det3.setPosition(2);
        det3.setColumnName("Licence Class");
        det3.setColumnFullName("Licence Class");
        listDetail.add(det3);

        UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
        copier.copy(det1, det4, null);
        det4.setFieldName("transporterName");
        det4.setPosition(3);
        det4.setColumnName("Transporter Name");
        det4.setColumnFullName("Transporter Name");
        listDetail.add(det4);

        UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
        copier.copy(det1, det5, null);
        det5.setFieldName("locName");
        det5.setPosition(4);
        det5.setColumnName("Default Location");
        det5.setColumnFullName("Default Location");
        listDetail.add(det5);

        UploadTemplateDetIdDto det6 = new UploadTemplateDetIdDto();
        copier.copy(det1, det6, null);
        det6.setFieldName("activeInd");
        det6.setPosition(5);
        det6.setColumnName("Status");
        det6.setColumnFullName("Status");
        listDetail.add(det6);

        UploadTemplateDetIdDto det7 = new UploadTemplateDetIdDto();
        copier.copy(det1, det7, null);
        det7.setFieldName("wt");
        det7.setPosition(6);
        det7.setColumnName("Capacity Weight (kg)");
        det7.setColumnFullName("Capacity Weight (kg)");
        listDetail.add(det7);

        UploadTemplateDetIdDto det8 = new UploadTemplateDetIdDto();
        copier.copy(det1, det8, null);
        det8.setFieldName("vol");
        det8.setPosition(7);
        det8.setColumnName("Capacity Volume (m³)");
        det8.setColumnFullName("Capacity Volume (m³)");
        listDetail.add(det8);

        UploadTemplateDetIdDto det9 = new UploadTemplateDetIdDto();
        copier.copy(det1, det9, null);
        det9.setFieldName("pkgs");
        det9.setPosition(8);
        det9.setColumnName("Packages");
        det9.setColumnFullName("Packages");
        listDetail.add(det9);

        UploadTemplateDetIdDto det10 = new UploadTemplateDetIdDto();
        copier.copy(det1, det10, null);
        det10.setFieldName("costPerKm");
        det10.setPosition(9);
        det10.setColumnName("Cost Per KM");
        det10.setColumnFullName("Cost Per KM");
        listDetail.add(det10);

        UploadTemplateDetIdDto det11 = new UploadTemplateDetIdDto();
        copier.copy(det1, det11, null);
        det11.setFieldName("available");
        det11.setPosition(10);
        det11.setColumnName("Available");
        det11.setColumnFullName("Available");
        listDetail.add(det11);

        uploadTemplateHdrIdDto.setListTempDetail(listDetail);
        listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

        return listUploadTemplateHdrIdDto;
    }

    public List<UploadTemplateHdrIdDto> mockUploadExcelTemplate() {
        List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
        UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
        uploadTemplateHdrIdDto.setCode("VEHICLE_SETTING_UPLOAD");
        uploadTemplateHdrIdDto.setTitle("VEHICLE SETTING");
        uploadTemplateHdrIdDto.setFileName("VEHICLE.xlsx");
        uploadTemplateHdrIdDto.setSheetSeqNo(0);
        uploadTemplateHdrIdDto.setStartRow(0);

        BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

        List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
        UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
        det1.setFieldName("wt");
        det1.setColumnName("Capacity Weight (kg)");
        det1.setColumnFullName("Capacity Weight (kg)");
        det1.setMaxLength(19);
        det1.setMandatoryInd(0);
        det1.setActiveInd(1);
        det1.setNoneDuplicated(1);
        det1.setPosition(0);
        listDetail.add(det1);

        UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
        copier.copy(det1, det2, null);
        det2.setFieldName("vol");
        det2.setColumnName("Capacity Volume (m³)");
        det2.setColumnFullName("Capacity Volume (m³)");
        det2.setMaxLength(19);
        det2.setMandatoryInd(0);
        det2.setNoneDuplicated(1);
        det2.setPosition(1);
        listDetail.add(det2);

        UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
        copier.copy(det1, det3, null);
        det3.setFieldName("pkgs");
        det3.setColumnName("Packages");
        det3.setColumnFullName("Packages");
        det3.setMaxLength(null);
        det3.setMandatoryInd(0);
        det3.setNoneDuplicated(1);
        det3.setPosition(2);
        listDetail.add(det3);

        UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
        copier.copy(det1, det4, null);
        det4.setFieldName("costPerKm");
        det4.setColumnName("Cost Per KM");
        det4.setColumnFullName("Cost Per KM");
        det4.setMandatoryInd(0);
        det4.setNoneDuplicated(1);
        det4.setPosition(3);
        listDetail.add(det4);

        UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
        copier.copy(det1, det5, null);
        det5.setFieldName("transporterName");
        det5.setColumnName("Transporter Name");
        det5.setColumnFullName("Transporter Name");
        det5.setMaxLength(255);
        det5.setMandatoryInd(0);
        det5.setNoneDuplicated(1);
        det5.setPosition(4);
        listDetail.add(det5);

        UploadTemplateDetIdDto det6 = new UploadTemplateDetIdDto();
        copier.copy(det1, det6, null);
        det6.setFieldName("locName");
        det6.setColumnName("Default Location Name");
        det6.setColumnFullName("Default Location Name");
        det6.setMaxLength(null);
        det6.setMandatoryInd(0);
        det6.setNoneDuplicated(1);
        det6.setPosition(5);
        listDetail.add(det6);

        UploadTemplateDetIdDto det7 = new UploadTemplateDetIdDto();
        copier.copy(det1, det7, null);
        det7.setFieldName("activeInd");
        det7.setColumnName("Active Status");
        det7.setColumnFullName("Active Status");
        det7.setMaxLength(null);
        det7.setMandatoryInd(0);
        det7.setNoneDuplicated(1);
        det7.setPosition(6);
        listDetail.add(det7);

        UploadTemplateDetIdDto det8 = new UploadTemplateDetIdDto();
        copier.copy(det1, det8, null);
        det8.setFieldName("vehicleRegNumber");
        det8.setColumnName("Vehicle Plate No.");
        det8.setColumnFullName("Vehicle Plate No.");
        det8.setMaxLength(255);
        det8.setMandatoryInd(1);
        det8.setNoneDuplicated(1);
        det8.setPosition(7);
        listDetail.add(det8);

        UploadTemplateDetIdDto det9 = new UploadTemplateDetIdDto();
        copier.copy(det1, det9, null);
        det9.setFieldName("vehicleType");
        det9.setColumnName("Vehicle Type");
        det9.setColumnFullName("Vehicle Type");
        det9.setMaxLength(255);
        det9.setMandatoryInd(1);
        det9.setNoneDuplicated(1);
        det9.setPosition(8);
        listDetail.add(det9);

        UploadTemplateDetIdDto det10 = new UploadTemplateDetIdDto();
        copier.copy(det1, det10, null);
        det10.setFieldName("defaultDriverName");
        det10.setColumnName("Default Driver Name");
        det10.setColumnFullName("Default Driver Name");
        det10.setMaxLength(255);
        det10.setMandatoryInd(0);
        det10.setNoneDuplicated(1);
        det10.setPosition(9);
        listDetail.add(det10);

        UploadTemplateDetIdDto det11 = new UploadTemplateDetIdDto();
        copier.copy(det1, det11, null);
        det11.setFieldName("licenceTypeRequired");
        det11.setColumnName("License Class");
        det11.setColumnFullName("License Class");
        det11.setMaxLength(255);
        det11.setMandatoryInd(0);
        det11.setNoneDuplicated(1);
        det11.setPosition(10);
        listDetail.add(det11);

        uploadTemplateHdrIdDto.setListTempDetail(listDetail);
        listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

        return listUploadTemplateHdrIdDto;
    }

    @Test
    public void testQueryWithProfileScope() throws Exception {
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

        //Create Vehicle using VehicleController
        this.createVehicleUsingController(sessionObj);

        List<Vehicle> lstVehicle = vehicleRepository.findAll();

        assertEquals(3, lstVehicle.size());

        DataTablesInput input = new DataTablesInput();
        String queryJson = objectMapper.writeValueAsString(input);

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
        List<VehicleTableDto> vehiclePage = objectMapper.readValue(
                listDataStr,
                objectMapper.getTypeFactory().constructCollectionType(List.class, VehicleTableDto.class)
        );

        assertEquals(2, vehiclePage.size());

        Map<Long, String> assertValue = new HashMap<>();
        lstVehicle.forEach(el -> {
            if (el.getVehicleRegNumber().equals("SDE1234A1") ||
                    el.getVehicleRegNumber().equals("SDE1234A2")) {
                assertValue.put(el.getId(), el.getVehicleRegNumber());
            }
        });
        assertTrue(vehiclePage.stream().anyMatch(el -> !Objects.isNull(assertValue.get(el.getId()))
                && assertValue.get(el.getId()).equals(el.getVehicleRegNumber())));
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
//        transporterDto1.setCustomerId(savedCustomer1.getId());
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
//        transporterDto2.setCustomerId(savedCustomer2.getId());
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
    public void testDownloadExcelProfileScope() throws Exception {
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

        this.createVehicleUsingController(sessionObj);
        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.VEHICLE_SETTING_EXPORT))
                .thenReturn(mockExportExcelTemplate());
        mvc.perform(post(BASE_URL + "/downloadExcel").requestAttr("SESSION_INFO", sessionObj)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}")).andExpect(status().isOk()).andReturn();
    }

    private void createVehicleUsingController(SessionUserInfoDTO sessionObj) throws Exception {
        Partners savedTransporter1 = partnersRepository.findByPartnerCodeAndTenantId("trans1", 1L);
        Partners savedTransporter2 = partnersRepository.findByPartnerCodeAndTenantId("trans2", 1L);

        VehicleDto vehicle1 = new VehicleDto();
        vehicle1.setVehicleRegNumber("SDE1234A1");
        vehicle1.setLicenceTypeRequired("Licence Class 3|Licence Class 2");
        vehicle1.setVehicleType("VehicleType1|VehicleType3");
        vehicle1.setTenantId(1L);
        vehicle1.setTransporterId(savedTransporter1.getId());
        vehicle1.setAssetDefaultLocId(3L);

        VehicleDto vehicle2 = new VehicleDto();
        vehicle2.setVehicleRegNumber("SDE1234A2");
        vehicle2.setLicenceTypeRequired("Licence Class 3|Licence Class 2");
        vehicle2.setVehicleType("VehicleType1|VehicleType3");
        vehicle2.setTenantId(1L);
        vehicle2.setTransporterId(savedTransporter1.getId());
        vehicle2.setAssetDefaultLocId(3L);

        VehicleDto vehicle3 = new VehicleDto();
        vehicle3.setVehicleRegNumber("SDE1234A3");
        vehicle3.setLicenceTypeRequired("Licence Class 3|Licence Class 2");
        vehicle3.setVehicleType("VehicleType1|VehicleType3");
        vehicle3.setTenantId(1L);
        vehicle3.setTransporterId(savedTransporter2.getId());
        vehicle3.setAssetDefaultLocId(3L);

        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        String vehicleJson1 = ow.writeValueAsString(vehicle1);
        String vehicleJson2 = ow.writeValueAsString(vehicle2);
        String vehicleJson3 = ow.writeValueAsString(vehicle3);

        mvc.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON).content(vehicleJson1)
                .requestAttr("SESSION_INFO", sessionObj))
                .andExpect(status().isOk()).andReturn();
        mvc.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON).content(vehicleJson2)
                .requestAttr("SESSION_INFO", sessionObj))
                .andExpect(status().isOk()).andReturn();
        mvc.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON).content(vehicleJson3)
                .requestAttr("SESSION_INFO", sessionObj))
                .andExpect(status().isOk()).andReturn();
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
        this.createTransporterUsingController(sessionObj);

        List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();

        Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);

        this.createVehicleUsingController(sessionObj);
        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.VEHICLE_SETTING_EXPORT))
                .thenReturn(mockExportExcelTemplate());

        String json1 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.VEHICLE_REG_NUMBER + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
        String json2 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.VEHICLE_LICENCE_TYPE + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";
        String json3 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.VEHICLE_LOCATION + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
        String json4 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.VEHICLE_TRANSPORTER_NAME + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";
        String json5 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.VEHICLE_TYPE + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";

        mvc.perform(post(BASE_URL + "/downloadExcel").requestAttr("SESSION_INFO", sessionObj)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json1)).andExpect(status().isOk()).andReturn();

        mvc.perform(post(BASE_URL + "/downloadExcel").requestAttr("SESSION_INFO", sessionObj)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json2)).andExpect(status().isOk()).andReturn();

        mvc.perform(post(BASE_URL + "/downloadExcel").requestAttr("SESSION_INFO", sessionObj)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json3)).andExpect(status().isOk()).andReturn();

        mvc.perform(post(BASE_URL + "/downloadExcel").requestAttr("SESSION_INFO", sessionObj)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json4)).andExpect(status().isOk()).andReturn();

        mvc.perform(post(BASE_URL + "/downloadExcel").requestAttr("SESSION_INFO", sessionObj)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json5)).andExpect(status().isOk()).andReturn();
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
        this.createTransporterUsingController(sessionObj);

        List<ProfileScopeDTO> lstProfileScope = new ArrayList<>();

        Mockito.when(aasClient.getProfileScopeByTenantUserId(any())).thenReturn(lstProfileScope);

        this.createVehicleUsingController(sessionObj);
        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.VEHICLE_SETTING_EXPORT))
                .thenReturn(mockExportExcelTemplate());

        Partners savedTransporter1 = partnersRepository.findByPartnerCodeAndTenantId("trans1", 1L);
        String json = "{\"vehicleRegNumber\":\"1\",\"vehicleType\":\"1\",\"licenceTypeRequired\":\"Licence Class 3\",\"partnerId\":" + savedTransporter1.getId() + ",\"locId\":3}";
        mvc.perform(post(BASE_URL + "/downloadExcel").requestAttr("SESSION_INFO", sessionObj)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isOk()).andReturn();
    }
}
