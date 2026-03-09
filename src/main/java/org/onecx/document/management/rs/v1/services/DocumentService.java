package org.onecx.document.management.rs.v1.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.nio.file.attribute.FileTime;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;
import org.onecx.document.management.domain.daos.AttachmentDAO;
import org.onecx.document.management.domain.daos.DocumentDAO;
import org.onecx.document.management.domain.daos.DocumentTypeDAO;
import org.onecx.document.management.domain.daos.MinioAuditLogDAO;
import org.onecx.document.management.domain.daos.StorageUploadAuditDAO;
import org.onecx.document.management.domain.daos.SupportedMimeTypeDAO;
import org.onecx.document.management.domain.models.entities.Attachment;
import org.onecx.document.management.domain.models.entities.Category;
import org.onecx.document.management.domain.models.entities.Channel;
import org.onecx.document.management.domain.models.entities.Document;
import org.onecx.document.management.domain.models.entities.DocumentCharacteristic;
import org.onecx.document.management.domain.models.entities.DocumentRelationship;
import org.onecx.document.management.domain.models.entities.DocumentSpecification;
import org.onecx.document.management.domain.models.entities.DocumentType;
import org.onecx.document.management.domain.models.entities.MinioAuditLog;
import org.onecx.document.management.domain.models.entities.RelatedObjectRef;
import org.onecx.document.management.domain.models.entities.RelatedPartyRef;
import org.onecx.document.management.domain.models.entities.SupportedMimeType;
import org.onecx.document.management.domain.models.enums.AttachmentUnit;
import org.onecx.document.management.rs.v1.exception.CustomException;
import org.onecx.document.management.rs.v1.exception.RestException;
import org.onecx.document.management.rs.v1.mappers.DocumentMapper;
import org.onecx.document.management.rs.v1.mappers.DocumentSpecificationMapper;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import gen.org.onecx.document.management.rs.v1.model.AttachmentCreateUpdateDTO;
import gen.org.onecx.document.management.rs.v1.model.DocumentCreateUpdateDTO;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

/**
 * Document service.
 */
@ApplicationScoped
public class DocumentService {

    @Inject
    DocumentDAO documentDAO;

    @Inject
    DocumentMapper documentMapper;

    @Inject
    DocumentTypeDAO typeDAO;

    @Inject
    DocumentSpecificationMapper documentSpecificationMapper;

    @Inject
    SupportedMimeTypeDAO mimeTypeDAO;

    @Inject
    AttachmentDAO attachmentDAO;

    @Inject
    StorageUploadAuditDAO storageUploadAuditDAO;

    @Inject
    MinioAuditLogDAO minioAuditLogDAO;

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "minio.bucket.folder")
    String bucketFolder;

    @ConfigProperty(name = "quarkus.minio.url")
    String minioUrl;

    private static final Pattern FILENAME_PATTERN = Pattern.compile("filename=\\\"(.*)\\\"");

    private static final String SLASH = "/";

    private static final String ATTACHMENT_ID_LIST_MEDIA_TYPE = "text/plain";

    private static final String FORM_DATA_MAP_KEY = "file";

    private static final String HEADER_KEY = "Content-Disposition";

    private static final String STRING_TOKEN_DELIMITER = ",";

    public Document createDocument(DocumentCreateUpdateDTO dto) {
        var document = documentMapper.map(dto);
        setType(dto, document);
        setSpecification(dto, document);
        setAttachments(dto, document);
        return documentDAO.create(document);
    }

    @Transactional
    public Map<String, Integer> uploadAttachment(String documentId, MultipartFormDataInput input)
            throws IOException {
        HashMap<String, Integer> map = new HashMap<>();
        var document = documentDAO.findDocumentById(documentId);
        if (Objects.isNull(document)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getDocumentNotFoundMsg(documentId));
        }
        Collection<FormValue> inputParts = input.getValues().get(FORM_DATA_MAP_KEY);
        String mediaType = resolveMediaType(input);
        Set<Attachment> attachmentsToProcess = resolveAttachmentsToProcess(document, inputParts, mediaType);
        if (attachmentsToProcess.isEmpty()) {
            return map;
        }
        attachmentsToProcess.forEach(
                attachment -> processAttachment(documentId, document, attachment, inputParts, map));
        return map;
    }

    public void createStorageUploadAuditRecords(String documentId, Document document, Attachment attachment) {
        var storageUploadAudit = documentMapper.mapToStorageUploadAudit(documentId, document, attachment);
        storageUploadAuditDAO.create(storageUploadAudit);
    }

    /**
     * Updates the basic fields in {@link Document}, updates collections and
     * elements in collections: {@link Attachment},
     * {@link DocumentCharacteristic}, {@link DocumentRelationship},
     * {@link RelatedPartyRef}, {@link Category},
     * updates objects: {@link Channel}, {@link RelatedObjectRef} and sets
     * {@link DocumentType}
     * and {@link DocumentSpecification}.
     *
     * @param dto a {@link DocumentCreateUpdateDTO}
     * @return a {@link Document}
     */
    @Transactional
    public Document updateDocument(Document document, DocumentCreateUpdateDTO dto) {
        documentMapper.update(dto, document);
        setType(dto, document);
        setSpecification(dto, document);
        updateChannelInDocument(document, dto);
        updateRelatedObjectRefInDocument(document, dto);
        documentMapper.updateTraceableCollectionsInDocument(document, dto);
        updateAttachmentsInDocument(document, dto);
        return document;
    }

    public InputStream getObjectFromObjectStore(String objectId)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        var getObjectArgs = GetObjectArgs.builder()
                .bucket(bucketFolder)
                .object(objectId)
                .build();
        return minioClient.getObject(getObjectArgs);
    }

    @Transactional
    public void updateAttachmentStatusInBulk(List<String> attachmentIds) {
        attachmentIds.stream().forEach(attachmentId -> {
            var attachment = attachmentDAO.findById(attachmentId);
            if (Objects.isNull(attachment)) {
                throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                        getAttachmentNotFoundMsg(attachmentId));
            }
            attachment.setStorageUploadStatus(false);
        });
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public void asyncDeleteForAttachments(String attachmentId) {
        Uni.createFrom().item(attachmentId).emitOn(Infrastructure.getDefaultWorkerPool()).subscribe().with(
                this::deleteFileInAttachmentAsync, Throwable::printStackTrace);
    }

    public void deleteFileInAttachmentAsync(String attachmentId) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketFolder)
                            .object(attachmentId)
                            .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException
                | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException
                | XmlParserException e) {
            var minioAuditLog = new MinioAuditLog();
            minioAuditLog.setAttachmentId(attachmentId);
            minioAuditLogDAO.create(minioAuditLog);
            throw new CustomException("An error occurred while deleting the attachment file.", e);
        }
    }

    @Transactional
    public List<String> getFilesIdToBeDeletedInDocument(Document document) {
        if (!Objects.isNull(document.getAttachments())) {
            List<String> attachmentIds = document.getAttachments().stream().map(TraceableEntity::getId)
                    .toList();
            updateAttachmentStatusInBulk(attachmentIds);
            return attachmentIds;
        }
        return Collections.emptyList();
    }

    /**
     * Retrieves the uploaded attachments of a document and returns a
     * {@link StreamingOutput} that streams them as a ZIP archive.
     *
     * @param documentId the ID of the document
     * @param clientTimezone the timezone string used for ZIP entry timestamps
     * @return a {@link StreamingOutput} containing all attachment files zipped
     */
    public StreamingOutput getAttachmentsZipStream(String documentId, String clientTimezone) {
        var document = documentDAO.findById(documentId);
        if (Objects.isNull(document)) {
            throw new RestException(Response.Status.BAD_REQUEST, Response.Status.BAD_REQUEST,
                    getDocumentNotFoundMsg(documentId));
        }
        Set<Attachment> attachments = getUploadedAttachments(document);
        if (attachments.isEmpty()) {
            return null;
        }
        return output -> {
            try (var zip = new ZipOutputStream(output)) {
                for (Attachment attachment : attachments) {
                    if (attachment == null) {
                        continue;
                    }
                    addAttachmentToZip(attachment, clientTimezone, zip);
                }
                zip.finish();
            }
        };
    }

    private String resolveMediaType(MultipartFormDataInput input) {
        for (Map.Entry<String, Collection<FormValue>> attribute : input.getValues().entrySet()) {
            for (FormValue fv : attribute.getValue()) {
                if (fv.isFileItem()) {
                    return fv.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
                }
            }
        }
        return "";
    }

    private Set<Attachment> resolveAttachmentsToProcess(Document document, Collection<FormValue> inputParts,
            String mediaType) throws IOException {
        Set<Attachment> attachmentSet = new HashSet<>();
        if (String.valueOf(MediaType.valueOf(mediaType)).equals(ATTACHMENT_ID_LIST_MEDIA_TYPE)) {
            List<String> attachmentIdList = getAttachmentIdList(inputParts.stream().toList());
            inputParts.remove(0);
            attachmentIdList.forEach(attachmentId -> document.getAttachments().stream()
                    .filter(attachment -> attachmentId.equals(attachment.getId()))
                    .findFirst()
                    .ifPresent(attachmentSet::add));
        } else {
            attachmentSet.addAll(document.getAttachments());
        }
        return attachmentSet;
    }

    private void processAttachment(String documentId, Document document, Attachment attachment,
            Collection<FormValue> inputParts, Map<String, Integer> map) {
        String strFilenameFileId = attachment.getId() + SLASH + attachment.getName();
        Optional<FormValue> matchedInputPart = inputParts.stream()
                .filter(inputPart -> attachment.getFileName().equals(inputPart.getFileName()))
                .findFirst();
        try {
            if (matchedInputPart.isPresent()) {
                InputStream inputPartBody = matchedInputPart.get().getFileItem().getInputStream();
                byte[] fileBytes = IOUtils.toByteArray(inputPartBody);
                String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(fileBytes));
                uploadFileToObjectStorage(fileBytes, attachment.getId());
                map.put(strFilenameFileId, Response.Status.CREATED.getStatusCode());
                updateAttachmentAfterUpload(attachment, new BigDecimal(fileBytes.length), contentType);
            }
        } catch (Exception e) {
            map.put(strFilenameFileId, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            createStorageUploadAuditRecords(documentId, document, attachment);
        }
    }

    private List<String> getAttachmentIdList(List<FormValue> inputPartList) throws IOException {
        List<String> attachmentIdList = new ArrayList<>();
        var stringTokenizer = new StringTokenizer(String.valueOf(inputPartList.get(0).getFileItem()),
                STRING_TOKEN_DELIMITER);
        while (stringTokenizer.hasMoreTokens()) {
            attachmentIdList.add(stringTokenizer.nextToken());
        }
        return attachmentIdList;
    }

    /**
     * Finds the type {@link DocumentType} by id and sets the type in the document
     * entity {@link Document}.
     *
     * @param dto a {@link DocumentCreateUpdateDTO}
     * @param document a {@link Document}
     */
    private void setType(DocumentCreateUpdateDTO dto, Document document) {

        var documentType = typeDAO.findById(dto.getTypeId());
        if (Objects.isNull(documentType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getDocumentNotFoundMsg(dto.getTypeId()));
        }
        document.setType(documentType);
    }

    /**
     * Finds the specification {@link DocumentSpecification} by id and sets the
     * specification
     * in the document entity {@link Document}.
     *
     * @param dto a {@link DocumentCreateUpdateDTO}
     * @param document a {@link Document}
     */
    private void setSpecification(DocumentCreateUpdateDTO dto, Document document) {
        if (Objects.isNull(dto.getSpecification())) {
            document.setSpecification(null);
        } else {
            gen.org.onecx.document.management.rs.v1.model.DocumentSpecificationCreateUpdateDTO docSpecDto = dto
                    .getSpecification();
            var documentSpecification = documentSpecificationMapper.map(docSpecDto);
            document.setSpecification(documentSpecification);
        }
    }

    /**
     * Finds the {@link SupportedMimeType} by the given id.
     *
     * @param dto a {@link AttachmentCreateUpdateDTO}
     * @return a {@link SupportedMimeType}
     *         or it throws an error if it can't find a {@link SupportedMimeType}
     *         given id.
     */
    private SupportedMimeType getSupportedMimeType(AttachmentCreateUpdateDTO dto) {
        SupportedMimeType mimeType = mimeTypeDAO.findById(dto.getMimeTypeId());
        if (Objects.isNull(mimeType)) {
            throw new RestException(Response.Status.NOT_FOUND, Response.Status.NOT_FOUND,
                    getSupportedMimeTypeNotFoundMsg(dto.getMimeTypeId()));
        }
        return mimeType;
    }

    /**
     * Finds attachment's mimeType {@link SupportedMimeType} by id
     * and sets it in the attachment entity {@link Attachment}, then add
     * {@link Attachment}
     * in document entity {@link Document}.
     *
     * @param dto a {@link DocumentCreateUpdateDTO}
     * @param document a {@link Document}
     */
    private void setAttachments(DocumentCreateUpdateDTO dto, Document document) {
        if (Objects.isNull(dto.getAttachments())) {
            document.setAttachments(null);
        } else {
            for (AttachmentCreateUpdateDTO attachmentDTO : dto.getAttachments()) {
                if (Objects.isNull(attachmentDTO.getId()) || attachmentDTO.getId().isEmpty()) {
                    var mimeType = getSupportedMimeType(attachmentDTO);
                    var attachment = documentMapper.mapAttachment(attachmentDTO);
                    attachment.setMimeType(mimeType);
                    attachment.setStorageUploadStatus(false);
                    document.getAttachments().add(attachment);
                }
            }
        }
    }

    /**
     * Updates {@link Channel} in {@link Document} or creates new {@link Channel}
     * and sets in {@link Document}.
     *
     * @param document a {@link Document}
     * @param updateDTO a {@link DocumentCreateUpdateDTO}
     */
    private void updateChannelInDocument(Document document, DocumentCreateUpdateDTO updateDTO) {
        if (Objects.isNull(updateDTO.getChannel().getId()) || updateDTO.getChannel().getId().isEmpty()) {
            var channel = documentMapper.mapChannel(updateDTO.getChannel());
            document.setChannel(channel);
        } else {
            documentMapper.updateChannel(updateDTO.getChannel(), document.getChannel());
        }
    }

    /**
     * Updates {@link RelatedObjectRef} in {@link Document} or creates new
     * {@link RelatedObjectRef} and sets in {@link Document}.
     *
     * @param document a {@link Document}
     * @param updateDTO a {@link DocumentCreateUpdateDTO}
     */
    private void updateRelatedObjectRefInDocument(Document document, DocumentCreateUpdateDTO updateDTO) {
        if (Objects.isNull(updateDTO.getRelatedObject())) {
            document.setRelatedObject(null);
        } else {
            if (Objects.isNull(updateDTO.getRelatedObject().getId())
                    || updateDTO.getRelatedObject().getId().isEmpty()) {
                var relatedObjectRef = documentMapper.mapRelatedObjectRef(updateDTO.getRelatedObject());
                document.setRelatedObject(relatedObjectRef);
            } else {
                documentMapper.updateRelatedObjectRef(updateDTO.getRelatedObject(), document.getRelatedObject());
            }
        }
    }

    /**
     * Updates {@link Attachment} in collection in {@link Document}
     * or creates {@link Attachment} sets {@link SupportedMimeType} in
     * {@link Attachment}
     * and add to collection or remove {@link Attachment} from collection.
     *
     * @param document a {@link Document}
     * @param updateDTO a {@link DocumentCreateUpdateDTO}
     */
    private void updateAttachmentsInDocument(Document document, DocumentCreateUpdateDTO updateDTO) {
        if (Objects.nonNull(updateDTO.getAttachments())) {
            for (Iterator<Attachment> i = document.getAttachments().iterator(); i.hasNext();) {
                Attachment entity = i.next();
                Optional<AttachmentCreateUpdateDTO> dtoOptional = updateDTO.getAttachments().stream()
                        .filter(dto -> dto.getId() != null)
                        .filter(dto -> entity.getId().equals(dto.getId()))
                        .findFirst();
                if (dtoOptional.isPresent()) {
                    var mimeType = getSupportedMimeType(dtoOptional.get());
                    documentMapper.updateAttachment(dtoOptional.get(), entity);
                    entity.setMimeType(mimeType);
                }
            }
            setAttachments(updateDTO, document);
        }
    }

    private void uploadFileToObjectStorage(byte[] fileBytes, String id)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketFolder)
                .object(id)
                .stream(new ByteArrayInputStream(fileBytes), fileBytes.length, -1)
                .build());

    }

    private void updateAttachmentAfterUpload(Attachment attachment, BigDecimal size, String contentType) {
        attachment.setSize(size);
        attachment.setSizeUnit(AttachmentUnit.BYTES);
        attachment.setStorage("MinIO");
        attachment.setType(contentType);
        attachment.setExternalStorageURL(minioUrl);
        attachment.setStorageUploadStatus(true);
    }

    private String getAttachmentNotFoundMsg(String attachmentId) {
        return String.format("The attachment with ID %s was not found.", attachmentId);
    }

    private String getDocumentNotFoundMsg(String documentId) {
        return String.format("The document with ID %s was not found.", documentId);
    }

    private String getSupportedMimeTypeNotFoundMsg(String mimeTypeId) {
        return String.format("The supported mime type with ID %s was not found.", mimeTypeId);
    }

    private Set<Attachment> getUploadedAttachments(Document document) {
        return document.getAttachments().stream()
                .filter(Attachment::getStorageUploadStatus)
                .collect(Collectors.toSet());
    }

    private void addAttachmentToZip(Attachment attachment, String clientTimezone, ZipOutputStream zip)
            throws IOException {
        try {
            InputStream object = getObjectFromObjectStore(attachment.getId());
            ZipEntry entry = buildZipEntry(attachment, clientTimezone, object);
            zip.putNextEntry(entry);
            IOUtils.copy(object, zip);
            zip.closeEntry();
        } catch (InvalidKeyException | InvalidResponseException | InsufficientDataException
                | NoSuchAlgorithmException | ServerException | InternalException | XmlParserException
                | ErrorResponseException e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "Failed to download file for attachment: " + attachment.getId(), e);
        }
    }

    private ZipEntry buildZipEntry(Attachment attachment, String clientTimezone, InputStream object)
            throws IOException {
        var entry = new ZipEntry(attachment.getFileName());
        entry.setSize(object.available());
        ZoneId clientZoneId = (clientTimezone != null && !clientTimezone.isEmpty())
                ? ZoneId.of(clientTimezone)
                : ZoneId.of("UTC");
        FileTime fileTime = resolveFileTime(attachment, clientZoneId);
        entry.setCreationTime(fileTime);
        entry.setLastModifiedTime(fileTime);
        return entry;
    }

    private FileTime resolveFileTime(Attachment attachment, ZoneId clientZoneId) {
        LocalDateTime attachmentDateTime = attachment.getCreationDate();
        return FileTime.from(Objects.requireNonNullElseGet(attachmentDateTime, LocalDateTime::now)
                .atZone(clientZoneId).toInstant());
    }

}
