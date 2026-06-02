package com.jvmdemos.disbursement.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The aggregate root representing the entire bulk disbursement operation.
 */
public record DisbursementBatch(
    UUID batchId,
    List<DisbursementItem> items,
    TransactionStatus status,
    String failureReason,
    Instant createdAt
) {
    // Helper to initialize a new batch
    public static DisbursementBatch initialize(List<DisbursementItem> items) {
        return new DisbursementBatch(
            UUID.randomUUID(),
            items,
            TransactionStatus.PENDING,
            null,
            Instant.now()
        );
    }
}
