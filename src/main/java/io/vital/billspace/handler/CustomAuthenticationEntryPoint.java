package io.vital.billspace.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vital.billspace.model.HttpResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper mapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        HttpResponse httpResponse = HttpResponse.builder()
                .message(authException.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .reason("You need to log in to access the resource.")
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .build();

        response.setContentType(MediaType.APPLICATION_JSON.getType());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        OutputStream outputStream = response.getOutputStream();
        mapper.writeValue(outputStream, httpResponse);
        outputStream.flush();

    }
}
