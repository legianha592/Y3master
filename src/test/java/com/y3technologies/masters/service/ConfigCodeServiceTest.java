/**
 * 
 */
package com.y3technologies.masters.service;

import com.y3technologies.masters.model.ConfigCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author beekhon.ong
 */
@SpringBootTest
public class ConfigCodeServiceTest {

	@Autowired
	ConfigCodeService configCodeService;

	ConfigCode newConfigCode;
	ConfigCode savedConfigCode;
	ConfigCode deletedConfigCode;
	ConfigCode configCode;

	/**
	 * @throws Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		newConfigCode = new ConfigCode();
		newConfigCode.setCode("AUTO_CLOSE_JOB");
		newConfigCode.setDescription("Auto Close Job Desc");
		newConfigCode.setUsageLevel("TENANT");
		newConfigCode.setConfigValue("TRUE");

	}

	/**
	 * @throws Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
		if(savedConfigCode != null) configCodeService.delete(savedConfigCode);
		configCode = null;
		newConfigCode = null;
		savedConfigCode = null;
		deletedConfigCode = null;
	}

	@Test
	void testCreateConfigCode() {
		savedConfigCode = configCodeService.save(newConfigCode);
		assertNotNull(savedConfigCode, "Config Code not null.");
		assertNotNull(savedConfigCode.getId(), "Config Code Id not null.");
	}
	
	@Test
	void testUpdateConfigCode() {
		savedConfigCode = configCodeService.save(newConfigCode);
		assertNotNull(savedConfigCode, "Config Code not null.");
		
		// update fields
		savedConfigCode.setCode("AUTO_CLOSE_JOB2");
		savedConfigCode.setDescription("Auto Close Job Desc2");
		savedConfigCode.setUsageLevel("PARTNERS");
		savedConfigCode.setConfigValue("TRUE");

		// save updated entity
		assertNull(configCode);
		configCode = configCodeService.save(savedConfigCode);
		assertEquals(savedConfigCode.getHashcode(), configCode.getHashcode());

		savedConfigCode.setVersion(configCode.getVersion());
	}
	
	@Test
	void testInActivateConfigCode() {
		savedConfigCode = configCodeService.save(newConfigCode);
		assertNotNull(savedConfigCode, "Config Code not null.");
		assertNotNull(savedConfigCode.getId(), "Config Code Id not null.");
		
		assertNull(deletedConfigCode);
		configCodeService.updateStatus(savedConfigCode.getId(), Boolean.FALSE);

		deletedConfigCode = configCodeService.getById(savedConfigCode.getId()).get();

		assertFalse(deletedConfigCode.getActiveInd());
	}

	@Test
	void testFindById() {
		savedConfigCode = configCodeService.save(newConfigCode);
		assertNotNull(savedConfigCode, "Config Code not null.");
		
		assertNull(configCode);
		configCode = configCodeService.getById(savedConfigCode.getId()).get();

		assertEquals(savedConfigCode.getId(), configCode.getId());
	}
	
	@Test
	void testDeleteConfigCode() {
		savedConfigCode = configCodeService.save(newConfigCode);
		assertNotNull(savedConfigCode, "Config Code not null.");
		
		Long id = savedConfigCode.getId();
		configCodeService.delete(savedConfigCode);
		assertFalse(configCodeService.existsById(id));
	}

	@Test
	void testExistById() {
		savedConfigCode = configCodeService.save(newConfigCode);
		assertNotNull(savedConfigCode, "Config Code not null.");
		assertNotNull(savedConfigCode.getId(), "Config Code Id not null.");

		assertTrue(configCodeService.existsById(savedConfigCode.getId()));
	}
}
