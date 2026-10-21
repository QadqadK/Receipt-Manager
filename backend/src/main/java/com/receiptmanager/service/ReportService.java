package com.receiptmanager.service;

import com.receiptmanager.dto.ReceiptDtos.ReceiptResponse;
import com.receiptmanager.dto.ReportDtos.MonthlyReportResponse;
import com.receiptmanager.dto.ReportDtos.StoreSummary;
import com.receiptmanager.entity.Receipt;
import com.receiptmanager.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptService receiptService;

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(Long userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        List<Receipt> receipts = receiptRepository.findByUserIdAndDateRangeWithItems(userId, from, to);
        List<ReceiptResponse> receiptResponses = receipts.stream().map(receiptService::toResponse).toList();

        Map<String, BigDecimal> totalsByStore = new LinkedHashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (ReceiptResponse r : receiptResponses) {
            totalsByStore.merge(r.storeName(), r.total(), BigDecimal::add);
            grandTotal = grandTotal.add(r.total());
        }

        List<StoreSummary> storeSummaries = totalsByStore.entrySet().stream()
                .map(e -> new StoreSummary(e.getKey(), e.getValue()))
                .toList();

        return new MonthlyReportResponse(year, month, receiptResponses, storeSummaries, grandTotal);
    }
}
