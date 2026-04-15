package org.tkit.onecx.document.rs.internal.controllers;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.document.domain.daos.AttachmentDAO;
import org.tkit.onecx.document.domain.daos.SupportedMimeTypeDAO;
import org.tkit.onecx.document.rs.internal.exception.RestException;
import org.tkit.onecx.document.rs.internal.mappers.SupportedMimeTypeMapper;

import gen.org.tkit.onecx.document.rs.internal.SupportedMimeTypeControllerApi;
import gen.org.tkit.onecx.document.rs.internal.model.SupportedMimeTypeCreateUpdateDTO;

@ApplicationScoped
public class SupportedMimeTypeController implements SupportedMimeTypeControllerApi {

    @Inject
    SupportedMimeTypeDAO supportedMimeTypeDAO;

    @Inject
    SupportedMimeTypeMapper supportedMimeTypeMapper;

    @Inject
    AttachmentDAO attachmentDAO;

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
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getMimeTypeNotFoundMsg(id));
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
            if (!attachmentDAO.findAttachmentsWithSupportedMimeTypeId(id).isEmpty()) {
                throw new RestException(Response.Status.BAD_REQUEST, Response.Status.BAD_REQUEST,
                        "You cannot delete supported mime-type with id " + id
                                + ". It is assigned to the attachment.");
            }
            supportedMimeTypeDAO.delete(supportedMimeType);
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                getMimeTypeNotFoundMsg(id));
    }

    @Override
    @Transactional
    public Response updateSupportedMimeTypeById(String id, SupportedMimeTypeCreateUpdateDTO supportedMimeTypeCreateUpdateDTO) {
        var supportedMimeType = supportedMimeTypeDAO.findById(id);
        if (Objects.isNull(supportedMimeType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getMimeTypeNotFoundMsg(id));
        }
        supportedMimeTypeMapper.update(supportedMimeTypeCreateUpdateDTO, supportedMimeType);
        return Response.status(Response.Status.OK)
                .entity(supportedMimeTypeMapper
                        .mapToDTO(supportedMimeTypeDAO.update(supportedMimeType)))
                .build();
    }

    private String getMimeTypeNotFoundMsg(String id) {
        return "The supported mime-type with id " + id + " was not found.";
    }

}
