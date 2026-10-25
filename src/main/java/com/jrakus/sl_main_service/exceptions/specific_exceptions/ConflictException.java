package com.jrakus.sl_main_service.exceptions.specific_exceptions;

import org.openapitools.model.Code;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class ConflictException extends AppException {
    private final static HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;

    public ConflictException(String message, Code code) {
        super(message, code);
    }

    public ConflictException(String message, Code code, Map<String, String> parameters) {
        super(message, code, parameters);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HTTP_STATUS;
    }
}
