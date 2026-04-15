package org.tkit.onecx.document.domain.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.TenantId;
import org.tkit.onecx.document.domain.models.embeddable.TimePeriod;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The RelatedParty entity.
 */
@Getter
@Setter
@Entity
@Table(name = "RELATED_PARTY")
public class RelatedPartyRef extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    /**
     * Name of the related party.
     */
    @Column(name = "NAME")
    private String name;
    /**
     * Role of the related party.
     */
    @Column(name = "ROLE")
    private String role;
    /**
     * Validity period of the related party.
     */
    @Embedded
    private TimePeriod validFor;

}
