package com.y3technologies.masters.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("deprecation")
public class DateFormatUtil {
	public static String dayForWeek(Date pTime) throws Exception {
		Calendar c = Calendar.getInstance();
		c.setTime(pTime);
		int dayForWeek = 0;
		if (c.get(Calendar.DAY_OF_WEEK) == 1) {
			dayForWeek = 7;
		} else {
			dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
		}
		String temp = "";
		switch (dayForWeek) {
		case 1:
			temp = "Monday";
			break;
		case 2:
			temp = "Tuesday";
			break;
		case 3:
			temp = "Wednesday";
			break;
		case 4:
			temp = "Thursday";
			break;
		case 5:
			temp = "Friday";
			break;
		case 6:
			temp = "Saturday";
			break;
		case 7:
			temp = "Sunday";
			break;
		}
		return temp;

	}

	public static Date getFirstMaxSendDate(String scheduledQuantity) {
		Date maxSendDate = new Date();
		Integer schQuantity = Integer.valueOf(scheduledQuantity);
		Calendar sCalendar = Calendar.getInstance();
		int maxSendDateMinutes = maxSendDate.getMinutes();
		sCalendar.setTime(maxSendDate);
		if (maxSendDateMinutes % schQuantity != 0) {
			if (maxSendDateMinutes + schQuantity < 60) {
				int i = 0;
				do {
					maxSendDateMinutes++;
					i++;
				} while (maxSendDateMinutes % schQuantity != 0);
				sCalendar.add(Calendar.MINUTE, i);
				maxSendDate = sCalendar.getTime();
			} else {
				sCalendar.add(Calendar.MINUTE, -maxSendDateMinutes);
				sCalendar.add(Calendar.MINUTE, schQuantity);
				maxSendDate = sCalendar.getTime();
			}

		}
		return maxSendDate;
	}

	public static String dateToString(Date date, String format) {
		if (StringUtils.isNotEmpty(format)) {
			SimpleDateFormat f = new SimpleDateFormat(format);
			return f.format(date);
		} else {
			SimpleDateFormat f = new SimpleDateFormat(ConstantVar.DATE_FORMATE);
			return f.format(date);
		}
	}

	public static String dateToString(Date date) {
		return dateToString(date, null);
	}

	public static String datetimeToString(Date date, String format) {
		if (StringUtils.isNotEmpty(format)) {
			SimpleDateFormat f = new SimpleDateFormat(format);
			return f.format(date);
		} else {
			SimpleDateFormat f = new SimpleDateFormat(ConstantVar.DATETIME_FORMATE);
			return f.format(date);
		}
	}

	public static String datetimeToString(Date date) {
		return datetimeToString(date, null);
	}

	public static Date stringToDate(String date, String format) throws ParseException {
		if (StringUtils.isNotEmpty(format)) {
			SimpleDateFormat f = new SimpleDateFormat(format);
			return f.parse(date);
		} else {
			SimpleDateFormat f = new SimpleDateFormat(ConstantVar.DATE_FORMATE);
			return f.parse(date);
		}
	}

	public static Date stringToDate(String date) throws ParseException {
		return stringToDate(date, null);
	}

	public static Date stringToDatetime(String date, String format) throws ParseException {
		if (StringUtils.isNotEmpty(format)) {
			SimpleDateFormat f = new SimpleDateFormat(format);
			return f.parse(date);
		} else {
			SimpleDateFormat f = new SimpleDateFormat(ConstantVar.DATETIME_FORMATE);
			return f.parse(date);
		}
	}

	public static Date stringToDatetime(String date) throws ParseException {
		return stringToDatetime(date, null);
	}

	public static int daysBetween(String smdate, String bdate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(sdf.parse(smdate));
		long time1 = cal.getTimeInMillis();
		cal.setTime(sdf.parse(bdate));
		long time2 = cal.getTimeInMillis();
		long between_days = (time2 - time1) / (1000 * 3600 * 24);

		return Integer.parseInt(String.valueOf(between_days));
	}

	public static int daysBetween(Date smdate, Date bdate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		smdate = sdf.parse(sdf.format(smdate));
		bdate = sdf.parse(sdf.format(bdate));
		Calendar cal = Calendar.getInstance();
		cal.setTime(smdate);
		long time1 = cal.getTimeInMillis();
		cal.setTime(bdate);
		long time2 = cal.getTimeInMillis();
		long between_days = (time2 - time1) / (1000 * 3600 * 24);

		return Integer.parseInt(String.valueOf(between_days));
	}

	public static String getAvailDate(String DayOfWeek) throws ParseException {
		Calendar cal = new GregorianCalendar();
		Date today = new Date();
		cal.set(today.getYear(), today.getMonth(), today.getDay());
		Integer dayOfWeekToday = cal.get(Calendar.DAY_OF_WEEK);
		Integer temp = 0;
		switch (DayOfWeek) {
		case "Monday":
			temp = 1;
			break;
		case "Tuesday":
			temp = 2;
			break;
		case "Wednesday":
			temp = 3;
			break;
		case "Thursday":
			temp = 4;
			break;
		case "Friday":
			temp = 5;
			break;
		case "Saturday":
			temp = 6;
			break;
		case "Sunday":
			temp = 7;
			break;
		}
		if (temp == dayOfWeekToday) {
			return DateFormatUtil.datetimeToString(new Date(), "dd-MM-yyyy");
		} else {
			int betweenOfDay = dayOfWeekToday - temp;
			betweenOfDay = Math.abs(betweenOfDay);
			return DateFormatUtil.datetimeToString(addDate(new Date(), Long.valueOf(betweenOfDay)), "dd-MM-yyyy");
		}
	}

	public static Date addDate(Date d, long day) throws ParseException {

		long time = d.getTime();
		day = day * 24 * 60 * 60 * 1000;
		time += day;
		return new Date(time);
	}

	// 1 minute = 60 seconds
	// 1 hour = 60 x 60 = 3600
	// 1 day = 3600 x 24 = 86400
	public static long getDifference(Date startDate, Date endDate, String flag) {

		// milliseconds
		long different = endDate.getTime() - startDate.getTime();

		System.out.println("startDate : " + startDate);
		System.out.println("endDate : " + endDate);
		System.out.println("different : " + different);

		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;

		long elapsedDays = different / daysInMilli;
		different = different % daysInMilli;

		long elapsedHours = different / hoursInMilli;
		different = different % hoursInMilli;

		long elapsedMinutes = different / minutesInMilli;
		different = different % minutesInMilli;

		long elapsedSeconds = different / secondsInMilli;
		if (flag.equals("day")) {
			return elapsedDays;
		} else if (flag.equals("hour")) {
			return elapsedHours;
		} else if (flag.equals("minutes")) {
			return elapsedMinutes;
		} else if (flag.equals("seconds")) {
			return elapsedSeconds;
		} else {
			return elapsedSeconds;
		}
	}

	public static Long getCurrentUTCMilisecond(){
		return Calendar.getInstance().getTimeInMillis();
	}
}
