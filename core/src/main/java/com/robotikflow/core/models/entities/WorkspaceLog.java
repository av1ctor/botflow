package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;
import java.util.Map;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.filters.WorkspaceLogFilter;
import com.robotikflow.core.util.converters.MapStringObjectToJsonConverter;

@Entity
@Table(name = "workspaces_logs")
@Inheritance(strategy = InheritanceType.JOINED)
public class WorkspaceLog 
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Workspace workspace;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private WorkspaceLogType type;

    @NotNull
    private String message;

    @Convert(converter = MapStringObjectToJsonConverter.class)
    private Map<String, Object> extra;

    @NotNull
    private ZonedDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public WorkspaceLog() 
    {
    }

    public WorkspaceLog(
        final WorkspaceLogFilter filters) 
    {
        this.workspace = new Workspace(filters.workspace);
        this.id = filters.id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Workspace getWorspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public WorkspaceLogType getType() {
        return type;
    }

    public void setType(WorkspaceLogType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}