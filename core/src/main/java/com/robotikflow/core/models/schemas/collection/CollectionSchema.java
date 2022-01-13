package com.robotikflow.core.models.schemas.collection;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CollectionSchema 
{
	@NotNull
	private float version;
	@NotNull
	private Map<@NotBlank String, Field> columns;
	@Valid
	private Map<@NotBlank String, Ref> refs;
	@Valid
	private List<Index> indexes;
	@Valid
	private List<View> views;
	@Valid
	private List<Form> forms;
	@Valid
	private Auth auth;
	@Valid
	private List<Report> reports;
	@Valid
	private Map<@NotBlank String, Klass> classes;
	@Valid
	private Map<@NotBlank String, Const> constants;
	@Valid
	private Map<@NotBlank String, Flow> flows;

	public float getVersion() {
		return version;
	}

	public void setVersion(float version) {
		this.version = version;
	}

	public Map<String, Field> getColumns() {
		return columns;
	}

	public void setColumns(Map<String, Field> columns) {
		this.columns = columns;
	}

	public Map<String, Ref> getRefs() {
		return refs;
	}

	public void setRefs(Map<String, Ref> refs) {
		this.refs = refs;
	}

	public List<Index> getIndexes() {
		return indexes;
	}

	public void setIndexes(List<Index> indexes) {
		this.indexes = indexes;
	}

	public List<View> getViews() {
		return views;
	}

	public void setViews(List<View> views) {
		this.views = views;
	}

	public List<Form> getForms() {
		return forms;
	}

	public void setForms(List<Form> forms) {
		this.forms = forms;
	}

	public Auth getAuth() {
		return auth;
	}

	public void setAuth(Auth auth) {
		this.auth = auth;
	}

	public List<Report> getReports() {
		return reports;
	}

	public void setReports(List<Report> reports) {
		this.reports = reports;
	}

	public Map<String, Klass> getClasses() {
		return classes;
	}

	public void setClasses(Map<String, Klass> classes) {
		this.classes = classes;
	}

	public Map<String, Const> getConstants() {
		return constants;
	}

	public void setConstants(Map<String, Const> constants) {
		this.constants = constants;
	}

	public Map<String, Flow> getFlows() {
		return flows;
	}

	public void setFlows(Map<String, Flow> flows) {
		this.flows = flows;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((auth == null) ? 0 : auth.hashCode());
		result = prime * result + ((classes == null) ? 0 : classes.hashCode());
		result = prime * result + ((columns == null) ? 0 : columns.hashCode());
		result = prime * result + ((constants == null) ? 0 : constants.hashCode());
		result = prime * result + ((flows == null) ? 0 : flows.hashCode());
		result = prime * result + ((indexes == null) ? 0 : indexes.hashCode());
		result = prime * result + ((refs == null) ? 0 : refs.hashCode());
		result = prime * result + ((reports == null) ? 0 : reports.hashCode());
		result = prime * result + Float.floatToIntBits(version);
		result = prime * result + ((views == null) ? 0 : views.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CollectionSchema other = (CollectionSchema) obj;
		if (auth == null) {
			if (other.auth != null)
				return false;
		} else if (!auth.equals(other.auth))
			return false;
		if (classes == null) {
			if (other.classes != null)
				return false;
		} else if (!classes.equals(other.classes))
			return false;
		if (columns == null) {
			if (other.columns != null)
				return false;
		} else if (!columns.equals(other.columns))
			return false;
		if (constants == null) {
			if (other.constants != null)
				return false;
		} else if (!constants.equals(other.constants))
			return false;
		if (flows == null) {
			if (other.flows != null)
				return false;
		} else if (!flows.equals(other.flows))
			return false;
		if (indexes == null) {
			if (other.indexes != null)
				return false;
		} else if (!indexes.equals(other.indexes))
			return false;
		if (refs == null) {
			if (other.refs != null)
				return false;
		} else if (!refs.equals(other.refs))
			return false;
		if (reports == null) {
			if (other.reports != null)
				return false;
		} else if (!reports.equals(other.reports))
			return false;
		if (Float.floatToIntBits(version) != Float.floatToIntBits(other.version))
			return false;
		if (views == null) {
			if (other.views != null)
				return false;
		} else if (!views.equals(other.views))
			return false;
		return true;
	}
}
