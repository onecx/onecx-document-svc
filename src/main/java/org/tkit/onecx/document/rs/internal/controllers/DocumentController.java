package org.tkit.onecx.document.rs.internal.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.document.domain.criteria.DocumentSearchCriteria;
import org.tkit.onecx.document.domain.daos.AttachmentDAO;
import org.tkit.onecx.document.domain.daos.ChannelDAO;
import org.tkit.onecx.document.domain.daos.DocumentDAO;
import org.tkit.onecx.document.domain.daos.StorageUploadAuditDAO;
import org.tkit.onecx.document.domain.models.entities.*;
import org.tkit.onecx.document.domain.models.entities.Channel;
import org.tkit.onecx.document.domain.models.entities.Document;
import org.tkit.onecx.document.domain.models.entities.StorageUploadAudit;
import org.tkit.onecx.document.rs.internal.exception.RestException;
import org.tkit.onecx.document.rs.internal.mappers.DocumentMapper;
import org.tkit.onecx.document.rs.internal.services.DocumentService;
import org.tkit.quarkus.jpa.daos.PageResult;

import gen.org.tkit.onecx.document.rs.internal.DocumentControllerApi;
import gen.org.tkit.onecx.document.rs.internal.model.DocumentCreateUpdateDTO;
import gen.org.tkit.onecx.document.rs.internal.model.DocumentSearchCriteriaDTO;
import io.quarkus.logging.Log;

@ApplicationScoped
public class DocumentController implements DocumentControllerApi {

    @Inject
    DocumentDAO documentDAO;

    @Inject
    ChannelDAO channelDAO;

    @Inject
    AttachmentDAO attachmentDAO;

    @Inject
    StorageUploadAuditDAO storageUploadAuditDAO;

    @Inject
    DocumentMapper documentMapper;

    @Inject
    DocumentService documentService;

    public static final DateTimeFormatter CUSTOM_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public Response getDocumentById(String id) {
        var document = documentDAO.findDocumentById(id);
        if (Objects.isNull(document)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getDocumentNotFoundMsg(id));
        }
        return Response.status(Response.Status.OK)
                .entity(documentMapper.mapDetail(document))
                .build();
    }

    @Override
    @Transactional
    public Response getDocumentByCriteria(DocumentSearchCriteriaDTO criteriaDTO) {
        DocumentSearchCriteria criteria = documentMapper.map(criteriaDTO);
        if (Objects.nonNull(criteriaDTO.getStartDate()) && !criteriaDTO.getStartDate().isEmpty()) { // added this for
                                                                                                    // date search

            criteria.setStartDate(LocalDateTime.parse(criteriaDTO.getStartDate(), CUSTOM_DATE_TIME_FORMATTER));
        }
        if (Objects.nonNull(criteriaDTO.getEndDate()) && !criteriaDTO.getEndDate().isEmpty()) {

            criteria.setEndDate(LocalDateTime.parse(criteriaDTO.getEndDate(), CUSTOM_DATE_TIME_FORMATTER));
        }
        PageResult<Document> documents = documentDAO.findBySearchCriteria(criteria);
        return Response.ok(documentMapper.mapToPageResultDTO(documents))
                .build();
    }

    @Override
    @Transactional
    public Response deleteDocumentById(String id) {
        var document = documentDAO.findById(id);
        if (Objects.isNull(document)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getDocumentNotFoundMsg(id));
        }
        documentDAO.delete(document);
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
    @Transactional
    public Response getFailedAttachmentData(String documentId) {
        List<StorageUploadAudit> failedAttachmentList = storageUploadAuditDAO
                .findFailedAttachmentsByDocumentId(documentId);
        return Response.status(Response.Status.OK)
                .entity(documentMapper.mapStorageUploadAudit(failedAttachmentList))
                .build();
    }

    @Override
    @Transactional
    public Response updateDocument(String id, DocumentCreateUpdateDTO documentCreateUpdateDTO) {
        var document = documentDAO.findDocumentById(id);
        if (Objects.isNull(document)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND, getDocumentNotFoundMsg(id));
        }
        document = documentService.updateDocument(document, documentCreateUpdateDTO);
        return Response.status(Response.Status.CREATED)
                .entity(documentMapper.mapDetail(documentDAO.update(document)))
                .build();
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
    @Transactional
    public Response bulkUpdateDocument(List<DocumentCreateUpdateDTO> documentCreateUpdateDTO) {
        Iterator<DocumentCreateUpdateDTO> it = documentCreateUpdateDTO.listIterator();
        List<Document> document1 = new ArrayList<>();
        while (it.hasNext()) {
            DocumentCreateUpdateDTO dto1 = it.next();
            var document = documentDAO.findDocumentById(dto1.getId());
            if (Objects.isNull(document)) {
                throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                        getDocumentNotFoundMsg(dto1.getId()));
            }
            try {
                document = documentService.updateDocument(document, dto1);
            } catch (Exception e) {
                Log.error(e);
            }
            document1.add(document);
        }
        return Response.status(Response.Status.CREATED)
                .entity(documentMapper.mapDetailBulk(documentDAO.update(document1.stream())))
                .build();
    }

    @Override
    @Transactional
    public Response deleteBulkDocuments(List<String> requestBody) {
        Iterator<String> itr = requestBody.iterator();
        while (itr.hasNext()) {
            String currentDocId = itr.next();
            var document = documentDAO.findById(currentDocId);
            if (Objects.isNull(document)) {
                throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                        getDocumentNotFoundMsg(currentDocId));
            }
            documentDAO.delete(document);
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    @Transactional
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

    private String getDocumentNotFoundMsg(String id) {
        return "Document with id " + id + " was not found.";

    }
}
