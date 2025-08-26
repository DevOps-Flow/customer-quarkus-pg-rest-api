package com.labsafer.customer.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.Map;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException ex) {
        List<Map<String, String>> violations = ex.getConstraintViolations().stream()
            .map(v -> Map.of(
                "property", v.getPropertyPath().toString(),
                "message", v.getMessage()
            )).toList();
        return Response.status(400)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("error", "validation_failed", "violations", violations))
                .build();
    }
}
