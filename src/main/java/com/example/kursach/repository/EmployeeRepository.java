package com.example.kursach.repository;

import com.example.kursach.repository.model.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EmployeeRepository {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeRepository.class);
    private final JdbcTemplate jdbc;

    public EmployeeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Employee> employeeRowMapper = new RowMapper<Employee>() {
        @Override
        public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
            Employee employee = new Employee();
            employee.setId(rs.getLong("id"));
            employee.setName(rs.getString("name"));
            employee.setShopId(rs.getLong("shop_id"));
            employee.setLogin(rs.getString("login"));
            employee.setPassword(rs.getString("password")); // Хеш пароля
            employee.setRole(rs.getString("role"));
            return employee;
        }
    };

    public Employee findByLoginAndPassword(String login, String password) {
        String sql = "SELECT id, name, shop_id, login, password, role FROM employees WHERE login = ? AND password = ?";

        try {
            Employee employee = jdbc.queryForObject(sql, employeeRowMapper, login, password);
            return employee;
        } catch (Exception e) {
            throw new RuntimeException("Неверный логин или пароль");
        }
    }

    public List<String> getPermissionsByRole(String role) {
        List<String> permissions = new ArrayList<>();

        if (role == null) {
            return permissions;
        }

        logger.info("Loading permissions for role: {}", role);

        switch (role.toUpperCase()) {
            case "ADMIN":
                permissions.add("products:READ");
                permissions.add("products:CREATE");
                permissions.add("products:UPDATE");
                permissions.add("products:DELETE");
                permissions.add("buy:CREATE");
                permissions.add("buy_history:READ");
                permissions.add("buy_history:DELETE");
                permissions.add("categories:READ");
                permissions.add("categories:CREATE");
                permissions.add("reports:READ");
                permissions.add("employees:READ");
                permissions.add("discounts:READ");
                permissions.add("discounts:CREATE");
                break;

            case "MANAGER":
                permissions.add("products:READ");
                permissions.add("buy:CREATE");
                permissions.add("buy_history:READ");
                permissions.add("categories:READ");
                permissions.add("reports:READ");
                permissions.add("discounts:READ");
                break;

            case "CASHIER":
                permissions.add("products:READ");
                permissions.add("buy:CREATE");
                permissions.add("buy_history:READ");
                permissions.add("categories:READ");
                break;

            default:
                logger.warn("Unknown role: {}", role);
                break;
        }

        logger.info("Loaded {} permissions for role {}", permissions.size(), role);
        return permissions;
    }

    public List<Employee> findAll() {
        String sql = "SELECT id, name, shop_id, login, password, role FROM employees ORDER BY id";
        return jdbc.query(sql, employeeRowMapper);
    }

    public List<Employee> findByShopId(Long shopId) {
        String sql = "SELECT id, name, shop_id, login, password, role FROM employees WHERE shop_id = ? ORDER BY id";
        return jdbc.query(sql, employeeRowMapper, shopId);
    }

    public Employee findById(Long id) {
        String sql = "SELECT id, name, shop_id, login, password, role FROM employees WHERE id = ?";
        try {
            return jdbc.queryForObject(sql, employeeRowMapper, id);
        } catch (Exception e) {
            throw new RuntimeException("Сотрудник не найден с id: " + id);
        }
    }

    public void save(Employee employee) {
        String sql = "INSERT INTO employees (name, shop_id, login, password, role) VALUES (?, ?, ?, ?, ?)";
        jdbc.update(sql,
                employee.getName(),
                employee.getShopId(),
                employee.getLogin(),
                employee.getPassword(),
                employee.getRole() != null ? employee.getRole() : "CASHIER"
        );
    }

    public void update(Employee employee) {
        String sql = "UPDATE employees SET name = ?, shop_id = ?, login = ?, password = ?, role = ? WHERE id = ?";
        jdbc.update(sql,
                employee.getName(),
                employee.getShopId(),
                employee.getLogin(),
                employee.getPassword(),
                employee.getRole(),
                employee.getId()
        );
    }

    public void updateRole(Long employeeId, String newRole) {
        String sql = "UPDATE employees SET role = ? WHERE id = ?";
        jdbc.update(sql, newRole, employeeId);
    }

    public void delete(Long id) {
        String sql = "DELETE FROM employees WHERE id = ?";
        jdbc.update(sql, id);
    }

    public boolean existsByLogin(String login) {
        String sql = "SELECT COUNT(*) FROM employees WHERE login = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, login);
        return count != null && count > 0;
    }

    public Employee findByLogin(String login) {
        String sql = "SELECT id, name, shop_id, login, password, role FROM employees WHERE login = ?";
        try {
            return jdbc.queryForObject(sql, employeeRowMapper, login);
        } catch (Exception e) {
            throw new RuntimeException("Сотрудник не найден");
        }
    }
}