package com.jvmdemos.disbursement.model;

public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    PARTIAL_SUCCESS // Reserved for the Flow D (Saga) scenario
}
