package com.y3technologies.masters.util;

import java.util.Collection;

public class ValidationUtils {

	public static final class Number {
		public static Boolean isPositive(Float value) {
			return value != null && value > 0;
		}

		public static boolean isPositive(Long value) {
			return value != null && value > 0;
		}

		public static Boolean isValidId(Long id) {
			return id != null && id > 0;
		}

		public static Boolean isValidIds(Long... ids) {

			if (ids != null) {
				for (Long i : ids) {
					if (!isValidId(i))
						return false;
				}
				return true;
			}
			return false;
		}

		public static boolean isValidId(Collection<Long> ids) {
			if (ids != null) {
				for (Long i : ids) {
					if (!isValidId(i))
						return false;
				}
				return true;
			}
			return false;
		}
	}

}
