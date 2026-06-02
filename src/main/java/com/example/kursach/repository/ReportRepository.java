package com.example.kursach.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class ReportRepository {

    private final JdbcTemplate jdbc;

    public ReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Получить проданные товары по списку чеков
    public List<Map<String, Object>> getSoldProducts(List<Long> buyIds) {
        if (buyIds.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", buyIds.stream().map(id -> "?").toArray(String[]::new));
        String sql = String.format("""
            SELECT 
                p.id,
                p.name,
                p.unit,
                SUM(bp.count) as total_count,
                p.price as price_per_unit,
                SUM(bp.count * p.price) as total_revenue
            FROM buy_product bp
            JOIN products p ON bp.product_id = p.id
            WHERE bp.buy_id IN (%s)
            GROUP BY p.id, p.name, p.unit, p.price
            ORDER BY total_revenue DESC
        """, placeholders);

        return jdbc.queryForList(sql, buyIds.toArray());
    }

    // Топ продаваемых товаров
    public List<Map<String, Object>> getTopSellingProducts(Long shopId, LocalDateTime start, LocalDateTime end, int limit) {
        String sql = """
            SELECT 
                p.id,
                p.name,
                p.unit,
                p.price,
                SUM(bp.count) as total_count,
                SUM(bp.count * p.price) as total_revenue
            FROM buy_product bp
            JOIN buy b ON bp.buy_id = b.id
            JOIN products p ON bp.product_id = p.id
            WHERE b.shop_id = ?
            AND b.created_at BETWEEN ? AND ?
            GROUP BY p.id, p.name, p.unit, p.price
            ORDER BY total_revenue DESC
            LIMIT ?
        """;

        return jdbc.queryForList(sql, shopId, start, end, limit);
    }

    // Продажи по категориям
    public List<Map<String, Object>> getSalesByCategory(Long shopId, LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT 
                c.id,
                c.name,
                COUNT(DISTINCT bp.buy_id) as check_count,
                SUM(bp.count) as total_quantity,
                SUM(bp.count * p.price) as total_revenue
            FROM buy_product bp
            JOIN buy b ON bp.buy_id = b.id
            JOIN products p ON bp.product_id = p.id
            JOIN categories c ON p.category_id = c.id
            WHERE b.shop_id = ?
            AND b.created_at BETWEEN ? AND ?
            GROUP BY c.id, c.name
            ORDER BY total_revenue DESC
        """;

        return jdbc.queryForList(sql, shopId, start, end);
    }

    // Почасовая статистика продаж
    public List<Map<String, Object>> getDailySalesByHour(Long shopId, LocalDateTime date) {
        String sql = """
            SELECT 
                EXTRACT(HOUR FROM created_at) as hour,
                COUNT(*) as checks_count,
                COALESCE(SUM(sum), 0) as total_sales
            FROM buy
            WHERE shop_id = ?
            AND DATE(created_at) = DATE(?)
            GROUP BY EXTRACT(HOUR FROM created_at)
            ORDER BY hour
        """;

        return jdbc.queryForList(sql, shopId, date);
    }

    // Детальная информация о продажах товаров
    public List<Map<String, Object>> getDetailedProductSales(Long shopId, LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT 
                p.id,
                p.name,
                p.unit,
                p.price as unit_price,
                SUM(bp.count) as quantity,
                SUM(bp.count * p.price) as total_amount,
                COUNT(DISTINCT bp.buy_id) as checks_count,
                MIN(b.created_at) as first_sale,
                MAX(b.created_at) as last_sale
            FROM buy_product bp
            JOIN buy b ON bp.buy_id = b.id
            JOIN products p ON bp.product_id = p.id
            WHERE b.shop_id = ?
            AND b.created_at BETWEEN ? AND ?
            GROUP BY p.id, p.name, p.unit, p.price
            ORDER BY total_amount DESC
        """;

        return jdbc.queryForList(sql, shopId, start, end);
    }

    // Сводка по дням
    public List<Map<String, Object>> getDailySummary(Long shopId, LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT 
                DATE(created_at) as sale_date,
                COUNT(*) as checks_count,
                SUM(sum) as daily_revenue,
                AVG(sum) as average_check
            FROM buy
            WHERE shop_id = ?
            AND created_at BETWEEN ? AND ?
            GROUP BY DATE(created_at)
            ORDER BY sale_date
        """;

        return jdbc.queryForList(sql, shopId, start, end);
    }

    // Самые продаваемые товары по количеству
    public List<Map<String, Object>> getMostSoldByQuantity(Long shopId, LocalDateTime start, LocalDateTime end, int limit) {
        String sql = """
            SELECT 
                p.id,
                p.name,
                p.unit,
                SUM(bp.count) as total_quantity
            FROM buy_product bp
            JOIN buy b ON bp.buy_id = b.id
            JOIN products p ON bp.product_id = p.id
            WHERE b.shop_id = ?
            AND b.created_at BETWEEN ? AND ?
            GROUP BY p.id, p.name, p.unit
            ORDER BY total_quantity DESC
            LIMIT ?
        """;

        return jdbc.queryForList(sql, shopId, start, end, limit);
    }

    public List<Map<String, Object>> getSalesByShop(LocalDateTime start, LocalDateTime end) {
        String sql = """
        SELECT 
            s.id,
            s.address,
            COUNT(b.id) as checks_count,
            COALESCE(SUM(b.sum), 0) as total_revenue,
            COALESCE(AVG(b.sum), 0) as average_check
        FROM shops s
        LEFT JOIN buy b ON s.id = b.shop_id 
            AND b.created_at BETWEEN ? AND ?
        GROUP BY s.id, s.address
        ORDER BY total_revenue DESC
    """;

        return jdbc.queryForList(sql, start, end);
    }

    // Топ товаров по всем магазинам
    public List<Map<String, Object>> getTopSellingProductsAllShops(LocalDateTime start, LocalDateTime end, int limit) {
        String sql = """
        SELECT 
            p.id,
            p.name,
            p.unit,
            p.price,
            SUM(bp.count) as total_count,
            SUM(bp.count * p.price) as total_revenue
        FROM buy_product bp
        JOIN buy b ON bp.buy_id = b.id
        JOIN products p ON bp.product_id = p.id
        WHERE b.created_at BETWEEN ? AND ?
        GROUP BY p.id, p.name, p.unit, p.price
        ORDER BY total_revenue DESC
        LIMIT ?
    """;

        return jdbc.queryForList(sql, start, end, limit);
    }

    // Продажи по категориям (все магазины)
    public List<Map<String, Object>> getSalesByCategoryAllShops(LocalDateTime start, LocalDateTime end) {
        String sql = """
        SELECT 
            c.id,
            c.name,
            COUNT(DISTINCT bp.buy_id) as check_count,
            SUM(bp.count) as total_quantity,
            SUM(bp.count * p.price) as total_revenue
        FROM buy_product bp
        JOIN buy b ON bp.buy_id = b.id
        JOIN products p ON bp.product_id = p.id
        JOIN categories c ON p.category_id = c.id
        WHERE b.created_at BETWEEN ? AND ?
        GROUP BY c.id, c.name
        ORDER BY total_revenue DESC
    """;

        return jdbc.queryForList(sql, start, end);
    }

    // Сводка по дням (все магазины)
    public List<Map<String, Object>> getDailySummaryAllShops(LocalDateTime start, LocalDateTime end) {
        String sql = """
        SELECT 
            DATE(created_at) as sale_date,
            COUNT(*) as checks_count,
            SUM(sum) as daily_revenue,
            AVG(sum) as average_check
        FROM buy
        WHERE created_at BETWEEN ? AND ?
        GROUP BY DATE(created_at)
        ORDER BY sale_date
    """;

        return jdbc.queryForList(sql, start, end);
    }

    // Детальные продажи товаров (все магазины)
    public List<Map<String, Object>> getDetailedProductSalesAllShops(LocalDateTime start, LocalDateTime end) {
        String sql = """
        SELECT 
            p.id,
            p.name,
            p.unit,
            p.price as unit_price,
            SUM(bp.count) as quantity,
            SUM(bp.count * p.price) as total_amount,
            COUNT(DISTINCT bp.buy_id) as checks_count,
            COUNT(DISTINCT b.shop_id) as shops_count
        FROM buy_product bp
        JOIN buy b ON bp.buy_id = b.id
        JOIN products p ON bp.product_id = p.id
        WHERE b.created_at BETWEEN ? AND ?
        GROUP BY p.id, p.name, p.unit, p.price
        ORDER BY total_amount DESC
    """;

        return jdbc.queryForList(sql, start, end);
    }
}