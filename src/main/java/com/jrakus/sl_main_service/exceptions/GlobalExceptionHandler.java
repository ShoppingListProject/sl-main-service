package com.jrakus.sl_main_service.exceptions;

import com.jrakus.sl_main_service.exceptions.specific_exceptions.AppException;
import org.openapitools.model.Code;
import org.openapitools.model.ErrorResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException appException) {
        return ResponseEntity
                .status(appException.getHttpStatus())
                .body(appException.getErrorResponse());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {

        ErrorResponse errorResponse = new ErrorResponse(
                Code.UNKNOWN,
                "Internal server error occurred."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    // TODO: Add this when you add logging to the app
//    @Override
//    protected ResponseEntity<Object> handleExceptionInternal(
//            Exception ex, Object body, HttpHeaders headers,
//            HttpStatusCode statusCode, WebRequest request) {
//        if (statusCode.is5xxServerError()) {
//            log.error("Server error while handling request", ex);
//        }
//        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
//    }
}
