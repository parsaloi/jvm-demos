package com.jvmdemos.disbursement.svc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TelcoWalletClient {
    private static final Logger log = LoggerFactory.getLogger(TelcoWalletClient.class);

    public String sendFunds(String phoneNumber, double amount) throws InterruptedException {
        log.info("🌐 NETWORK: Initiating transfer of KES {} to {}...", amount, phoneNumber);

        if (phoneNumber.endsWith("999")) {
            Thread.sleep(50);
            log.error("❌ NETWORK ERROR: Account Blocked for {}!", phoneNumber);
            throw new RuntimeException("Account Suspended: " + phoneNumber);
        }

        Thread.sleep(400); // Simulate API latency
        String receipt = "TRX-" + System.currentTimeMillis();
        log.info("✅ NETWORK SUCCESS: Funds delivered to {}. Receipt: {}", phoneNumber, receipt);
        return receipt;
    }
}
