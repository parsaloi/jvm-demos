package com.jvmdemos.disbursement.svc;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BatchRepository extends JpaRepository<DisbursementBatch, UUID> {}
