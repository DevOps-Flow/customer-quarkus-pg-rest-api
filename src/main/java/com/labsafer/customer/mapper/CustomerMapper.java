package com.labsafer.customer.mapper;

import com.labsafer.customer.domain.Customer;
import com.labsafer.customer.dto.CustomerDTO;

public class CustomerMapper {
    public static Customer toEntity(CustomerDTO dto) {
        if (dto == null) return null;
        return new Customer(
            dto.getId(),
            dto.getLastName(),
            dto.getMiddleName(),
            dto.getFirstName(),
            dto.getEmail(),
            dto.getMobile()
        );
    }

    public static CustomerDTO toDTO(Customer entity) {
        if (entity == null) return null;
        return new CustomerDTO(
            entity.getId(),
            entity.getLastName(),
            entity.getMiddleName(),
            entity.getFirstName(),
            entity.getEmail(),
            entity.getMobile()
        );
    }

    public static void updateEntity(Customer entity, CustomerDTO dto) {
        if (dto.getLastName() != null) entity.setLastName(dto.getLastName());
        if (dto.getMiddleName() != null) entity.setMiddleName(dto.getMiddleName());
        if (dto.getFirstName() != null) entity.setFirstName(dto.getFirstName());
        if (dto.getEmail() != null) entity.setEmail(dto.getEmail());
        if (dto.getMobile() != null) entity.setMobile(dto.getMobile());
    }
}

