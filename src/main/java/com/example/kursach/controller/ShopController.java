package com.example.kursach.controller;

import com.example.kursach.dto.EmployeeDTO;
import com.example.kursach.repository.model.Shop;
import com.example.kursach.security.AuditLogger;
import com.example.kursach.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shops")
public class ShopController {

    private static final Logger logger = LoggerFactory.getLogger(ShopController.class);
    private final ShopService shopService;
    private final AuditLogger auditLogger;

    public ShopController(ShopService shopService, AuditLogger auditLogger) {
        this.shopService = shopService;
        this.auditLogger = auditLogger;
    }

    @GetMapping
    public String listShops(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        List<Map<String, Object>> shops = shopService.getAllShopsWithStats();

        model.addAttribute("employee", employee);
        model.addAttribute("shops", shops);

        return "shops";
    }

    @GetMapping("/create")
    public String createShopForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        model.addAttribute("employee", employee);
        return "create-shop";
    }

    @PostMapping("/create")
    public String createShop(@RequestParam String address,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        try {
            shopService.createShop(address);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "CREATE", "Shop", "Создан магазин: " + address);
            redirectAttributes.addFlashAttribute("success", "Магазин успешно добавлен");
        } catch (Exception e) {
            logger.error("Error creating shop: ", e);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "CREATE_FAILED", "Shop", "Ошибка создания магазина: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/shops";
    }

    @GetMapping("/edit/{id}")
    public String editShopForm(@PathVariable Long id,
                               HttpServletRequest request,
                               Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        try {
            model.addAttribute("shop", shopService.getShopById(id));
        } catch (Exception e) {
            return "redirect:/shops";
        }

        model.addAttribute("employee", employee);
        return "edit-shop";
    }

    @PostMapping("/edit/{id}")
    public String editShop(@PathVariable Long id,
                           @RequestParam String address,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        try {
            shopService.updateShop(id, address);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "UPDATE", "Shop", "Изменен магазин: " + id);
            redirectAttributes.addFlashAttribute("success", "Магазин обновлен");
        } catch (Exception e) {
            logger.error("Error updating shop: ", e);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "UPDATE_FAILED", "Shop", "Ошибка изменения магазина: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/shops";
    }

    @PostMapping("/delete/{id}")
    public String deleteShop(@PathVariable Long id,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        try {
            Shop shop = shopService.getShopById(id);
            shopService.deleteShop(id);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "DELETE", "Shop", "Удален магазин: : " + shop.getAddress());
            redirectAttributes.addFlashAttribute("success", "Магазин удален");
        } catch (Exception e) {
            logger.error("Error deleting shop: ", e);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "DELETE_FAILED", "Buy", "Ошибка удаления магазина: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/shops";
    }
}