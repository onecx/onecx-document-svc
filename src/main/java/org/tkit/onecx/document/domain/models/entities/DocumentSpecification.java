package org.tkit.onecx.document.domain.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The DocumentSpecification entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DOCUMENT_SPECIFICATION")
public class DocumentSpecification extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    /**
     * Name of the document specification.
     */
    @Column(name = "NAME")
    private String name;
    /**
     * Service specification version.
     */
    @Column(name = "VERSION")
    private String specificationVersion;

}
