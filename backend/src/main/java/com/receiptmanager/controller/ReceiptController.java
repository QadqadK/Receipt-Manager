package com.receiptmanager.controller;

import com.receiptmanager.dto.ReceiptDtos.ReceiptRequest;
import com.receiptmanager.dto.ReceiptDtos.ReceiptResponse;
import com.receiptmanager.security.CurrentUser;
import com.receiptmanager.service.ReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping
    public ResponseEntity<ReceiptResponse> create(@Valid @RequestBody ReceiptRequest request) {
        return ResponseEntity.ok(receiptService.create(CurrentUser.userId(), request));
    }

    @GetMapping
    public ResponseEntity<Page<ReceiptResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(receiptService.list(CurrentUser.userId(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiptResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(receiptService.getOne(CurrentUser.userId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReceiptResponse> update(@PathVariable Long id, @Valid @RequestBody ReceiptRequest request) {
        return ResponseEntity.ok(receiptService.update(CurrentUser.userId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        receiptService.delete(CurrentUser.userId(), id);
        return ResponseEntity.noContent().build();
    }
}
