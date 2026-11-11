package com.jrakus.sl_main_service.exceptions;

import com.jrakus.sl_main_service.exceptions.specific_exceptions.AppException;
import org.openapitools.model.Code;
import org.openapitools.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException appException) {
        return ResponseEntity
                .status(appException.getHttpStatus())
                .body(appException.getErrorResponse());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {

        log.error("Unexpected exception while handling request", exception);

        ErrorResponse errorResponse = new ErrorResponse(
                Code.UNKNOWN,
                "Internal server error occurred."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        if (statusCode.is5xxServerError()) {
            log.error("Server error while handling request", ex);
        }
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }
}
