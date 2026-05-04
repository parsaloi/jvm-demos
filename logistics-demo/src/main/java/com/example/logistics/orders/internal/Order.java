package com.example.logistics.orders.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    private UUID id;
    private String customerEmail;
    private BigDecimal totalAmount;

    protected Order() {} // Required by JPA

    public Order(UUID id, String customerEmail, BigDecimal totalAmount) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
    }
}