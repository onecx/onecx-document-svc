package org.tkit.onecx.document.domain.daos;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.onecx.document.domain.models.entities.DocumentType;
import org.tkit.quarkus.jpa.daos.AbstractDAO;

/**
 * DocumentTypeDAO class.
 */
@ApplicationScoped
public class DocumentTypeDAO extends AbstractDAO<DocumentType> {
}
