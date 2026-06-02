package com.example.kursach.repository;

import com.example.kursach.repository.model.Category;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoryRepository {

    private final JdbcTemplate jdbc;

    public CategoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Category> categoryRowMapper = (rs, rowNum) -> {
        Category category = new Category();
        category.setId(rs.getLong("id"));
        category.setName(rs.getString("name"));
        return category;
    };

    public List<Category> findAll() {
        String sql = "SELECT * FROM categories ORDER BY name";
        return jdbc.query(sql, categoryRowMapper);
    }

    public Category findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try {
            return jdbc.queryForObject(sql, categoryRowMapper, id);
        } catch (Exception e) {
            throw new RuntimeException("Категория не найдена с id: " + id);
        }
    }

    public void save(Category category) {
        String sql = "INSERT INTO categories (name) VALUES (?)";
        jdbc.update(sql, category.getName());
    }

    public void update(Category category) {
        String sql = "UPDATE categories SET name = ? WHERE id = ?";
        jdbc.update(sql, category.getName(), category.getId());
    }

    public void delete(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        jdbc.update(sql, id);
    }

    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM categories WHERE name = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, name);
        return count != null && count > 0;
    }

    public boolean hasProducts(Long categoryId) {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, categoryId);
        return count != null && count > 0;
    }
}