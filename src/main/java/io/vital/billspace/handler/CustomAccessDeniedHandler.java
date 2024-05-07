package io.vital.billspace.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vital.billspace.model.HttpResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper mapper;
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        HttpResponse httpResponse = HttpResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .reason("You don't have enough permission.")
                .statusCode(HttpStatus.FORBIDDEN.value())
                .httpStatus(HttpStatus.FORBIDDEN)
                .build();

        response.setContentType(MediaType.APPLICATION_JSON.getType());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        OutputStream outputStream = response.getOutputStream();
        mapper.writeValue(outputStream, httpResponse);
        outputStream.flush();
    }
}
