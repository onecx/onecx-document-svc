package org.tkit.onecx.document.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.tkit.onecx.document.test.AbstractTest.USER;

import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.document.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithDBData(value = { "document-test-data.xml" }, deleteBeforeInsert = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = USER, scopes = { "ocx-doc:read", "ocx-doc:write", "ocx-doc:delete" })
@TestHTTPEndpoint(SupportedMimeTypeController.class)
class SupportedMimeTypeControllerTest extends AbstractTest {

    @Test
    @DisplayName("Gets all supported mime-types.")
    void testSuccessfulGetAllSupportedMimeTypes() {
        var getResponse = given().auth()
                .oauth2(keycloakTestClient.getClientAccessToken(USER))
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get().then().statusCode(OK.getStatusCode()).extract().as(String[].class);
        assertThat(getResponse).hasSize(3);
    }
}
