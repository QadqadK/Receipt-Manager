package com.receiptmanager.service;

import com.receiptmanager.dto.ReceiptDtos.ReceiptItemRequest;
import com.receiptmanager.dto.ReceiptDtos.ReceiptItemResponse;
import com.receiptmanager.dto.ReceiptDtos.ReceiptRequest;
import com.receiptmanager.dto.ReceiptDtos.ReceiptResponse;
import com.receiptmanager.entity.Receipt;
import com.receiptmanager.entity.ReceiptItem;
import com.receiptmanager.entity.User;
import com.receiptmanager.exception.ApiException;
import com.receiptmanager.repository.ReceiptRepository;
import com.receiptmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReceiptResponse create(Long userId, ReceiptRequest request) {
        User user = userRepository.getReferenceById(userId);
        Receipt receipt = Receipt.builder()
                .user(user)
                .storeName(request.storeName())
                .receiptDate(request.receiptDate())
                .category(request.category())
                .build();
        for (ReceiptItemRequest itemRequest : request.items()) {
            receipt.addItem(ReceiptItem.builder()
                    .itemName(itemRequest.itemName())
                    .price(itemRequest.price())
                    .quantity(itemRequest.quantity())
                    .build());
        }
        receiptRepository.save(receipt);
        return toResponse(receipt);
    }

    @Transactional
    public ReceiptResponse update(Long userId, Long receiptId, ReceiptRequest request) {
        Receipt receipt = getOwnedReceipt(userId, receiptId);
        receipt.setStoreName(request.storeName());
        receipt.setReceiptDate(request.receiptDate());
        receipt.setCategory(request.category());
        receipt.getItems().clear();
        for (ReceiptItemRequest itemRequest : request.items()) {
            receipt.addItem(ReceiptItem.builder()
                    .itemName(itemRequest.itemName())
                    .price(itemRequest.price())
                    .quantity(itemRequest.quantity())
                    .build());
        }
        return toResponse(receipt);
    }

    @Transactional
    public void delete(Long userId, Long receiptId) {
        Receipt receipt = getOwnedReceipt(userId, receiptId);
        receiptRepository.delete(receipt);
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getOne(Long userId, Long receiptId) {
        return toResponse(getOwnedReceipt(userId, receiptId));
    }

    @Transactional(readOnly = true)
    public Page<ReceiptResponse> list(Long userId, Pageable pageable) {
        return receiptRepository.findByUserIdOrderByReceiptDateDesc(userId, pageable)
                .map(this::toResponse);
    }

    private Receipt getOwnedReceipt(Long userId, Long receiptId) {
        return receiptRepository.findByIdAndUserId(receiptId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Receipt not found"));
    }

    ReceiptResponse toResponse(Receipt receipt) {
        List<ReceiptItemResponse> items = receipt.getItems().stream()
                .map(i -> new ReceiptItemResponse(
                        i.getId(),
                        i.getItemName(),
                        i.getPrice(),
                        i.getQuantity(),
                        i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                .toList();
        BigDecimal total = items.stream()
                .map(ReceiptItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ReceiptResponse(receipt.getId(), receipt.getStoreName(), receipt.getReceiptDate(), receipt.getCategory(), items, total);
    }
}
