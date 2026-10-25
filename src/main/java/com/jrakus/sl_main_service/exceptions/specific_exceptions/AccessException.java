package com.jrakus.sl_main_service.exceptions.specific_exceptions;

import org.openapitools.model.Code;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class AccessException extends AppException {
    private final static HttpStatus HTTP_STATUS = HttpStatus.FORBIDDEN;

    public AccessException(String message, Code code) {
        super(message, code);
    }

    public AccessException(String message, Code code, Map<String, String> parameters) {
        super(message, code, parameters);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return null;
    }
}
