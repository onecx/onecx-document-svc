package org.tkit.onecx.document.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.tkit.onecx.document.test.AbstractTest.USER;

import java.util.List;

import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.document.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.document.rs.internal.model.AttachmentDTO;
import gen.org.tkit.onecx.document.rs.internal.model.AttachmentMetadataUploadDTO;
import gen.org.tkit.onecx.document.rs.internal.model.AttachmentStorageAuditRequestDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;

@QuarkusTest
@WithDBData(value = { "document-test-data.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = USER, scopes = "ocx-doc:read")
class AttachmentControllerTest extends AbstractTest {

    private static final String BASE_PATH = "/internal/attachment";
    private static final String EXISTING_ATTACHMENT_ID = "101";
    private static final String EXISTING_ATTACHMENT_NAME = "atachment_1";
    private static final String EXISTING_ATTACHMENT_ID_UNFLAGGED = "109";
    private static final String NONEXISTENT_ATTACHMENT_ID = "9999";
    private static final String EXISTING_DOCUMENT_ID = "51";
    private static final String NONEXISTENT_DOCUMENT_ID = "9999";

    @Test
    @DisplayName("Returns attachment details by id.")
    void testGetAttachmentDetailsSuccess() {
        Response response = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH + "/" + EXISTING_ATTACHMENT_ID);

        response.then().statusCode(OK.getStatusCode());
        AttachmentDTO dto = response.as(AttachmentDTO.class);
        assertThat(dto.getId()).isEqualTo(EXISTING_ATTACHMENT_ID);
        assertThat(dto.getName()).isEqualTo(EXISTING_ATTACHMENT_NAME);
    }

    @Test
    @DisplayName("Returns 404 when getting attachment details for a nonexistent id.")
    void testGetAttachmentDetailsNotFound() {
        given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH + "/" + NONEXISTENT_ATTACHMENT_ID)
                .then().statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("Uploads attachment metadata successfully.")
    void testUploadAttachmentsMetadataSuccess() {
        AttachmentMetadataUploadDTO dto = new AttachmentMetadataUploadDTO();
        dto.setAttachmentId(EXISTING_ATTACHMENT_ID_UNFLAGGED);
        dto.setSize(1024L);
        dto.setType("application/pdf");

        Response response = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(dto))
                .when()
                .patch(BASE_PATH + "/metadata");

        response.then().statusCode(OK.getStatusCode());
    }

    @Test
    @DisplayName("Returns 400 when uploading metadata for a nonexistent attachment.")
    void testUploadAttachmentsMetadataAttachmentNotFound() {
        AttachmentMetadataUploadDTO dto = new AttachmentMetadataUploadDTO();
        dto.setAttachmentId(NONEXISTENT_ATTACHMENT_ID);
        dto.setSize(1024L);
        dto.setType("application/pdf");

        given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(dto))
                .when()
                .patch(BASE_PATH + "/metadata")
                .then().statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    @DisplayName("Creates storage audit logs successfully.")
    void testCreateStorageAuditsForAttachmentsSuccess() {
        AttachmentStorageAuditRequestDTO dto = new AttachmentStorageAuditRequestDTO();
        dto.setDocumentId(EXISTING_DOCUMENT_ID);
        dto.setAttachmentId(EXISTING_ATTACHMENT_ID);

        Response response = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(dto))
                .when()
                .post(BASE_PATH + "/storage-audit");

        response.then().statusCode(CREATED.getStatusCode());
    }

    @Test
    @DisplayName("Returns 400 when creating storage audit with a nonexistent document.")
    void testCreateStorageAuditsDocumentNotFound() {
        AttachmentStorageAuditRequestDTO dto = new AttachmentStorageAuditRequestDTO();
        dto.setDocumentId(NONEXISTENT_DOCUMENT_ID);
        dto.setAttachmentId(EXISTING_ATTACHMENT_ID);

        given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(dto))
                .when()
                .post(BASE_PATH + "/storage-audit")
                .then().statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    @DisplayName("Returns 400 when creating storage audit with a nonexistent attachment.")
    void testCreateStorageAuditsAttachmentNotFound() {
        AttachmentStorageAuditRequestDTO dto = new AttachmentStorageAuditRequestDTO();
        dto.setDocumentId(EXISTING_DOCUMENT_ID);
        dto.setAttachmentId(NONEXISTENT_ATTACHMENT_ID);

        given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(dto))
                .when()
                .post(BASE_PATH + "/storage-audit")
                .then().statusCode(BAD_REQUEST.getStatusCode());
    }
}
