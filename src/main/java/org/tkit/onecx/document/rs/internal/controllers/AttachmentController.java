package org.tkit.onecx.document.rs.internal.controllers;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.document.rs.internal.exception.RestException;
import org.tkit.onecx.document.rs.internal.mappers.DocumentMapper;
import org.tkit.onecx.document.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.document.rs.internal.services.AttachmentService;

import gen.org.tkit.onecx.document.rs.internal.AttachmentControllerApi;
import gen.org.tkit.onecx.document.rs.internal.model.AttachmentMetadataUploadDTO;
import gen.org.tkit.onecx.document.rs.internal.model.AttachmentStorageAuditRequestDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class AttachmentController implements AttachmentControllerApi {

    @Inject
    AttachmentService attachmentService;

    @Inject
    DocumentMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response getAttachmentDetails(String attachmentId) {
        final var attachment = attachmentService.getAttachmentDetails(attachmentId);
        return Response.ok(mapper.mapAttachment(attachment)).build();
    }

    @Override
    public Response uploadAttachmentsMetadata(List<AttachmentMetadataUploadDTO> attachmentMetadataUploadDTOs) {
        attachmentService.updateAttachmentsMetadata(attachmentMetadataUploadDTOs);
        return Response.ok().build();
    }

    @Override
    public Response createStorageAuditsForAttachments(
            List<AttachmentStorageAuditRequestDTO> attachmentStorageAuditRequestDTOs) {
        attachmentService.createStorageAuditLogs(attachmentStorageAuditRequestDTOs);
        return Response.status(Response.Status.CREATED).build();
    }

    @ServerExceptionMapper(priority = 1)
    public Response handleRestException(RestException exception) {
        return Response.status(exception.getStatus())
                .entity(exceptionMapper.map(exception))
                .build();
    }
}
