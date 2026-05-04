package org.tkit.onecx.document.domain.models.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.TenantId;
import org.tkit.onecx.document.domain.models.embeddable.TimePeriod;
import org.tkit.onecx.document.domain.models.enums.AttachmentUnit;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The Attachment entity.
 */
@Getter
@Setter
@Entity
@Table(name = "ATTACHMENT")
public class Attachment extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;
    /**
     * Name of the attachment.
     */
    @Column(name = "NAME")
    private String name;
    /**
     * Description of the attachment.
     */
    @Column(name = "DESCRIPTION")
    private String description;
    /**
     * Type of the attachment.
     */
    @Column(name = "TYPE")
    private String type;
    /**
     * Size of teh attachment.
     */
    private BigDecimal size;
    /**
     * Size unit of the attachment.
     */
    @Column(name = "SIZE_UNIT")
    @Enumerated(EnumType.STRING)
    private AttachmentUnit sizeUnit;
    /**
     * Validity period of the related party.
     */
    @Embedded
    private TimePeriod validFor;

    private String mimeType;

    @Transient
    private String file;

    /**
     * Original name of the attached file.
     */
    @Column(name = "FILENAME")
    private String fileName;

    @Column(name = "STORAGE_UPLOAD_STATUS")
    private Boolean storageUploadStatus;

}
