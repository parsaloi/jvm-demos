package com.jvmdemos.disbursement.model;

import java.util.UUID;

/**
 * The final outcome returned by the Orchestrator after the scope closes.
 */
public record DisbursementResult(
    UUID batchId,
    TransactionStatus finalStatus,
    String message
) {}
