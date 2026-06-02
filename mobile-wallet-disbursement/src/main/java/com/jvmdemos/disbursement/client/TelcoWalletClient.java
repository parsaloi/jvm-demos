package com.jvmdemos.disbursement.client;

import com.jvmdemos.disbursement.model.DisbursementRequest;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates an external Telco/Mobile Money API gateway.
 * Designed to be executed strictly by Virtual Threads within a StructuredTaskScope.
 */
public class TelcoWalletClient {

    /**
     * Executes the network transfer.
     * * @param request The disbursement payload.
     * @return A mock transaction receipt string.
     * @throws InterruptedException If the thread is interrupted by the TaskScope (Cancellation).
     */
    public String sendFunds(DisbursementRequest request) throws InterruptedException {
        String threadId = Thread.currentThread().toString();

        System.out.printf("[%s] 🌐 NETWORK: Initiating transfer of KES %s to %s...%n",
                threadId, request.amount(), request.phoneNumber());

        try {
            // 1. The Fast-Fail Trigger (Simulate a suspended account)
            if (request.phoneNumber().endsWith("999")) {
                // Tiny delay to ensure other virtual threads have started their "I/O"
                Thread.sleep(50);
                System.out.printf("[%s] ❌ NETWORK ERROR: Account Blocked for %s! Throwing exception.%n",
                        threadId, request.phoneNumber());
                throw new RuntimeException("Account Suspended: " + request.phoneNumber());
            }

            // 2. The Slow Task Trigger (Simulate a degraded downstream API)
            if (request.phoneNumber().endsWith("888")) {
                Thread.sleep(5000); // 5-second hang
            } else {
                // 3. The Happy Path (Standard API latency between 200ms and 600ms)
                long latency = ThreadLocalRandom.current().nextLong(200, 600);
                Thread.sleep(latency);
            }

            String receipt = "TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            System.out.printf("[%s] ✅ NETWORK SUCCESS: Funds delivered to %s. Receipt: %s%n",
                    threadId, request.phoneNumber(), receipt);

            return receipt;

        } catch (InterruptedException e) {
            // 4. The "Aha!" Moment: Catching the Scope Cancellation
            // When the scope shuts down on failure, it interrupts all sleeping/blocked virtual threads.
            System.out.printf("[%s] ⚡ CANCELLED: Network call to %s aborted mid-flight by Orchestrator!%n",
                    threadId, request.phoneNumber());

            // Re-interrupt the thread to preserve the signal for the JVM
            Thread.currentThread().interrupt();
            throw e;
        }
    }
}
