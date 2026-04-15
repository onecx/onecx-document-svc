package org.tkit.onecx.document.domain.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The DocumentRelationship entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DOCUMENT_RELATIONSHIP")
public class DocumentRelationship extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    /**
     * Type of the relationship.
     */
    @Column(name = "TYPE")
    private String type;
    /**
     * Id of the referenced document.
     */
    @Column(name = "DOCUMENT_REF_ID")
    private String documentRefId;

}
