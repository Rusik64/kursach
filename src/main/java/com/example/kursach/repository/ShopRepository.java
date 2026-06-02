package com.example.kursach.repository;

import com.example.kursach.repository.model.Shop;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ShopRepository {

    private final JdbcTemplate jdbc;

    public ShopRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Shop> shopRowMapper = new RowMapper<Shop>() {
        @Override
        public Shop mapRow(ResultSet rs, int rowNum) throws SQLException {
            Shop shop = new Shop();
            shop.setId(rs.getLong("id"));
            shop.setAddress(rs.getString("address"));
            return shop;
        }
    };

    public List<Shop> findAll() {
        String sql = "SELECT id, address FROM shops ORDER BY id";
        return jdbc.query(sql, shopRowMapper);
    }

    public Shop findById(Long id) {
        String sql = "SELECT id, address FROM shops WHERE id = ?";
        try {
            return jdbc.queryForObject(sql, shopRowMapper, id);
        } catch (Exception e) {
            throw new RuntimeException("Магазин не найден с id: " + id);
        }
    }

    public void save(Shop shop) {
        String sql = "INSERT INTO shops (address) VALUES (?)";
        jdbc.update(sql, shop.getAddress());
    }

    public void update(Shop shop) {
        String sql = "UPDATE shops SET address = ? WHERE id = ?";
        jdbc.update(sql, shop.getAddress(), shop.getId());
    }

    public void delete(Long id) {
        // Удаляем связанные данные
        jdbc.update("DELETE FROM shop_product WHERE shop_id = ?", id);
        jdbc.update("DELETE FROM employees WHERE shop_id = ?", id);
        jdbc.update("DELETE FROM buy WHERE shop_id = ?", id);

        // Удаляем магазин
        String sql = "DELETE FROM shops WHERE id = ?";
        jdbc.update(sql, id);
    }

    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM shops WHERE id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public int getEmployeeCount(Long shopId) {
        String sql = "SELECT COUNT(*) FROM employees WHERE shop_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, shopId);
        return count != null ? count : 0;
    }

    public int getProductCount(Long shopId) {
        String sql = "SELECT COUNT(*) FROM shop_product WHERE shop_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, shopId);
        return count != null ? count : 0;
    }

    public String getAddressById(Long id) {
        String sql = "SELECT address FROM shops WHERE id = ?";
        try {
            return jdbc.queryForObject(sql, String.class, id);
        } catch (Exception e) {
            return "Неизвестный магазин";
        }
    }
}
