package com.labsafer.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class CustomerDTO {
    private UUID id;

    @NotBlank @Size(max = 100)
    private String lastName;

    @Size(max = 100)
    private String middleName;

    @NotBlank @Size(max = 100)
    private String firstName;

    @NotBlank @Email @Size(max = 150)
    private String email;

    @Size(max = 50)
    private String mobile;

    public CustomerDTO() {}

    public CustomerDTO(UUID id, String lastName, String middleName, String firstName, String email, String mobile) {
        this.id = id;
        this.lastName = lastName;
        this.middleName = middleName;
        this.firstName = firstName;
        this.email = email;
        this.mobile = mobile;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String v) { this.middleName = v; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getMobile() { return mobile; }
    public void setMobile(String v) { this.mobile = v; }
}
