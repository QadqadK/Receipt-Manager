package com.receiptmanager.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReportDtos {

    public record StoreSummary(String storeName, BigDecimal total) {
    }

    public record MonthlyReportResponse(
            int year,
            int month,
            List<ReceiptDtos.ReceiptResponse> receipts,
            List<StoreSummary> byStore,
            BigDecimal grandTotal) {
    }
}
