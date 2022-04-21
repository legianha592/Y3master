package com.y3technologies.masters.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.y3technologies.masters.client.EmailClient;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.ConfigCodeDto;
import com.y3technologies.masters.dto.excel.UploadTemplateDetIdDto;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.exception.RestErrorMessage;
import com.y3technologies.masters.model.ConfigCode;
import com.y3technologies.masters.model.ConfigCodeUsageLevelEnum;
import com.y3technologies.masters.repository.ConfigCodeRepository;
import com.y3technologies.masters.service.ConfigCodeService;
import com.y3technologies.masters.service.impl.ConfigCodeServiceImpl;
import com.y3technologies.masters.util.DateFormatUtil;
import com.y3technologies.masters.util.ExcelUtils;
import com.y3technologies.masters.util.MessagesUtilities;
import org.hamcrest.Matchers;
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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConfigCodeControllerTests {

    private static final Logger logger = LoggerFactory.getLogger(ConfigCodeControllerTests.class);
    private static final String TEST_CONFIG_CODE = "CONFIG_CODE_1";
    private String BASE_URL;
    private static ObjectMapper objectMapper;

    private String TENANT, PARTNERS, ALL;
    private MockMvc mvc;
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    MessagesUtilities messagesUtilities;

    @Autowired
    private ConfigCodeRepository configCodeRepository;

    @MockBean
    private ExcelClient excelClient;

    @MockBean
    private EmailClient emailClient;

    @SpyBean
    private ConfigCodeService configCodeServiceSpy;

    @SpyBean
    private ConfigCodeServiceImpl configCodeServiceImpl;

    @SpyBean
    private ExcelUtils excelUtils;

    ConfigCode savedConfigCode;
    ConfigCode configCode;

    @Value("/${api.version.masters:v1}")
    private String apiVersion;

    @PostConstruct
    public void setApiVersion() {
        BASE_URL = apiVersion + "/configCode";
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

        TENANT = messagesUtilities.getResourceMessage(ConfigCodeUsageLevelEnum.TENANT.getUsageLevel(), LocaleContextHolder.getLocale());
        PARTNERS = messagesUtilities.getResourceMessage(ConfigCodeUsageLevelEnum.PARTNERS.getUsageLevel(), LocaleContextHolder.getLocale());
        ALL = messagesUtilities.getResourceMessage(ConfigCodeUsageLevelEnum.ALL.getUsageLevel(), LocaleContextHolder.getLocale());

        configCode = new ConfigCode();
        configCode.setCode(TEST_CONFIG_CODE);
        configCode.setDescription("Desc");
        configCode.setUsageLevel(PARTNERS);
        configCode.setConfigValue("TRUE");

        ModelMapper mapper = new ModelMapper();
    }

    @AfterEach
    public void remove() throws Exception {
		configCodeRepository.deleteAll();
        configCodeRepository = null;
        configCode = null;
        savedConfigCode = null;
    }

    @Test
    @Transactional
    public void testUpdateConfigCode() throws Exception {
        savedConfigCode = configCodeRepository.save(configCode);

        assertNotNull(configCodeRepository.findById(savedConfigCode.getId()));
        assertEquals(true, savedConfigCode.getActiveInd());

        ConfigCodeDto configCodeDto = new ConfigCodeDto();
        configCodeDto.setId(savedConfigCode.getId());
        configCodeDto.setCode(savedConfigCode.getCode());
        configCodeDto.setDescription("Desc2");
        configCodeDto.setUsageLevel(PARTNERS);
        configCodeDto.setActiveInd(Boolean.FALSE);
        configCodeDto.setConfigValue("test1|test2");

        MockHttpServletResponse response = this.mvc.perform(
                post(BASE_URL + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(configCodeDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        ConfigCode updatedConfigCode = objectMapper.readValue(response.getContentAsString(), ConfigCode.class);
        assertEquals(configCodeDto.getId(), updatedConfigCode.getId());
        assertEquals(savedConfigCode.getCode(), updatedConfigCode.getCode());
        assertEquals(configCodeDto.getDescription(), updatedConfigCode.getDescription());
        assertEquals(configCodeDto.getUsageLevel(), updatedConfigCode.getUsageLevel());
        assertEquals(configCodeDto.getConfigValue(), updatedConfigCode.getConfigValue());
        assertEquals(configCodeDto.getActiveInd(), updatedConfigCode.getActiveInd());
    }

    @Test
    @Transactional
    public void testUpdateConfigCodeWithInvalidId() throws Exception {
        savedConfigCode = configCodeRepository.save(configCode);

        assertNotNull(configCodeRepository.findById(savedConfigCode.getId()));
        assertEquals(true, savedConfigCode.getActiveInd());

        ConfigCodeDto configCodeDto = new ConfigCodeDto();
        configCodeDto.setId(0L);
        configCodeDto.setDescription("Desc2");
        configCodeDto.setUsageLevel(PARTNERS);
        configCodeDto.setConfigValue("test1|test2");

        MockHttpServletResponse response = this.mvc.perform(
                post(BASE_URL + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(configCodeDto)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
        ArrayList<String> errorMessages = message.getMessages();
        assertTrue(errorMessages.contains("Configuration Code is invalid."));

    }

    @Test
    @Transactional
    public void testUpdateConfigCodeWithInvalidUsageLevel() throws Exception {
        savedConfigCode = configCodeRepository.save(configCode);

        assertNotNull(configCodeRepository.findById(savedConfigCode.getId()));
        assertEquals(true, savedConfigCode.getActiveInd());

        ConfigCodeDto configCodeDto = new ConfigCodeDto();
        configCodeDto.setId(savedConfigCode.getId());
        configCodeDto.setDescription("Desc2");
        configCodeDto.setUsageLevel("TEST");
        configCodeDto.setConfigValue("test1|test2");

        MockHttpServletResponse response = this.mvc.perform(
                post(BASE_URL + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(configCodeDto)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
        ArrayList<String> errorMessages = message.getMessages();
        assertTrue(errorMessages.contains("Usage Level is invalid."));

    }

    @Test
    @Transactional
    public void testUpdateConfigCodeStatus() throws Exception {
        configCode = configCodeRepository.save(configCode);

        assertNotNull(configCodeRepository.findById(configCode.getId()));
        assertEquals(true, configCode.getActiveInd());

        MockHttpServletResponse response = this.mvc
                .perform(get(BASE_URL + "/updateStatus?id=" + configCode.getId() + "&status=" + false).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse();

        String id = objectMapper.readValue(response.getContentAsString(), String.class);
        assertNotNull(id);
    }

    @Test
    @Transactional
    public void testUpdateConfigCodeStatusWithInvalidId() throws Exception {
        configCode = configCodeRepository.save(configCode);

        assertNotNull(configCodeRepository.findById(configCode.getId()));
        assertEquals(true, configCode.getActiveInd());

        MockHttpServletResponse response = this.mvc
                .perform(get(BASE_URL + "/updateStatus?id=" + 0L + "&status=" + false).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn().getResponse();

        RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
        ArrayList<String> errorMessages = message.getMessages();
        assertTrue(errorMessages.contains("Configuration Code is invalid."));
    }

    @Test
    @Transactional
    public void testRetrieveById() throws Exception {
        savedConfigCode = configCodeRepository.save(configCode);

        assertNotNull(configCodeRepository.findById(savedConfigCode.getId()));
        assertEquals(true, savedConfigCode.getActiveInd());

        MockHttpServletResponse response = this.mvc
                .perform(get(BASE_URL + "/retrieve?id=" + savedConfigCode.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse();

        ConfigCode configCode = objectMapper.readValue(response.getContentAsString(), ConfigCode.class);
        assertEquals(savedConfigCode.getCode(), configCode.getCode());
        assertEquals(savedConfigCode.getDescription(), configCode.getDescription());
        assertEquals(savedConfigCode.getUsageLevel(), configCode.getUsageLevel());
        assertEquals(savedConfigCode.getConfigValue(), configCode.getConfigValue());

    }

    @Test
    @Transactional
    public void testRetrieveByInvalidId() throws Exception {
        savedConfigCode = configCodeRepository.save(configCode);

        assertNotNull(configCodeRepository.findById(savedConfigCode.getId()));
        assertEquals(true, savedConfigCode.getActiveInd());

        MockHttpServletResponse response = this.mvc
                .perform(get(BASE_URL + "/retrieve?id=" + 0L).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn().getResponse();

        RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
        ArrayList<String> errorMessages = message.getMessages();
        assertTrue(errorMessages.contains("Configuration Code is invalid."));

    }

    @Test
    public void testConfigCodeController() throws Exception {
        String urlPrefix = "/v1/configCode";
        /***findAll***/
        String result = mvc.perform(post(BASE_URL + "/findAll"))
                .andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        logger.info("findAll result is {}", result);
    }

    @Test
    public void testGetUsageLevelList() throws Exception {
        MockHttpServletResponse response = this.mvc
                .perform(get(BASE_URL + "/usageLevel/findAll").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse();

        List<String> usageLevelList = objectMapper.readValue(response.getContentAsString(), List.class);
        assertEquals(3, usageLevelList.size());

    }

    @Test
    public void testFindByUsageLevel() throws Exception {
        List<ConfigCode> existingList = configCodeRepository.findByUsageLevel("partners");

        savedConfigCode = configCodeRepository.save(configCode);

        assertNotNull(configCodeRepository.findById(savedConfigCode.getId()));
        assertEquals(true, savedConfigCode.getActiveInd());

        MockHttpServletResponse response = this.mvc
                .perform(get(BASE_URL + "/findByUsageLevel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("usageLevel", "partners"))
                .andExpect(status().isOk()).andReturn().getResponse();

        List<ConfigCode> configCodeList = objectMapper.readValue(response.getContentAsString(), new TypeReference<List<ConfigCode>>() {
        });
        assertEquals(existingList.size() + 1, configCodeList.size());
    }

    @Test
    public void testFindByInvalidUsageLevel() throws Exception {
        MockHttpServletResponse response = this.mvc
                .perform(get(BASE_URL + "/findByUsageLevel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("usageLevel", "TEST"))
                .andExpect(status().isBadRequest()).andReturn().getResponse();

        RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
        ArrayList<String> errorMessages = message.getMessages();
        assertTrue(errorMessages.contains("Usage Level is invalid."));
    }

    @Test
    public void testGetAllConfigCodeWithFilterByCodeAndUsageLevel() throws Exception {
        savedConfigCode = configCodeRepository.save(configCode);

        assertNotNull(configCodeRepository.findById(savedConfigCode.getId()));
        assertEquals(true, savedConfigCode.getActiveInd());

        MultiValueMap<String, String> params = new HttpHeaders();
        params.add("PAGE", "0");
        params.add("SIZE", "10");
        params.add("SORTBY", "code,asc|activeInd,desc");

        Map<String, String> map = new HashMap<>();
        map.put("code", "code");
        map.put("usageLevel", "PARTNERS");

        ConfigCode configCode = new ConfigCode();
        configCode.setCode("code");
        configCode.setUsageLevel("PARTNERS");

        Pageable pageable = PageRequest.of(0, 10, Sort.by(new Sort.Order(Sort.Direction.ASC, "code")));
        long count = configCodeRepository
                .findAllConfigCodeWithPagination(pageable, map).get().count();

        MockHttpServletResponse response = this.mvc.perform(
                post(BASE_URL + "/filterBy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .params(params)
                        .content(objectMapper.writeValueAsString(configCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", Matchers.hasSize(Math.toIntExact(count))))
                .andReturn().getResponse();

        Optional<JsonNode> responseNode = Optional.ofNullable(objectMapper.readValue(response.getContentAsString(), JsonNode.class));
        Assertions.assertTrue(responseNode.isPresent());
        responseNode.ifPresent(rNode -> {
            List<ConfigCode> result = null;
            try {
                result = objectMapper.readValue(rNode.get("content").toString(), new TypeReference<List<ConfigCode>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            Assertions.assertNotNull(result);
            Assertions.assertFalse(result.isEmpty());
        });

    }

    @Test
    public void testGetByCode() throws Exception {
        MockHttpServletResponse response = this.mvc
                .perform(get(BASE_URL + "/retrieveByCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("code", "TPT_REQUEST_SEQ_GEN"))
                .andExpect(status().isOk()).andReturn().getResponse();

        ConfigCodeDto retrievedConfigCode = objectMapper.readValue(response.getContentAsString(), ConfigCodeDto.class);
        assertEquals(savedConfigCode.getCode(), retrievedConfigCode.getCode());
    }

    @Test
    public void testUpdateTptRequestSeqGenConfigValue() throws Exception {
        configCode.setConfigValue("1");
        savedConfigCode = configCodeRepository.save(configCode);
        ConfigCode configCode = configCodeRepository.findByCode(savedConfigCode.getCode()).orElse(null);
        Assertions.assertNotNull(configCode);
        String newConfigValue = "2";
        MockHttpServletResponse response = this.mvc
                .perform(put(BASE_URL + "/updateTptRequestSeqGenConfigValue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("code", configCode.getCode())
                        .param("configValue", newConfigValue))
                .andExpect(status().isOk()).andReturn().getResponse();

        ConfigCodeDto retrievedConfigCode = objectMapper.readValue(response.getContentAsString(), ConfigCodeDto.class);
        assertEquals(newConfigValue, retrievedConfigCode.getConfigValue());
    }

    @Test
    void uploadExcelSuccessFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "CONFIG_CODE_SAVE.xlsx",
                "\tapplication/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ClassPathResource("excel-templates/ConfigCode/CONFIG_CODE_SAVE.xlsx").getInputStream()
        );

        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONFIGCODE_SETTING_UPLOAD)).thenReturn(mockUploadExcelTemplate());

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

        Optional<ConfigCode> configCode = configCodeRepository.findByCode("UNITTEST1");
        Assertions.assertTrue(configCode.isPresent());
        savedConfigCode = configCode.get();
        Assertions.assertEquals("Unit test 1", savedConfigCode.getDescription());
        Assertions.assertEquals("TRUE|FALSE", savedConfigCode.getConfigValue());
    }

    @Test
    void uploadExcelErrorFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "CONFIG_CODE_ERROR.xlsx",
                "\tapplication/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ClassPathResource("excel-templates/ConfigCode/CONFIG_CODE_ERROR.xlsx").getInputStream()
        );

        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONFIGCODE_SETTING_UPLOAD)).thenReturn(mockUploadExcelTemplate());

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

        Optional<ConfigCode> configCode = configCodeRepository.findByCode("FAIL1");
        Assertions.assertFalse(configCode.isPresent());
    }

   	@Test
	void uploadFormulaExcelSuccessFile() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "CONFIG_CODE_FORMULA_SUCCESS.xlsx",
				"\tapplication/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
				new ClassPathResource("excel-templates/ConfigCode/CONFIG_CODE_FORMULA_SUCCESS.xlsx").getInputStream());

		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONFIGCODE_SETTING_UPLOAD))
				.thenReturn(mockUploadExcelTemplate());

		Mockito.when(emailClient.sendUploadExcelEmail(any(), any()))
				.thenReturn(new ResponseEntity<Boolean>(HttpStatus.OK));

		MockHttpServletResponse response = this.mvc
				.perform(multipart(BASE_URL + "/uploadFiles").file(file)
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk()).andReturn().getResponse();

		String message = response.getContentAsString();
		Assertions.assertEquals(messagesUtilities.getMessageWithParam("upload.excel.success", null), message);

		List<ConfigCode> configCodes = configCodeRepository.findAll();
		Assertions.assertEquals(3, configCodes.size());
	}

	@Test
	void uploadFormulaExcelErrorFile() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "CONFIG_CODE_FORMULA_FAIL.xlsx",
				"\tapplication/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
				new ClassPathResource("excel-templates/ConfigCode/CONFIG_CODE_FORMULA_FAIL.xlsx").getInputStream());

		Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONFIGCODE_SETTING_UPLOAD))
				.thenReturn(mockUploadExcelTemplate());

		Mockito.when(emailClient.sendUploadExcelEmail(any(), any()))
				.thenReturn(new ResponseEntity<Boolean>(HttpStatus.OK));

		MockHttpServletResponse response = this.mvc
				.perform(multipart(BASE_URL + "/uploadFiles").file(file)
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().isOk()).andReturn().getResponse();

		String message = response.getContentAsString();
		Assertions.assertEquals(messagesUtilities.getMessageWithParam("upload.excel.success", null), message);

		List<ConfigCode> configCodes = configCodeRepository.findAll();
		Assertions.assertEquals(0, configCodes.size());
	}


    @Test
    void testDownloadExcel() throws Exception {

            Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONFIGCODE_SETTING_EXPORT))
                            .thenReturn(mockExportExcelTemplate());

            Mockito.when(configCodeServiceSpy.getAll()).thenReturn(mockListConfigCode());

            this.mvc.perform(post(BASE_URL + "/downloadExcel")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}")).andExpect(status().isOk()).andReturn();
    }

    private List<ConfigCodeDto> mockListConfigCodeDto() {
        List<ConfigCodeDto> configCodeDtoList = new ArrayList<>();

        ConfigCodeDto configCodeDto = new ConfigCodeDto();
        configCodeDto.setCode("SAVE15");
        configCodeDto.setExcelRowPosition(3);
        configCodeDtoList.add(configCodeDto);

        ConfigCodeDto configCodeDto1 = new ConfigCodeDto();
        configCodeDto1.setCode("SAVE15");
        configCodeDto1.setExcelRowPosition(5);
        configCodeDtoList.add(configCodeDto1);

        return configCodeDtoList;
    }

    public List<ConfigCode> mockListConfigCode() {
        List<ConfigCode> listConfigCode = new ArrayList();
        ConfigCode configCode = new ConfigCode();
        configCode.setCode("Test Code");
        configCode.setConfigValue("Test Value");
        configCode.setDescription("Test Description");
        configCode.setUsageLevel("Test Usage Level");
        configCode.setActiveInd(true);
        listConfigCode.add(configCode);
        return listConfigCode;
    }

    public List<UploadTemplateHdrIdDto> mockExportExcelTemplate(){
        List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
        UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
        uploadTemplateHdrIdDto.setCode("CONFIGCODE_SETTING_EXPORT");
        uploadTemplateHdrIdDto.setTitle("CONFIGURATION SETTINGS");
        uploadTemplateHdrIdDto.setFileName("CONFIGURATION_SETTINGS.xlsx");
        uploadTemplateHdrIdDto.setSheetSeqNo(0);
        uploadTemplateHdrIdDto.setStartRow(0);

        BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

        List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
        UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
        det1.setFieldName("code");
        det1.setPosition(0);
        det1.setWidth(6400);
        det1.setAlignment("HorizontalAlignment.CENTER");
        det1.setColumnName("Config Code");
        det1.setColumnFullName("Config Code");
        det1.setActiveInd(1);
        listDetail.add(det1);

        UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
        copier.copy(det1,det2,null);
        det2.setFieldName("description");
        det2.setPosition(1);
        det2.setColumnName("Description");
        det2.setColumnFullName("Description");
        listDetail.add(det2);

        UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
        copier.copy(det1,det3,null);
        det3.setFieldName("configValue");
        det3.setPosition(3);
        det3.setColumnName("Config Value");
        det3.setColumnFullName("Config Value");
        listDetail.add(det3);

        UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
        copier.copy(det1,det4,null);
        det4.setFieldName("activeInd");
        det4.setPosition(4);
        det4.setColumnName("Status");
        det4.setColumnFullName("Status");
        listDetail.add(det4);

        UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
        copier.copy(det1,det5,null);
        det5.setFieldName("usageLevel");
        det5.setPosition(2);
        det5.setColumnName("Usage Level");
        det5.setColumnFullName("Usage Level");
        listDetail.add(det5);

        uploadTemplateHdrIdDto.setListTempDetail(listDetail);
        listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

        return listUploadTemplateHdrIdDto;
    }

    public List<UploadTemplateHdrIdDto> mockUploadExcelTemplate(){
        List<UploadTemplateHdrIdDto> listUploadTemplateHdrIdDto = new ArrayList<>();
        UploadTemplateHdrIdDto uploadTemplateHdrIdDto = new UploadTemplateHdrIdDto();
        uploadTemplateHdrIdDto.setCode("CONFIGCODE_SETTING_UPLOAD");
        uploadTemplateHdrIdDto.setTitle("CONFIGURATION SETTINGS");
        uploadTemplateHdrIdDto.setFileName("CONFIGURATION_SETTINGS.xlsx");
        uploadTemplateHdrIdDto.setSheetSeqNo(0);
        uploadTemplateHdrIdDto.setStartRow(0);

        BeanCopier copier = BeanCopier.create(UploadTemplateDetIdDto.class, UploadTemplateDetIdDto.class, false);

        List<UploadTemplateDetIdDto> listDetail = new ArrayList<>();
        UploadTemplateDetIdDto det1 = new UploadTemplateDetIdDto();
        det1.setFieldName("code");
        det1.setColumnName("Config Code");
        det1.setColumnFullName("Config Code");
        det1.setMaxLength(255);
        det1.setMandatoryInd(1);
        det1.setNoneDuplicated(1);
        det1.setPosition(0);
        det1.setActiveInd(1);
        listDetail.add(det1);

        UploadTemplateDetIdDto det2 = new UploadTemplateDetIdDto();
        copier.copy(det1,det2,null);
        det2.setFieldName("description");
        det2.setMandatoryInd(0);
        det2.setColumnName("Description");
        det2.setColumnFullName("Description");
        det2.setNoneDuplicated(1);
        det2.setPosition(1);
        listDetail.add(det2);

        UploadTemplateDetIdDto det3 = new UploadTemplateDetIdDto();
        copier.copy(det1,det3,null);
        det3.setFieldName("configValue");
        det3.setMandatoryInd(0);
        det3.setColumnName("Config Value");
        det3.setColumnFullName("Config Value");
        det3.setNoneDuplicated(1);
        det3.setPosition(2);
        listDetail.add(det3);

        UploadTemplateDetIdDto det4 = new UploadTemplateDetIdDto();
        copier.copy(det1,det4,null);
        det4.setFieldName("activeInd");
        det4.setMaxLength(null);
        det4.setMandatoryInd(1);
        det4.setColumnName("Status");
        det4.setColumnFullName("Status");
        det4.setNoneDuplicated(1);
        det4.setPosition(3);
        listDetail.add(det4);

        UploadTemplateDetIdDto det5 = new UploadTemplateDetIdDto();
        copier.copy(det1,det5,null);
        det5.setFieldName("usageLevel");
        det5.setMandatoryInd(1);
        det5.setColumnName("Usage Level");
        det5.setColumnFullName("Usage Level");
        det5.setNoneDuplicated(1);
        det5.setPosition(4);
        listDetail.add(det5);

        uploadTemplateHdrIdDto.setListTempDetail(listDetail);
        listUploadTemplateHdrIdDto.add(uploadTemplateHdrIdDto);

        return listUploadTemplateHdrIdDto;
    }

    @Test
    void testDownloadExcelSorting() throws Exception {
        ConfigCode configCode1 = new ConfigCode();
        configCode1.setCode("Test Code1");
        configCode1.setConfigValue("Test Value1");
        configCode1.setDescription("Test Description1");
        configCode1.setUsageLevel("Test Usage Level1");
        configCode1.setActiveInd(true);
        ConfigCode configCode2 = new ConfigCode();
        configCode2.setCode("Test Code2");
        configCode2.setConfigValue("Test Value2");
        configCode2.setDescription("Test Description2");
        configCode2.setUsageLevel("Test Usage Level2");
        configCode2.setActiveInd(true);
        ConfigCode configCode3 = new ConfigCode();
        configCode3.setCode("Test Code3");
        configCode3.setConfigValue("Test Value3");
        configCode3.setDescription("Test Description3");
        configCode3.setUsageLevel("Test Usage Level3");
        configCode3.setActiveInd(true);

        configCodeRepository.save(configCode1);
        configCodeRepository.save(configCode2);
        configCodeRepository.save(configCode3);

        assertEquals(3, configCodeRepository.count());

        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONFIGCODE_SETTING_EXPORT))
                .thenReturn(mockExportExcelTemplate());

        String json1 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.CONFIG_CODE_CODE + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
        String json2 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.CONFIG_CODE_CONFIG_VALUE + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";
        String json3 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.CONFIG_CODE_DESCRIPTION + "," + AppConstants.CommonSortDirection.ASCENDING + "\"}";
        String json4 = "{\"sortBy\": \"" + AppConstants.SortPropertyName.CONFIG_CODE_USAGE_LEVEL + "," + AppConstants.CommonSortDirection.DESCENDING + "\"}";

        this.mvc.perform(post(BASE_URL + "/downloadExcel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json1))
                .andExpect(status().isOk()).andReturn();

        this.mvc.perform(post(BASE_URL + "/downloadExcel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json2))
                .andExpect(status().isOk()).andReturn();

        this.mvc.perform(post(BASE_URL + "/downloadExcel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json3))
                .andExpect(status().isOk()).andReturn();

        this.mvc.perform(post(BASE_URL + "/downloadExcel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json4))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void testDownloadExcelFilter() throws Exception {
        ConfigCode configCode1 = new ConfigCode();
        configCode1.setCode("Test Code1");
        configCode1.setConfigValue("Test Value1");
        configCode1.setDescription("Test Description1");
        configCode1.setUsageLevel("Test Usage Level1");
        configCode1.setActiveInd(true);
        ConfigCode configCode2 = new ConfigCode();
        configCode2.setCode("Test Code2");
        configCode2.setConfigValue("Test Value2");
        configCode2.setDescription("Test Description2");
        configCode2.setUsageLevel("Test Usage Level2");
        configCode2.setActiveInd(true);
        ConfigCode configCode3 = new ConfigCode();
        configCode3.setCode("Test Code3");
        configCode3.setConfigValue("Test Value3");
        configCode3.setDescription("Test Description3");
        configCode3.setUsageLevel("Test Usage Level3");
        configCode3.setActiveInd(true);

        configCodeRepository.save(configCode1);
        configCodeRepository.save(configCode2);
        configCodeRepository.save(configCode3);

        assertEquals(3, configCodeRepository.count());

        Mockito.when(excelClient.findTemplateByCode(AppConstants.ExcelTemplateCodes.CONFIGCODE_SETTING_EXPORT))
                .thenReturn(mockExportExcelTemplate());

        String json1 = "{\"" + AppConstants.SortPropertyName.CONFIG_CODE_CODE + "\": \"" + "ABC" + "\"}";
        String json2 = "{\"" + AppConstants.SortPropertyName.CONFIG_CODE_CONFIG_VALUE + "\": \"" + "TRUE" + "\"}";
        String json3 = "{\"" + AppConstants.SortPropertyName.CONFIG_CODE_DESCRIPTION + "\": \"" + "ABC"+ "\"}";
        String json4 = "{\"" + AppConstants.SortPropertyName.CONFIG_CODE_USAGE_LEVEL + "\": \"" + "TENANT" + "\"}";

        this.mvc.perform(post(BASE_URL + "/downloadExcel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json1))
                .andExpect(status().isOk()).andReturn();

        this.mvc.perform(post(BASE_URL + "/downloadExcel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json2))
                .andExpect(status().isOk()).andReturn();

        this.mvc.perform(post(BASE_URL + "/downloadExcel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json3))
                .andExpect(status().isOk()).andReturn();

        this.mvc.perform(post(BASE_URL + "/downloadExcel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json4))
                .andExpect(status().isOk()).andReturn();
    }
}
