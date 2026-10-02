package com.dobebets.api.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Stores a customer's phone-based account credentials.
 */
@Entity
@Table(name = "customer_accounts")
public class CustomerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Store phone numbers in international format, such as +255712345678.
    @Column(name = "phone_number", nullable = false, unique = true, length = 16)
    private String phoneNumber;

    // Store only a password hash here, never the customer's plain password.
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountRole role = AccountRole.CUSTOMER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected CustomerAccount() {
        // JPA requires a no-argument constructor.
    }

    public CustomerAccount(String phoneNumber, String passwordHash) {
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.role = AccountRole.CUSTOMER;
    }

    @PrePersist
    void setCreatedAt() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AccountRole getRole() {
        return role;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}