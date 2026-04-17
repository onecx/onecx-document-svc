package org.tkit.onecx.document.rs.internal.controllers;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.document.domain.daos.SupportedMimeTypeDAO;
import org.tkit.onecx.document.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.document.rs.internal.mappers.SupportedMimeTypeMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;

import gen.org.tkit.onecx.document.rs.internal.SupportedMimeTypeControllerApi;
import gen.org.tkit.onecx.document.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.document.rs.internal.model.SupportedMimeTypeCreateUpdateDTO;

@ApplicationScoped
public class SupportedMimeTypeController implements SupportedMimeTypeControllerApi {

    @Inject
    SupportedMimeTypeDAO supportedMimeTypeDAO;

    @Inject
    SupportedMimeTypeMapper supportedMimeTypeMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    @Transactional
    public Response createSupportedMimeType(SupportedMimeTypeCreateUpdateDTO supportedMimeTypeCreateUpdateDTO) {
        var supportedMimeType = supportedMimeTypeDAO.create(supportedMimeTypeMapper.map(supportedMimeTypeCreateUpdateDTO));
        return Response.status(Response.Status.CREATED)
                .entity(supportedMimeTypeMapper.mapToDTO(supportedMimeType))
                .build();
    }

    @Override
    public Response getSupportedMimeTypeById(String id) {
        var supportedMimeType = supportedMimeTypeDAO.findById(id);
        if (Objects.isNull(supportedMimeType)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK)
                .entity(supportedMimeTypeMapper.mapToDTO(supportedMimeType))
                .build();
    }

    @Override
    public Response getAllSupportedMimeTypes() {
        return Response.status(Response.Status.OK)
                .entity(supportedMimeTypeMapper.findAllSupportedMimeTypes(supportedMimeTypeDAO.findAll()
                        .toList()))
                .build();
    }

    @Override
    @Transactional
    public Response deleteSupportedMimeTypeId(String id) {
        var supportedMimeType = supportedMimeTypeDAO.findById(id);
        if (Objects.nonNull(supportedMimeType)) {
            supportedMimeTypeDAO.delete(supportedMimeType);
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    @Transactional
    public Response updateSupportedMimeTypeById(String id, SupportedMimeTypeCreateUpdateDTO supportedMimeTypeCreateUpdateDTO) {
        var supportedMimeType = supportedMimeTypeDAO.findById(id);
        if (Objects.isNull(supportedMimeType)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        supportedMimeTypeMapper.update(supportedMimeTypeCreateUpdateDTO, supportedMimeType);
        return Response.status(Response.Status.OK)
                .entity(supportedMimeTypeMapper
                        .mapToDTO(supportedMimeTypeDAO.update(supportedMimeType)))
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
