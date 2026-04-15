package org.tkit.onecx.document.domain.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The Channel entity.
 */
@Getter
@Setter
@Entity
@Table(name = "CHANNEL")
public class Channel extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    /**
     * Name of the channel.
     */
    @Column(name = "NAME")
    private String name;

}
