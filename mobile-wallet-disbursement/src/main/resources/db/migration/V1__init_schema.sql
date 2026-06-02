CREATE TABLE IF NOT EXISTS disbursement_batch 
(
	batch_id UUID PRIMARY KEY,
        status VARCHAR(50) NOT NULL,
        failure_reason TEXT,
        created_at TIMESTAMP NOT NULL
);