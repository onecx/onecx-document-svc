package org.tkit.onecx.document.rs.internal.controllers;

import static jakarta.transaction.Transactional.TxType.NOT_SUPPORTED;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.document.domain.daos.DocumentTypeDAO;
import org.tkit.onecx.document.rs.internal.mappers.DocumentTypeMapper;
import org.tkit.onecx.document.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.document.rs.internal.services.DocumentTypeService;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;

import gen.org.tkit.onecx.document.rs.internal.DocumentTypeControllerApi;
import gen.org.tkit.onecx.document.rs.internal.model.DocumentTypeCreateUpdateDTO;
import gen.org.tkit.onecx.document.rs.internal.model.ProblemDetailResponseDTO;

@ApplicationScoped
@Transactional(value = NOT_SUPPORTED)
public class DocumentTypeController implements DocumentTypeControllerApi {
    @Inject
    DocumentTypeDAO documentTypeDAO;
    @Inject
    DocumentTypeMapper documentTypeMapper;
    @Inject
    DocumentTypeService documentTypeService;
    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response createDocumentType(DocumentTypeCreateUpdateDTO documentTypeCreateUpdateDTO) {
        var documentType = documentTypeService.createDocumentType(documentTypeCreateUpdateDTO);
        return Response.status(Response.Status.CREATED)
                .entity(documentTypeMapper.mapDocumentType(documentType))
                .build();
    }

    @Override
    public Response getDocumentTypeById(String id) {
        var documentType = documentTypeDAO.findById(id);
        if (Objects.isNull(documentType)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK)
                .entity(documentTypeMapper.mapDocumentType(documentType))
                .build();
    }

    @Override
    public Response getAllTypesOfDocument() {
        return Response.status(Response.Status.OK)
                .entity(documentTypeMapper.findAllDocumentType(
                        documentTypeDAO.findAllAsList()))
                .build();
    }

    @Override
    public Response deleteDocumentTypeById(String id) {
        boolean deleted = documentTypeService.deleteDocumentTypeById(id);
        if (deleted) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response updateDocumentTypeById(String id, DocumentTypeCreateUpdateDTO documentTypeCreateUpdateDTO) {
        var documentType = documentTypeService.updateDocumentTypeById(id, documentTypeCreateUpdateDTO);
        if (Objects.isNull(documentType)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
