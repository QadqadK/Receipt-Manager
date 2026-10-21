package com.receiptmanager.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.receiptmanager.dto.ReceiptDtos.ReceiptItemResponse;
import com.receiptmanager.dto.ReceiptDtos.ReceiptResponse;
import com.receiptmanager.dto.ReportDtos.MonthlyReportResponse;
import com.receiptmanager.dto.ReportDtos.StoreSummary;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Component
public class PdfReportGenerator {

    private static final DateTimeFormatter GENERATED_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final Font COMPANY_FONT = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.TIMES_BOLD, 14);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10);
    private static final Font HEADING_FONT = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font TOTAL_FONT = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
    private static final Font FOOTER_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8);

    public byte[] generateMonthlyReport(MonthlyReportResponse report) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            String monthName = Month.of(report.month()).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            document.add(buildHeaderBar(monthName, report.year()));

            Paragraph rule = new Paragraph(" ");
            rule.setSpacingAfter(14);
            document.add(rule);

            document.add(buildItemsTable(report));

            Paragraph storeTotalsHeading = new Paragraph("Totals by Store", HEADING_FONT);
            storeTotalsHeading.setSpacingBefore(18);
            storeTotalsHeading.setSpacingAfter(8);
            document.add(storeTotalsHeading);
            document.add(buildStoreTotalsTable(report));

            Paragraph grandTotal = new Paragraph("Grand Total: RM " + report.grandTotal(), TOTAL_FONT);
            grandTotal.setSpacingBefore(14);
            document.add(grandTotal);

            Paragraph generatedOn = new Paragraph(
                    "Generated: " + LocalDate.now().format(GENERATED_DATE_FORMAT), SUBTITLE_FONT);
            generatedOn.setSpacingBefore(30);
            generatedOn.setAlignment(Element.ALIGN_CENTER);
            document.add(generatedOn);

            Paragraph footer = new Paragraph(
                    "CONFIDENTIAL - This report is generated for tax reporting purposes only and is intended solely "
                            + "for the recipient's use. Unauthorized distribution, copying, or disclosure of this document "
                            + "is strictly prohibited.",
                    FOOTER_FONT);
            footer.setSpacingBefore(6);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
        } finally {
            document.close();
        }
        return out.toByteArray();
    }

    private PdfPTable buildHeaderBar(String monthName, int year) {
        PdfPTable header = new PdfPTable(new float[]{1f, 1f});
        header.setWidthPercentage(100);

        Paragraph companyBlock = new Paragraph();
        companyBlock.add(new com.lowagie.text.Chunk("Receipt Management\n", COMPANY_FONT));
        companyBlock.add(new com.lowagie.text.Chunk("System JOKO XYZ", COMPANY_FONT));
        companyBlock.setAlignment(Element.ALIGN_LEFT);
        PdfPCell companyCell = new PdfPCell(companyBlock);
        companyCell.setBorder(Element.RECTANGLE);
        companyCell.setBorderWidthTop(0);
        companyCell.setBorderWidthLeft(0);
        companyCell.setBorderWidthRight(0);
        companyCell.setBorderWidthBottom(1.2f);
        companyCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        companyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        companyCell.setPaddingBottom(8);
        header.addCell(companyCell);

        Paragraph titleBlock = new Paragraph("Monthly Receipt Report — " + monthName + " " + year, TITLE_FONT);
        titleBlock.setAlignment(Element.ALIGN_CENTER);
        PdfPCell titleCell = new PdfPCell(titleBlock);
        titleCell.setBorder(Element.RECTANGLE);
        titleCell.setBorderWidthTop(0);
        titleCell.setBorderWidthLeft(0);
        titleCell.setBorderWidthRight(0);
        titleCell.setBorderWidthBottom(1.2f);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setPaddingBottom(8);
        header.addCell(titleCell);

        return header;
    }

    private PdfPTable buildItemsTable(MonthlyReportResponse report) {
        PdfPTable table = new PdfPTable(new float[]{2.2f, 1.1f, 1.5f, 1.4f, 1.6f, 0.6f, 1.6f});
        table.setWidthPercentage(100);

        addHeaderCell(table, "Store", Element.ALIGN_LEFT);
        addHeaderCell(table, "Date", Element.ALIGN_LEFT);
        addHeaderCell(table, "Category", Element.ALIGN_LEFT);
        addHeaderCell(table, "Item", Element.ALIGN_LEFT);
        addHeaderCell(table, "Price", Element.ALIGN_RIGHT);
        addHeaderCell(table, "Qty", Element.ALIGN_RIGHT);
        addHeaderCell(table, "Line Total", Element.ALIGN_RIGHT);

        for (ReceiptResponse receipt : report.receipts()) {
            for (ReceiptItemResponse item : receipt.items()) {
                addCell(table, receipt.storeName(), Element.ALIGN_LEFT);
                addCell(table, receipt.receiptDate().toString(), Element.ALIGN_LEFT);
                addCell(table, receipt.category(), Element.ALIGN_LEFT);
                addCell(table, item.itemName(), Element.ALIGN_LEFT);
                addMoneyCell(table, item.price().toString());
                addCell(table, String.valueOf(item.quantity()), Element.ALIGN_RIGHT);
                addMoneyCell(table, item.lineTotal().toString());
            }
        }

        if (report.receipts().isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("No receipts for this month.", CELL_FONT));
            empty.setColspan(7);
            empty.setPadding(6);
            table.addCell(empty);
        }

        return table;
    }

    private PdfPTable buildStoreTotalsTable(MonthlyReportResponse report) {
        PdfPTable table = new PdfPTable(new float[]{3f, 1f});
        table.setWidthPercentage(100);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        addHeaderCell(table, "Store", Element.ALIGN_LEFT);
        addHeaderCell(table, "Total", Element.ALIGN_RIGHT);
        for (StoreSummary summary : report.byStore()) {
            addCell(table, summary.storeName(), Element.ALIGN_LEFT);
            addMoneyCell(table, summary.total().toString());
        }
        return table;
    }

    private void addHeaderCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setPadding(7);
        cell.setHorizontalAlignment(alignment);
        cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
        table.addCell(cell);
    }

    private void addMoneyCell(PdfPTable table, String amount) {
        PdfPTable inner = new PdfPTable(new float[]{0.7f, 2f});
        inner.setWidthPercentage(100);

        PdfPCell currencyCell = new PdfPCell(new Phrase("RM", CELL_FONT));
        currencyCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        currencyCell.setPadding(0);
        currencyCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        currencyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        inner.addCell(currencyCell);

        PdfPCell amountCell = new PdfPCell(new Phrase(amount, CELL_FONT));
        amountCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        amountCell.setPadding(0);
        amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amountCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        amountCell.setNoWrap(true);
        inner.addCell(amountCell);

        PdfPCell outer = new PdfPCell(inner);
        outer.setPadding(6);
        outer.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(outer);
    }

    private void addCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, CELL_FONT));
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        // Numeric columns are always short and right-aligned; keep them on one line.
        // Text columns (store/date/category/item) must wrap instead of overflowing into the next cell.
        cell.setNoWrap(alignment == Element.ALIGN_RIGHT);
        table.addCell(cell);
    }
}
