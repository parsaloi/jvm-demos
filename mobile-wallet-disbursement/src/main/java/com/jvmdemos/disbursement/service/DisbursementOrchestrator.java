package com.jvmdemos.disbursement.service;

import com.jvmdemos.disbursement.client.TelcoWalletClient;
import com.jvmdemos.disbursement.model.DisbursementBatch;
import com.jvmdemos.disbursement.model.DisbursementItem;
import com.jvmdemos.disbursement.model.DisbursementRequest;
import com.jvmdemos.disbursement.model.DisbursementResult;
import com.jvmdemos.disbursement.model.TransactionStatus;
import com.jvmdemos.disbursement.repository.DisbursementRepository;

import java.util.List;
import java.util.concurrent.StructuredTaskScope;

/**
 * Orchestrates the bulk disbursement process matching the Java 25 (JEP 505) Preview API.
 */
public class DisbursementOrchestrator {

    private final DisbursementRepository repository;
    private final TelcoWalletClient telcoClient;

    public DisbursementOrchestrator(DisbursementRepository repository, TelcoWalletClient telcoClient) {
        this.repository = repository;
        this.telcoClient = telcoClient;
    }

    public DisbursementResult processBatch(DisbursementBatch batch) {
        System.out.printf("[%s] 🚀 ORCHESTRATOR: Starting processing for Batch %s%n",
                Thread.currentThread(), batch.batchId());

        // 1. CARRIER THREAD: Initialize state in the DB
        repository.insertBatch(batch);

        // 2. ORCHESTRATION: Open the scope matching JEP 505
        try (var scope = StructuredTaskScope.open()) {

            // 3. FAN-OUT: Fork virtual threads for network I/O
            List<StructuredTaskScope.Subtask<String>> subtasks = batch.items().stream()
                    .map(item -> scope.fork(() -> telcoClient.sendFunds(
                            new DisbursementRequest(item.phoneNumber(), item.amount()))))
                    .toList();

            // 4. SYNCHRONIZATION: In JDK 25, the default open() policy causes join()
            // to throw FailedException immediately if any subtask fails.
            scope.join();

            // 5. HAPPY PATH: All subtasks succeeded
            repository.updateBatchStatus(batch.batchId(), TransactionStatus.SUCCESS, null);

            return new DisbursementResult(
                    batch.batchId(),
                    TransactionStatus.SUCCESS,
                    "All " + subtasks.size() + " transfers completed successfully."
            );

        } catch (StructuredTaskScope.FailedException e) {
            // 6. FAST-FAIL PATH: Specifically catch JEP 505's FailedException
            String failureReason = e.getCause() != null ? e.getCause().getMessage() : "Subtask failed";
            System.out.printf("[%s] ⚠️ ORCHESTRATOR: Scope short-circuited via FailedException! Rolling back. Reason: %s%n",
                    Thread.currentThread(), failureReason);

            // CARRIER THREAD: Execute compensation
            repository.updateBatchStatus(batch.batchId(), TransactionStatus.FAILED, failureReason);

            return new DisbursementResult(
                    batch.batchId(),
                    TransactionStatus.FAILED,
                    "Batch failed mid-flight due to: " + failureReason
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            repository.updateBatchStatus(batch.batchId(), TransactionStatus.FAILED, "System Interrupted");
            throw new RuntimeException("Disbursement processing was unexpectedly interrupted", e);
        }
    }
}
