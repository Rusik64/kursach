package com.example.kursach.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ShopProductRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Integer getStockForUpdate(Long shopId, Long productId) {
        return jdbcTemplate.queryForObject("""
            SELECT count FROM shop_product
            WHERE shop_id = ? AND product_id = ?
            FOR UPDATE
        """, Integer.class, shopId, productId);
    }

    public void decreaseStock(Long shopId, Long productId, int count) {
        jdbcTemplate.update("""
            UPDATE shop_product
            SET count = count - ?
            WHERE shop_id = ? AND product_id = ?
        """, count, shopId, productId);
    }
}