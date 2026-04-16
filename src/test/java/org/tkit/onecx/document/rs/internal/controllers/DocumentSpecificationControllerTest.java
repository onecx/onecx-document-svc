package org.tkit.onecx.document.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.tkit.onecx.document.test.AbstractTest.USER;

import java.util.List;

import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.document.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.document.rs.internal.model.DocumentSpecificationCreateUpdateDTO;
import gen.org.tkit.onecx.document.rs.internal.model.DocumentSpecificationDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

@QuarkusTest
@WithDBData(value = { "document-test-data.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = USER, scopes = "ocx-doc:all")
class DocumentSpecificationControllerTest extends AbstractTest {

    private static final String BASE_PATH = "/internal/document-specification";
    private static final String EXISTING_DOCUMENT_SPECIFICATION_ID = "251";
    private static final String EXISTING_DOCUMENT_SPECIFICATION_DELETE_ID = "253";
    private static final String NONEXISTENT_DOCUMENT_SPECIFICATION_ID = "10000";
    private static final String NAME_OF_DOCUMENT_SPECIFICATION_1 = "specification_1";
    private static final String VERSION_OF_DOCUMENT_SPECIFICATION_1 = "v_1";

    @Test
    @DisplayName("Saves specification of document with the required fields with validated data.")
    void testSuccessfulCreateDocumentSpecification() {
        final String documentSpecificationName = "DOCUMENT_SPECIFICATION_NAME";
        final String documentSpecificationVersion = "DOCUMENT_SPECIFICATION_VERSION";
        DocumentSpecificationCreateUpdateDTO documentSpecificationCreateDTO = new DocumentSpecificationCreateUpdateDTO();
        documentSpecificationCreateDTO.setName(documentSpecificationName);
        documentSpecificationCreateDTO.setSpecificationVersion(documentSpecificationVersion);

        Response postResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentSpecificationCreateDTO)
                .when()
                .post(BASE_PATH);
        postResponse.then().statusCode(CREATED.getStatusCode());

        DocumentSpecificationDTO dto = postResponse.as(DocumentSpecificationDTO.class);
        assertThat(dto.getName()).isEqualTo(documentSpecificationCreateDTO.getName());
        assertThat(dto.getSpecificationVersion())
                .isEqualTo(documentSpecificationCreateDTO.getSpecificationVersion());
    }

    @Test
    @DisplayName("Saves specification of document without version.")
    void testSuccessfulCreateDocumentSpecificationWithoutVersion() {
        final String documentSpecificationName = "DOCUMENT_SPECIFICATION_NAME";
        DocumentSpecificationCreateUpdateDTO documentSpecificationCreateDTO = new DocumentSpecificationCreateUpdateDTO();
        documentSpecificationCreateDTO.setName(documentSpecificationName);
        documentSpecificationCreateDTO.setSpecificationVersion(null);

        Response postResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentSpecificationCreateDTO)
                .when()
                .post(BASE_PATH);
        postResponse.then().statusCode(CREATED.getStatusCode());

        DocumentSpecificationDTO dto = postResponse.as(DocumentSpecificationDTO.class);
        assertThat(dto.getName()).isEqualTo(documentSpecificationCreateDTO.getName());
    }

    @Test
    @DisplayName("Saves specification of document without name.")
    void testFailedCreateDocumentSpecificationWithoutName() {
        final String documentSpecificationVersion = "DOCUMENT_SPECIFICATION_VERSION";
        DocumentSpecificationCreateUpdateDTO documentSpecificationCreateDTO = new DocumentSpecificationCreateUpdateDTO();
        documentSpecificationCreateDTO.setName(null);
        documentSpecificationCreateDTO.setSpecificationVersion(documentSpecificationVersion);

        Response postResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentSpecificationCreateDTO)
                .when()
                .post(BASE_PATH);
        postResponse.then().statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    @DisplayName("Deletes specification of document by id.")
    void testSuccessfulDeleteSupportedMimeTypeById() {
        Response deleteResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .delete(BASE_PATH + "/" + EXISTING_DOCUMENT_SPECIFICATION_DELETE_ID);
        deleteResponse.then().statusCode(NO_CONTENT.getStatusCode());

        Response getResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH);
        getResponse.then().statusCode(OK.getStatusCode());

        List<DocumentSpecificationDTO> documentSpecificationDTOS = getResponse
                .as(getDocumentSpecificationDTOTypeRef());
        assertThat(documentSpecificationDTOS).hasSize(2);
    }

    @Test
    @DisplayName("Returns exception when trying to delete specification of document assigned to the document.")
    void testFailedDeleteDocumentSpecificationWithAssignedId() {
        Response deleteResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .when()
                .delete(BASE_PATH + "/" + EXISTING_DOCUMENT_SPECIFICATION_ID);
        deleteResponse.then().statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    @DisplayName("Returns exception when trying to delete specification of document for a nonexistent id.")
    void testFailedDeleteDocumentSpecificationById() {
        Response deleteResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .delete(BASE_PATH + "/" + NONEXISTENT_DOCUMENT_SPECIFICATION_ID);
        deleteResponse.then().statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("Updates name and version in specification of document.")
    void testSuccessfulUpdateDocumentSpecification() {
        final String documentSpecificationName = "TEST_UPDATE_SUPPORTED_MIME_TYPE_NAME";
        final String documentSpecificationVersion = "TEST_UPDATE_SUPPORTED_MIME_TYPE_DESCRIPTION";
        DocumentSpecificationCreateUpdateDTO documentSpecificationUpdateDTO = new DocumentSpecificationCreateUpdateDTO();
        documentSpecificationUpdateDTO.setName(documentSpecificationName);
        documentSpecificationUpdateDTO.setSpecificationVersion(documentSpecificationVersion);

        Response putResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentSpecificationUpdateDTO)
                .when()
                .put(BASE_PATH + "/" + EXISTING_DOCUMENT_SPECIFICATION_ID);
        putResponse.then().statusCode(OK.getStatusCode());

        DocumentSpecificationDTO dto = putResponse.as(DocumentSpecificationDTO.class);
        assertThat(dto.getId()).isEqualTo(EXISTING_DOCUMENT_SPECIFICATION_ID);
        assertThat(dto.getName()).isEqualTo(documentSpecificationUpdateDTO.getName());
        assertThat(dto.getSpecificationVersion())
                .isEqualTo(documentSpecificationUpdateDTO.getSpecificationVersion());
    }

    @Test
    @DisplayName("Returns exception when trying to update specification of document for a nonexistent id.")
    void testFailedUpdateDocumentSpecificationById() {
        final String documentSpecificationName = "TEST_UPDATE_SUPPORTED_MIME_TYPE_NAME";
        final String documentSpecificationVersion = "TEST_UPDATE_SUPPORTED_MIME_TYPE_DESCRIPTION";
        DocumentSpecificationCreateUpdateDTO documentSpecificationUpdateDTO = new DocumentSpecificationCreateUpdateDTO();
        documentSpecificationUpdateDTO.setName(documentSpecificationName);
        documentSpecificationUpdateDTO.setSpecificationVersion(documentSpecificationVersion);

        Response putResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentSpecificationUpdateDTO)
                .when()
                .put(BASE_PATH + "/" + NONEXISTENT_DOCUMENT_SPECIFICATION_ID);
        putResponse.then().statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("Gets all specifications of document.")
    void testSuccessfulGetAllDocumentSpecifications() {
        Response getResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH);
        getResponse.then().statusCode(OK.getStatusCode());

        List<DocumentSpecificationDTO> documentSpecificationDTOS = getResponse
                .as(getDocumentSpecificationDTOTypeRef());
        assertThat(documentSpecificationDTOS).hasSize(3);
        assertThat(documentSpecificationDTOS.get(0).getId()).isEqualTo(EXISTING_DOCUMENT_SPECIFICATION_ID);
        assertThat(documentSpecificationDTOS.get(0).getName()).isEqualTo(NAME_OF_DOCUMENT_SPECIFICATION_1);
        assertThat(documentSpecificationDTOS.get(0).getSpecificationVersion())
                .isEqualTo(VERSION_OF_DOCUMENT_SPECIFICATION_1);
    }

    @Test
    @DisplayName("Returns document specification by id.")
    void testSuccessfulGetDocumentSpecification() {
        Response response = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(BASE_PATH + "/" + EXISTING_DOCUMENT_SPECIFICATION_ID);

        response.then().statusCode(200);
        DocumentSpecificationDTO documentSpecificationDTO = response.as(DocumentSpecificationDTO.class);

        assertThat(documentSpecificationDTO.getId()).isEqualTo(EXISTING_DOCUMENT_SPECIFICATION_ID);
        assertThat(documentSpecificationDTO.getName()).isEqualTo(NAME_OF_DOCUMENT_SPECIFICATION_1);
        assertThat(documentSpecificationDTO.getSpecificationVersion())
                .isEqualTo(VERSION_OF_DOCUMENT_SPECIFICATION_1);
    }

    @Test
    @DisplayName("Returns exception when trying to get document specification for a nonexistent id.")
    void testFailedGetDocumentSpecification() {
        Response response = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .when()
                .get(BASE_PATH + "/" + NONEXISTENT_DOCUMENT_SPECIFICATION_ID);

        response.then().statusCode(NOT_FOUND.getStatusCode());
    }

    private TypeRef<List<DocumentSpecificationDTO>> getDocumentSpecificationDTOTypeRef() {
        return new TypeRef<>() {
        };
    }
}
