package org.tkit.onecx.document.rs.internal.services;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.document.domain.daos.DocumentSpecificationDAO;
import org.tkit.onecx.document.domain.models.entities.DocumentSpecification;
import org.tkit.onecx.document.rs.internal.mappers.DocumentSpecificationMapper;

import gen.org.tkit.onecx.document.rs.internal.model.DocumentSpecificationCreateUpdateDTO;

@ApplicationScoped
public class DocumentSpecificationService {
    @Inject
    DocumentSpecificationDAO documentSpecificationDAO;
    @Inject
    DocumentSpecificationMapper documentSpecificationMapper;

    @Transactional
    public DocumentSpecification createDocumentSpecification(DocumentSpecificationCreateUpdateDTO dto) {
        return documentSpecificationDAO.create(documentSpecificationMapper.map(dto));
    }

    @Transactional
    public boolean deleteDocumentSpecificationById(String id) {
        var documentSpecification = documentSpecificationDAO.findById(id);
        if (Objects.nonNull(documentSpecification)) {
            documentSpecificationDAO.delete(documentSpecification);
            return true;
        }
        return false;
    }

    @Transactional
    public DocumentSpecification updateDocumentSpecificationById(String id, DocumentSpecificationCreateUpdateDTO dto) {
        var documentSpecification = documentSpecificationDAO.findById(id);
        if (Objects.isNull(documentSpecification)) {
            return null;
        }
        documentSpecificationMapper.update(dto, documentSpecification);
        return documentSpecificationDAO.update(documentSpecification);
    }
}
