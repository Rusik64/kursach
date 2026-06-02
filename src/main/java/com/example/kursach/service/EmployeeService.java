package com.example.kursach.service;

import com.example.kursach.dto.EmployeeDTO;
import com.example.kursach.repository.EmployeeRepository;
import com.example.kursach.repository.model.Employee;
import com.example.kursach.security.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee login(String login, String password) {
        Employee employee = employeeRepository.findByLoginAndPassword(login, password);

        // Загружаем права пользователя
        List<String> permissions = employeeRepository.getPermissionsByRole(employee.getRole());
        employee.setPermissions(permissions); // нужно добавить поле в Employee

        return employee;
    }

    public boolean hasPermission(EmployeeDTO employee, String resource, String action) {
        if (employee == null) return false;

        // ADMIN имеет все права
        if ("ADMIN".equals(employee.getRole())) {
            return true;
        }

        String requiredPermission = resource + ":" + action;
        return employee.getPermissions().contains(requiredPermission);
    }

    public boolean isAdmin(Employee employee) {
        return employee != null && "ADMIN".equals(employee.getRole());
    }

    public boolean isManager(Employee employee) {
        return employee != null && ("MANAGER".equals(employee.getRole()) || "ADMIN".equals(employee.getRole()));
    }

    public boolean isCashier(Employee employee) {
        return employee != null;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public void createEmployee(String name, Long shopId, String login, String password, String role) {
        if (employeeRepository.existsByLogin(login)) {
            throw new RuntimeException("Сотрудник с таким логином уже существует");
        }

        Employee employee = new Employee();
        employee.setName(name);
        employee.setShopId(shopId);
        employee.setLogin(login);
        employee.setPassword(PasswordEncoder.hashPassword(password));
        employee.setRole(role);
        employeeRepository.save(employee);
        logger.info("Создан новый сотрудник: {}", login);
    }

    public void updateEmployee(Long id, String name, Long shopId, String login, String password, String role) {
        Employee employee = employeeRepository.findById(id);

        Employee existingByLogin = null;
        try {
            existingByLogin = employeeRepository.findByLogin(login);
        } catch (Exception e) {
            // Логин свободен
        }

        if (existingByLogin != null && !existingByLogin.getId().equals(id)) {
            throw new RuntimeException("Сотрудник с таким логином уже существует");
        }

        employee.setName(name);
        employee.setShopId(shopId);
        employee.setLogin(login);

        // Если указан новый пароль - хешируем его
        if (password != null && !password.trim().isEmpty()) {
            employee.setPassword(PasswordEncoder.hashPassword(password));
        }
        // Если пароль не указан - оставляем старый

        employee.setRole(role);
        employeeRepository.update(employee);
        logger.info("Обновлен сотрудник: id={}, login={}", id, login);
    }

    public void updateRole(Long employeeId, String newRole) {
        employeeRepository.updateRole(employeeId, newRole);
        logger.info("Изменена роль сотрудника: id={}, role={}", employeeId, newRole);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.delete(id);
        logger.info("Удален сотрудник: id={}", id);
    }
}