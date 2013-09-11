package com.omf.resourcecontroller.generator;

import java.util.Calendar;
import java.util.TimeZone;

public class TimestampGenerator {
	static Calendar calendar;
	
	static {
		calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}
		
	public static long getTimestamp() {
		return (calendar.getTimeInMillis() + 500) / 1000;
	}
}
