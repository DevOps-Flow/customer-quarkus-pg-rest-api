package com.labsafer.customer.web;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
    @Override
    public Response toResponse(NotFoundException ex) {
        return Response.status(404)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("error", "not_found", "message", ex.getMessage()))
                .build();
    }
}
