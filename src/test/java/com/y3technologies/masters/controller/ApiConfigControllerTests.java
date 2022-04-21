package com.y3technologies.masters.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.gson.Gson;
import com.y3technologies.masters.client.ExcelClient;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.apiConfig.AddConfigRequest;
import com.y3technologies.masters.dto.apiConfig.ApiConfigDTO;
import com.y3technologies.masters.dto.apiConfig.ApiConfigFilter;
import com.y3technologies.masters.dto.apiConfig.ApiEnum;
import com.y3technologies.masters.dto.apiConfig.GenerateApiKeyResponse;
import com.y3technologies.masters.dto.apiConfig.UpdateApiConfigRequest;
import com.y3technologies.masters.dto.apiConfig.UpdateApiConfigStatusRequest;
import com.y3technologies.masters.dto.excel.UploadTemplateHdrIdDto;
import com.y3technologies.masters.model.Lookup;
import com.y3technologies.masters.model.Partners;
import com.y3technologies.masters.model.apiConfig.ApiConfig;
import com.y3technologies.masters.model.apiConfig.ApiType;
import com.y3technologies.masters.model.apiConfig.AuthenticationType;
import com.y3technologies.masters.repository.ApiConfigRepository;
import com.y3technologies.masters.repository.LookupRepository;
import com.y3technologies.masters.repository.PartnersRepository;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@PropertySource("classpath:application.properties")
@Transactional
public class ApiConfigControllerTests {

    private static final Logger logger = LoggerFactory.getLogger(ApiConfigControllerTests.class);
    private String BASE_URL;
    private static ObjectMapper objectMapper;

    private MockMvc mvc;
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ApiConfigRepository apiConfigRepository;

    @Autowired
    private PartnersRepository partnersRepository;

    @Autowired
    private LookupRepository lookupRepository;

    @MockBean
    private ExcelClient excelClient;

    @Value("/${api.version.masters:v1}")
    private String apiVersion;

    @PostConstruct
    public void setApiVersion() {
        BASE_URL = apiVersion + "/apiConfig";
    }

    private AddConfigRequest createConfigRequestIncoming;
    private AddConfigRequest createConfigRequestOutgoing;
    private ApiConfigFilter apiConfigFilter = new ApiConfigFilter();
    SessionUserInfoDTO sessionObj;
    private static final String description = "https://www.google.com/webhp?hl=vi&ictx=2&sa=X&ved=0ahUKEwij49eE5dDyAhVxJaYKHTSDAxMQPQgJ";
    private static final String url = "https://www.google.com/webhp?hl=vi&ictx=2&sa=X&ved=0ahUKEwij49eE5dDyAhVxJaYKHTSDAxMQPQgJ";
    private static final String urn = "https://www.google.com/webhp?hl=vi&ictx=2&sa=X&ved=0ahUKEwij49eE5dDyAhVxJaYKHTSDAxMQPQgJ";
    private static final String apiKey = "https://www.google.com/webhp?hl=vi&ictx=2&sa=X&ved=0ahUKEwij49eE5dDyAhVxJaYKHTSDAxMQPQgJ";
    private static final String username = "abc";
    private static final String password = "123";
    private Partners customer;
    private Lookup apiIncomingLookup;
    private Lookup apiOutgoingLookup;

    @BeforeAll
    public static void setup() throws Exception {
        objectMapper = new ObjectMapper().registerModule(new ParameterNamesModule()).registerModule(new Jdk8Module())
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

        // create customer
        customer = new Partners();
        customer.setPartnerCode("testApiConfig");
        customer.setPartnerName("testApiConfig");
        customer.setTenantId(1L);
        partnersRepository.save(customer);

        // get api id
        apiIncomingLookup = lookupRepository.findByLookupTypeAndLookupCode("API_CONFIG",
                "Transport Request (Incoming)");
        apiOutgoingLookup = lookupRepository.findByLookupTypeAndLookupCode("API_CONFIG", "Status Update (Outgoing)");
    }

    @Test
    public void testCreateApiConfigOutgoingSuccess() throws Exception {
        // create outgoing request
        createConfigRequestOutgoing = new AddConfigRequest();
        createConfigRequestOutgoing.setApiId(apiOutgoingLookup.getId());
        createConfigRequestOutgoing.setCustomerId(customer.getId());
        createConfigRequestOutgoing.setDescription(description);
        createConfigRequestOutgoing.setUrl(url);
        createConfigRequestOutgoing.setUrn(urn);
        createConfigRequestOutgoing.setUsername(username);
        createConfigRequestOutgoing.setPassword(password);
        createConfigRequestOutgoing.setIsActive(true);
        // case success
        String createResult = mvc
                .perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createConfigRequestOutgoing))
                        .requestAttr("SESSION_INFO", sessionObj))
                .andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        logger.info("create result is {}", createResult);
    }

    @Test
    public void testCreateApiConfigOutgoingFail() throws Exception {
        // create outgoing request without url
        createConfigRequestOutgoing = new AddConfigRequest();
        createConfigRequestOutgoing.setApiId(apiOutgoingLookup.getId());
        createConfigRequestOutgoing.setCustomerId(customer.getId());
        createConfigRequestOutgoing.setDescription(description);
        createConfigRequestOutgoing.setUrn(urn);
        createConfigRequestOutgoing.setUsername(username);
        createConfigRequestOutgoing.setPassword(password);
        createConfigRequestOutgoing.setIsActive(true);
        // case fail without url
        String createResult = mvc
                .perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createConfigRequestOutgoing))
                        .requestAttr("SESSION_INFO", sessionObj))
                .andDo(print()).andExpect(status().is4xxClientError()).andReturn().getResponse().getContentAsString();
        logger.info("create result is {}", createResult);
    }

    @Test
    public void testCreateApiConfigIncomingSuccess() throws Exception {
        // create incoming request
        createConfigRequestIncoming = new AddConfigRequest();
        createConfigRequestIncoming.setApiId(apiIncomingLookup.getId());
        createConfigRequestIncoming.setCustomerId(customer.getId());
        createConfigRequestIncoming.setDescription(description);
        createConfigRequestIncoming.setUrl(url);
        createConfigRequestIncoming.setUrn(urn);
        createConfigRequestIncoming.setApiKey(apiKey);
        createConfigRequestIncoming.setIsActive(true);
        // case success
        String createResult = mvc
                .perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createConfigRequestIncoming))
                        .requestAttr("SESSION_INFO", sessionObj))
                .andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        logger.info("create result is {}", createResult);
    }

    @Test
    public void testCreateApiConfigIncomingFail() throws Exception {
        // create incoming request without urn
        createConfigRequestIncoming = new AddConfigRequest();
        createConfigRequestIncoming.setApiId(apiIncomingLookup.getId());
        createConfigRequestIncoming.setCustomerId(customer.getId());
        createConfigRequestIncoming.setDescription(description);
        createConfigRequestIncoming.setUrl(url);
        createConfigRequestIncoming.setUrn("");
        createConfigRequestIncoming.setApiKey(apiKey);
        createConfigRequestIncoming.setIsActive(true);
        // case fail without urn
        String createResult = mvc
                .perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createConfigRequestIncoming))
                        .requestAttr("SESSION_INFO", sessionObj))
                .andDo(print()).andExpect(status().is4xxClientError()).andReturn().getResponse().getContentAsString();
        logger.info("create result is {}", createResult);
    }

    @Test
    public void testGetListApiConfigByFilterSuccess() throws Exception {
        // create filter
        apiConfigFilter.setApiId(new ArrayList<>());
        apiConfigFilter.setCustomerName("");
        apiConfigFilter.setApiType("");
        // case fail without urn
        Gson gson = new Gson();
        String createResult = mvc
                .perform(post(BASE_URL + "/filterBy").contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(apiConfigFilter)).requestAttr("SESSION_INFO", sessionObj))
                .andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        logger.info("create result is {}", createResult);
    }

    @Test
    public void testGetListApiConfigByFilterFailed() throws Exception {
        // create filter
        // case fail without urn
        String createResult = mvc
                .perform(post(BASE_URL + "/filterBy").contentType(MediaType.APPLICATION_JSON).content("")
                        .requestAttr("SESSION_INFO", sessionObj))
                .andDo(print()).andExpect(status().is5xxServerError()).andReturn().getResponse().getContentAsString();
        logger.info("create result is {}", createResult);
    }

    @Test
    public void testGenerateApiKeySuccess() throws Exception {
        String responseStr = mvc
                .perform(get(BASE_URL + "/newApiKey").contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("SESSION_INFO", sessionObj))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        GenerateApiKeyResponse response = objectMapper.readValue(responseStr, GenerateApiKeyResponse.class);
        assertNotNull(response);
        assertNotNull(response.getApiKey());
    }

    @Test
    public void testRetrieveApiConfigSuccess() throws Exception {
        ApiConfig apiConfig = mockApiConfig();
        apiConfigRepository.save(apiConfig);
        String responseStr = mvc
                .perform(get(BASE_URL + "/retrieve").contentType(MediaType.APPLICATION_JSON)
                        .param("id", apiConfig.getId().toString()).requestAttr("SESSION_INFO", sessionObj))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ApiConfigDTO response = objectMapper.readValue(responseStr, ApiConfigDTO.class);
        assertNotNull(response);
        assertEquals(response.getId(), apiConfig.getId());
    }

    @Test
    public void testUpdateApiConfigSuccess() throws Exception {
        ApiConfig apiConfig = mockApiConfig();
        apiConfigRepository.save(apiConfig);
        assertNotNull(apiConfig.getId());

        String updateDescription = "new description";
        String updateUrl = url + "updated";
        String updateUrn = urn + "updated";
        String updateApiKey = apiKey + "updated";

        UpdateApiConfigRequest updateRequest = new UpdateApiConfigRequest();
        updateRequest.setDescription(updateDescription);
        updateRequest.setUrl(updateUrl);
        updateRequest.setUrn(updateUrn);
        updateRequest.setApiKey(updateApiKey);
        updateRequest.setIsActive(true);

        String responseStr = mvc
                .perform(put(BASE_URL + "/update").contentType(MediaType.APPLICATION_JSON)
                        .param("id", apiConfig.getId().toString())
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .requestAttr("SESSION_INFO", sessionObj))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ApiConfigDTO response = objectMapper.readValue(responseStr, ApiConfigDTO.class);
        assertNotNull(response);
        assertEquals(response.getId(), apiConfig.getId());
        assertEquals(response.getDescription(), updateDescription);
        assertEquals(response.getUrl(), updateUrl);
        assertEquals(response.getUrn(), updateUrn);
        assertEquals(response.getApiKey(), updateApiKey);
    }

    @Test
    public void testListApiTypeSuccess() throws Exception {
        String responseStr = mvc
                .perform(get(BASE_URL + "/listApiType").contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("SESSION_INFO", sessionObj))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<ApiEnum> response = objectMapper.readValue(responseStr, new TypeReference<List<ApiEnum>>() {
        });
        assertEquals(2, response.size());
    }

    @Test
    public void testDownloadExcelSuccess() throws Exception {
        ApiConfig apiConfig = mockApiConfig();
        apiConfigRepository.save(apiConfig);
        assertNotNull(apiConfig.getId());

        List<UploadTemplateHdrIdDto> template = mockExcelTemplate();
        doReturn(template).when(excelClient).findTemplateByCode(any());

        ApiConfigFilter apiConfigFilter = new ApiConfigFilter();
        apiConfigFilter.setApiId(new ArrayList<>());
        apiConfigFilter.setCustomerName("");
        apiConfigFilter.setApiType("");

        Gson gson = new Gson();
        mvc.perform(post(BASE_URL + "/downloadExcel").contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(apiConfigFilter)).requestAttr("SESSION_INFO", sessionObj))
                .andExpect(status().isOk()).andReturn();
    }

    private List<UploadTemplateHdrIdDto> mockExcelTemplate() {
        List<UploadTemplateHdrIdDto> result = new ArrayList<>();
        UploadTemplateHdrIdDto template = new UploadTemplateHdrIdDto();
        template.setTitle("API CONFIG SETTING");
        template.setFileName("API.xlsx");
        result.add(template);
        return result;
    }

    @Test
    public void testApiConfigStatusSuccess() throws Exception {
        ApiConfig apiConfig = mockApiConfig();
        apiConfigRepository.save(apiConfig);
        assertNotNull(apiConfig.getId());

        boolean newStatus = false;

        UpdateApiConfigStatusRequest updateStatusRequest = new UpdateApiConfigStatusRequest();
        updateStatusRequest.setIsActive(newStatus);

        String responseStr = mvc
                .perform(put(BASE_URL + "/updateStatus").contentType(MediaType.APPLICATION_JSON)
                        .param("id", apiConfig.getId().toString())
                        .content(objectMapper.writeValueAsString(updateStatusRequest))
                        .requestAttr("SESSION_INFO", sessionObj))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ApiConfigDTO response = objectMapper.readValue(responseStr, ApiConfigDTO.class);
        assertEquals(newStatus, response.getIsActive());
    }

    private ApiConfig mockApiConfig() {
        ApiConfig apiConfig = new ApiConfig();
        apiConfig.setAuthenType(AuthenticationType.API_KEY);
        apiConfig.setCustomer(customer);
        apiConfig.setApiKey(apiKey);
        apiConfig.setType(ApiType.INCOMING);
        apiConfig.setLookup(apiIncomingLookup);
        apiConfig.setUrl(url);
        apiConfig.setUrn(urn);
        apiConfig.setTenantId(1L);
        return apiConfig;
    }
}
