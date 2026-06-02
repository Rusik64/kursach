package com.example.kursach.controller;

import com.example.kursach.dto.EmployeeDTO;
import com.example.kursach.mapper.EmployeeMapper;
import com.example.kursach.repository.EmployeeRepository;
import com.example.kursach.repository.ShopRepository;
import com.example.kursach.repository.model.Employee;
import com.example.kursach.security.AuditLogger;
import com.example.kursach.security.PasswordEncoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final ShopRepository shopRepository;
    private final AuditLogger auditLogger;

    public AuthController(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper, ShopRepository shopRepository, AuditLogger auditLogger) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.shopRepository = shopRepository;
        this.auditLogger = auditLogger;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String login,
                        @RequestParam String password,
                        HttpServletRequest request,
                        Model model) {
        logger.info("=== Login attempt for: {} ===", login);

        try {
            // Получаем сотрудника по логину
            Employee employee = employeeRepository.findByLogin(login);

            // Проверяем пароль
            if (!PasswordEncoder.verifyPassword(password, employee.getPassword())) {
                auditLogger.log(login, "LOGIN_FAILED", "Неверный пароль");
                throw new RuntimeException("Неверный пароль");
            }

            auditLogger.log(employee.getName() + " (" + login + ")",
                    "LOGIN", "Успешный вход в систему");
            logger.info("Employee from DB: id={}, name={}, role={}",
                    employee.getId(), employee.getName(), employee.getRole());

            List<String> permissions = employeeRepository.getPermissionsByRole(employee.getRole());
            EmployeeDTO employeeDTO = employeeMapper.toDTO(employee, permissions);

            // Получаем адрес магазина
            String shopAddress = shopRepository.getAddressById(employee.getShopId());
            employeeDTO.setShopAddress(shopAddress);

            HttpSession session = request.getSession(true);
            session.setAttribute("employee", employeeDTO);

            logger.info("EmployeeDTO saved to session: {}", session.getId());

            return "redirect:/";

        } catch (Exception e) {
            logger.error("Login failed", e);
            model.addAttribute("error", "Неверный логин или пароль");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
            if (employee != null) {
                auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                        "LOGOUT", "Выход из системы");
            }
            session.invalidate();
        }
        return "redirect:/login";
    }
}