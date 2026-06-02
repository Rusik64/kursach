package com.example.kursach.controller;

import com.example.kursach.dto.EmployeeDTO;
import com.example.kursach.repository.model.Category;
import com.example.kursach.security.AuditLogger;
import com.example.kursach.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoriesController {

    private static final Logger logger = LoggerFactory.getLogger(CategoriesController.class);
    private final CategoryService categoryService;
    private final AuditLogger auditLogger;

    public CategoriesController(CategoryService categoryService, AuditLogger auditLogger) {
        this.categoryService = categoryService;
        this.auditLogger = auditLogger;
    }

    @GetMapping
    public String listCategories(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        List<Category> categories = categoryService.getAll();

        model.addAttribute("employee", employee);
        model.addAttribute("categories", categories);

        return "categories";
    }

    @GetMapping("/create")
    public String createCategoryForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        model.addAttribute("employee", employee);
        return "create-category";
    }

    @PostMapping("/create")
    public String createCategory(@RequestParam String name,
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
            categoryService.createCategory(name);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "CREATE", "Category", "Создана категория: " + name);
            redirectAttributes.addFlashAttribute("success", "Категория успешно создана");
        } catch (Exception e) {
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "CREATE_FAILED", "Category", "Ошибка создания категории: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String editCategoryForm(@PathVariable Long id,
                                   HttpServletRequest request,
                                   Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        if (employee == null) return "redirect:/login";

        if (!employee.isAdmin()) {
            return "redirect:/";
        }

        Category category = categoryService.getCategoryById(id);

        model.addAttribute("employee", employee);
        model.addAttribute("category", category);

        return "edit-category";
    }

    @PostMapping("/edit/{id}")
    public String editCategory(@PathVariable Long id,
                               @RequestParam String name,
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
            categoryService.updateCategory(id, name);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "UPDATE", "Category", "Изменен магазин: " + id);
            redirectAttributes.addFlashAttribute("success", "Категория обновлена");
        } catch (Exception e) {
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "UPDATE_FAILED", "Category", "Ошибка изменения магазина: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id,
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
            Category category = categoryService.getCategoryById(id);
            categoryService.deleteCategory(id);
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "DELETE", "Category", "Удален магазин: : " + category.getName());
            redirectAttributes.addFlashAttribute("success", "Категория удалена");
        } catch (Exception e) {
            auditLogger.log(employee.getName() + " (" + employee.getLogin() + ")",
                    "DELETE_FAILED", "Category", "Ошибка удаления магазина: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/categories";
    }
}