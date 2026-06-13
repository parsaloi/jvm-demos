package com.jvmdemos.disbursement.svc;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/disbursements")
public class DisbursementController {

    private final DisbursementOrchestrator orchestrator;

    public DisbursementController(DisbursementOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/bulk")
    public ResponseEntity<String> executeBulk(@RequestBody List<String> phoneNumbers) {
        try {
            String result = orchestrator.processBatch(phoneNumbers);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
