package com.jrakus.sl_main_service.controllers;

import com.jrakus.sl_main_service.properties.ConstantsProperties;
import org.openapitools.api.ConstantsApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ConstantController implements ConstantsApi {

    ConstantsProperties constantsProperties;

    public ConstantController(ConstantsProperties constantsProperties) {
        this.constantsProperties = constantsProperties;
    }

    @Override
    public ResponseEntity<List<String>> getAllCategories() {

        List<String> categories = constantsProperties.getCategories();

        return ResponseEntity.ok(categories);
    }

    @Override
    public ResponseEntity<List<String>> getAllUnits() {

        List<String> units = constantsProperties.getUnits();

        return ResponseEntity.ok(units);
    }
}
