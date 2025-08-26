package com.labsafer.customer.service;

import com.labsafer.customer.domain.Customer;
import com.labsafer.customer.dto.CustomerDTO;
import com.labsafer.customer.mapper.CustomerMapper;
import com.labsafer.customer.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CustomerServiceImpl implements CustomerService {

    @Inject
    CustomerRepository repository;

    @Transactional
    @Override
    public CustomerDTO create(CustomerDTO dto) {
        if (dto.getId() == null) dto.setId(UUID.randomUUID());
        repository.findByIdOptional(dto.getId()).ifPresent(c -> {
            throw new WebApplicationException("ID já existente", 409);
        });
        repository.findByEmail(dto.getEmail()).ifPresent(c -> {
            throw new WebApplicationException("Email já cadastrado", 409);
        });
        Customer entity = CustomerMapper.toEntity(dto);
        repository.persist(entity);
        return CustomerMapper.toDTO(entity);
    }

    @Override
    public CustomerDTO get(UUID id) {
        Customer entity = repository.findByIdOptional(id)
            .orElseThrow(() -> new WebApplicationException("Cliente não encontrado", 404));
        return CustomerMapper.toDTO(entity);
    }

    @Override
    public List<CustomerDTO> list(int page, int size) {
        return repository.findAll().page(page, size).list()
                .stream().map(CustomerMapper::toDTO).toList();
    }

    @Transactional
    @Override
    public CustomerDTO update(UUID id, CustomerDTO dto) {
        Customer entity = repository.findByIdOptional(id)
            .orElseThrow(() -> new WebApplicationException("Cliente não encontrado", 404));
        if (dto.getEmail() != null && !dto.getEmail().equals(entity.getEmail())) {
            repository.findByEmail(dto.getEmail()).ifPresent(c -> {
                throw new WebApplicationException("Email já cadastrado", 409);
            });
        }
        CustomerMapper.updateEntity(entity, dto);
        return CustomerMapper.toDTO(entity);
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        Customer entity = repository.findByIdOptional(id)
            .orElseThrow(() -> new WebApplicationException("Cliente não encontrado", 404));
        repository.delete(entity);
    }
}
