package com.y3technologies.masters.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.annotation.PostConstruct;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.y3technologies.masters.dto.LookupDto;
import com.y3technologies.masters.model.Lookup;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Transactional
@PropertySource("classpath:application.properties")
public class LookupControllerTests {

	private static final Logger logger = LoggerFactory.getLogger(LookupControllerTests.class);

	private MockMvc mvc;
	@Autowired
	private WebApplicationContext wac;

	private static ObjectMapper objectMapper;

	@Value("${api.version.masters}")
	private String apiVersion;

	private String BASE_URL;

	@PostConstruct
	public void setApiVersion() {
		BASE_URL = "/" + apiVersion + "/lookup";
	}

	@Before
	public void setup() {
		objectMapper = new ObjectMapper().registerModule(new ParameterNamesModule()).registerModule(new Jdk8Module())
				.registerModule(new JavaTimeModule());
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		this.mvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@Test
	public void testLookupController() throws Exception {
		String result;

		/*** listByParam ***/
		Lookup model = new Lookup();
		model.setLookupType("EquipmentType");
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(model);
		result = mvc
				.perform(post(BASE_URL + "/listByParam").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("listByParam result is {}", result);

		/*** create ***/
		LookupDto createLookup = new LookupDto();
		createLookup.setTenantId(10L);
		createLookup.setActiveInd(true);
		createLookup.setLookupType("New Type");
		createLookup.setLookupCode("testing_only");
		createLookup.setLookupDescription("This is testing only");
		createLookup.setSeq(1);

		String createResult = mvc
				.perform(post(BASE_URL + "/create").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createLookup)))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("create result is {}", createResult);
		Long id = Long.valueOf(createResult);

		/*** retrieve ***/

		String findResult = mvc.perform(get(BASE_URL + "/retrieve/" + id)).andDo(print()).andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		logger.info("FindById result is {}", findResult);
//	    
		/*** update ***/

		LookupDto updateLookup = new LookupDto();
		updateLookup.setId(id);
		updateLookup.setLookupType("NEW Test");
		updateLookup.setActiveInd(true);
		updateLookup.setLookupCode("testing_only_update");
		updateLookup.setLookupDescription("This is testing only update");
		updateLookup.setSeq(1);

		String updateResult = mvc
				.perform(post(BASE_URL + "/update").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateLookup)))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("create result is {}", updateResult);
	}

}
