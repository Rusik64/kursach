package com.example.kursach.controller;

import com.example.kursach.dto.DiscountDTO;
import com.example.kursach.dto.EmployeeDTO;
import com.example.kursach.dto.ProductDTO;
import com.example.kursach.repository.model.Category;
import com.example.kursach.security.AuditLogger;
import com.example.kursach.service.CategoryService;
import com.example.kursach.service.DiscountService;
import com.example.kursach.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/discounts")
public class DiscountController {

    private static final Logger logger = LoggerFactory.getLogger(DiscountController.class);

    private final DiscountService discountService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final AuditLogger auditLogger;

    public DiscountController(DiscountService discountService,
                              ProductService productService,
                              CategoryService categoryService, AuditLogger auditLogger) {
        this.discountService = discountService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.auditLogger = auditLogger;
    }

    @GetMapping
    public String listDiscounts(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isManager()) {
            return "redirect:/";
        }

        List<DiscountDTO> discounts = discountService.getAllDiscounts();

        model.addAttribute("employee", employee);
        model.addAttribute("discounts", discounts);

        return "discounts";
    }

    @GetMapping("/create")
    public String createDiscountForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isManager()) return "redirect:/";

        List<ProductDTO> products = productService.getAllProducts();
        List<Category> categories = categoryService.getAll();

        model.addAttribute("employee", employee);
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);

        // Устанавливаем даты по умолчанию
        model.addAttribute("defaultStartDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        model.addAttribute("defaultEndDate", LocalDateTime.now().plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return "create-discount";
    }

    @PostMapping("/create")
    public String createDiscount(@RequestParam String discountType,
                                 @RequestParam(required = false) Long productId,
                                 @RequestParam(required = false) Long categoryId,
                                 @RequestParam Double percentage,
                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endDate,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isManager()) return "redirect:/";

        try {
            Long finalProductId = "product".equals(discountType) ? productId : null;
            Long finalCategoryId = "category".equals(discountType) ? categoryId : null;

            discountService.createDiscount(finalProductId, finalCategoryId, percentage, startDate, endDate);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "CREATE", "Discount", "Создана скидка: " + percentage + "%");
            redirectAttributes.addFlashAttribute("success", "Скидка успешно добавлена");
        } catch (Exception e) {
            logger.error("Error creating discount: ", e);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "CREATE_FAILED", "Discount", "Ошибка: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/discounts";
    }

    @GetMapping("/edit/{id}")
    public String editDiscountForm(@PathVariable Long id,
                                   HttpServletRequest request,
                                   Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isManager()) return "redirect:/";

        DiscountDTO discount = discountService.getDiscountById(id);
        List<ProductDTO> products = productService.getAllProducts();
        List<Category> categories = categoryService.getAll();

        model.addAttribute("employee", employee);
        model.addAttribute("discount", discount);
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);

        return "edit-discount";
    }

    @PostMapping("/edit/{id}")
    public String editDiscount(@PathVariable Long id,
                               @RequestParam String discountType,
                               @RequestParam(required = false) Long productId,
                               @RequestParam(required = false) Long categoryId,
                               @RequestParam Double percentage,
                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endDate,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isManager()) return "redirect:/";

        try {
            Long finalProductId = "product".equals(discountType) ? productId : null;
            Long finalCategoryId = "category".equals(discountType) ? categoryId : null;

            discountService.updateDiscount(id, finalProductId, finalCategoryId, percentage, startDate, endDate);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "UPDATE", "Discount", id, "Изменена скидка: " + id);
            redirectAttributes.addFlashAttribute("success", "Скидка обновлена");
        } catch (Exception e) {
            logger.error("Error updating discount: ", e);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "UPDATE_DENIED", "Discount", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/discounts";
    }

    @PostMapping("/delete/{id}")
    public String deleteDiscount(@PathVariable Long id,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        // Только ADMIN может удалять скидки
        if (!employee.isAdmin()) {
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "DELETE_DENIED", "Discount", id, "Недостаточно прав");
            redirectAttributes.addFlashAttribute("error", "Недостаточно прав для удаления скидки");
            return "redirect:/discounts";
        }

        try {
            discountService.deleteDiscount(id);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "DELETE", "Discount", id, "Скидка удалена");
            redirectAttributes.addFlashAttribute("success", "Скидка удалена");
        } catch (Exception e) {
            logger.error("Error deleting discount: ", e);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "DELETE_FAILED", "Discount", id, "Ошибка: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/discounts";
    }
}