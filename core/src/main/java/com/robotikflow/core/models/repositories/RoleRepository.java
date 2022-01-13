package com.robotikflow.core.models.repositories;

import java.time.ZonedDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.robotikflow.core.models.entities.Role;
import com.robotikflow.core.models.entities.RoleType;
import com.robotikflow.core.models.entities.User;

public interface RoleRepository 
	extends JpaRepository<Role, Long>
{
	@Query( "select" +
			"	p" +
			"	from Role p" +
			"	where p.name = :name" +
			"		and p.deletedBy is null")
	Role findByNome(
		RoleType name);
	
	@Query( "update" +
			"	Role p" +
			"	set p.deletedBy = :user," +
			"		p.deletedAt = :currentDate" +
			"	where p = :role")
	@Modifying(clearAutomatically = true)
	@Transactional
	void delete(
		Role role, 
		User user, 
		ZonedDateTime currentDate);

	@Query( "update" +
			"	Role p" +
			"	set p.deletedBy = :user," +
			"		p.deletedAt = :currentDate" +
			"	where p.id = :idRole")
	@Modifying(clearAutomatically = true)
	@Transactional
	void deletedById(
		Long idRole, 
		User user, 
		ZonedDateTime currentDate);
}
