package co.zw.bancabc.canteen_system_backend.service;

import co.zw.bancabc.canteen_system_backend.dto.ReportResponse;
import co.zw.bancabc.canteen_system_backend.dto.ReportSummary;
import co.zw.bancabc.canteen_system_backend.model.Order;
import co.zw.bancabc.canteen_system_backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final OrderRepository orderRepository;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public List<ReportResponse> generateReport(LocalDateTime start, LocalDateTime end) {
        if (start == null) start = LocalDateTime.now().minusDays(30);
        if (end == null) end = LocalDateTime.now();
        List<Order> orders = orderRepository.findConfirmedOrdersBetween(start, end);
        return orders.stream().map(this::toReport).collect(Collectors.toList());
    }

    public ReportSummary summarize(LocalDateTime start, LocalDateTime end) {
        if (start == null) start = LocalDateTime.now().minusDays(30);
        if (end == null) end = LocalDateTime.now();
        List<ReportResponse> rows = generateReport(start, end);
        BigDecimal revenue = rows.stream()
                .map(ReportResponse::getTotalAmount)
                .filter(x -> x != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = rows.isEmpty()
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(rows.size()), 2, RoundingMode.HALF_UP);
        return ReportSummary.builder()
                .totalOrders(rows.size())
                .totalRevenue(revenue)
                .averageOrderValue(avg)
                .startDate(start)
                .endDate(end)
                .build();
    }

    public byte[] generateExcel(LocalDateTime start, LocalDateTime end) throws Exception {
        List<ReportResponse> rows = generateReport(start, end);
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Orders Report");

            CellStyle headerStyle = wb.createCellStyle();
            Font hf = wb.createFont();
            hf.setBold(true);
            headerStyle.setFont(hf);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"Order ID", "Username", "Email", "Meal",
                    "Amount", "Has Drink", "Drink Price", "Total",
                    "Order Date", "Confirmed At"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            int r = 1;
            for (ReportResponse row : rows) {
                Row x = sheet.createRow(r++);
                x.createCell(0).setCellValue(row.getOrderId() != null ? row.getOrderId() : 0);
                x.createCell(1).setCellValue(nz(row.getUsername()));
                x.createCell(2).setCellValue(nz(row.getUserEmail()));
                x.createCell(3).setCellValue(nz(row.getMealName()));
                x.createCell(4).setCellValue(dbl(row.getAmount()));
                x.createCell(5).setCellValue(row.isHasDrink() ? "Yes" : "No");
                x.createCell(6).setCellValue(dbl(row.getDrinkPrice()));
                x.createCell(7).setCellValue(dbl(row.getTotalAmount()));
                x.createCell(8).setCellValue(row.getOrderDate() != null ? row.getOrderDate().format(DTF) : "");
                x.createCell(9).setCellValue(row.getConfirmedAt() != null ? row.getConfirmedAt().format(DTF) : "");
            }

            // Summary rows
            BigDecimal total = rows.stream().map(ReportResponse::getTotalAmount)
                    .filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
            int sr = r + 2;
            Row sum1 = sheet.createRow(sr);
            sum1.createCell(0).setCellValue("Total Orders:");
            sum1.createCell(1).setCellValue(rows.size());
            Row sum2 = sheet.createRow(sr + 1);
            sum2.createCell(0).setCellValue("Total Revenue:");
            sum2.createCell(1).setCellValue(total.doubleValue());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    private ReportResponse toReport(Order o) {
        return ReportResponse.builder()
                .orderId(o.getId())
                .username(o.getUser() != null ? o.getUser().getUsername() : null)
                .userEmail(o.getUser() != null ? o.getUser().getEmail() : null)
                .mealName(o.getMealName())
                .amount(o.getAmount())
                .hasDrink(o.isHasDrink())
                .drinkPrice(o.getDrinkPrice())
                .totalAmount(o.getTotalAmount())
                .orderDate(o.getOrderDate())
                .confirmedAt(o.getConfirmedAt())
                .build();
    }

    private String nz(String s) { return s == null ? "" : s; }
    private double dbl(BigDecimal b) { return b == null ? 0d : b.doubleValue(); }
}