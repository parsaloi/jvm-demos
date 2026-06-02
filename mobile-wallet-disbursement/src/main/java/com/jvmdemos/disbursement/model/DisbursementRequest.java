package com.jvmdemos.disbursement.model;

import java.math.BigDecimal;

/**
 * Represents the incoming API payload for a single transfer.
 */
public record DisbursementRequest(
    String phoneNumber,
    BigDecimal amount
) {
    public DisbursementRequest {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Disbursement amount must be greater than zero.");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be blank.");
        }
    }
}
