package com.jrakus.sl_main_service.api;

import com.jrakus.sl_main_service.properties.ConstantsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConstantsApiIntegrationTests extends ApiIntegrationTestBase {

    @Autowired
    private ConstantsProperties constantsProperties;

    @Test
    void shouldReturnConfiguredUnits() {

        // when
        ResponseEntity<List<String>> response = get("/constants/units", STRING_LIST);

        // then
        assertEquals(200, response.getStatusCode().value());
        assertFalse(constantsProperties.getUnits().isEmpty());
        assertEquals(constantsProperties.getUnits(), response.getBody());
    }

    @Test
    void shouldReturnConfiguredCategories() {

        // when
        ResponseEntity<List<String>> response = get("/constants/categories", STRING_LIST);

        // then
        assertEquals(200, response.getStatusCode().value());
        assertFalse(constantsProperties.getCategories().isEmpty());
        assertEquals(constantsProperties.getCategories(), response.getBody());
    }
}
