package org.onecx.document.management.rs.v1.services;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.onecx.document.management.domain.daos.DocumentDAO;
import org.onecx.document.management.domain.daos.DocumentTypeDAO;
import org.onecx.document.management.domain.daos.SupportedMimeTypeDAO;
import org.onecx.document.management.domain.models.entities.Attachment;
import org.onecx.document.management.domain.models.entities.Document;
import org.onecx.document.management.domain.models.entities.SupportedMimeType;
import org.onecx.document.management.rs.v1.exception.RestException;
import org.onecx.document.management.rs.v1.mappers.DocumentMapper;
import org.onecx.document.management.rs.v1.mappers.DocumentSpecificationMapper;

import gen.org.onecx.document.management.rs.v1.model.AttachmentCreateUpdateDTO;
import gen.org.onecx.document.management.rs.v1.model.DocumentCreateUpdateDTO;

@ApplicationScoped
public class DocumentService {

    @Inject
    DocumentDAO documentDAO;

    @Inject
    DocumentMapper documentMapper;

    @Inject
    DocumentTypeDAO typeDAO;

    @Inject
    DocumentSpecificationMapper documentSpecificationMapper;

    @Inject
    SupportedMimeTypeDAO mimeTypeDAO;

    public Document createDocument(DocumentCreateUpdateDTO dto) {
        var document = documentMapper.map(dto);
        setType(dto, document);
        setSpecification(dto, document);
        setAttachments(dto, document);
        return documentDAO.create(document);
    }

    @Transactional
    public Document updateDocument(Document document, DocumentCreateUpdateDTO dto) {
        documentMapper.update(dto, document);
        setType(dto, document);
        setSpecification(dto, document);
        updateChannelInDocument(document, dto);
        updateRelatedObjectRefInDocument(document, dto);
        documentMapper.updateTraceableCollectionsInDocument(document, dto);
        updateAttachmentsInDocument(document, dto);
        return document;
    }

    private void setType(DocumentCreateUpdateDTO dto, Document document) {
        var documentType = typeDAO.findById(dto.getTypeId());
        if (Objects.isNull(documentType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getDocumentNotFoundMsg(dto.getTypeId()));
        }
        document.setType(documentType);
    }

    /**
     * Finds the specification {@link DocumentSpecification} by id and sets the
     * specification
     * in the document entity {@link Document}.
     *
     * @param dto a {@link DocumentCreateUpdateDTO}
     * @param document a {@link Document}
     */
    private void setSpecification(DocumentCreateUpdateDTO dto, Document document) {
        if (Objects.isNull(dto.getSpecification())) {
            document.setSpecification(null);
        } else {
            var docSpecDto = dto.getSpecification();
            var documentSpecification = documentSpecificationMapper.map(docSpecDto);
            document.setSpecification(documentSpecification);
        }
    }

    /**
     * Finds the {@link SupportedMimeType} by the given id.
     *
     * @param dto a {@link AttachmentCreateUpdateDTO}
     * @return a {@link SupportedMimeType}
     *         or it throws an error if it can't find a {@link SupportedMimeType}
     *         given id.
     */
    private SupportedMimeType getSupportedMimeType(AttachmentCreateUpdateDTO dto) {
        SupportedMimeType mimeType = mimeTypeDAO.findById(dto.getMimeTypeId());
        if (Objects.isNull(mimeType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getSupportedMimeTypeNotFoundMsg(dto.getMimeTypeId()));
        }
        return mimeType;
    }

    /**
     * Finds attachment's mimeType {@link SupportedMimeType} by id
     * and sets it in the attachment entity {@link Attachment}, then add
     * {@link Attachment}
     * in document entity {@link Document}.
     *
     * @param dto a {@link DocumentCreateUpdateDTO}
     * @param document a {@link Document}
     */
    private void setAttachments(DocumentCreateUpdateDTO dto, Document document) {
        if (Objects.isNull(dto.getAttachments())) {
            document.setAttachments(null);
        } else {
            for (AttachmentCreateUpdateDTO attachmentDTO : dto.getAttachments()) {
                if (Objects.isNull(attachmentDTO.getId()) || attachmentDTO.getId().isEmpty()) {
                    var mimeType = getSupportedMimeType(attachmentDTO);
                    var attachment = documentMapper.mapAttachment(attachmentDTO);
                    attachment.setMimeType(mimeType);
                    attachment.setStorageUploadStatus(false);
                    document.getAttachments().add(attachment);
                }
            }
        }
    }

    /**
     * Updates {@link Channel} in {@link Document} or creates new {@link Channel}
     * and sets in {@link Document}.
     *
     * @param document a {@link Document}
     * @param updateDTO a {@link DocumentCreateUpdateDTO}
     */
    private void updateChannelInDocument(Document document, DocumentCreateUpdateDTO updateDTO) {
        if (Objects.isNull(updateDTO.getChannel().getId()) || updateDTO.getChannel().getId().isEmpty()) {
            var channel = documentMapper.mapChannel(updateDTO.getChannel());
            document.setChannel(channel);
        } else {
            documentMapper.updateChannel(updateDTO.getChannel(), document.getChannel());
        }
    }

    private void updateRelatedObjectRefInDocument(Document document, DocumentCreateUpdateDTO updateDTO) {
        if (Objects.isNull(updateDTO.getRelatedObject())) {
            document.setRelatedObject(null);
        } else if (Objects.isNull(updateDTO.getRelatedObject().getId())
                || updateDTO.getRelatedObject().getId().isEmpty()) {
            var relatedObjectRef = documentMapper.mapRelatedObjectRef(updateDTO.getRelatedObject());
            document.setRelatedObject(relatedObjectRef);
        } else {
            documentMapper.updateRelatedObjectRef(updateDTO.getRelatedObject(), document.getRelatedObject());
        }
    }

    private void updateAttachmentsInDocument(Document document, DocumentCreateUpdateDTO updateDTO) {
        if (Objects.nonNull(updateDTO.getAttachments())) {
            for (Iterator<Attachment> i = document.getAttachments().iterator(); i.hasNext();) {
                Attachment entity = i.next();
                Optional<AttachmentCreateUpdateDTO> dtoOptional = updateDTO.getAttachments().stream()
                        .filter(dto -> dto.getId() != null)
                        .filter(dto -> entity.getId().equals(dto.getId()))
                        .findFirst();
                if (dtoOptional.isPresent()) {
                    var mimeType = getSupportedMimeType(dtoOptional.get());
                    documentMapper.updateAttachment(dtoOptional.get(), entity);
                    entity.setMimeType(mimeType);
                }
            }
            setAttachments(updateDTO, document);
        }
    }

    private String getDocumentNotFoundMsg(String documentId) {
        return String.format("The document with ID %s was not found.", documentId);
    }

    private String getSupportedMimeTypeNotFoundMsg(String mimeTypeId) {
        return String.format("The supported mime type with ID %s was not found.", mimeTypeId);
    }
}
