package com.example.logistics.orders;

import com.example.logistics.orders.internal.Order;
import com.example.logistics.orders.internal.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    private final OrderRepository repository;
    private final ApplicationEventPublisher events;

    // Standard constructor injection
    public OrderService(OrderRepository repository, ApplicationEventPublisher events) {
        this.repository = repository;
        this.events = events;
    }

    @Transactional
    public void placeOrder(String customerEmail, BigDecimal amount) {
        var orderId = UUID.randomUUID();
        log.info("📝 Order Module: Saving order {} to database...", orderId);
        repository.save(new Order(orderId, customerEmail, amount));

        // =====================================================================
        // 🚨 DEMO TRAP: UNCOMMENT TO TRIGGER ARCHITECTURE VIOLATION
        // We are going to attempt to directly instantiate an internal class 
        // from the Shipping module. Standard Spring allows this. Modulith does not.
        // =====================================================================
        //
        // var illegalAccess = new com.example.logistics.shipping.internal.ShippingListener();
        // log.warn("🚨 Wait, I successfully bypassed the event registry: {}", illegalAccess);
        //
        // =====================================================================

        log.info("📢 Order Module: Publishing OrderPlacedEvent...");
        events.publishEvent(new OrderPlacedEvent(orderId, customerEmail, amount));
    }
}
