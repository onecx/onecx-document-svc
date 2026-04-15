package org.tkit.onecx.document.rs.internal.controllers;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.document.domain.daos.DocumentDAO;
import org.tkit.onecx.document.domain.daos.DocumentSpecificationDAO;
import org.tkit.onecx.document.rs.internal.exception.RestException;
import org.tkit.onecx.document.rs.internal.mappers.DocumentSpecificationMapper;

import gen.org.tkit.onecx.document.rs.internal.DocumentSpecificationControllerApi;
import gen.org.tkit.onecx.document.rs.internal.model.DocumentSpecificationCreateUpdateDTO;

@ApplicationScoped
public class DocumentSpecificationController implements DocumentSpecificationControllerApi {

    @Inject
    DocumentSpecificationDAO documentSpecificationDAO;

    @Inject
    DocumentSpecificationMapper documentSpecificationMapper;

    @Inject
    DocumentDAO documentDAO;

    @Inject
    EntityManager entityManager;

    @Override
    @Transactional
    public Response createDocumentSpecification(DocumentSpecificationCreateUpdateDTO documentSpecificationCreateUpdateDTO) {
        if (Objects.isNull(documentSpecificationCreateUpdateDTO.getName())
                || Objects.equals(documentSpecificationCreateUpdateDTO.getName().trim(), "")) {
            throw new RestException(Response.Status.BAD_REQUEST, Response.Status.BAD_REQUEST,
                    "createDocumentSpecification.documentSpecificationCreateUpdateDTO.name: must not be blank");
        }
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
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getSpecificationNotFoundMsg(id));
        }
        return Response.status(Response.Status.OK)
                .entity(documentSpecificationMapper.mapToDTO(documentSpecification))
                .build();
    }

    @Override
    public Response getAllDocumentSpecifications() {
        return Response.status(Response.Status.OK)
                .entity(documentSpecificationMapper
                        .findAllDocumentSpecifications(documentSpecificationDAO.findAll()
                                .toList()))
                .build();
    }

    @Override
    @Transactional
    public Response deleteDocumentSpecificationById(String id) {
        var documentSpecification = documentSpecificationDAO.findById(id);
        if (Objects.nonNull(documentSpecification)) {
            if (!documentDAO.findDocumentsWithDocumentSpecificationId(id).isEmpty()) {
                throw new RestException(Response.Status.BAD_REQUEST, Response.Status.BAD_REQUEST,
                        "You cannot delete specification of document with id " + id
                                + ". It is assigned to the document.");
            }
            entityManager.remove(documentSpecification);
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                getSpecificationNotFoundMsg(id));
    }

    @Override
    @Transactional
    public Response updateDocumentSpecificationById(String id,
            DocumentSpecificationCreateUpdateDTO documentSpecificationCreateUpdateDTO) {
        var documentSpecification = documentSpecificationDAO.findById(id);
        if (Objects.isNull(documentSpecification)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getSpecificationNotFoundMsg(id));
        }
        documentSpecificationMapper.update(documentSpecificationCreateUpdateDTO, documentSpecification);
        return Response.status(Response.Status.OK)
                .entity(documentSpecificationMapper
                        .mapToDTO(documentSpecificationDAO.update(documentSpecification)))
                .build();
    }

    private String getSpecificationNotFoundMsg(String id) {
        return "The document specification with id " + id + " was not found.";
    }

}
