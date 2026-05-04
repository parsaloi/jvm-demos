package com.example.logistics.shipping.internal;

import com.example.logistics.orders.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class ShippingListener {
    private static final Logger log = LoggerFactory.getLogger(ShippingListener.class);

    @ApplicationModuleListener
    void on(OrderPlacedEvent event) throws InterruptedException {
        log.info("🚚 Shipping Module: Assigning rider for order {} [Thread: {}]", 
                 event.orderId(), Thread.currentThread().getName());
        
        // Simulating heavy work to prove it doesn't block the main thread
        Thread.sleep(2000); 
        
        log.info("✅ Shipping Module: Rider assigned for order {}", event.orderId());
    }
}