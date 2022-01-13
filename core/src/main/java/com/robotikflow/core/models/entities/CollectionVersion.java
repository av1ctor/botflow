package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.util.IdUtil;

@Entity
@Table(name = "collections_versions")
public class CollectionVersion 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String pubId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Collection collection;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    private CollectionVersionChange change;

    @NotNull
    private String diff;

    @NotNull
    private ZonedDateTime createdAt;

    public CollectionVersion() 
    {
        this.pubId = IdUtil.genId();
    }

    public CollectionVersion(Collection collection, String diff) 
    {
        this();
        this.collection = collection;
        this.diff = diff;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPubId() {
        return pubId;
    }

    public void setPubId(String pubId) {
        this.pubId = pubId;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public CollectionVersionChange getChange() {
        return change;
    }

    public void setChange(CollectionVersionChange change) {
        this.change = change;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }
}