package org.tkit.onecx.document.rs.internal.controllers;

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
import org.tkit.quarkus.jpa.exceptions.ConstraintException;

import gen.org.tkit.onecx.document.rs.internal.DocumentTypeControllerApi;
import gen.org.tkit.onecx.document.rs.internal.model.DocumentTypeCreateUpdateDTO;
import gen.org.tkit.onecx.document.rs.internal.model.ProblemDetailResponseDTO;

@ApplicationScoped
public class DocumentTypeController implements DocumentTypeControllerApi {

    @Inject
    DocumentTypeDAO documentTypeDAO;

    @Inject
    DocumentTypeMapper documentTypeMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    @Transactional
    public Response createDocumentType(DocumentTypeCreateUpdateDTO documentTypeCreateUpdateDTO) {
        var documentType = documentTypeDAO.create(documentTypeMapper.map(documentTypeCreateUpdateDTO));
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
    @Transactional
    public Response deleteDocumentTypeById(String id) {
        var documentType = documentTypeDAO.findById(id);
        if (Objects.nonNull(documentType)) {
            documentTypeDAO.delete(documentType);
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    @Transactional
    public Response updateDocumentTypeById(String id, DocumentTypeCreateUpdateDTO documentTypeCreateUpdateDTO) {
        var documentType = documentTypeDAO.findById(id);
        if (Objects.isNull(documentType)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        documentTypeMapper.update(documentTypeCreateUpdateDTO, documentType);
        return Response.status(Response.Status.CREATED)
                .entity(documentTypeMapper.mapDocumentType(documentTypeDAO.update(documentType)))
                .build();
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
