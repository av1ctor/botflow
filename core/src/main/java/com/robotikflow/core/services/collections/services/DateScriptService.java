package com.robotikflow.core.services.collections.services;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateScriptService
{
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	private ZonedDateTime date;
	
	public DateScriptService()
	{
	}
	
	public DateScriptService(ZonedDateTime date)
	{
		this.date = date;
	}

	public DateScriptService from(ZonedDateTime date)
	{
		this.date = date;
		return this;
	}
	
	public DateScriptService from(String text)
	{
		date = ZonedDateTime.parse(text);
		return this;
	}
	
	public DateScriptService from(Integer ms)
	{
		return from(ms.longValue());
	}

	public DateScriptService from(Long ms)
	{
		date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneOffset.systemDefault());
		return this;
	}

	public DateScriptService from(Double ms)
	{
		return from(ms.longValue());
	}

	public String toISOString()
	{
		return date.toOffsetDateTime().format(formatter);
	}
	
	public Long toMs()
	{
		return date.toInstant().toEpochMilli();
	}
	
	public Date toDate()
	{
		return Date.from(date.toInstant());
	}

	public ZonedDateTime toIso()
	{
		return date;
	}

	public String format(String pattern)
	{
		return date.toOffsetDateTime().format(DateTimeFormatter.ofPattern(pattern));
	}
	
	public DateScriptService setDate(Integer day, Integer month)
	{
		date = date.withMonth(month.intValue()).withDayOfMonth(day.intValue());
		return this;
	}

	public DateScriptService setDate(Long day, Long month)
	{
		return setDate(day.intValue(), month.intValue());
	}

	public DateScriptService setDay(Integer day)
	{
		date = date.withDayOfMonth(day.intValue());
		return this;
	}

	public DateScriptService setDay(Long day)
	{
		return setDay(day.intValue());
	}

	public DateScriptService setTime(Integer hour, Integer minute, Integer second)
	{
		date = date.withHour(hour.intValue()).withMinute(minute.intValue()).withSecond(second.intValue());
		return this;
	}
	
	public DateScriptService setTime(Long hour, Long minute, Long second)
	{
		return setTime(hour.intValue(), minute.intValue(), second.intValue());
	}

	public DateScriptService plus(Integer seconds)
	{
		return plus(seconds.longValue());
	}
	
	public DateScriptService plus(Long seconds)
	{
		date = date.plusSeconds(seconds);
		return this;
	}

	public DateScriptService plus(Double seconds)
	{
		return plus(seconds.longValue());
	}

	public DateScriptService plusMinutes(Integer minutes)
	{
		return plusMinutes(minutes.longValue());
	}

	public DateScriptService plusMinutes(Long minutes)
	{
		date = date.plusMinutes(minutes);
		return this;
	}

	public DateScriptService plusMinutes(Double minutes)
	{
		return plusMinutes(minutes.longValue());
	}

	public DateScriptService plusHours(Integer hours)
	{
		return plusHours(hours.longValue());
	}

	public DateScriptService plusHours(Long hours)
	{
		date = date.plusHours(hours);
		return this;
	}

	public DateScriptService plusHours(Double hours)
	{
		return plusHours(hours.longValue());
	}

	public DateScriptService plusDays(Integer days)
	{
		return plusDays(days.longValue());
	}

	public DateScriptService plusDays(Long days)
	{
		date = date.plusDays(days);
		return this;
	}

	public DateScriptService plusDays(Double days)
	{
		return plusDays(days.longValue());
	}

	public DateScriptService atStartOfDay(ZonedDateTime date)
	{
		return new DateScriptService(date.withHour(0));
	}

	public DateScriptService atStartOfDay()
	{
		return atStartOfDay(date);
	}

	public DateScriptService atEndOfDay(ZonedDateTime date)
	{
		return new DateScriptService(date.withHour(23).withMinute(59).withSecond(59));
	}

	public DateScriptService atEndOfDay()
	{
		return atEndOfDay(date);
	}

	public DateScriptService atStartOfMonth(ZonedDateTime date)
	{
		return atStartOfDay(date.withDayOfMonth(1));
	}

	public DateScriptService atStartOfMonth()
	{
		return atStartOfMonth(date);
	}

	public DateScriptService atEndOfMonth(ZonedDateTime date)
	{
		return atEndOfDay(date.plusMonths(1).withDayOfMonth(1).minusDays(1));
	}

	public DateScriptService atEndOfMonth()
	{
		return atEndOfMonth(date);
	}

	public DateScriptService atStartOfYear(ZonedDateTime date)
	{
		return atStartOfMonth(date.withMonth(1));
	}

	public DateScriptService atStartOfYear()
	{
		return atStartOfYear(date);
	}

	public DateScriptService atEndOfYear(ZonedDateTime date)
	{
		return atEndOfMonth(date.withMonth(12));
	}

	public DateScriptService atEndOfYear()
	{
		return atEndOfYear(date);
	}

	public Boolean isDue()
	{
		return date.isAfter(ZonedDateTime.now());
	}

	public Boolean isBefore(DateScriptService other)
	{
		return date.isBefore(other.date);
	}

	public Boolean isAfter(DateScriptService other)
	{
		return date.isAfter(other.date);
	}
}

