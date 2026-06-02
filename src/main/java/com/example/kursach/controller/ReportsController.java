package com.example.kursach.controller;

import com.example.kursach.dto.EmployeeDTO;
import com.example.kursach.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);
    private final ReportService reportService;

    public ReportsController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String reportsPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isManager()) {
            return "redirect:/";
        }

        model.addAttribute("employee", employee);
        model.addAttribute("isAdmin", employee.isAdmin());

        return "reports";
    }

    @GetMapping("/sales")
    public String salesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false, defaultValue = "summary") String type,
            HttpServletRequest request,
            Model model) {

        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isManager()) return "redirect:/";

        // Определяем shopId для отчета
        Long reportShopId;

        if (employee.isAdmin() && shopId != null && shopId > 0) {
            // Админ может выбрать конкретный магазин
            reportShopId = shopId;
        } else if (employee.isAdmin() && (shopId == null || shopId == 0)) {
            // Админ смотрит все магазины (shopId = 0 означает все)
            reportShopId = 0L;
        } else {
            // Менеджер и кассир видят только свой магазин
            reportShopId = employee.getShopId();
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        Map<String, Object> report;

        if (reportShopId == 0) {
            // Отчет по всем магазинам
            if ("products".equals(type)) {
                report = reportService.getProductSalesReportAllShops(start, end);
            } else {
                report = reportService.getReportAllShops(start, end);
            }
        } else {
            // Отчет по конкретному магазину
            if ("products".equals(type)) {
                report = reportService.getProductSalesReport(reportShopId, start, end);
            } else {
                report = reportService.getReportByDateRange(reportShopId, start, end);
            }
        }

        model.addAttribute("employee", employee);
        model.addAttribute("report", report);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("reportType", type);
        model.addAttribute("selectedShopId", reportShopId);
        model.addAttribute("isAdmin", employee.isAdmin());
        model.addAttribute("isAllShops", reportShopId == 0);

        return "sales-report";
    }
}