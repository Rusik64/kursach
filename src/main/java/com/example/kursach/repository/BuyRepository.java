package com.example.kursach.repository;

import com.example.kursach.repository.model.Buy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class BuyRepository {

    private final JdbcTemplate jdbc;

    public BuyRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Buy> buyRowMapper = (rs, rowNum) -> {
        Buy buy = new Buy();
        buy.setId(rs.getLong("id"));
        buy.setShopId(rs.getLong("shop_id"));
        buy.setSum(rs.getDouble("sum"));
        buy.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return buy;
    };

    public List<Buy> findAllByShopId(Long shopId) {
        String sql = """
            SELECT id, shop_id, sum, created_at
            FROM buy
            WHERE shop_id = ?
            ORDER BY created_at DESC
        """;

        return jdbc.query(sql, buyRowMapper, shopId);
    }

    public Buy findById(Long buyId) {
        String sql = "SELECT id, shop_id, sum, created_at FROM buy WHERE id = ?";
        return jdbc.queryForObject(sql, buyRowMapper, buyId);
    }

    @Transactional
    public Buy createBuy(Buy buy) {
        String insertBuySql = "INSERT INTO buy (shop_id, sum, created_at) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertBuySql, new String[]{"id"});
            ps.setLong(1, buy.getShopId());
            ps.setDouble(2, buy.getSum());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Long buyId = keyHolder.getKey().longValue();
        buy.setId(buyId);
        buy.setCreatedAt(LocalDateTime.now());

        return buy;
    }

    public Integer getProductStock(Long shopId, Long productId) {
        String sql = "SELECT count FROM shop_product WHERE shop_id = ? AND product_id = ?";
        try {
            return jdbc.queryForObject(sql, Integer.class, shopId, productId);
        } catch (Exception e) {
            return 0;
        }
    }

    public void updateProductStock(Long shopId, Long productId, int quantityChange) {
        String sql = """
            UPDATE shop_product 
            SET count = count + ? 
            WHERE shop_id = ? AND product_id = ?
        """;
        jdbc.update(sql, quantityChange, shopId, productId);
    }

    public List<Buy> findByShopIdAndDateRange(Long shopId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT id, shop_id, sum, created_at
            FROM buy
            WHERE shop_id = ? 
            AND created_at >= ? 
            AND created_at <= ?
            ORDER BY created_at DESC
        """;

        return jdbc.query(
                sql,
                buyRowMapper,
                shopId,
                Timestamp.valueOf(startDate),
                Timestamp.valueOf(endDate)
        );
    }

    public int countByShopIdAndDateRange(Long shopId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
        SELECT COUNT(*)
        FROM buy
        WHERE shop_id = ? 
        AND created_at >= ? 
        AND created_at <= ?
    """;

        Integer count = jdbc.queryForObject(sql, Integer.class, shopId,
                Timestamp.valueOf(startDate), Timestamp.valueOf(endDate));
        return count != null ? count : 0;
    }

    public List<Buy> findRecentBuys(Long shopId, int limit) {
        String sql = """
        SELECT id, shop_id, sum, created_at
        FROM buy
        WHERE shop_id = ?
        ORDER BY created_at DESC
        LIMIT ?
    """;
        return jdbc.query(sql, buyRowMapper, shopId, limit);
    }

    public List<Buy> findByDateRange(Long shopId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
        SELECT id, shop_id, sum, created_at
        FROM buy
        WHERE shop_id = ? 
        AND created_at >= ? 
        AND created_at <= ?
        ORDER BY created_at DESC
    """;

        return jdbc.query(sql, buyRowMapper, shopId,
                Timestamp.valueOf(startDate), Timestamp.valueOf(endDate));
    }

    public List<Buy> findAllByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
        SELECT id, shop_id, sum, created_at
        FROM buy
        WHERE created_at >= ? AND created_at <= ?
        ORDER BY created_at DESC
    """;

        return jdbc.query(sql, buyRowMapper,
                Timestamp.valueOf(startDate), Timestamp.valueOf(endDate));
    }
}