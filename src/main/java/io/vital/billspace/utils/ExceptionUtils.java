package io.vital.billspace.utils;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vital.billspace.exception.APIException;
import io.vital.billspace.model.HttpResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import java.io.InvalidClassException;
import java.io.OutputStream;
import java.time.LocalDateTime;

@Slf4j
public class ExceptionUtils {
    public static <HttpServletRequest> void processError(HttpServletRequest request,
                                                         HttpServletResponse response, Exception ex) {
        HttpResponse httpResponse;
        if(ex instanceof APIException || ex instanceof DisabledException || ex instanceof LockedException ||
                ex instanceof InvalidClassException || ex instanceof BadCredentialsException){
            httpResponse = getHttpResponse(response, ex.getMessage(), HttpStatus.BAD_REQUEST);
        }else if (ex instanceof TokenExpiredException){
            httpResponse = getHttpResponse(response, ex.getMessage(), HttpStatus.UNAUTHORIZED);
        } else{
            httpResponse = getHttpResponse(response, "An error occurred. Please Try Again.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        writeResponse(response, httpResponse);
        log.error(ex.getMessage());

    }

    private static void writeResponse(HttpServletResponse response, HttpResponse httpResponse) {
        OutputStream outputStream;
        try {
            outputStream = response.getOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(outputStream, httpResponse);
            outputStream.flush();
        }catch (Exception ex){
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static HttpResponse getHttpResponse(HttpServletResponse response, String message,
                                                HttpStatus httpStatus) {
        HttpResponse httpResponse = HttpResponse.builder()
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .reason("Authorization Failure.")
                .statusCode(httpStatus.value())
                .httpStatus(httpStatus)
                .build();
        response.setContentType(MediaType.APPLICATION_JSON.getType());
        response.setStatus(httpStatus.value());
        return httpResponse;
    }


}
