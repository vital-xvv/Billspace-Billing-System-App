package io.vital.billspace.exception;

import io.vital.billspace.model.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ExceptionHandler extends ResponseEntityExceptionHandler
        implements ErrorController {

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        log.error(ex.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .reason(ex.getMessage())
                        .developerMessage(ex.toString())
                        .httpStatus(HttpStatus.resolve(statusCode.value()))
                        .message("User has been created.")
                        .statusCode(statusCode.value())
                        .build(), statusCode);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode statusCode,
                                                                  WebRequest request) {
        log.error(ex.getMessage());
        List<FieldError> errors = ex.getBindingResult().getFieldErrors();
        String message = errors.stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .reason(message)
                        .developerMessage(ex.toString())
                        .httpStatus(HttpStatus.resolve(statusCode.value()))
                        .statusCode(statusCode.value())
                        .build(), statusCode);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    protected ResponseEntity<HttpResponse> SQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .reason(ex.getMessage().contains("Duplicate entry") ? "Information already exists" : ex.getMessage())
                        .developerMessage(ex.toString())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build(), HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<HttpResponse> BadCredentialsException(BadCredentialsException ex) {
        log.error(ex.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .reason(ex.getMessage() + ", Incorrect email or password")
                        .developerMessage(ex.toString())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build(), HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(APIException.class)
    protected ResponseEntity<HttpResponse> APIException(APIException ex) {
        log.error(ex.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .reason(ex.getMessage())
                        .developerMessage(ex.toString())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build(), HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<HttpResponse> AccessDeniedException(AccessDeniedException ex) {
        log.error(ex.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .reason("Access denied")
                        .developerMessage(ex.toString())
                        .httpStatus(HttpStatus.FORBIDDEN)
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .build(), HttpStatus.FORBIDDEN);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(EmptyResultDataAccessException.class)
    protected ResponseEntity<HttpResponse> EmptyResultDataAccessException(EmptyResultDataAccessException ex) {
        log.error(ex.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .reason(ex.getMessage().contains("expected 1, actual 0") ? "Record not found" : ex.getMessage())
                        .developerMessage(ex.toString())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build(), HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    protected ResponseEntity<HttpResponse> Exception(Exception ex) {
        log.error(ex.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .reason(ex.getMessage())
                        .developerMessage(ex.toString())
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
