package com.css.system.utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.css.system.annotation.Static;

@Static
public final class DateUtility{
	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	public static final DateFormat dateTimeWithMilliFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

	/**
	 * Static
	 */
	private DateUtility(){
	}
	// current
	public static int getYear(){
		return getYear(Calendar.getInstance());
	}
	public static int getMonth(){
		return getMonth(Calendar.getInstance());
	}
	public static int getDay(){
		return getDay(Calendar.getInstance());
	}
	// by calendar
	public static int getYear(final Calendar c){
		return (c == null ? getYear() : c.get(Calendar.YEAR));
	}
	public static int getMonth(final Calendar c){
		return (c == null ? getMonth() : c.get(Calendar.MONTH) + 1);
	}
	public static int getDay(final Calendar c){
		return (c == null ? getDay() : c.get(Calendar.DAY_OF_MONTH));
	}
	// to string
	public static String getYear(final int year, final Character padding){
		return (padding == null ? String.valueOf(year) : StringUtils.leftPad(String.valueOf(year), 4, padding));
	}
	public static String getMonth(final int month, final Character padding){
		return (padding == null ? String.valueOf(month) : StringUtils.leftPad(String.valueOf(month), 2, padding));
	}
	public static String getDay(final int day, final Character padding){
		return (padding == null ? String.valueOf(day) : StringUtils.leftPad(String.valueOf(day), 2, padding));
	}
	// to calendar
	public static Calendar getCalendar(final Date date){
		if(date == null){
			return null;
		}
		final Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c;
	}
	public static Calendar getCalendar(final long milli){
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(milli);
		return c;
	}
	// to date
	public static Date getDate(final Calendar c){
		return (c == null ? null : c.getTime());
	}
	public static Date getDate(final long milli){
		return getCalendar(milli).getTime();
	}
	// to long
	public static Long getMilli(final Calendar c){
		return (c == null ? null : c.getTimeInMillis());
	}
	public static Long getMilli(final Date date){
		return (date == null ? null : getCalendar(date).getTimeInMillis());
	}
	// if year-month-day is valid
	public static boolean isValidDate(final int year, final int month, final int day){
		final Calendar c = getCalendar(year, month, day);
		return year == getYear(c) && month == getMonth(c) && day == getDay(c);
	}
	// construct calendar/date/long from date
	public static Calendar getCalendar(final int year, final int month, final int day){
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0);
		c.set(year, month - 1, day);
		return c;
	}
	public static Date getDate(final int year, final int month, final int day){
		return getCalendar(year, month, day).getTime();
	}
	public static long getMilli(final int year, final int month, final int day){
		return getCalendar(year, month, day).getTimeInMillis();
	}
	// construct calendar/date/long from string
	public static Calendar getCalendar(final String input){
		final String tmp = StringUtils.trimToEmpty(input);
		int sepPos1 = -1;
		int sepPos2 = -1;
		int sepEnd = -1;
		for(int i = 0; i < tmp.length(); i++){
			if(StringUtility.isNumeric(tmp.charAt(i)) == false){
				if(sepPos1 == -1){
					sepPos1 = i;
				}else if(sepPos2 == -1){
					sepPos2 = i;
				}else if(sepEnd == -1){
					sepEnd = i;
					break;
				}
			}
		}
		if(sepPos1 == -1){
			// no separator
			if(tmp.length() < 8){
				return null;
			}
			// yyyymmdd
			final int year1 = Integer.parseInt(tmp.substring(0, 4));
			final int month1 = Integer.parseInt(tmp.substring(4, 6));
			final int day1 = Integer.parseInt(tmp.substring(6, 8));
			final boolean isDate1 = isValidDate(year1, month1, day1);
			// ddmmyyyy
			final int year2 = Integer.parseInt(tmp.substring(4, 8));
			final int month2 = Integer.parseInt(tmp.substring(2, 4));
			final int day2 = Integer.parseInt(tmp.substring(0, 2));
			final boolean isDate2 = isValidDate(year2, month2, day2);
			if(isDate1){
				if(isDate2){
					if(Math.abs(year1 - getYear()) > Math.abs(year2 - getYear())){
						return getCalendar(year2, month2, day2);
					}else{
						return getCalendar(year1, month1, day1);
					}
				}else{
					return getCalendar(year1, month1, day1);
				}
			}else{
				if(isDate2){
					return getCalendar(year2, month2, day2);
				}else{
					return null;
				}
			}
		}else{
			// has separator
			if(sepPos2 == -1){
				return null;
			}else if(sepEnd == -1){
				sepEnd = tmp.length();
			}
			final int first = Integer.parseInt(tmp.substring(0, sepPos1));
			final int second = Integer.parseInt(tmp.substring(sepPos1 + 1, sepPos2));
			final int third = Integer.parseInt(tmp.substring(sepPos2 + 1, sepEnd));
			// yyyymmdd
			final boolean isDate1 = isValidDate(first, second, third);
			// ddmmyyyy
			final boolean isDate2 = isValidDate(third, second, first);
			if(isDate1){
				if(isDate2){
					if(Math.abs(first - getYear()) > Math.abs(third - getYear())){
						return getCalendar(third, second, first);
					}else{
						return getCalendar(first, second, third);
					}
				}else{
					return getCalendar(first, second, third);
				}
			}else{
				if(isDate2){
					return getCalendar(third, second, first);
				}else{
					return null;
				}
			}
		}
	}
	// compare days
	public static int compareUptoDay(final Calendar c1, final Calendar c2){
		final long t1 = c1.getTimeInMillis() / 86400000;
		final long t2 = c2.getTimeInMillis() / 86400000;
		if(t1 > t2){
			return 1;
		}else if(t1 < t2){
			return -1;
		}
		return 0;
	}
	public static int compareUptoDay(final Date date1, final Date date2){
		final long t1 = date1.getTime() / 86400000;
		final long t2 = date2.getTime() / 86400000;
		if(t1 > t2){
			return 1;
		}else if(t1 < t2){
			return -1;
		}
		return 0;
	}
	public static int compareUptoDay(final long milli1, final long milli2){
		final long t1 = milli1 / 86400000;
		final long t2 = milli2 / 86400000;
		if(t1 > t2){
			return 1;
		}else if(t1 < t2){
			return -1;
		}
		return 0;
	}
}
