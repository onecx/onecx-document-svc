package org.tkit.onecx.document.domain.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The Category entity.
 */
@Getter
@Setter
@Entity
@Table(name = "CATEGORY")
public class Category extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    /**
     * Name of the category.
     */
    @Column(name = "NAME")
    private String name;
    /**
     * Version of the category.
     */
    @Column(name = "VERSION")
    private String categoryVersion;

}
