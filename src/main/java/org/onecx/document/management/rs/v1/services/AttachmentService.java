package org.onecx.document.management.rs.v1.services;

import java.util.List;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.onecx.document.management.domain.daos.AttachmentDAO;
import org.onecx.document.management.domain.daos.DocumentDAO;
import org.onecx.document.management.domain.daos.StorageUploadAuditDAO;
import org.onecx.document.management.domain.models.entities.Attachment;
import org.onecx.document.management.rs.v1.exception.RestException;
import org.onecx.document.management.rs.v1.exception.RestExceptionCode;
import org.onecx.document.management.rs.v1.mappers.DocumentMapper;

import gen.org.onecx.document.management.rs.v1.model.AttachmentMetadataUploadDTO;
import gen.org.onecx.document.management.rs.v1.model.AttachmentStorageAuditRequestDTO;

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
        final var attachment = attachmentDAO.findById(attachmentId);

        if (Objects.isNull(attachment)) {
            throw createNotFoundException(String.format(ATT_NOT_FOUND_MSG, attachmentId),
                    RestExceptionCode.ATTACHMENT_NOT_FOUND);
        }

        return attachment;
    }

    @Transactional
    public void updateAttachmentsMetadata(List<AttachmentMetadataUploadDTO> attachmentMetadataUploadDTO) {
        for (AttachmentMetadataUploadDTO dto : attachmentMetadataUploadDTO) {
            final var attachmentToUpdate = attachmentDAO.findById(dto.getAttachmentId());

            if (Objects.isNull(attachmentToUpdate)) {
                throw createNotFoundException(String.format(ATT_NOT_FOUND_MSG, dto.getAttachmentId()),
                        RestExceptionCode.ATTACHMENT_NOT_FOUND);
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
                throw createNotFoundException(msg, RestExceptionCode.DOCUMENT_NOT_FOUND);
            }
            if (Objects.isNull(attachment)) {
                var msg = String.format(ATT_NOT_FOUND_MSG, request.getAttachmentId());
                throw createNotFoundException(msg, RestExceptionCode.ATTACHMENT_NOT_FOUND);
            }

            final var audit = documentMapper.mapToStorageUploadAudit(request.getDocumentId(), document, attachment);
            uploadAuditDAO.create(audit);
        }
    }

    private RestException createNotFoundException(final String message, final RestExceptionCode code) {
        return new RestException(code, Response.Status.NOT_FOUND, message);
    }
}
