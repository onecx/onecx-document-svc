package org.tkit.onecx.document.rs.internal.services;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.onecx.document.domain.daos.DocumentTypeDAO;
import org.tkit.onecx.document.domain.models.entities.DocumentType;
import org.tkit.onecx.document.rs.internal.mappers.DocumentTypeMapper;

import gen.org.tkit.onecx.document.rs.internal.model.DocumentTypeCreateUpdateDTO;

@ApplicationScoped
public class DocumentTypeService {
    @Inject
    DocumentTypeDAO documentTypeDAO;
    @Inject
    DocumentTypeMapper documentTypeMapper;

    @Transactional
    public DocumentType createDocumentType(DocumentTypeCreateUpdateDTO dto) {
        return documentTypeDAO.create(documentTypeMapper.map(dto));
    }

    @Transactional
    public boolean deleteDocumentTypeById(String id) {
        var documentType = documentTypeDAO.findById(id);
        if (Objects.nonNull(documentType)) {
            documentTypeDAO.delete(documentType);
            return true;
        }
        return false;
    }

    @Transactional
    public DocumentType updateDocumentTypeById(String id, DocumentTypeCreateUpdateDTO dto) {
        var documentType = documentTypeDAO.findById(id);
        if (Objects.isNull(documentType)) {
            return null;
        }
        documentTypeMapper.update(dto, documentType);
        return documentTypeDAO.update(documentType);
    }
}
