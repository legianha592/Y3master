package com.y3technologies.masters.controller;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.jersey.core.impl.provider.entity.XMLRootObjectProvider;
import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.CommonTagDto;
import com.y3technologies.masters.dto.LocationDto;
import com.y3technologies.masters.dto.aas.SessionUserInfoDTO;
import com.y3technologies.masters.dto.filter.CommonTagFilter;
import com.y3technologies.masters.exception.RestErrorMessage;
import com.y3technologies.masters.model.Location;
import com.y3technologies.masters.model.comm.AddrDTO;
import com.y3technologies.masters.repository.CommonTagRepository;
import com.y3technologies.masters.service.CommonTagService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.y3technologies.masters.model.CommonTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Transactional
@PropertySource("classpath:application.properties")
public class CommonTagControllerTests {
	
	private static final Logger logger = LoggerFactory.getLogger(CommonTagControllerTests.class);
	
	private MockMvc mvc;
	@Autowired
	private WebApplicationContext wac;
	
	@Value("${api.version.masters}")
	private String apiVersion;

	private String BASE_URL;

	private CommonTag model;

	private CommonTagDto commonTagDto;

	private CommonTag commonTag;

	@Autowired
	private CommonTagRepository commonTagRepository;

	@Autowired
	private CommonTagService commonTagService;

	@Autowired
	private MessageSource messageSource;

	private SessionUserInfoDTO sessionObj;

	@PostConstruct
	public void setApiVersion() {
		BASE_URL = "/" + apiVersion + "/commonTag";
	}

	@Before
	public void setup() {

		this.mvc = MockMvcBuilders.webAppContextSetup(wac).build();
		sessionObj = new SessionUserInfoDTO();
		sessionObj.setAasTenantId(1L);
		sessionObj.setTimezone("Asia/Singapore");
	}

	@AfterEach
	public void remove() throws Exception {
		CommonTagFilter commonTagFilter = new CommonTagFilter();
		commonTagFilter.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		commonTagFilter.setTag("TEST_LOCATION_TAG");
		commonTagFilter.setTenantId(1L);
		List<CommonTag> savedCommonTag = commonTagService.findByFilter(commonTagFilter);
		if(null != savedCommonTag && 0 < savedCommonTag.size()) {
			commonTagRepository.deleteAll(savedCommonTag);
		}
		commonTagRepository = null;
		model = null;
		commonTagDto = null;
		commonTag = null;
	}

	@Test
	public void testCommonTagController() throws Exception {
	
		String result;
	    
	    /***listByParam***/
		model = new CommonTag();
	    model.setTagType("LicenseType");
	    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
	    String requestJson = ow.writeValueAsString(model);
	    result = mvc.perform(post(BASE_URL + "/listByParam").contentType(MediaType.APPLICATION_JSON)
	                .content(requestJson))
	            .andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
	    logger.info("listByParam result is {}", result);
	}

	@Test
	@DisplayName("Create Common Tag")
	public void testCreateCommonTag() throws Exception {
		commonTagDto = new CommonTagDto();
		commonTagDto.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		commonTagDto.setTag("TEST_LOCATION_TAG");
		commonTagDto.setTenantId(1L);
		ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(commonTagDto);

		MockHttpServletResponse response = this.mvc.perform(post(BASE_URL + "/create")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson).requestAttr("SESSION_INFO", sessionObj))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse();

		model = mapper.readValue(response.getContentAsString(), new TypeReference<CommonTag>(){});
		assertNotNull(model);
		assertNotNull(model.getId());
		assertEquals(model.getTenantId(), commonTagDto.getTenantId());
		assertEquals(model.getTag(), commonTagDto.getTag());
		assertEquals(model.getTagType(), commonTagDto.getTagType());
	}

	@Test
	@DisplayName("Create duplicate Common Tag")
	public void testCreateCommonTagDuplicate() throws Exception {
		commonTag = new CommonTag();
		commonTag.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		commonTag.setTag("TEST_LOCATION_TAG");
		commonTag.setTenantId(1L);
		commonTag = commonTagRepository.save(commonTag);
		assertNotNull(commonTag.getId());

		commonTagDto = new CommonTagDto();
		commonTagDto.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		commonTagDto.setTag("TEST_LOCATION_TAG");
		commonTagDto.setTenantId(1L);

		ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(commonTagDto);

		MockHttpServletResponse response = this.mvc.perform(
				post(BASE_URL + "/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson)
						.requestAttr("SESSION_INFO", sessionObj))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse();

		RestErrorMessage message = mapper.readValue(response.getContentAsString(), RestErrorMessage.class);
		ArrayList<String> errorMessages = message.getMessages();
		assertTrue(errorMessages.contains(messageSource.getMessage("exception.common.tag.location.tag.duplicate", null, Locale.getDefault())));
	}

	@Test
	@DisplayName("Find by filter Common Tag")
	public void testFindByFilter() throws Exception {
		List<CommonTag> savedCommonTagList = new ArrayList<>();
		List<CommonTag> commonTagList = new ArrayList<>();

		commonTag = new CommonTag();
		commonTag.setTagType(AppConstants.CommonTag.LOCATION_TAG);
		commonTag.setTag("TEST_LOCATION_TAG");
		commonTag.setTenantId(1L);
		commonTag = commonTagRepository.save(commonTag);
		assertNotNull(commonTag.getId());

		CommonTagFilter savedCommonTagFilter = new CommonTagFilter();
		savedCommonTagFilter.setTagType(commonTag.getTagType());
		savedCommonTagFilter.setTag(commonTag.getTag());
		savedCommonTagFilter.setTenantId(commonTag.getTenantId());

		commonTagList = commonTagService.findByFilter(savedCommonTagFilter);

		CommonTagFilter commonTagFilter = new CommonTagFilter();
		commonTagFilter.setTenantId(1L);
		commonTagFilter.setTag("TEST_LOCATION_TAG");
		commonTagFilter.setTagType(AppConstants.CommonTag.LOCATION_TAG);

		ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

		MultiValueMap<String, String> params = new HttpHeaders();
		params.add("tenantId", savedCommonTagFilter.getTenantId().toString());
		params.add("tag", savedCommonTagFilter.getTag());
		params.add("tagType", savedCommonTagFilter.getTagType());

		MockHttpServletResponse response = this.mvc.perform(get(BASE_URL + "/findByFilter")
				.contentType(MediaType.APPLICATION_JSON)
				.params(params))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse();

		commonTagList = mapper.readValue(response.getContentAsString(), new TypeReference<List<CommonTag>>(){});
		assertTrue(commonTagList.size() > 0);
		assertNotNull(commonTagList.get(0));
		assertNotNull(commonTagList.get(0).getId());
		assertEquals(commonTag.getTenantId(), commonTagList.get(0).getTenantId());
		assertEquals(commonTag.getTag(), commonTagList.get(0).getTag());
		assertEquals(commonTag.getTagType(), commonTagList.get(0).getTagType());
	}
}
