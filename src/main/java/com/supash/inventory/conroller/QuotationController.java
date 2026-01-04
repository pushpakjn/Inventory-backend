package com.supash.inventory.conroller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.supash.inventory.dto.CreateQuotationRequest;
import com.supash.inventory.model.Quotation;
import com.supash.inventory.service.QuotationService;

@RestController
@RequestMapping("/quotation")
public class QuotationController {

    private final QuotationService service;

    public QuotationController(QuotationService service) {
        this.service = service;
    }

    /* ================= CREATE ================= */
    @PostMapping("/confirm")
    public ResponseEntity<Quotation> confirm(
            @RequestBody CreateQuotationRequest request,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                service.createQuotation(request, auth.getName())
        );
    }

    /* ================= LIST ================= */
    @GetMapping
    public ResponseEntity<List<Quotation>> list(Authentication auth) {
        return ResponseEntity.ok(
                service.getQuotations(auth)
        );
    }

    /* ================= GET ONE (EDIT PAGE) ================= */
    @GetMapping("/{id}")
    public ResponseEntity<Quotation> getById(
            @PathVariable String id,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                service.getQuotation(id, auth)
        );
    }

    /* ================= UPDATE ================= */
    @PutMapping("/{id}")
    public ResponseEntity<Quotation> update(
            @PathVariable String id,
            @RequestBody CreateQuotationRequest request,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                service.updateQuotation(id, request, auth)
        );
    }

    /* ================= DELETE ================= */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable String id,
            Authentication auth
    ) {
        service.deleteQuotation(id, auth);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/next-number")
    public ResponseEntity<String> nextQuotationNumber() {
        return ResponseEntity.ok(service.generateQuotationNo());
    }
}
