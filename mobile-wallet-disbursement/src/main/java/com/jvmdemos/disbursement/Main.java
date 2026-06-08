package com.jvmdemos.disbursement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.jvmdemos.disbursement.client.TelcoWalletClient;
import com.jvmdemos.disbursement.model.DisbursementBatch;
import com.jvmdemos.disbursement.model.DisbursementItem;
import com.jvmdemos.disbursement.model.DisbursementRequest;
import com.jvmdemos.disbursement.repository.DisbursementRepository;
import com.jvmdemos.disbursement.service.DisbursementOrchestrator;
import com.jvmdemos.disbursement.service.SagaDisbursementOrchestrator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Bootstrapping Java 25 Mobile Wallet Disbursement Demo ===");

        // 1. Configure HikariCP for your local PostgreSQL cluster
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres"); // Update DB name if needed
        config.setUsername("postgres");
        config.setPassword("postgres"); // Specify your local cluster password here
        config.setMaximumPoolSize(5);   // Low pool limits prove we aren't leaking connections
        config.setMinimumIdle(2);

        try (HikariDataSource dataSource = new HikariDataSource(config)) {

            // 2. Initialize raw database DDL schema
            initSchema(dataSource);

            // 3. Assemble application components
            DisbursementRepository repository = new DisbursementRepository(dataSource);
            TelcoWalletClient telcoClient = new TelcoWalletClient();
            DisbursementOrchestrator orchestrator = new DisbursementOrchestrator(repository, telcoClient);

            // --- SIMULATION 1: HAPPY PATH ---
            System.out.println("\n--- Starting Simulation 1: All Successful Transfers ---");
            UUID happyBatchId = UUID.randomUUID();
            List<DisbursementItem> happyItems = List.of(
                DisbursementItem.fromRequest(happyBatchId, new DisbursementRequest("254711111111", new BigDecimal("1000"))),
                DisbursementItem.fromRequest(happyBatchId, new DisbursementRequest("254722222222", new BigDecimal("2500"))),
                DisbursementItem.fromRequest(happyBatchId, new DisbursementRequest("254733333333", new BigDecimal("500")))
            );
            orchestrator.processBatch(DisbursementBatch.initialize(happyItems));

            System.out.println("\n--------------------------------------------------------------------------------\n");

            // --- SIMULATION 2: FAST-FAIL ---
            System.out.println("--- Starting Simulation 2: Interrupted Fast-Fail (Triggered via 999) ---");
            UUID failBatchId = UUID.randomUUID();
            List<DisbursementItem> failItems = List.of(
                DisbursementItem.fromRequest(failBatchId, new DisbursementRequest("254744444444", new BigDecimal("5000"))),
                DisbursementItem.fromRequest(failBatchId, new DisbursementRequest("254755555888", new BigDecimal("1200"))), // Slow task (5000ms delay)
                DisbursementItem.fromRequest(failBatchId, new DisbursementRequest("254799999999", new BigDecimal("3000"))), // Fast-fail trigger (50ms delay)
                DisbursementItem.fromRequest(failBatchId, new DisbursementRequest("254766666666", new BigDecimal("750")))
            );
            orchestrator.processBatch(DisbursementBatch.initialize(failItems));

            // --- SIMULATION 3: SAGA PATTERN ---
            System.out.println("\n--------------------------------------------------------------------------------\n");
            System.out.println("--- Starting Simulation 3: Saga Pattern (Partial Success) ---");

            // Instantiate the specialized Saga orchestrator
            SagaDisbursementOrchestrator sagaOrchestrator = new SagaDisbursementOrchestrator(repository, telcoClient);

            UUID sagaBatchId = UUID.randomUUID();
            List<DisbursementItem> sagaItems = List.of(
                DisbursementItem.fromRequest(sagaBatchId, new DisbursementRequest("254777777777", new BigDecimal("4000"))), // Happy path
                DisbursementItem.fromRequest(sagaBatchId, new DisbursementRequest("254799999999", new BigDecimal("3000"))), // Fast-fail trigger
                DisbursementItem.fromRequest(sagaBatchId, new DisbursementRequest("254788888888", new BigDecimal("1500")))  // Happy path
            );

            sagaOrchestrator.processBatch(DisbursementBatch.initialize(sagaItems));
        }
    }

    private static void initSchema(HikariDataSource ds) {
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS disbursement_batch (
                batch_id UUID PRIMARY KEY,
                status VARCHAR(50) NOT NULL,
                failure_reason TEXT,
                created_at TIMESTAMP NOT NULL
            );
            CREATE TABLE IF NOT EXISTS disbursement_item (
                item_id UUID PRIMARY KEY,
                batch_id UUID,
                phone_number VARCHAR(20) NOT NULL,
                amount DECIMAL NOT NULL,
                status VARCHAR(50) NOT NULL,
                failure_reason TEXT
            );
        """;

        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
            System.out.println("⚙️ Database schema initialized successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Critical failure initializing PostgreSQL tables", e);
        }
    }
}
