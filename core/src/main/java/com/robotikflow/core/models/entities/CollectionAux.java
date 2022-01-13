package com.robotikflow.core.models.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "collections_auxs")
public class CollectionAux 
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private CollectionWithSchema principal;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    private CollectionWithSchema aux;

    @NotNull
    @Column(name = "\"order\"")
    private int order;

    public CollectionAux() {
    }
    
    public CollectionAux(CollectionAux aux) 
    {
        this.aux = aux.aux;
        this.principal = aux.principal;
        this.order = aux.order;
    }

    public CollectionAux(CollectionAux aux, CollectionWithSchema principal) 
    {
        this.aux = aux.aux;
        this.principal = principal;
        this.order = aux.order;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CollectionWithSchema getPrincipal() {
        return principal;
    }

    public void setPrincipal(CollectionWithSchema principal) {
        this.principal = principal;
    }

    public CollectionWithSchema getAux() {
        return aux;
    }

    public void setAux(CollectionWithSchema aux) {
        this.aux = aux;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}