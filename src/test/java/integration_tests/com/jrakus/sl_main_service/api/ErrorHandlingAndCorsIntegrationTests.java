package com.jrakus.sl_main_service.api;

import com.jrakus.sl_main_service.properties.CorsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlingAndCorsIntegrationTests extends ApiIntegrationTestBase {

    @Autowired
    private CorsProperties corsProperties;

    @Test
    void shouldRejectMalformedJson() {

        // given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // when
        ResponseEntity<String> response = rest.exchange(
                "/api/v1/recipes/" + USER_ID, HttpMethod.POST, new HttpEntity<>("{not json", headers), String.class
        );

        // then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void shouldRejectRecipeWithoutRequiredFields() {

        // when
        ResponseEntity<String> response = post("/recipes/" + USER_ID, java.util.Map.of(), String.class);

        // then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void shouldReturnNotFoundForUnknownRoute() {

        // when
        ResponseEntity<String> response = get("/does-not-exist", String.class);

        // then
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void shouldAllowTrustedOriginInCorsPreflight() {

        // given
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin(corsProperties.getTrustOrigin());
        headers.set(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");

        // when
        ResponseEntity<String> response = rest.exchange(
                "/api/v1/recipes/" + USER_ID, HttpMethod.OPTIONS, new HttpEntity<>(headers), String.class
        );

        // then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(corsProperties.getTrustOrigin(), response.getHeaders().getAccessControlAllowOrigin());
    }

    @Test
    void shouldRejectUntrustedOriginInCorsPreflight() {

        // given
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin("http://evil.example.com");
        headers.set(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");

        // when
        ResponseEntity<String> response = rest.exchange(
                "/api/v1/recipes/" + USER_ID, HttpMethod.OPTIONS, new HttpEntity<>(headers), String.class
        );

        // then
        assertEquals(403, response.getStatusCode().value());
        assertNull(response.getHeaders().getAccessControlAllowOrigin());
    }

    @Test
    void shouldReportHealthAsUp() {

        // when
        ResponseEntity<String> response = rest.getForEntity("/actuator/health", String.class);

        // then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("\"status\":\"UP\""));
    }

    @Test
    void shouldServeOpenApiSpecification() {

        // when
        ResponseEntity<String> response = rest.getForEntity("/api/api.yaml", String.class);

        // then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("openapi"));
    }
}
