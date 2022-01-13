package com.robotikflow.core.services;

import java.util.List;

import com.robotikflow.core.models.entities.Timezone;
import com.robotikflow.core.models.repositories.IconRepository;
import com.robotikflow.core.models.repositories.TimezoneRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MiscService 
{
	@Autowired
	private IconRepository iconRepo;
	
	@Autowired
	private TimezoneRepository timezoneRepo;
	
	/**
	 * 
	 * @param pageable
	 * @return
	 */
	public List<String> findAllIcons(
		final Pageable pageable) 
	{
		return iconRepo.findAll_(pageable);
	}

	/**
	 * 
	 * @param pageable
	 * @return
	 */
	public List<Timezone> findAllTimezones(
		final Pageable pageable) 
	{
		return timezoneRepo.findAll_(pageable);
	}
}
