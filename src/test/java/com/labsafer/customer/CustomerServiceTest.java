package com.labsafer.customer;

import com.labsafer.customer.dto.CustomerDTO;
import com.labsafer.customer.service.CustomerService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class CustomerServiceTest {

    @Inject
    CustomerService service;

    @Test
    void crud() {
        CustomerDTO dto = new CustomerDTO(null,"Doe",null,"Jane","jane.doe@example.com","+55 11 90000-1111");
        CustomerDTO created = service.create(dto);
        assertNotNull(created.getId());

        CustomerDTO got = service.get(created.getId());
        assertEquals("Jane", got.getFirstName());

        CustomerDTO upd = new CustomerDTO(null,null,null,null,"jane.d@example.com",null);
        CustomerDTO updated = service.update(created.getId(), upd);
        assertEquals("jane.d@example.com", updated.getEmail());

        service.delete(created.getId());
    }
}
