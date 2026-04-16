package org.tkit.onecx.document.rs.internal.services;

import java.util.List;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.document.domain.daos.AttachmentDAO;
import org.tkit.onecx.document.domain.daos.DocumentDAO;
import org.tkit.onecx.document.domain.daos.StorageUploadAuditDAO;
import org.tkit.onecx.document.domain.models.entities.Attachment;
import org.tkit.onecx.document.rs.internal.exceptions.DocumentException;
import org.tkit.onecx.document.rs.internal.mappers.DocumentMapper;

import gen.org.tkit.onecx.document.rs.internal.model.AttachmentMetadataUploadDTO;
import gen.org.tkit.onecx.document.rs.internal.model.AttachmentStorageAuditRequestDTO;

@ApplicationScoped
public class AttachmentService {

    private final AttachmentDAO attachmentDAO;
    private final DocumentDAO documentDAO;
    private final StorageUploadAuditDAO uploadAuditDAO;
    private final DocumentMapper documentMapper;

    private static final String ATT_NOT_FOUND_MSG = "Attachment %s not found";
    private static final String DOC_NOT_FOUND_MSG = "Document %s not found";

    @Inject
    public AttachmentService(AttachmentDAO attachmentDAO, DocumentDAO documentDAO,
            StorageUploadAuditDAO storageUploadAuditDAO, DocumentMapper documentMapper) {
        this.attachmentDAO = attachmentDAO;
        this.documentDAO = documentDAO;
        this.uploadAuditDAO = storageUploadAuditDAO;
        this.documentMapper = documentMapper;
    }

    public Attachment getAttachmentDetails(final String attachmentId) {
        return attachmentDAO.findById(attachmentId);
    }

    @Transactional
    public void updateAttachmentsMetadata(List<AttachmentMetadataUploadDTO> attachmentMetadataUploadDTO) {
        for (AttachmentMetadataUploadDTO dto : attachmentMetadataUploadDTO) {
            final var attachmentToUpdate = attachmentDAO.findById(dto.getAttachmentId());

            if (Objects.isNull(attachmentToUpdate)) {
                throw createNotFoundException(String.format(ATT_NOT_FOUND_MSG, dto.getAttachmentId()));
            }

            final var updatedAttachment = documentMapper.updateAttachment(dto, attachmentToUpdate);
            updatedAttachment.setStorageUploadStatus(true);
            attachmentDAO.update(updatedAttachment);
        }
    }

    @Transactional
    public void createStorageAuditLogs(List<AttachmentStorageAuditRequestDTO> requests) {
        for (AttachmentStorageAuditRequestDTO request : requests) {
            final var document = documentDAO.findDocumentById(request.getDocumentId());
            final var attachment = attachmentDAO.findById(request.getAttachmentId());

            if (Objects.isNull(document)) {
                var msg = String.format(DOC_NOT_FOUND_MSG, request.getDocumentId());
                throw createNotFoundException(msg);
            }
            if (Objects.isNull(attachment)) {
                var msg = String.format(ATT_NOT_FOUND_MSG, request.getAttachmentId());
                throw createNotFoundException(msg);
            }

            final var audit = documentMapper.mapToStorageUploadAudit(request.getDocumentId(), document, attachment);
            uploadAuditDAO.create(audit);
        }
    }

    private DocumentException createNotFoundException(final String message) {
        return new DocumentException(Response.Status.NOT_FOUND, message);
    }
}
