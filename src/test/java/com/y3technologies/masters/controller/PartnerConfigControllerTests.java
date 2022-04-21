package com.y3technologies.masters.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.y3technologies.masters.client.AasClient;
import com.y3technologies.masters.client.AddrContactClient;
import com.y3technologies.masters.dto.PartnerConfigDto;
import com.y3technologies.masters.exception.RestErrorMessage;
import com.y3technologies.masters.model.*;
import com.y3technologies.masters.repository.*;
import com.y3technologies.masters.service.PartnerConfigService;
import com.y3technologies.masters.util.MessagesUtilities;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PartnerConfigControllerTests {
	
	private static final Logger logger = LoggerFactory.getLogger(PartnerConfigControllerTests.class);
	private String BASE_URL;
	private static ObjectMapper objectMapper;
	private static final String TEST_CONFIG_CODE = "CONFIG_CODE_1";

	private MockMvc mvc;
	@Autowired
	private WebApplicationContext wac;

	@Autowired
	MessagesUtilities messagesUtilities;

	@Autowired
	private PartnersRepository partnersRepository;

	@Autowired
	private PartnerConfigRepository partnerConfigRepository;

	@Value("/${api.version.masters:v1}")
	private String apiVersion;

	@MockBean
	AddrContactClient addrContactClient;

	@MockBean
	AasClient aasClient;

	@PostConstruct
	public void setApiVersion() {
		BASE_URL = apiVersion +"/partnerConfig";
	}
	private String PARTNERS;
	private Partners savedPartners;

	@Autowired
	private ConfigCodeRepository configCodeRepository;

	@Autowired
	private PartnerConfigService partnerConfigService;

	PartnerConfig savedPartnerConfig;
	ConfigCode configCode;
	PartnerConfigDto partnerConfigDto;
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
		PARTNERS = messagesUtilities.getResourceMessage(ConfigCodeUsageLevelEnum.PARTNERS.getUsageLevel(), LocaleContextHolder.getLocale());

		savedPartners = new Partners();
		savedPartners.setPartnerCode("code");
		savedPartners.setPartnerName("Name");
		savedPartners.setTenantId(1L);
		partnersRepository.save(savedPartners);

		configCode = new ConfigCode();
		configCode.setCode(TEST_CONFIG_CODE);
		configCode.setDescription("Desc");
		configCode.setUsageLevel(PARTNERS);
		configCode.setConfigValue("TRUE");
		configCodeRepository.save(configCode);

		partnerConfigDto = new PartnerConfigDto();
		partnerConfigDto.setPartnerId(savedPartners.getId());
		partnerConfigDto.setConfigCodeId(configCode.getId());
		partnerConfigDto.setValue("TRUE");

	}

	@AfterEach
	public void remove() throws Exception{

		partnersRepository.deleteAll();
		partnerConfigRepository.deleteAll();
		configCodeRepository.deleteAll();
		partnersRepository = null;
		partnerConfigRepository = null;
		configCodeRepository = null;
	}

	@Test
	@Transactional
	public void testCreatePartnerConfig() throws Exception {
		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(partnerConfigDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		String id = objectMapper.readValue(response.getContentAsString(), String.class);
		assertNotNull(id);

		Optional<PartnerConfig> partnerConfig = partnerConfigRepository.findById(Long.valueOf(id));
		assertNotNull(partnerConfig);
		assertEquals(partnerConfig.get().getConfigCode().getId(), configCode.getId());
		assertEquals(partnerConfig.get().getValue(), partnerConfigDto.getValue());

	}

	@Test
	@Transactional
	public void testUpdatePartnerConfigWithInvalidId() throws Exception {
		partnerConfigDto.setId(99999L);

		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(partnerConfigDto)))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse();

		RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		ArrayList<String> errorMessages = message.getMessages();
		assertTrue(errorMessages.contains("Partner configuration is invalid."));
	}

	@Test
	@Transactional
	public void testUpdateCustomerStatus() throws Exception {
		PartnerConfig model = new PartnerConfig();
		model.setConfigCode(configCode);
		model.setPartners(savedPartners);
		model.setValue(partnerConfigDto.getValue());
		partnerConfigService.save(model);

		assertNotNull(partnerConfigRepository.findById(model.getId()));
		assertEquals(true, model.getActiveInd());

		MockHttpServletResponse response = this.mvc
				.perform(get(BASE_URL + "/updateStatus?id=" + model.getId() + "&status=" + false).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse();

		String id = objectMapper.readValue(response.getContentAsString(), String.class);
		assertNotNull(id);
	}

	@Test
	@Transactional
	public void testUpdatePartnerConfigStatusWithInvalidId() throws Exception {
		MockHttpServletResponse response = this.mvc
				.perform(get(BASE_URL + "/updateStatus?id=" + 0L + "&status=" + false).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn().getResponse();

		RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		ArrayList<String> errorMessages = message.getMessages();
		assertTrue(errorMessages.contains("Partner configuration is invalid."));
	}

	@Test
	@Transactional
	public void testRetrieveById() throws Exception {
		PartnerConfig model = new PartnerConfig();
		model.setConfigCode(configCode);
		model.setPartners(savedPartners);
		model.setValue(partnerConfigDto.getValue());
		partnerConfigService.save(model);

		MockHttpServletResponse response = this.mvc
				.perform(get(BASE_URL + "/retrieve?id=" + model.getId()).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse();

		Optional<PartnerConfig> partnerConfig = partnerConfigRepository.findById(model.getId());
		assertNotNull(partnerConfig);
		assertEquals(partnerConfig.get().getValue(), partnerConfigDto.getValue());
		assertEquals(partnerConfig.get().getConfigCode().getId(), configCode.getId());

	}

	@Test
	@Transactional
	public void testRetrieveByInvalidId() throws Exception {

		MockHttpServletResponse response = this.mvc
				.perform(get(BASE_URL + "/retrieve?id=" + 0L).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn().getResponse();

		RestErrorMessage message = objectMapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		ArrayList<String> errorMessages = message.getMessages();
		assertTrue(errorMessages.contains("Partner configuration is invalid."));

	}

	@Test
	@Transactional
	public void testFindBySearch() throws Exception {
		PartnerConfig model = new PartnerConfig();
		model.setConfigCode(configCode);
		model.setPartners(savedPartners);
		model.setValue(partnerConfigDto.getValue());
		partnerConfigService.save(model);

		MockHttpServletResponse response = this.mvc
				.perform(get(BASE_URL + "/findBySearch").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse();

	}

}
