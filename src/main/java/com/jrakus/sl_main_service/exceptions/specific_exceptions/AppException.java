package com.jrakus.sl_main_service.exceptions.specific_exceptions;

import org.openapitools.model.Code;
import org.openapitools.model.ErrorResponse;
import org.springframework.http.HttpStatus;

import java.util.Map;

public abstract class AppException extends RuntimeException {
    private final Code code;
    private final Map<String, String> parameters;

    public AppException(String message, Code code) {
        this(message, code, null);
    }

    public AppException(String message, Code code, Map<String,String> parameters) {
        super(message);
        this.code = code;
        this.parameters = parameters;
    }

    public Code getCode() {
        return code;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public ErrorResponse getErrorResponse() {
        ErrorResponse errorResponse = new ErrorResponse(code, this.getMessage());
        errorResponse.setParams(this.parameters);

        return errorResponse;
    }

    public abstract HttpStatus getHttpStatus();
}
