package com.y3technologies.masters.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.y3technologies.masters.dto.MilkrunTripDto;
import com.y3technologies.masters.dto.MilkrunVehicleDto;
import com.y3technologies.masters.util.BootResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Transactional
@PropertySource("classpath:application.properties")
public class MilkrunVehicleTripControllerTest {
	private static final Logger logger = LoggerFactory.getLogger(MilkrunVehicleTripControllerTest.class);

	private MockMvc mvc;
	@Autowired
	private WebApplicationContext wac;

	@Value("${api.version.masters}")
	private String apiVersion;

	private String BASE_URL;

	@PostConstruct
	public void setApiVersion() {
		BASE_URL = "/" + apiVersion + "/milkrunVehicleTrip";
	}

	@Before
	public void setup() {
		this.mvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@Test
	public void testMilkrunVehicleTripController() throws Exception {

		String result = null;

		/*** create ***/

		MilkrunVehicleDto createDto = initCreateMilkrunVehicle();
		ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		JavaTimeModule module = new JavaTimeModule();
		module.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
		module.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
		mapper.registerModule(module);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(createDto);

		result = mvc.perform(post(BASE_URL + "/save").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("create result is {}", result);

		BootResponse data = mapper.readValue(result, BootResponse.class);

		String dataJson = ow.writeValueAsString(data.getData());

		MilkrunVehicleDto createResult = mapper.readValue(dataJson, MilkrunVehicleDto.class);

		Long id = createResult.getId();

		/*** queryMilkrunSchedule ***/
		RequestBuilder request = get(BASE_URL + "/queryMilkrunSchedule?id=" + id);
		String resultJson = mvc.perform(request).andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
		logger.info("queryMilkrunSchedule result is {}", resultJson);

		/*** queryMilkrunScheduleUsingTenantAndCustomer ***/
		request = get(BASE_URL + "/queryMilkrunScheduleUsingTenantAndCustomer?tenantId=" + 1l + "&customerId=" + 1l);
		resultJson = mvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("queryMilkrunScheduleUsingTenantAndCustomer result is {}", resultJson);

		/*** updateMilkrunVehicle ***/
		MilkrunVehicleDto updateDto = initUpdateMilkrunVehicle(createResult);
		requestJson = ow.writeValueAsString(updateDto);
		result = mvc
				.perform(post(BASE_URL + "/updateMilkrunVehicle").contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("updateMilkrunVehicle result is {}", result);

		/*** changeTripActive ***/
		MilkrunTripDto tripDto = new MilkrunTripDto();
		tripDto.setId(createResult.getTripDtolist().get(0).getId());
		tripDto.setActiveInd(Boolean.FALSE);
		requestJson = ow.writeValueAsString(tripDto);
		result = mvc
				.perform(post(BASE_URL + "/changeTripActive").contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("changeTripActive result is {}", result);

		/*** ChangeMilkrunScheduleActive ***/
		MilkrunVehicleDto dto = new MilkrunVehicleDto();
		dto.setId(id);
		dto.setActiveInd(Boolean.FALSE);
		requestJson = ow.writeValueAsString(dto);
		result = mvc
				.perform(post(BASE_URL + "/ChangeMilkrunScheduleActive").contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		logger.info("ChangeMilkrunScheduleActive result is {}", result);

	}

	private MilkrunVehicleDto initUpdateMilkrunVehicle(MilkrunVehicleDto dto) {

		MilkrunVehicleDto milkrunVehicleDto = new MilkrunVehicleDto();
		milkrunVehicleDto.setVehicleId(4l);
		milkrunVehicleDto.setDriverId(3l);
		milkrunVehicleDto.setId(dto.getId());
		milkrunVehicleDto.setVersion(dto.getVersion());

		return milkrunVehicleDto;
	}

	private MilkrunVehicleDto initCreateMilkrunVehicle() {
		LocalTime localTime = LocalTime.now();

		MilkrunVehicleDto milkrunVehicleDto = new MilkrunVehicleDto();
		List<MilkrunTripDto> tripDtolist = new ArrayList<MilkrunTripDto>();
		MilkrunTripDto milkrunTripDto1 = new MilkrunTripDto();
		milkrunTripDto1.setTripSequence(1);
		milkrunTripDto1.setVisitSequence(1);
		milkrunTripDto1.setLocationId(1068L);
		milkrunTripDto1.setDayOfWeek("Sun,Mon,Tue,Wed,Fri,Sat");
		milkrunTripDto1.setStartTime(localTime.withHour(10));
		milkrunTripDto1.setEndTime(localTime.withHour(14));
		milkrunTripDto1.setTPTRequestActivity("PickUp");
		MilkrunTripDto milkrunTripDto2 = new MilkrunTripDto();
		milkrunTripDto2.setTripSequence(1);
		milkrunTripDto2.setVisitSequence(2);
		milkrunTripDto2.setLocationId(1069L);
		milkrunTripDto2.setStartTime(localTime.withHour(10));
		milkrunTripDto2.setEndTime(localTime.withHour(14));
		milkrunTripDto2.setDayOfWeek("Sun,Mon,Tue,Wed,Sat");
		milkrunTripDto2.setTPTRequestActivity("TakeOver");

		tripDtolist.add(milkrunTripDto1);
		tripDtolist.add(milkrunTripDto2);
		milkrunVehicleDto.setTenantId(5l);
		milkrunVehicleDto.setCustomerId(10l);
		milkrunVehicleDto.setVehicleId(3l);
		milkrunVehicleDto.setDriverId(2l);
		milkrunVehicleDto.setTripDtolist(tripDtolist);

		return milkrunVehicleDto;
	}
}
