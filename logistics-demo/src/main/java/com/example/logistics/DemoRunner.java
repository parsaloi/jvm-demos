package com.example.logistics;

import com.example.logistics.orders.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DemoRunner implements CommandLineRunner {
    private final OrderService orderService;

    public DemoRunner(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n=================================================");
        System.out.println("📦 PLACING NEW ORDER...");
        System.out.println("=================================================\n");
        orderService.placeOrder("jug-nairobi@example.com", new BigDecimal("1550.00"));
    }
}