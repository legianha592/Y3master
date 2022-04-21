/**
 * 
 */
package com.y3technologies.masters.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author beekhon.ong
 */
public enum ConfigCodeUsageLevelEnum {
	TENANT("config.code.usage.level.tenant"),
	PARTNERS("config.code.usage.level.partners"),
	ALL("config.code.usage.level.all");

	private String usageLevel;
	private static final Map<String, ConfigCodeUsageLevelEnum> USAGE_LEVEL_LOOKUP = new HashMap<>();

	ConfigCodeUsageLevelEnum(String usageLevel) {
		this.usageLevel = usageLevel;
	}

	public String getUsageLevel() {
		return usageLevel;
	}

	static {
		for (ConfigCodeUsageLevelEnum e: values()) {
			USAGE_LEVEL_LOOKUP.put(e.getUsageLevel(), e);
		}
	}

	public static ConfigCodeUsageLevelEnum valueOfUsageLevel(String usageLevel) {
		return USAGE_LEVEL_LOOKUP.get(usageLevel);
	}

}
