package com.labsafer.customer.service;

import com.labsafer.customer.dto.CustomerDTO;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    CustomerDTO create(CustomerDTO dto);
    CustomerDTO get(UUID id);
    List<CustomerDTO> list(int page, int size);
    CustomerDTO update(UUID id, CustomerDTO dto);
    void delete(UUID id);
}
