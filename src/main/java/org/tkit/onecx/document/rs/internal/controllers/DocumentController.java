package org.tkit.onecx.document.rs.internal.controllers;

import static jakarta.transaction.Transactional.TxType.NOT_SUPPORTED;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.document.domain.criteria.DocumentSearchCriteria;
import org.tkit.onecx.document.domain.daos.ChannelDAO;
import org.tkit.onecx.document.domain.daos.DocumentDAO;
import org.tkit.onecx.document.domain.daos.StorageUploadAuditDAO;
import org.tkit.onecx.document.domain.models.entities.Channel;
import org.tkit.onecx.document.domain.models.entities.Document;
import org.tkit.onecx.document.domain.models.entities.StorageUploadAudit;
import org.tkit.onecx.document.rs.internal.exceptions.DocumentException;
import org.tkit.onecx.document.rs.internal.mappers.DocumentMapper;
import org.tkit.onecx.document.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.document.rs.internal.services.DocumentService;

import gen.org.tkit.onecx.document.rs.internal.DocumentControllerApi;
import gen.org.tkit.onecx.document.rs.internal.model.DocumentCreateUpdateDTO;
import gen.org.tkit.onecx.document.rs.internal.model.DocumentSearchCriteriaDTO;
import gen.org.tkit.onecx.document.rs.internal.model.ProblemDetailResponseDTO;

@ApplicationScoped
@Transactional(value = NOT_SUPPORTED)
public class DocumentController implements DocumentControllerApi {
    @Inject
    DocumentDAO documentDAO;
    @Inject
    ChannelDAO channelDAO;
    @Inject
    StorageUploadAuditDAO storageUploadAuditDAO;
    @Inject
    DocumentMapper documentMapper;
    @Inject
    DocumentService documentService;
    @Inject
    ExceptionMapper exceptionMapper;
    public static final DateTimeFormatter CUSTOM_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public Response getDocumentById(String id) {
        var document = documentDAO.findDocumentById(id);
        if (Objects.isNull(document)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK)
                .entity(documentMapper.mapDetail(document))
                .build();
    }

    @Override
    public Response searchDocumentsByCriteria(DocumentSearchCriteriaDTO criteriaDTO) {
        DocumentSearchCriteria criteria = documentMapper.map(criteriaDTO);
        var result = documentService.searchDocuments(criteria);
        return Response.ok(result).build();
    }

    @Override
    public Response deleteDocumentById(String id) {
        boolean deleted = documentService.deleteDocumentById(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response createDocument(DocumentCreateUpdateDTO documentCreateUpdateDTO) {
        var document = documentService.createDocument(documentCreateUpdateDTO);
        return Response.status(Response.Status.CREATED)
                .entity(documentMapper.mapDetail(document))
                .build();
    }

    @Override
    public Response getFailedAttachmentData(String documentId) {
        List<StorageUploadAudit> failedAttachmentList = storageUploadAuditDAO
                .findFailedAttachmentsByDocumentId(documentId);
        return Response.status(Response.Status.OK)
                .entity(documentMapper.mapStorageUploadAudit(failedAttachmentList))
                .build();
    }

    @Override
    public Response updateDocument(String id, DocumentCreateUpdateDTO documentCreateUpdateDTO) {
        var document = documentService.updateDocumentById(id, documentCreateUpdateDTO);
        if (Objects.isNull(document)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response getAllChannels() {
        // List of unique alphabetically sorted channel names ignoring cases
        List<Channel> uniqueSortedChannelNames = channelDAO.findAllSortedByNameAsc()
                .filter(distinctByKey(c -> c.getName().toLowerCase(Locale.ROOT)))
                .toList();
        return Response.status(Response.Status.OK)
                .entity(documentMapper.mapChannels(uniqueSortedChannelNames))
                .build();
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = Collections.newSetFromMap(new ConcurrentHashMap<>());
        return t -> seen.add(keyExtractor.apply(t));
    }

    @Override
    public Response bulkUpdateDocument(List<DocumentCreateUpdateDTO> documentCreateUpdateDTO) {
        var documents = documentService.bulkUpdateDocuments(documentCreateUpdateDTO);
        if (documents == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK)
                .entity(documentMapper.mapDetailBulk(documents.stream()))
                .build();
    }

    @Override
    public Response deleteBulkDocuments(List<String> requestBody) {
        boolean deleted = documentService.deleteBulkDocuments(requestBody);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response showAllDocumentsByCriteria(DocumentSearchCriteriaDTO criteriaDTO) {
        DocumentSearchCriteria criteria = documentMapper.map(criteriaDTO);
        if (Objects.nonNull(criteriaDTO.getStartDate()) && !criteriaDTO.getStartDate().isEmpty()) {
            criteria.setStartDate(LocalDateTime.parse(criteriaDTO.getStartDate(), CUSTOM_DATE_TIME_FORMATTER));
        }
        if (Objects.nonNull(criteriaDTO.getEndDate()) && !criteriaDTO.getEndDate().isEmpty()) {
            criteria.setEndDate(LocalDateTime.parse(criteriaDTO.getEndDate(), CUSTOM_DATE_TIME_FORMATTER));
        }
        List<Document> documents = documentDAO.findAllDocumentsBySearchCriteria(criteria);
        return Response.ok(documentMapper.mapDocuments(documents))
                .build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> documentException(DocumentException ex) {
        return exceptionMapper.documentException(ex);
    }
}
