package com.example.kursach.repository;

import com.example.kursach.repository.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbc;

    public ProductRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Product> productRowMapper = (rs, rowNum) -> {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setCategoryId(rs.getLong("category_id"));
        product.setUnit(rs.getString("unit"));
        product.setPrice(rs.getDouble("price"));
        return product;
    };

    public List<Product> findAllByShopId(Long shopId) {
        String sql = """
            SELECT p.id, p.name, p.category_id, p.unit, p.price,
                   c.name as category_name, 
                   COALESCE(sp.count, 0) as available_count
            FROM products p
            JOIN categories c ON p.category_id = c.id
            LEFT JOIN shop_product sp ON p.id = sp.product_id AND sp.shop_id = ?
            ORDER BY c.name, p.name
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            Product product = new Product();
            product.setId(rs.getLong("id"));
            product.setName(rs.getString("name"));
            product.setCategoryId(rs.getLong("category_id"));
            product.setUnit(rs.getString("unit"));
            product.setPrice(rs.getDouble("price"));
            return product;
        }, shopId);
    }

    public String getCategoryNameByProductId(Long productId) {
        String sql = """
            SELECT c.name 
            FROM products p 
            JOIN categories c ON p.category_id = c.id 
            WHERE p.id = ?
        """;
        return jdbc.queryForObject(sql, String.class, productId);
    }

    public Integer getProductCountInShop(Long shopId, Long productId) {
        String sql = "SELECT count FROM shop_product WHERE shop_id = ? AND product_id = ?";
        try {
            return jdbc.queryForObject(sql, Integer.class, shopId, productId);
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Product> findByCategoryAndShop(Long shopId, Long categoryId) {
        String sql = """
            SELECT p.*, c.name as category_name, sp.count as available_count
            FROM products p
            JOIN categories c ON p.category_id = c.id
            LEFT JOIN shop_product sp ON p.id = sp.product_id AND sp.shop_id = ?
            WHERE p.category_id = ?
            ORDER BY p.name
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            Product product = new Product();
            product.setId(rs.getLong("id"));
            product.setName(rs.getString("name"));
            product.setCategoryId(rs.getLong("category_id"));
            product.setUnit(rs.getString("unit"));
            product.setPrice(rs.getDouble("price"));
            return product;
        }, shopId, categoryId);
    }

    public Product findById(Long productId) {
        String sql = "SELECT * FROM products WHERE id = ?";
        return jdbc.queryForObject(sql, productRowMapper, productId);
    }

    public List<Product> findAll() {
        String sql = "SELECT id, name, category_id, unit, price FROM products ORDER BY name";
        return jdbc.query(sql, productRowMapper);
    }

    public Map<Long, Product> findByIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return new HashMap<>();
        }

        // Создаем плейсхолдеры для IN запроса
        String placeholders = productIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = String.format(
                "SELECT id, name, category_id, unit, price FROM products WHERE id IN (%s)",
                placeholders
        );

        // Преобразуем List<Long> в массив Object[] для jdbc.query
        Object[] params = productIds.toArray();

        List<Product> products = jdbc.query(sql, productRowMapper, params);

        // Преобразуем в Map для быстрого доступа по ID
        Map<Long, Product> productMap = new HashMap<>();
        for (Product product : products) {
            productMap.put(product.getId(), product);
        }

        return productMap;
    }

//    public List<Product> findByIdsWithCategory(List<Long> productIds) {
//        if (productIds == null || productIds.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        String placeholders = productIds.stream()
//                .map(id -> "?")
//                .collect(Collectors.joining(","));
//
//        String sql = String.format("""
//            SELECT p.id, p.name, p.category_id, p.unit, p.price, c.name as category_name
//            FROM products p
//            JOIN categories c ON p.category_id = c.id
//            WHERE p.id IN (%s)
//            ORDER BY p.name
//        """, placeholders);
//
//        Object[] params = productIds.toArray();
//
//        return jdbc.query(sql, (rs, rowNum) -> {
//            Product product = new Product();
//            product.setId(rs.getLong("id"));
//            product.setName(rs.getString("name"));
//            product.setCategoryId(rs.getLong("category_id"));
//            product.setUnit(rs.getString("unit"));
//            product.setPrice(rs.getDouble("price"));
//            product.setCategoryName(rs.getString("category_name"));
//            return product;
//        }, params);
//    }

    public void save(Product product) {
        String sql = "INSERT INTO products (name, category_id, unit, price) VALUES (?, ?, ?, ?)";
        jdbc.update(sql, product.getName(), product.getCategoryId(), product.getUnit(), product.getPrice());
    }

    public void update(Product product) {
        String sql = "UPDATE products SET name = ?, category_id = ?, unit = ?, price = ? WHERE id = ?";
        jdbc.update(sql, product.getName(), product.getCategoryId(), product.getUnit(), product.getPrice(), product.getId());
    }

    public void delete(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        jdbc.update(sql, id);
    }

    public void updateOrInsertStock(Long productId, Long shopId, Integer count) {
        // Сначала проверяем, есть ли запись
        String checkSql = "SELECT COUNT(*) FROM shop_product WHERE shop_id = ? AND product_id = ?";
        Integer exists = jdbc.queryForObject(checkSql, Integer.class, shopId, productId);

        if (exists != null && exists > 0) {
            String updateSql = "UPDATE shop_product SET count = ? WHERE shop_id = ? AND product_id = ?";
            jdbc.update(updateSql, count, shopId, productId);
        } else {
            String insertSql = "INSERT INTO shop_product (shop_id, product_id, count) VALUES (?, ?, ?)";
            jdbc.update(insertSql, shopId, productId, count);
        }
    }
}