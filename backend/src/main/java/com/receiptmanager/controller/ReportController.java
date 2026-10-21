package com.receiptmanager.controller;

import com.receiptmanager.dto.ReceiptDtos.ReceiptItemResponse;
import com.receiptmanager.dto.ReceiptDtos.ReceiptResponse;
import com.receiptmanager.dto.ReportDtos.MonthlyReportResponse;
import com.receiptmanager.security.CurrentUser;
import com.receiptmanager.service.PdfReportGenerator;
import com.receiptmanager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final PdfReportGenerator pdfReportGenerator;

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyReportResponse> monthly(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(reportService.getMonthlyReport(CurrentUser.userId(), year, month));
    }

    @GetMapping("/monthly/export")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam int year,
            @RequestParam int month) {
        MonthlyReportResponse report = reportService.getMonthlyReport(CurrentUser.userId(), year, month);
        String csv = toCsv(report);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        String filename = "receipts-%04d-%02d.csv".formatted(year, month);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/monthly/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam int year,
            @RequestParam int month) {
        MonthlyReportResponse report = reportService.getMonthlyReport(CurrentUser.userId(), year, month);
        byte[] bytes = pdfReportGenerator.generateMonthlyReport(report);

        String filename = "receipts-%04d-%02d.pdf".formatted(year, month);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    private String toCsv(MonthlyReportResponse report) {
        StringBuilder sb = new StringBuilder();
        sb.append("Store,Date,Category,Item,Price,Quantity,Line Total\n");
        for (ReceiptResponse receipt : report.receipts()) {
            for (ReceiptItemResponse item : receipt.items()) {
                sb.append(csvField(receipt.storeName())).append(',')
                        .append(receipt.receiptDate()).append(',')
                        .append(csvField(receipt.category())).append(',')
                        .append(csvField(item.itemName())).append(',')
                        .append(item.price()).append(',')
                        .append(item.quantity()).append(',')
                        .append(item.lineTotal()).append('\n');
            }
        }
        sb.append('\n');
        sb.append("Store Totals\n");
        report.byStore().forEach(s -> sb.append(csvField(s.storeName())).append(',').append(s.total()).append('\n'));
        sb.append('\n');
        sb.append("Grand Total,").append(report.grandTotal()).append('\n');
        return sb.toString();
    }

    private String csvField(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
