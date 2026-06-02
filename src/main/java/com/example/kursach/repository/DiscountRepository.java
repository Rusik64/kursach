package com.example.kursach.repository;

import com.example.kursach.repository.model.Discount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class DiscountRepository {

    private final JdbcTemplate jdbc;

    public DiscountRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Discount> discountRowMapper = new RowMapper<Discount>() {
        @Override
        public Discount mapRow(ResultSet rs, int rowNum) throws SQLException {
            Discount discount = new Discount();
            discount.setId(rs.getLong("id"));

            long productId = rs.getLong("product_id");
            if (!rs.wasNull()) {
                discount.setProductId(productId);
            }

            long categoryId = rs.getLong("category_id");
            if (!rs.wasNull()) {
                discount.setCategoryId(categoryId);
            }

            discount.setPercentage(rs.getDouble("percentage"));

            // Если даты null, устанавливаем значения по умолчанию
            Timestamp startDate = rs.getTimestamp("start_date");
            if (startDate != null) {
                discount.setStartDate(startDate.toLocalDateTime());
            } else {
                discount.setStartDate(LocalDateTime.of(2024, 1, 1, 0, 0));
            }

            Timestamp endDate = rs.getTimestamp("end_date");
            if (endDate != null) {
                discount.setEndDate(endDate.toLocalDateTime());
            } else {
                discount.setEndDate(LocalDateTime.of(2099, 12, 31, 23, 59));
            }

            return discount;
        }
    };

    public Discount findActiveByProduct(Long productId) {
        LocalDateTime now = LocalDateTime.now();
        String sql = """
            SELECT * FROM discounts 
            WHERE product_id = ? 
            AND start_date <= ? AND end_date >= ?
            LIMIT 1
        """;

        List<Discount> discounts = jdbc.query(sql, discountRowMapper,
                productId, Timestamp.valueOf(now), Timestamp.valueOf(now));
        return discounts.isEmpty() ? null : discounts.get(0);
    }

    /**
     * Найти активную скидку на категорию (на текущий момент)
     */
    public Discount findActiveByCategory(Long categoryId) {
        LocalDateTime now = LocalDateTime.now();
        String sql = """
            SELECT * FROM discounts 
            WHERE category_id = ? 
            AND start_date <= ? AND end_date >= ?
            LIMIT 1
        """;

        List<Discount> discounts = jdbc.query(sql, discountRowMapper,
                categoryId, Timestamp.valueOf(now), Timestamp.valueOf(now));
        return discounts.isEmpty() ? null : discounts.get(0);
    }

    /**
     * Найти скидку на товар, активную на указанную дату
     */
    public Discount findByProductAndDate(Long productId, LocalDateTime date) {
        String sql = """
            SELECT * FROM discounts 
            WHERE product_id = ? 
            AND start_date <= ? AND end_date >= ?
            LIMIT 1
        """;

        List<Discount> discounts = jdbc.query(sql, discountRowMapper,
                productId, Timestamp.valueOf(date), Timestamp.valueOf(date));
        return discounts.isEmpty() ? null : discounts.get(0);
    }

    /**
     * Найти скидку на категорию, активную на указанную дату
     */
    public Discount findByCategoryAndDate(Long categoryId, LocalDateTime date) {
        String sql = """
            SELECT * FROM discounts 
            WHERE category_id = ? 
            AND start_date <= ? AND end_date >= ?
            LIMIT 1
        """;

        List<Discount> discounts = jdbc.query(sql, discountRowMapper,
                categoryId, Timestamp.valueOf(date), Timestamp.valueOf(date));
        return discounts.isEmpty() ? null : discounts.get(0);
    }

    public Discount findByProduct(Long productId) {
        String sql = "SELECT * FROM discounts WHERE product_id = ? LIMIT 1";
        List<Discount> discounts = jdbc.query(sql, discountRowMapper, productId);
        return discounts.isEmpty() ? null : discounts.get(0);
    }

    public Discount findByCategory(Long categoryId) {
        String sql = "SELECT * FROM discounts WHERE category_id = ? LIMIT 1";
        List<Discount> discounts = jdbc.query(sql, discountRowMapper, categoryId);
        return discounts.isEmpty() ? null : discounts.get(0);
    }

    public List<Discount> findAll() {
        String sql = "SELECT * FROM discounts ORDER BY id";
        return jdbc.query(sql, discountRowMapper);
    }

    public Discount findById(Long id) {
        String sql = "SELECT * FROM discounts WHERE id = ?";
        try {
            return jdbc.queryForObject(sql, discountRowMapper, id);
        } catch (Exception e) {
            throw new RuntimeException("Скидка не найдена с id: " + id);
        }
    }

    public List<Discount> findByProductId(Long productId) {
        String sql = "SELECT * FROM discounts WHERE product_id = ?";
        return jdbc.query(sql, discountRowMapper, productId);
    }

    public List<Discount> findByCategoryId(Long categoryId) {
        String sql = "SELECT * FROM discounts WHERE category_id = ?";
        return jdbc.query(sql, discountRowMapper, categoryId);
    }

    public void save(Discount discount) {
        String sql = """
        INSERT INTO discounts (product_id, category_id, percentage, start_date, end_date) 
        VALUES (?, ?, ?, ?, ?)
    """;
        jdbc.update(sql,
                discount.getProductId(),
                discount.getCategoryId(),
                discount.getPercentage(),
                discount.getStartDate() != null ? Timestamp.valueOf(discount.getStartDate()) : Timestamp.valueOf(LocalDateTime.now()),
                discount.getEndDate() != null ? Timestamp.valueOf(discount.getEndDate()) : Timestamp.valueOf(LocalDateTime.now().plusMonths(1))
        );
    }

    public void update(Discount discount) {
        String sql = """
        UPDATE discounts 
        SET product_id = ?, category_id = ?, percentage = ?, 
            start_date = ?, end_date = ? 
        WHERE id = ?
    """;
        jdbc.update(sql,
                discount.getProductId(),
                discount.getCategoryId(),
                discount.getPercentage(),
                discount.getStartDate() != null ? Timestamp.valueOf(discount.getStartDate()) : null,
                discount.getEndDate() != null ? Timestamp.valueOf(discount.getEndDate()) : null,
                discount.getId()
        );
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM discounts WHERE id = ?";
        jdbc.update(sql, id);
    }

    public boolean existsByProductId(Long productId) {
        String sql = "SELECT COUNT(*) FROM discounts WHERE product_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, productId);
        return count != null && count > 0;
    }

    public boolean existsByCategoryId(Long categoryId) {
        String sql = "SELECT COUNT(*) FROM discounts WHERE category_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, categoryId);
        return count != null && count > 0;
    }

    public List<Discount> findAllWithDetails() {
        String sql = """
            SELECT d.id, d.product_id, d.category_id, d.percentage, 
                   d.start_date, d.end_date,
                   p.name as product_name, c.name as category_name
            FROM discounts d
            LEFT JOIN products p ON d.product_id = p.id
            LEFT JOIN categories c ON d.category_id = c.id
            ORDER BY d.id
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            Discount discount = new Discount();
            discount.setId(rs.getLong("id"));

            long productId = rs.getLong("product_id");
            if (!rs.wasNull()) {
                discount.setProductId(productId);
                discount.setProductName(rs.getString("product_name"));
            }

            long categoryId = rs.getLong("category_id");
            if (!rs.wasNull()) {
                discount.setCategoryId(categoryId);
                discount.setCategoryName(rs.getString("category_name"));
            }

            discount.setPercentage(rs.getDouble("percentage"));

            // ВАЖНО: извлекаем даты
            Timestamp startDate = rs.getTimestamp("start_date");
            if (startDate != null) {
                discount.setStartDate(startDate.toLocalDateTime());
            }

            Timestamp endDate = rs.getTimestamp("end_date");
            if (endDate != null) {
                discount.setEndDate(endDate.toLocalDateTime());
            }

            return discount;
        });
    }

    public Discount findByIdWithDetails(Long id) {
        String sql = """
            SELECT d.id, d.product_id, d.category_id, d.percentage, 
                   d.start_date, d.end_date,
                   p.name as product_name, c.name as category_name
            FROM discounts d
            LEFT JOIN products p ON d.product_id = p.id
            LEFT JOIN categories c ON d.category_id = c.id
            WHERE d.id = ?
        """;

        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> {
                Discount discount = new Discount();
                discount.setId(rs.getLong("id"));

                long productId = rs.getLong("product_id");
                if (!rs.wasNull()) {
                    discount.setProductId(productId);
                    discount.setProductName(rs.getString("product_name"));
                }

                long categoryId = rs.getLong("category_id");
                if (!rs.wasNull()) {
                    discount.setCategoryId(categoryId);
                    discount.setCategoryName(rs.getString("category_name"));
                }

                discount.setPercentage(rs.getDouble("percentage"));

                Timestamp startDate = rs.getTimestamp("start_date");
                if (startDate != null) {
                    discount.setStartDate(startDate.toLocalDateTime());
                }

                Timestamp endDate = rs.getTimestamp("end_date");
                if (endDate != null) {
                    discount.setEndDate(endDate.toLocalDateTime());
                }

                return discount;
            }, id);
        } catch (Exception e) {
            throw new RuntimeException("Скидка не найдена с id: " + id);
        }
    }
}