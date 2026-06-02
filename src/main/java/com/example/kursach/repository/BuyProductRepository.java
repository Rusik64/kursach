package com.example.kursach.repository;

import com.example.kursach.repository.model.BuyProduct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BuyProductRepository {

    private final JdbcTemplate jdbc;

    public BuyProductRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<BuyProduct> buyProductRowMapper = (rs, rowNum) -> {
        BuyProduct buyProduct = new BuyProduct();
        buyProduct.setBuyId(rs.getLong("buy_id"));
        buyProduct.setProductId(rs.getLong("product_id"));
        buyProduct.setCount(rs.getInt("count"));
        return buyProduct;
    };

    public List<BuyProduct> findByBuyId(Long buyId) {
        String sql = "SELECT buy_id, product_id, count FROM buy_product WHERE buy_id = ?";
        return jdbc.query(sql, buyProductRowMapper, buyId);
    }

    public void save(BuyProduct buyProduct) {
        String sql = "INSERT INTO buy_product (buy_id, product_id, count) VALUES (?, ?, ?)";
        jdbc.update(sql, buyProduct.getBuyId(), buyProduct.getProductId(), buyProduct.getCount());
    }

    public void saveAll(List<BuyProduct> buyProducts) {
        String sql = "INSERT INTO buy_product (buy_id, product_id, count) VALUES (?, ?, ?)";

        jdbc.batchUpdate(sql, buyProducts, buyProducts.size(), (ps, buyProduct) -> {
            ps.setLong(1, buyProduct.getBuyId());
            ps.setLong(2, buyProduct.getProductId());
            ps.setInt(3, buyProduct.getCount());
        });
    }
}