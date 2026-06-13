-- liquibase formatted sql

-- changeset parsaloi:1-init-schema
CREATE TABLE disbursement_batch (
    id UUID PRIMARY KEY,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE disbursement_batch_phone_numbers (
    disbursement_batch_id UUID NOT NULL,
    phone_numbers VARCHAR(20) NOT NULL,
    CONSTRAINT fk_disbursement_batch
        FOREIGN KEY (disbursement_batch_id)
        REFERENCES disbursement_batch (id)
        ON DELETE CASCADE
);
