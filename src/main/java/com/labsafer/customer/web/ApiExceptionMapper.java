package com.labsafer.customer.web;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Override
    public Response toResponse(WebApplicationException ex) {
        int status = ex.getResponse() != null ? ex.getResponse().getStatus() : 500;
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("error", ex.getMessage(), "status", status))
                .build();
    }
}
