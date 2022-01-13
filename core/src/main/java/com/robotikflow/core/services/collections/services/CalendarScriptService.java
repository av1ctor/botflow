package com.robotikflow.core.services.collections.services;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

public class CalendarScriptService
{
	public DateScriptService from(Date valor)
	{
		return new DateScriptService(ZonedDateTime.ofInstant(valor.toInstant(), ZoneId.systemDefault()));
	}
	
	public DateScriptService from(Date valor, String timeZone)
	{
		return new DateScriptService(ZonedDateTime.ofInstant(valor.toInstant(), ZoneId.of(timeZone)));
	}

	public DateScriptService from(ZonedDateTime valor)
	{
		return new DateScriptService(valor);
	}
	
	public DateScriptService from(String valor)
	{
		return new DateScriptService(ZonedDateTime.parse(valor));
	}

	public DateScriptService from(Integer ano)
	{
		return from(ano, 1, 1, 0, 0, 0);
	}

	public DateScriptService from(Long ano)
	{
		return from(ano.intValue());
	}

	public DateScriptService from(Integer ano, Integer mes)
	{
		return from(ano, mes, 1, 0, 0, 0);
	}
	
	public DateScriptService from(Long ano, Long mes)
	{
		return from(ano.intValue(), mes.intValue());
	}
	
	public DateScriptService from(Integer ano, Integer mes, Integer dia)
	{
		return from(ano, mes, dia, 0, 0, 0);
	}
	
	public DateScriptService from(Long ano, Long mes, Long dia)
	{
		return from(ano.intValue(), mes.intValue(), dia.intValue());
	}
	
	public DateScriptService from(Integer ano, Integer mes, Integer dia, Integer hora, Integer minuto, Integer segundo)
	{
		return new DateScriptService(
			ZonedDateTime.of(
				ano.intValue(), mes.intValue(), dia.intValue(), 
				hora.intValue(), minuto.intValue(), segundo.intValue(), 0, 
				ZoneId.systemDefault()));
	}

	public DateScriptService from(Long ano, Long mes, Long dia, Long hora, Long minuto, Long segundo)
	{
		return from(ano.intValue(), mes.intValue(), dia.intValue(), hora.intValue(), minuto.intValue(), segundo.intValue());
	}

	public Long diff(String unit, DateScriptService d1, DateScriptService d2)
	{
		var duration = Duration.between(d1.toIso(), d2.toIso());
		
		switch(unit.charAt(0))
		{
		case 's':
			return duration.toSeconds();
		case 'm':
			return duration.toMinutes();
		case 'h':
			return duration.toHours();
		case 'd':
			return duration.toDays();
		}
		
		return 0L;
	}

	private ZonedDateTime _now()
	{
		return Instant.now().atZone(ZoneOffset.systemDefault());
	}
	
	private ZonedDateTime _now(String timeZone)
	{
		return Instant.now().atZone(ZoneId.of(timeZone));
	}
	
	public DateScriptService now()
	{
		return new DateScriptService(_now());
	}
	
	public DateScriptService now(String timeZone)
	{
		return new DateScriptService(_now(timeZone));
	}

	public Long year()
	{
		return (long)_now().getYear();
	}

	public Long year(String timeZone)
	{
		return (long)_now(timeZone).getYear();
	}
	
	public String year2()
	{
		return String.format("%02d", year() % 100);
	}

	public String year2(String timeZone)
	{
		return String.format("%02d", year(timeZone) % 100);
	}

	public String year4()
	{
		return String.format("%04d", year());
	}

	public String year4(String timeZone)
	{
		return String.format("%04d", year(timeZone));
	}

	public Long month()
	{
		return (long)_now().getMonthValue();
	}

	public Long month(String timeZone)
	{
		return (long)_now(timeZone).getMonthValue();
	}

	public String month2()
	{
		return String.format("%02d", month());
	}

	public String month2(String timeZone)
	{
		return String.format("%02d", month(timeZone));
	}

	public Long day()
	{
		return (long)_now().getDayOfMonth();
	}

	public Long day(String timeZone)
	{
		return (long)_now(timeZone).getDayOfMonth();
	}

	public String day2()
	{
		return String.format("%02d", day());
	}

	public String day2(String timeZone)
	{
		return String.format("%02d", day(timeZone));
	}
}
