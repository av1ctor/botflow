package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "documents_logs")
public class DocumentLog 
    extends WorkspaceLog
{
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Document document;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}