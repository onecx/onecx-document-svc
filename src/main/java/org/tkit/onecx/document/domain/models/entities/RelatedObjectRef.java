package org.tkit.onecx.document.domain.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The RelatedObject entity.
 */
@Getter
@Setter
@Entity
@Table(name = "RELATED_OBJECT")
public class RelatedObjectRef extends TraceableEntity {
    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    /**
     * Describes the involvement to the related object.
     */
    @Column(name = "INVOLVEMENT")
    private String involvement;
    /**
     * Type of the related object .
     */
    @Column(name = "RO_TYPE")
    private String objectReferenceType;
    /**
     * Id of the related object.
     */
    @Column(name = "RO_ID")
    private String objectReferenceId;

}
