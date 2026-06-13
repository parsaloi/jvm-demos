package com.jvmdemos.disbursement.svc;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.StructuredTaskScope;

@Service
public class DisbursementOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(DisbursementOrchestrator.class);
    private final BatchRepository repository;
    private final TelcoWalletClient telcoClient;

    public DisbursementOrchestrator(BatchRepository repository, TelcoWalletClient telcoClient) {
        this.repository = repository;
        this.telcoClient = telcoClient;
    }

    @Transactional
    public String processBatch(List<String> phoneNumbers) {
        DisbursementBatch batch = repository.save(new DisbursementBatch(phoneNumbers, "PENDING"));
        log.info("🚀 ORCHESTRATOR: Database locked. Batch {} inserted as PENDING.", batch.getId());

        // Snapshot ThreadLocals (e.g., MDC traceId)
        ContextSnapshot snapshot = ContextSnapshotFactory.builder().build().captureAll();

        try (var scope = StructuredTaskScope.open()) {

            List<StructuredTaskScope.Subtask<String>> tasks = phoneNumbers.stream()
                .map(phone -> scope.fork(() -> {
                    // Restore context inside the Virtual Thread
                    try (ContextSnapshot.Scope ctx = snapshot.setThreadLocals()) {
                        return telcoClient.sendFunds(phone, 1000.0);
                    }
                })).toList();

            scope.join(); // Synchronization Point

            batch.setStatus("SUCCESS");
            repository.save(batch);
            log.info("✅ ORCHESTRATOR: DB Commit SUCCESS for Batch {}", batch.getId());
            return "Batch Processed: " + batch.getId();

        } catch (StructuredTaskScope.FailedException e) {
            log.error("⚠️ ORCHESTRATOR: Fast-fail triggered! Exception: {}", e.getCause().getMessage());
            // Because we are in @Transactional, throwing a RuntimeException triggers a DB rollback
            throw new RuntimeException("Batch Failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted", e);
        }
    }
}
