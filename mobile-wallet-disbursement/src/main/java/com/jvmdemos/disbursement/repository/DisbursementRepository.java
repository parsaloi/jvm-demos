package com.jvmdemos.disbursement.repository;

import com.jvmdemos.disbursement.model.DisbursementBatch;
import com.jvmdemos.disbursement.model.TransactionStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Handles all state transitions for disbursements.
 * Executed STRICTLY on the parent carrier thread to prevent JDBC/Virtual Thread pinning and pool exhaustion.
 */
public class DisbursementRepository {

    private final DataSource dataSource;

    public DisbursementRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Initializes the batch in the database BEFORE the StructuredTaskScope opens.
     */
    public void insertBatch(DisbursementBatch batch) {
        String sql = "INSERT INTO disbursement_batch (batch_id, status, created_at) VALUES (?, ?, ?)";

        // try-with-resources ensures the connection is immediately returned to HikariCP
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, batch.batchId());
            stmt.setString(2, batch.status().name());
            stmt.setTimestamp(3, Timestamp.from(batch.createdAt()));

            stmt.executeUpdate();

            System.out.printf("[%s] 🟢 DB COMMIT: Inserted Batch %s as PENDING.%n",
                    Thread.currentThread(), batch.batchId());

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize disbursement batch in database", e);
        }
    }

    /**
     * Updates the batch status AFTER the StructuredTaskScope either succeeds or short-circuits.
     */
    public void updateBatchStatus(UUID batchId, TransactionStatus status, String failureReason) {
        String sql = "UPDATE disbursement_batch SET status = ?, failure_reason = ? WHERE batch_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setString(2, failureReason);
            stmt.setObject(3, batchId);

            stmt.executeUpdate();

            String statusIcon = status == TransactionStatus.SUCCESS ? "✅" : "❌";
            System.out.printf("[%s] %s DB COMMIT: Updated Batch %s to %s. Reason: %s%n",
                    Thread.currentThread(), statusIcon, batchId, status.name(),
                    failureReason != null ? failureReason : "None");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update disbursement batch status", e);
        }
    }
}
