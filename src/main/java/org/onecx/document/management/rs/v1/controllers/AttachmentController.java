package org.onecx.document.management.rs.v1.controllers;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.onecx.document.management.rs.v1.exception.RestException;
import org.onecx.document.management.rs.v1.mappers.DocumentMapper;
import org.onecx.document.management.rs.v1.mappers.ExceptionMapper;
import org.onecx.document.management.rs.v1.services.AttachmentService;

import gen.org.onecx.document.management.rs.v1.AttachmentControllerV1Api;
import gen.org.onecx.document.management.rs.v1.model.AttachmentMetadataUploadDTO;
import gen.org.onecx.document.management.rs.v1.model.AttachmentStorageAuditRequestDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class AttachmentController implements AttachmentControllerV1Api {

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
