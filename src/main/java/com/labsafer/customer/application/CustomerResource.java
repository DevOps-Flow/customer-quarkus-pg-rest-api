package com.labsafer.customer.application;

import com.labsafer.customer.dto.CustomerDTO;
import com.labsafer.customer.service.CustomerService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    CustomerService service;

    @POST
    public Response create(@Valid CustomerDTO dto) {
        CustomerDTO created = service.create(dto);
        return Response.created(URI.create("/api/v1/customers/" + created.getId())).entity(created).build();
    }

    @GET
    @Path("/{id}")
    public CustomerDTO get(@PathParam("id") UUID id) {
        return service.get(id);
    }

    @GET
    public List<CustomerDTO> list(@QueryParam("page") @DefaultValue("0") int page,
                                  @QueryParam("size") @DefaultValue("20") int size) {
        return service.list(page, size);
    }

    @PUT
    @Path("/{id}")
    public CustomerDTO update(@PathParam("id") UUID id, @Valid CustomerDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
