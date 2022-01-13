package com.robotikflow.core.models.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "collections_versions_changes")
public class CollectionVersionChange 
{
    @Id
    @Enumerated(EnumType.ORDINAL)
    private CollectionVersionChangeId id;

    @NotNull
    @Column(name = "\"desc\"")
    private String desc;

    public CollectionVersionChangeId getId() {
        return id;
    }

    public void setId(CollectionVersionChangeId id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
