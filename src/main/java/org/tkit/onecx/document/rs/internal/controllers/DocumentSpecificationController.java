package org.tkit.onecx.document.rs.internal.controllers;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.document.domain.daos.DocumentSpecificationDAO;
import org.tkit.onecx.document.rs.internal.mappers.DocumentSpecificationMapper;
import org.tkit.onecx.document.rs.internal.mappers.ExceptionMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;

import gen.org.tkit.onecx.document.rs.internal.DocumentSpecificationControllerApi;
import gen.org.tkit.onecx.document.rs.internal.model.DocumentSpecificationCreateUpdateDTO;
import gen.org.tkit.onecx.document.rs.internal.model.ProblemDetailResponseDTO;

@ApplicationScoped
public class DocumentSpecificationController implements DocumentSpecificationControllerApi {

    @Inject
    DocumentSpecificationDAO documentSpecificationDAO;

    @Inject
    DocumentSpecificationMapper documentSpecificationMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    @Transactional
    public Response createDocumentSpecification(DocumentSpecificationCreateUpdateDTO documentSpecificationCreateUpdateDTO) {
        var documentSpecification = documentSpecificationDAO
                .create(documentSpecificationMapper.map(documentSpecificationCreateUpdateDTO));
        return Response.status(Response.Status.CREATED)
                .entity(documentSpecificationMapper.mapToDTO(documentSpecification))
                .build();
    }

    @Override
    public Response getDocumentSpecificationById(String id) {
        var documentSpecification = documentSpecificationDAO.findById(id);
        if (Objects.isNull(documentSpecification)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK)
                .entity(documentSpecificationMapper.mapToDTO(documentSpecification))
                .build();
    }

    @Override
    public Response getAllDocumentSpecifications() {
        return Response.status(Response.Status.OK)
                .entity(documentSpecificationMapper
                        .findAllDocumentSpecifications(documentSpecificationDAO.findAllAsList()))
                .build();
    }

    @Override
    @Transactional
    public Response deleteDocumentSpecificationById(String id) {
        var documentSpecification = documentSpecificationDAO.findById(id);
        if (Objects.nonNull(documentSpecification)) {
            documentSpecificationDAO.delete(documentSpecification);
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    @Transactional
    public Response updateDocumentSpecificationById(String id,
            DocumentSpecificationCreateUpdateDTO documentSpecificationCreateUpdateDTO) {
        var documentSpecification = documentSpecificationDAO.findById(id);
        if (Objects.isNull(documentSpecification)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        documentSpecificationMapper.update(documentSpecificationCreateUpdateDTO, documentSpecification);
        return Response.status(Response.Status.OK)
                .entity(documentSpecificationMapper
                        .mapToDTO(documentSpecificationDAO.update(documentSpecification)))
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

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> optimisticLockException(OptimisticLockException ex) {
        return exceptionMapper.optimisticLock(ex);
    }

}
