package org.tkit.onecx.document.domain.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The DocumentCharacteristic entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DOCUMENT_CHARACTERISTIC")
public class DocumentCharacteristic extends TraceableEntity {
    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    /**
     * Name of the characteristic.
     */
    @Column(name = "NAME")
    private String name;
    /**
     * Value of the characteristic.
     */
    @Column(name = "VALUE")
    private String value;

}
