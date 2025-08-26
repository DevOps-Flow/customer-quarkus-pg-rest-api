package com.labsafer.customer.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "email", nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "mobile", length = 50)
    private String mobile;

    public Customer() {}

    public Customer(UUID id, String lastName, String middleName, String firstName, String email, String mobile) {
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
