package com.jvmdemos.disbursement.service;

import com.jvmdemos.disbursement.client.TelcoWalletClient;
import com.jvmdemos.disbursement.model.*;
import com.jvmdemos.disbursement.repository.DisbursementRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.StructuredTaskScope;

public class SagaDisbursementOrchestrator {

    private final DisbursementRepository repository;
    private final TelcoWalletClient telcoClient;

    public SagaDisbursementOrchestrator(DisbursementRepository repository, TelcoWalletClient telcoClient) {
        this.repository = repository;
        this.telcoClient = telcoClient;
    }

    public DisbursementResult processBatch(DisbursementBatch batch) {
        System.out.printf("[%s] 🚀 SAGA ORCHESTRATOR: Starting processing for Batch %s%n",
                Thread.currentThread(), batch.batchId());

        // 1. CARRIER THREAD: Initialize Batch and Items in DB
        repository.insertBatch(batch);
        batch.items().forEach(repository::insertItem);

        // 2. ORCHESTRATION: Open scope with our custom Saga Joiner
        PartialSuccessJoiner<String> joiner = new PartialSuccessJoiner<>();

        try (var scope = StructuredTaskScope.open(joiner)) {

            // Map Subtasks to Items so we can correlate failures later
            Map<StructuredTaskScope.Subtask<String>, DisbursementItem> taskToItemMap = new HashMap<>();

            for (DisbursementItem item : batch.items()) {
                var subtask = scope.fork(() -> telcoClient.sendFunds(
                        new DisbursementRequest(item.phoneNumber(), item.amount())));
                taskToItemMap.put(subtask, item);
            }

            // 3. SYNCHRONIZATION: Waits for all to finish (no short-circuiting)
            List<StructuredTaskScope.Subtask<String>> results = scope.join();

            // 4. EVALUATION & COMPENSATION: Process individual results
            int successCount = 0;
            int failCount = 0;

            for (var subtask : results) {
                DisbursementItem item = taskToItemMap.get(subtask);

                if (subtask.state() == StructuredTaskScope.Subtask.State.SUCCESS) {
                    repository.updateItemStatus(item.itemId(), TransactionStatus.SUCCESS, null);
                    successCount++;
                } else if (subtask.state() == StructuredTaskScope.Subtask.State.FAILED) {
                    String reason = subtask.exception().getMessage();
                    repository.updateItemStatus(item.itemId(), TransactionStatus.FAILED, reason);
                    failCount++;
                }
            }

            // Determine final aggregate batch status
            TransactionStatus finalStatus = failCount == 0 ? TransactionStatus.SUCCESS
                                          : (successCount > 0 ? TransactionStatus.PARTIAL_SUCCESS : TransactionStatus.FAILED);

            repository.updateBatchStatus(batch.batchId(), finalStatus, failCount + " items failed.");

            return new DisbursementResult(batch.batchId(), finalStatus,
                String.format("Batch Saga completed: %d succeeded, %d failed.", successCount, failCount));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            repository.updateBatchStatus(batch.batchId(), TransactionStatus.FAILED, "System Interrupted");
            throw new RuntimeException("Saga interrupted", e);
        }
    }
}
