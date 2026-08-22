package org.example.ecommerceapplication.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.ecommerceapplication.response.ApiResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            @NonNull AccessDeniedException exception
    ) throws IOException {

        String message = request.getRequestURI().equals("/api/v1/categories")
                ? "Access denied: an ADMIN role is required to create a category."
                : "Access denied: you do not have permission to perform this action.";

        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .status(HttpStatus.FORBIDDEN.value())
                .payload(null)
                .timestamp(Instant.now())
                .build();
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
