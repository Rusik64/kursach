package com.example.kursach.controller;

import com.example.kursach.dto.EmployeeDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    public IndexController() {
        logger.info("=== DashboardController CREATED ===");
    }

    @GetMapping("/")
    public String index(HttpServletRequest request, Model model) {
        logger.info("=== DashboardController: / called ===");
        logger.info("Request URI: {}", request.getRequestURI());
        logger.info("Request URL: {}", request.getRequestURL());

        HttpSession session = request.getSession(false);

        if (session == null) {
            logger.warn("No session!");
            return "redirect:/login";
        }

        logger.info("Session ID: {}", session.getId());

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");

        if (employee == null) {
            logger.warn("Employee is null!");
            return "redirect:/login";
        }

        logger.info("Employee: id={}, name={}, role={}",
                employee.getId(), employee.getName(), employee.getRole());

        model.addAttribute("employee", employee);

        return "index";
    }
}
