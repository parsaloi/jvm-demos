package com.jvmdemos.disbursement.svc;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
public class DisbursementBatch {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String status;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> phoneNumbers;

    public DisbursementBatch() {}
    public DisbursementBatch(List<String> phoneNumbers, String status) {
        this.phoneNumbers = phoneNumbers;
        this.status = status;
    }

    public UUID getId() { return id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getPhoneNumbers() { return phoneNumbers; }
}
