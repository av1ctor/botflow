package com.robotikflow.core.models.entities;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.util.converters.MapStringObjectToJsonConverter;

@Entity
@Table(name = "objects_states")
public class ObjState 
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Convert(converter = MapStringObjectToJsonConverter.class)
	private Map<String, Object> state = new HashMap<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Map<String, Object> getState() {
		return state;
	}

	public void setState(Map<String, Object> state) {
		this.state = state;
	}
}
