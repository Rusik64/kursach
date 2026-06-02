package com.example.kursach.service;

import com.example.kursach.repository.BuyRepository;
import com.example.kursach.repository.ReportRepository;
import com.example.kursach.repository.model.Buy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final BuyRepository buyRepository;
    private final ReportRepository reportRepository;

    public ReportService(BuyRepository buyRepository, ReportRepository reportRepository) {
        this.buyRepository = buyRepository;
        this.reportRepository = reportRepository;
    }

    // Отчет по всем магазинам (для ADMIN)
    public Map<String, Object> getReportAllShops(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> report = new HashMap<>();

        // Получаем все чеки за период по всем магазинам
        List<Buy> buys = buyRepository.findAllByDateRange(start, end);

        double totalRevenue = buys.stream().mapToDouble(Buy::getSum).sum();
        int totalChecks = buys.size();
        double averageCheck = totalChecks > 0 ? totalRevenue / totalChecks : 0;

        report.put("startDate", start);
        report.put("endDate", end);
        report.put("totalChecks", totalChecks);
        report.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        report.put("averageCheck", Math.round(averageCheck * 100.0) / 100.0);
        report.put("allShops", true);

        // Статистика по магазинам
        List<Map<String, Object>> shopsStats = reportRepository.getSalesByShop(start, end);
        report.put("shopsStats", shopsStats);

        // Топ-10 товаров по всем магазинам
        List<Map<String, Object>> topProducts = reportRepository.getTopSellingProductsAllShops(start, end, 10);
        report.put("topProducts", topProducts);

        // Продажи по категориям (все магазины)
        List<Map<String, Object>> salesByCategory = reportRepository.getSalesByCategoryAllShops(start, end);
        report.put("salesByCategory", salesByCategory);

        // Сводка по дням
        List<Map<String, Object>> dailySummary = reportRepository.getDailySummaryAllShops(start, end);
        report.put("dailySummary", dailySummary);

        return report;
    }

    // Отчет по конкретному магазину
    public Map<String, Object> getReportByDateRange(Long shopId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> report = new HashMap<>();

        List<Buy> buys = buyRepository.findByShopIdAndDateRange(shopId, start, end);

        double totalRevenue = buys.stream().mapToDouble(Buy::getSum).sum();
        int totalChecks = buys.size();
        double averageCheck = totalChecks > 0 ? totalRevenue / totalChecks : 0;

        report.put("startDate", start);
        report.put("endDate", end);
        report.put("shopId", shopId);
        report.put("totalChecks", totalChecks);
        report.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        report.put("averageCheck", Math.round(averageCheck * 100.0) / 100.0);
        report.put("allShops", false);

        if (!buys.isEmpty()) {
            List<Long> buyIds = buys.stream().map(Buy::getId).toList();
            List<Map<String, Object>> soldProducts = reportRepository.getSoldProducts(buyIds);
            report.put("soldProducts", soldProducts);
            report.put("totalProductsSold", soldProducts.stream()
                    .mapToLong(p -> ((Number) p.get("total_count")).longValue())
                    .sum());
        } else {
            report.put("soldProducts", List.of());
            report.put("totalProductsSold", 0L);
        }

        List<Map<String, Object>> topProducts = reportRepository.getTopSellingProducts(shopId, start, end, 10);
        report.put("topProducts", topProducts);

        List<Map<String, Object>> salesByCategory = reportRepository.getSalesByCategory(shopId, start, end);
        report.put("salesByCategory", salesByCategory);

        return report;
    }

    // Отчет по товарам (конкретный магазин)
    public Map<String, Object> getProductSalesReport(Long shopId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> report = new HashMap<>();
        report.put("startDate", start);
        report.put("endDate", end);
        report.put("shopId", shopId);
        report.put("allShops", false);

        List<Map<String, Object>> products = reportRepository.getDetailedProductSales(shopId, start, end);
        report.put("products", products);

        long totalQuantity = products.stream()
                .mapToLong(p -> ((Number) p.get("quantity")).longValue())
                .sum();
        double totalAmount = products.stream()
                .mapToDouble(p -> ((Number) p.get("total_amount")).doubleValue())
                .sum();

        report.put("totalQuantity", totalQuantity);
        report.put("totalAmount", Math.round(totalAmount * 100.0) / 100.0);

        return report;
    }

    // Отчет по товарам (все магазины)
    public Map<String, Object> getProductSalesReportAllShops(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> report = new HashMap<>();
        report.put("startDate", start);
        report.put("endDate", end);
        report.put("allShops", true);

        List<Map<String, Object>> products = reportRepository.getDetailedProductSalesAllShops(start, end);
        report.put("products", products);

        long totalQuantity = products.stream()
                .mapToLong(p -> ((Number) p.get("quantity")).longValue())
                .sum();
        double totalAmount = products.stream()
                .mapToDouble(p -> ((Number) p.get("total_amount")).doubleValue())
                .sum();

        report.put("totalQuantity", totalQuantity);
        report.put("totalAmount", Math.round(totalAmount * 100.0) / 100.0);

        // Распределение по магазинам
        List<Map<String, Object>> shopsStats = reportRepository.getSalesByShop(start, end);
        report.put("shopsStats", shopsStats);

        return report;
    }
}