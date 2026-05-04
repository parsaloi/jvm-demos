package com.example.logistics.billing.internal;

import com.example.logistics.orders.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class BillingListener {
    private static final Logger log = LoggerFactory.getLogger(BillingListener.class);

    @ApplicationModuleListener
    void on(OrderPlacedEvent event) throws InterruptedException {
        log.info("💰 Billing Module: Generating invoice for {} [Thread: {}]", 
                 event.customerEmail(), Thread.currentThread().getName());
        
        Thread.sleep(1000);
        
        log.info("✅ Billing Module: Invoice generated for order {}", event.orderId());
    }
}