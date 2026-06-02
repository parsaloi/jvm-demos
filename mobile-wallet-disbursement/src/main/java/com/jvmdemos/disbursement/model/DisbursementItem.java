package com.jvmdemos.disbursement.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a persisted, trackable individual transaction within a batch.
 */
public record DisbursementItem(
    UUID itemId,
    UUID batchId,
    String phoneNumber,
    BigDecimal amount,
    TransactionStatus status,
    String failureReason
) {
    // Helper method to create a new item from a request
    public static DisbursementItem fromRequest(UUID batchId, DisbursementRequest request) {
        return new DisbursementItem(
            UUID.randomUUID(),
            batchId,
            request.phoneNumber(),
            request.amount(),
            TransactionStatus.PENDING,
            null
        );
    }
}
