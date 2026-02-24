package org.onecx.document.management.rs.v1.controllers;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.onecx.document.management.domain.daos.DocumentDAO;
import org.onecx.document.management.domain.daos.DocumentTypeDAO;
import org.onecx.document.management.rs.v1.exception.RestException;
import org.onecx.document.management.rs.v1.mappers.DocumentTypeMapper;

import gen.org.onecx.document.management.rs.v1.DocumentTypeControllerV1Api;
import gen.org.onecx.document.management.rs.v1.model.DocumentTypeCreateUpdateDTO;

@ApplicationScoped
public class DocumentTypeController implements DocumentTypeControllerV1Api {

    @Inject
    DocumentTypeDAO documentTypeDAO;

    @Inject
    DocumentTypeMapper documentTypeMapper;

    @Inject
    DocumentDAO documentDAO;

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
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getTypeNotFoundMsg(id));
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
            if (!documentDAO.findDocumentsWithDocumentTypeId(id).isEmpty()) {
                throw new RestException(Response.Status.BAD_REQUEST, Response.Status.BAD_REQUEST,
                        "You cannot delete type of document with id " + id
                                + ". It is assigned to the document.");
            }
            documentTypeDAO.delete(documentType);
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getTypeNotFoundMsg(id));
    }

    @Override
    @Transactional
    public Response updateDocumentTypeById(String id, DocumentTypeCreateUpdateDTO documentTypeCreateUpdateDTO) {
        var documentType = documentTypeDAO.findById(id);
        if (Objects.isNull(documentType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getTypeNotFoundMsg(id));
        }
        documentTypeMapper.update(documentTypeCreateUpdateDTO, documentType);
        return Response.status(Response.Status.CREATED)
                .entity(documentTypeMapper.mapDocumentType(documentTypeDAO.update(documentType)))
                .build();
    }

    private String getTypeNotFoundMsg(String id) {
        return "The document type with id " + id + " was not found.";
    }
}
